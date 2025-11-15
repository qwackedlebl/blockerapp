# BlockerApp - 24-Hour Hackathon Architecture

## 1. Overall System Architecture

### High-Level Components

```
┌─────────────────────────────────────────────────────────────┐
│                        BlockerApp                            │
├─────────────────────────────────────────────────────────────┤
│  UI Layer (Activities)                                       │
│  ├─ MainActivity (App List + Lock/Unlock toggles)           │
│  ├─ BlockerOverlayActivity (Fullscreen lock screen)         │
│  └─ TotpActivity (Generate/Import secret keys)              │
├─────────────────────────────────────────────────────────────┤
│  Background Services                                         │
│  └─ AppBlockerAccessibilityService (Monitor app launches)   │
├─────────────────────────────────────────────────────────────┤
│  Business Logic                                              │
│  ├─ TotpManager (Generate 6-digit codes)                    │
│  ├─ PreferencesHelper (Store locked apps & secrets)         │
│  └─ AppListManager (Query installed apps)                   │
├─────────────────────────────────────────────────────────────┤
│  Data Layer                                                  │
│  └─ SharedPreferences (JSON storage with Gson)              │
└─────────────────────────────────────────────────────────────┘
```

## 2. Recommended Android Components

### Core Components (Use These)
- **AccessibilityService**: Detect app launches in real-time
- **SharedPreferences**: Simple key-value storage (skip Room DB)
- **PackageManager**: Query all installed apps
- **Activity with FLAG_SHOW_WHEN_LOCKED**: Fullscreen overlay blocker
- **Handler/Runnable**: Update TOTP codes every 30 seconds

### Skip These (Too Slow for Hackathon)
- ❌ Room Database (overkill for simple data)
- ❌ ViewModel/LiveData (adds complexity)
- ❌ Dependency Injection (Dagger/Hilt)
- ❌ Coroutines/RxJava (use simple threads)
- ❌ Navigation Component (direct Intent navigation)

## 3. Locking Mechanism Options

### Option A: AccessibilityService + Overlay (RECOMMENDED ⭐)
**Pros:**
- Works on all Android versions (API 21+)
- Fast to implement (~4 hours)
- Reliable detection via `TYPE_WINDOW_STATE_CHANGED` events
- No root required

**Cons:**
- User must manually enable accessibility service
- Can be disabled by tech-savvy users

**Implementation:**
```kotlin
class AppBlockerAccessibilityService : AccessibilityService() {
    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        if (event.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            val packageName = event.packageName?.toString()
            if (PreferencesHelper.isLocked(packageName)) {
                // Launch blocking overlay
                startActivity(Intent(this, BlockerOverlayActivity::class.java))
            }
        }
    }
}
```

### Option B: UsageStatsManager Polling
**Pros:**
- Official API

**Cons:**
- Requires constant polling (battery drain)
- Delayed detection (200-500ms lag)
- More complex implementation

### Option C: Device Admin API
**Pros:**
- Very secure

**Cons:**
- Requires factory reset to uninstall (demo killer!)
- Complex setup flow
- Not suitable for hackathon

**DECISION: Use Option A (AccessibilityService + Overlay)**

## 4. TOTP Implementation

### Library Choice: Apache Commons Codec
```gradle
implementation 'org.apache.commons:commons-codec:1.15'
```

### TOTP Algorithm (RFC 6238)
```
TOTP = HOTP(K, T)
where:
  K = Secret Key (Base32 encoded)
  T = floor(current_unix_time / 30)
  HOTP = Truncate(HMAC-SHA1(K, T))
```

### TotpManager Implementation
```kotlin
object TotpManager {
    fun generateSecretKey(): String {
        val random = SecureRandom()
        val bytes = ByteArray(20)
        random.nextBytes(bytes)
        return Base32().encodeToString(bytes)
    }
    
    fun generateCode(secretKey: String, timeStep: Long = 30): String {
        val key = Base32().decode(secretKey)
        val time = System.currentTimeMillis() / 1000 / timeStep
        val msg = ByteBuffer.allocate(8).putLong(time).array()
        
        val mac = Mac.getInstance("HmacSHA1")
        mac.init(SecretKeySpec(key, "HmacSHA1"))
        val hash = mac.doFinal(msg)
        
        val offset = hash[hash.size - 1].toInt() and 0x0f
        val binary = ((hash[offset].toInt() and 0x7f) shl 24) or
                     ((hash[offset + 1].toInt() and 0xff) shl 16) or
                     ((hash[offset + 2].toInt() and 0xff) shl 8) or
                     (hash[offset + 3].toInt() and 0xff)
        
        val otp = binary % 1000000
        return otp.toString().padStart(6, '0')
    }
    
    fun verifyCode(secretKey: String, inputCode: String): Boolean {
        val currentCode = generateCode(secretKey)
        return currentCode == inputCode
    }
}
```

### Data Flow Diagram

**Setup Flow (Phone B → Phone A):**
```
Phone B                                Phone A
───────                                ───────
[Generate Secret]
      ↓
[Display: ABCD1234...]
      ↓
[Copy to Clipboard]
      ↓
   (SMS) ─────────────────────────→ [Receive SMS]
                                          ↓
                                    [Paste Secret]
                                          ↓
                                    [Select App to Lock]
                                          ↓
                                    [Save: app + secret]
```

**Unlock Flow (Phone B → Phone A):**
```
Phone B                                Phone A
───────                                ───────
[Display TOTP: 123456]                [User tries to open locked app]
      ↓                                       ↓
[Copy Code]                            [BlockerOverlayActivity appears]
      ↓                                       ↓
   (SMS) ─────────────────────────→    [User enters: 123456]
                                              ↓
                                        [Verify code matches]
                                              ↓
                                        [Unlock for 5 minutes]
```

## 5. Data Models

### LockedApp (Data Class)
```kotlin
data class LockedApp(
    val packageName: String,           // e.g., "com.instagram.android"
    val appName: String,                // e.g., "Instagram"
    val iconBase64: String? = null,     // Optional: for UI
    val isLocked: Boolean = false,      // Manual lock state
    val isTotpEnabled: Boolean = false, // TOTP protection
    val secretKey: String? = null,      // Base32 TOTP secret
    val lastUnlockTime: Long = 0L       // Timestamp (for temporary unlock)
)
```

### Storage Format (SharedPreferences)
```json
{
  "locked_apps": [
    {
      "packageName": "com.instagram.android",
      "appName": "Instagram",
      "isLocked": true,
      "isTotpEnabled": true,
      "secretKey": "JBSWY3DPEHPK3PXP",
      "lastUnlockTime": 0
    }
  ]
}
```

### PreferencesHelper
```kotlin
object PreferencesHelper {
    private const val PREFS_NAME = "blocker_prefs"
    private const val KEY_LOCKED_APPS = "locked_apps"
    
    private lateinit var prefs: SharedPreferences
    private val gson = Gson()
    
    fun init(context: Context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }
    
    fun getLockedApps(): List<LockedApp> {
        val json = prefs.getString(KEY_LOCKED_APPS, "[]")
        val type = object : TypeToken<List<LockedApp>>() {}.type
        return gson.fromJson(json, type)
    }
    
    fun saveLockedApps(apps: List<LockedApp>) {
        val json = gson.toJson(apps)
        prefs.edit().putString(KEY_LOCKED_APPS, json).apply()
    }
    
    fun isLocked(packageName: String?): Boolean {
        return getLockedApps().any { 
            it.packageName == packageName && it.isLocked 
        }
    }
}
```

## 6. Project Structure

```
app/src/main/
├── java/com/hackathon/blockerapp/
│   ├── ui/
│   │   ├── MainActivity.kt
│   │   ├── BlockerOverlayActivity.kt
│   │   ├── TotpActivity.kt
│   │   └── adapters/
│   │       └── AppListAdapter.kt
│   ├── service/
│   │   └── AppBlockerAccessibilityService.kt
│   ├── utils/
│   │   ├── TotpManager.kt
│   │   ├── PreferencesHelper.kt
│   │   └── PermissionHelper.kt
│   ├── models/
│   │   └── LockedApp.kt
│   └── BlockerApplication.kt
├── res/
│   ├── layout/
│   │   ├── activity_main.xml
│   │   ├── activity_blocker_overlay.xml
│   │   ├── activity_totp.xml
│   │   └── item_app.xml
│   ├── xml/
│   │   └── accessibility_service_config.xml
│   └── values/
│       ├── strings.xml
│       └── themes.xml
└── AndroidManifest.xml
```

## 7. Implementation Roadmap (24-Hour Timeline)

### Phase 1: Project Setup & Basic UI (Hours 0-3)
- ✅ Create new Android Studio project (Kotlin, API 26+)
- ✅ Add dependencies (Gson, Commons Codec)
- ✅ Create MainActivity with RecyclerView
- ✅ Implement AppListAdapter to show installed apps
- ✅ Add Switch/Toggle for each app
- ✅ Create PreferencesHelper for data storage

**Milestone: Can see all installed apps in a list**

### Phase 2: Manual Locking (Hours 3-8)
- ✅ Implement AccessibilityService
- ✅ Add accessibility_service_config.xml
- ✅ Create BlockerOverlayActivity (fullscreen)
- ✅ Add "Unlock" button (manual unlock)
- ✅ Test: Toggle app → Try to open → Blocked
- ✅ Request permissions (Accessibility, Overlay)

**Milestone: Basic locking/unlocking works**

### Phase 3: TOTP Foundation (Hours 8-12)
- ✅ Implement TotpManager utility
- ✅ Add unit test for TOTP generation (verify with Google Authenticator)
- ✅ Create TotpActivity UI
- ✅ Add "Generate Secret" button
- ✅ Add "Import Secret" EditText + button
- ✅ Display current TOTP code with countdown timer

**Milestone: Can generate and display TOTP codes**

### Phase 4: TOTP Integration (Hours 12-16)
- ✅ Modify LockedApp model (add secretKey field)
- ✅ Update MainActivity: Add "TOTP Lock" option
- ✅ Link app → secret key association
- ✅ Modify BlockerOverlayActivity: Add code input field
- ✅ Validate TOTP code before unlocking
- ✅ Add temporary unlock (5 minutes)

**Milestone: Full TOTP workflow functional**

### Phase 5: Polish & Testing (Hours 16-22)
- ✅ Add search functionality to MainActivity
- ✅ Improve BlockerOverlayActivity UI (make it pretty)
- ✅ Add copy-to-clipboard for secret keys
- ✅ Test edge cases (wrong codes, expired codes)
- ✅ Add app icon and name to blocker screen
- ✅ Test on emulator with multiple apps

**Milestone: Stable, testable demo**

### Phase 6: Final Testing & Demo Prep (Hours 22-24)
- ✅ Create demo scenario (Instagram locked with TOTP)
- ✅ Write quick user guide
- ✅ Fix critical bugs
- ✅ Add simple onboarding screen
- ✅ Test accessibility service persistence
- ✅ Prepare presentation/slides

**Milestone: Ready to demo!**

## 8. Hackathon Shortcuts & Hacks

### Speed-Optimizing Shortcuts

1. **Skip Encryption**: Store secrets in plain text SharedPreferences
   - Production apps should use Android Keystore
   - For 24 hours: `prefs.putString("secret", key)`

2. **Use Material Components Default**: Don't customize themes
   ```gradle
   implementation 'com.google.android.material:material:1.9.0'
   ```
   Use `MaterialButton`, `MaterialCardView` out of the box

3. **Global Application Context**:
   ```kotlin
   class BlockerApplication : Application() {
       companion object {
           lateinit var instance: BlockerApplication
       }
       override fun onCreate() {
           super.onCreate()
           instance = this
       }
   }
   ```
   Access anywhere: `BlockerApplication.instance`

4. **Hardcode Time Windows**:
   - TOTP valid for ±1 time step (90 seconds total)
   - Temporary unlock: 5 minutes hardcoded
   - No configuration UI needed

5. **Skip Input Validation**:
   - Assume users enter valid 6-digit codes
   - No regex checking on secret keys
   - Crash = feature (you'll see the bug immediately)

6. **Single Activity Flags**:
   ```kotlin
   intent.addFlags(
       Intent.FLAG_ACTIVITY_NEW_TASK or
       Intent.FLAG_ACTIVITY_CLEAR_TOP or
       Intent.FLAG_ACTIVITY_NO_HISTORY
   )
   ```
   Prevents back button issues

7. **Simplified Accessibility Service**:
   - Only handle `TYPE_WINDOW_STATE_CHANGED`
   - Ignore other 20+ event types
   - No need for `onInterrupt()` implementation

8. **Use Synthetic Imports** (if using older Kotlin):
   ```kotlin
   import kotlinx.android.synthetic.main.activity_main.*
   // Direct access: recyclerView.adapter = ...
   ```
   Or use ViewBinding (2 minutes to enable)

9. **Inline Permission Requests**:
   ```kotlin
   if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
       if (!Settings.canDrawOverlays(this)) {
           startActivity(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION))
       }
   }
   ```

10. **Toast for Debugging**:
    ```kotlin
    Toast.makeText(context, "App: $packageName", Toast.LENGTH_SHORT).show()
    ```
    Faster than logcat filtering

### Critical Permissions (Don't Forget!)

**AndroidManifest.xml**:
```xml
<uses-permission android:name="android.permission.SYSTEM_ALERT_WINDOW" />
<uses-permission android:name="android.permission.QUERY_ALL_PACKAGES" />
<uses-permission android:name="android.permission.PACKAGE_USAGE_STATS"
    tools:ignore="ProtectedPermissions" />

<application>
    <service
        android:name=".service.AppBlockerAccessibilityService"
        android:permission="android.permission.BIND_ACCESSIBILITY_SERVICE"
        android:exported="true">
        <intent-filter>
            <action android:name="android.accessibility.AccessibilityService" />
        </intent-filter>
        <meta-data
            android:name="android.accessibilityservice"
            android:resource="@xml/accessibility_service_config" />
    </service>
</application>
```

**accessibility_service_config.xml**:
```xml
<accessibility-service
    xmlns:android="http://schemas.android.com/apk/res/android"
    android:accessibilityEventTypes="typeWindowStateChanged"
    android:accessibilityFeedbackType="feedbackGeneric"
    android:canRetrieveWindowContent="false"
    android:notificationTimeout="0" />
```

## 9. Testing Strategy

### Manual Test Checklist
- [ ] Install app on emulator
- [ ] Grant accessibility permission
- [ ] Grant overlay permission
- [ ] Lock Instagram (or Chrome)
- [ ] Try to open locked app → Should be blocked
- [ ] Unlock via button → Should open app
- [ ] Generate TOTP secret → Should show key
- [ ] Copy secret to clipboard → Should paste correctly
- [ ] Enable TOTP lock on app
- [ ] Try to open → Should ask for code
- [ ] Enter wrong code → Should stay blocked
- [ ] Enter correct code → Should unlock
- [ ] Wait 30 seconds → Code should change

### Demo Apps to Lock
- Chrome (always installed)
- Clock
- Calculator
- Any social media app (if available)

## 10. Potential Issues & Solutions

| Issue | Solution |
|-------|----------|
| Accessibility service stops after reboot | Add auto-start instructions in onboarding |
| User can disable accessibility service | Add periodic check in MainActivity.onResume() |
| Blocker screen dismissed by home button | Use `FLAG_ACTIVITY_NO_HISTORY` so it reopens |
| TOTP codes don't match Phone A & B | Ensure both devices have correct time (use NTP) |
| App crashes on Android 11+ | Add `QUERY_ALL_PACKAGES` permission |
| Overlay doesn't show | Check `Settings.canDrawOverlays()` |

## 11. Libraries & Dependencies

**build.gradle (app)**:
```gradle
dependencies {
    implementation 'androidx.core:core-ktx:1.10.1'
    implementation 'androidx.appcompat:appcompat:1.6.1'
    implementation 'com.google.android.material:material:1.9.0'
    implementation 'androidx.constraintlayout:constraintlayout:2.1.4'
    
    // JSON parsing
    implementation 'com.google.code.gson:gson:2.10.1'
    
    // TOTP (Base32 + HMAC)
    implementation 'commons-codec:commons-codec:1.15'
}
```

## 12. Success Criteria

### Minimum Viable Product (MVP)
- ✅ Display installed apps
- ✅ Lock/unlock apps with toggle
- ✅ Blocked app shows overlay
- ✅ Overlay can be dismissed

### Full Feature Set
- ✅ Generate TOTP secret keys
- ✅ Import secret keys (paste)
- ✅ Display current TOTP code
- ✅ TOTP code updates every 30s
- ✅ Validate TOTP code to unlock
- ✅ Temporary unlock (5 min)

### Stretch Goals (If Time Permits)
- QR code generation for secret keys
- Multiple secret keys per app
- Lock duration timer
- Statistics (how many times blocked)

---

## Final Notes

This architecture prioritizes **working code over clean code**. After the hackathon, you can refactor to:
- Use Room instead of SharedPreferences
- Add proper encryption (Keystore)
- Implement MVVM architecture
- Add unit tests (currently 0%)
- Handle edge cases properly

**Good luck with your hackathon! 🚀**

