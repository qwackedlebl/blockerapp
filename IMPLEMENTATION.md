# BlockerApp Implementation Guide

## Quick Start

### 1. Open the Project
1. Open Android Studio
2. Select "Open an Existing Project"
3. Navigate to `C:\Users\ilyas\StudioProjects\blockerapp`
4. Wait for Gradle sync to complete

### 2. Build and Run
```bash
# In Android Studio, click the "Run" button (green play icon)
# Or use the menu: Run > Run 'app'
```

### 3. Grant Permissions
The app requires two critical permissions:

**A. Accessibility Service**
1. Open Settings > Accessibility
2. Find "BlockerApp" in the list
3. Toggle it ON
4. Confirm the warning dialog

**B. Overlay Permission**
1. Settings > Apps > Special app access > Display over other apps
2. Find "BlockerApp"
3. Allow permission

## How to Use

### Basic Locking (Manual Unlock)

1. **Lock an App**
   - Open BlockerApp
   - Find the app you want to lock (use search if needed)
   - Toggle the switch ON for that app
   
2. **Test the Lock**
   - Try to open the locked app
   - You'll see a black blocking screen
   - Click "Unlock App" to access it temporarily (5 minutes)

### TOTP-Based Locking (Two-Device Setup)

#### On Phone B (Code Generator)
1. Open BlockerApp
2. Tap the key icon (FAB) at bottom-right
3. Tap "Generate" to create a secret key
4. Tap "Copy" to copy the secret key
5. Send via SMS to Phone A
6. The current 6-digit code is displayed and updates every 30 seconds

#### On Phone A (Protected Device)
1. Open BlockerApp
2. Find the app you want to protect
3. Toggle the lock switch ON
4. Tap the settings icon next to the app
5. Paste the secret key received from Phone B
6. Tap "Import from Field"
7. Tap "Enable TOTP"
8. The app is now TOTP-protected

#### Unlocking a TOTP-Protected App
1. Try to open the locked app on Phone A
2. Blocking screen appears with code input
3. Get current code from Phone B (visible in TOTP Manager)
4. Enter the 6-digit code on Phone A
5. Tap "Verify & Unlock"
6. App unlocks for 5 minutes

## Project Structure

```
app/src/main/java/com/hackathon/blockerapp/
├── BlockerApplication.kt              # App initialization
├── models/
│   └── LockedApp.kt                   # Data model
├── service/
│   └── AppBlockerAccessibilityService.kt  # Background monitor
├── ui/
│   ├── MainActivity.kt                # App list screen
│   ├── BlockerOverlayActivity.kt      # Lock screen
│   ├── TotpActivity.kt                # TOTP manager
│   └── adapters/
│       └── AppListAdapter.kt          # RecyclerView adapter
└── utils/
    ├── PreferencesHelper.kt           # Data storage
    ├── TotpManager.kt                 # TOTP generation
    └── PermissionHelper.kt            # Permission checks
```

## Architecture Overview

### Components

1. **AccessibilityService** (`AppBlockerAccessibilityService`)
   - Runs in background
   - Detects when apps are launched
   - Checks if app is locked
   - Launches blocking overlay if needed

2. **Data Storage** (`PreferencesHelper`)
   - Uses SharedPreferences + Gson
   - Stores locked apps and secret keys
   - Simple JSON serialization

3. **TOTP Engine** (`TotpManager`)
   - Generates Base32 secret keys
   - Creates 6-digit codes (30-second validity)
   - Verifies codes with ±30 second tolerance

4. **UI Components**
   - `MainActivity`: List all installed apps, toggle locks
   - `BlockerOverlayActivity`: Fullscreen lock screen
   - `TotpActivity`: Generate/import secret keys, view codes

## Testing Checklist

- [ ] App displays list of installed apps
- [ ] Can lock an app via toggle switch
- [ ] Locked app shows blocking screen when launched
- [ ] Manual unlock button works
- [ ] Can generate TOTP secret key
- [ ] Can copy secret key to clipboard
- [ ] Can import secret key
- [ ] TOTP code updates every 30 seconds
- [ ] Can enable TOTP for an app
- [ ] TOTP-locked app requires code to unlock
- [ ] Wrong code shows error
- [ ] Correct code unlocks app
- [ ] Temporary unlock lasts 5 minutes
- [ ] Search functionality works
- [ ] Accessibility service persists after app restart

## Troubleshooting

### "App doesn't block anything"
- Check if Accessibility Service is enabled in Settings
- Check if Overlay permission is granted
- Restart the device after enabling permissions

### "Accessibility Service keeps disabling"
- Some devices (Xiaomi, Samsung) kill background services
- Add BlockerApp to battery optimization whitelist
- Enable "Autostart" permission

### "TOTP codes don't match"
- Ensure both devices have correct time (enable auto-sync)
- Check if secret key was copied correctly
- Codes are valid for 90 seconds (±30 seconds tolerance)

### "App crashes when opening locked app"
- Check logcat for errors
- Ensure overlay permission is granted
- Try clearing app data and re-granting permissions

### Build Errors
```bash
# Sync Gradle
File > Sync Project with Gradle Files

# Clean and rebuild
Build > Clean Project
Build > Rebuild Project

# Invalidate caches
File > Invalidate Caches / Restart
```

## Known Limitations

1. **Not Production-Ready**
   - Secret keys stored in plain text
   - No encryption
   - Minimal error handling
   - Can be bypassed by tech-savvy users

2. **Battery Usage**
   - Accessibility Service runs continuously
   - Moderate battery impact

3. **Android Version Compatibility**
   - Tested on Android 8.0+ (API 26+)
   - Some features may not work on older versions

## Development Timeline (24-Hour Hackathon)

### Phase 1: Basic Locking (0-8 hours) ✅
- Project setup
- MainActivity with app list
- AccessibilityService implementation
- BlockerOverlayActivity
- Manual lock/unlock

### Phase 2: TOTP Integration (8-16 hours) ✅
- TotpManager utility
- Secret key generation
- Code display with countdown
- TOTP verification in blocker screen

### Phase 3: Polish (16-22 hours)
- Search functionality
- Permission handling
- UI improvements
- Bug fixes

### Phase 4: Testing (22-24 hours)
- End-to-end testing
- Demo preparation
- Documentation

## Next Steps (Post-Hackathon)

1. **Security Improvements**
   - Use Android Keystore for secret storage
   - Encrypt SharedPreferences
   - Add biometric authentication

2. **Features**
   - QR code sharing for secrets
   - App usage statistics
   - Custom unlock durations
   - Whitelist mode (lock all except selected)

3. **UX Enhancements**
   - Onboarding tutorial
   - Better error messages
   - Dark theme
   - App icons in list

## API Documentation

### TotpManager

```kotlin
// Generate a random secret key
val secretKey = TotpManager.generateSecretKey()

// Generate current 6-digit code
val code = TotpManager.generateCode(secretKey)

// Verify a code
val isValid = TotpManager.verifyCode(secretKey, "123456")

// Get remaining seconds
val seconds = TotpManager.getRemainingSeconds()
```

### PreferencesHelper

```kotlin
// Get all locked apps
val apps = PreferencesHelper.getLockedApps()

// Save apps
PreferencesHelper.saveLockedApps(updatedList)

// Check if app is locked
val locked = PreferencesHelper.isLocked("com.instagram.android")

// Update a single app
PreferencesHelper.updateApp(modifiedApp)
```

## Resources

- **RFC 6238**: TOTP Specification
- **Apache Commons Codec**: Base32 encoding
- **Material Design 3**: UI components
- **Android Accessibility**: Background app monitoring

## Support

For issues during the hackathon:
1. Check logcat output
2. Review ARCHITECTURE.md
3. Test on emulator first
4. Verify permissions are granted

## License

Hackathon project - Use as needed!

