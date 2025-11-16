# Persistent Storage Guide

## Overview

BlockerApp uses **SharedPreferences** with **Gson** for JSON serialization to store persistent data. All data persists across app restarts and device reboots, but is cleared when the app is uninstalled.

## Storage Location

All persistent storage is managed through `PreferencesHelper.kt` utility class.

**Path:** `/app/src/main/java/com/hackathon/blockerapp/utils/PreferencesHelper.kt`

---

## Data Models

### 1. SecretKeyEntry

Stores a labeled secret key pair.

**Location:** `/app/src/main/java/com/hackathon/blockerapp/models/SecretKeyEntry.kt`

```kotlin
data class SecretKeyEntry(
    val label: String,      // Human-readable label (e.g., "Friend's Phone")
    val secretKey: String   // Base32-encoded TOTP secret key
)
```

### 2. LockedApp

Stores information about locked apps.

**Location:** `/app/src/main/java/com/hackathon/blockerapp/models/LockedApp.kt`

```kotlin
data class LockedApp(
    val packageName: String,
    val appName: String,
    val isLocked: Boolean = false,
    val isTotpEnabled: Boolean = false,
    val secretKey: String? = null,
    val lastUnlockTime: Long = 0L
)
```

---

## API Reference

### Initialization

**Must be called before any other PreferencesHelper methods** (typically in `MainActivity.onCreate()`):

```kotlin
PreferencesHelper.init(context)
```

---

## Secret Key List Management

Store and manage a list of labeled secret keys (e.g., for multiple friends' devices).

### Get All Secret Keys

```kotlin
val keys: List<SecretKeyEntry> = PreferencesHelper.getSecretKeys()
```

**Returns:** List of all stored secret key entries (empty list if none)

**Example:**
```kotlin
val keys = PreferencesHelper.getSecretKeys()
keys.forEach { entry ->
    println("${entry.label}: ${entry.secretKey}")
}
// Output:
// Alice's Phone: JBSWY3DPEHPK3PXP
// Bob's Phone: ABCD1234EFGH5678
```

---

### Append Secret Key

Add a new secret key to the **end** of the list.

```kotlin
PreferencesHelper.appendSecretKey(label: String, secretKey: String)
```

**Parameters:**
- `label`: Human-readable name for this secret
- `secretKey`: Base32-encoded TOTP secret

**Example:**
```kotlin
PreferencesHelper.appendSecretKey("Alice's Phone", "JBSWY3DPEHPK3PXP")
PreferencesHelper.appendSecretKey("Bob's Phone", "ABCD1234EFGH5678")

// List now contains: [Alice's Phone, Bob's Phone]
```

---

### Remove Secret Key by Index

Remove a secret key at a specific position.

```kotlin
val success: Boolean = PreferencesHelper.removeSecretKeyAt(index: Int)
```

**Parameters:**
- `index`: Position in the list (0-based)

**Returns:**
- `true` if removed successfully
- `false` if index is out of bounds

**Example:**
```kotlin
// List: [Alice's Phone, Bob's Phone, Carol's Phone]

PreferencesHelper.removeSecretKeyAt(1)  // Removes "Bob's Phone"

// List now: [Alice's Phone, Carol's Phone]
```

---

### Remove Secret Key by Label

Remove the first secret key matching the given label.

```kotlin
val success: Boolean = PreferencesHelper.removeSecretKeyByLabel(label: String)
```

**Parameters:**
- `label`: The label to search for (case-sensitive)

**Returns:**
- `true` if found and removed
- `false` if not found

**Example:**
```kotlin
// List: [Alice's Phone, Bob's Phone]

PreferencesHelper.removeSecretKeyByLabel("Alice's Phone")  // Returns true

// List now: [Bob's Phone]

PreferencesHelper.removeSecretKeyByLabel("Unknown")  // Returns false (not found)
```

---

## Device Secret Key (Write-Once Storage)

Store a unique secret key for this device. Can only be set **once** (immutable after first write).

### Check if Device Secret Exists

```kotlin
val exists: Boolean = PreferencesHelper.hasDeviceSecretKey()
```

**Returns:** `true` if the device secret has been set, `false` otherwise

**Example:**
```kotlin
if (!PreferencesHelper.hasDeviceSecretKey()) {
    // First launch - need to initialize
}
```

---

### Get Device Secret Key

```kotlin
val secret: String? = PreferencesHelper.getDeviceSecretKey()
```

**Returns:**
- The device secret key if set
- `null` if not yet initialized

**Example:**
```kotlin
val deviceKey = PreferencesHelper.getDeviceSecretKey()
if (deviceKey != null) {
    // Use the key
    val code = TotpManager.generateCode(deviceKey)
}
```

---

### Set Device Secret Key (Write-Once)

Set the device secret key. **Can only be called once successfully.**

```kotlin
val success: Boolean = PreferencesHelper.setDeviceSecretKey(secretKey: String)
```

**Parameters:**
- `secretKey`: Base32-encoded TOTP secret to store

**Returns:**
- `true` if set successfully (first time)
- `false` if already exists (subsequent calls)

**Example:**
```kotlin
// First launch
val newSecret = TotpManager.generateSecretKey()
val success = PreferencesHelper.setDeviceSecretKey(newSecret)  // Returns true

// Try to set again
PreferencesHelper.setDeviceSecretKey("different_key")  // Returns false (already set)
```

---

## Locked Apps Management

Manage which apps are locked and their TOTP settings.

### Get All Locked Apps

```kotlin
val apps: List<LockedApp> = PreferencesHelper.getLockedApps()
```

### Get Specific Locked App

```kotlin
val app: LockedApp? = PreferencesHelper.getLockedApp(packageName: String?)
```

### Check if App is Locked

Checks if an app is locked **and** not temporarily unlocked.

```kotlin
val isLocked: Boolean = PreferencesHelper.isLocked(packageName: String?)
```

**Note:** Returns `false` if the app was unlocked in the last 5 minutes.

### Update App Settings

```kotlin
PreferencesHelper.updateApp(updatedApp: LockedApp)
```

---

## Complete Usage Examples

### Example 1: First Launch Setup

```kotlin
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize PreferencesHelper
        PreferencesHelper.init(this)

        // Check if this is first launch
        if (!PreferencesHelper.hasDeviceSecretKey()) {
            // Generate and store device-specific secret
            val deviceSecret = TotpManager.generateSecretKey()
            PreferencesHelper.setDeviceSecretKey(deviceSecret)

            Toast.makeText(this, "Device initialized", Toast.LENGTH_SHORT).show()
        }
    }
}
```

---

### Example 2: Managing Friend's Secret Keys

```kotlin
class FriendKeysActivity : AppCompatActivity() {

    fun addFriendKey() {
        // User enters label and secret
        val label = binding.labelInput.text.toString()
        val secret = binding.secretInput.text.toString()

        // Append to list
        PreferencesHelper.appendSecretKey(label, secret)

        refreshList()
    }

    fun removeFriend(index: Int) {
        if (PreferencesHelper.removeSecretKeyAt(index)) {
            Toast.makeText(this, "Removed successfully", Toast.LENGTH_SHORT).show()
            refreshList()
        } else {
            Toast.makeText(this, "Invalid index", Toast.LENGTH_SHORT).show()
        }
    }

    fun refreshList() {
        val keys = PreferencesHelper.getSecretKeys()

        // Display in RecyclerView or ListView
        keys.forEachIndexed { index, entry ->
            println("[$index] ${entry.label}")
        }
    }
}
```

---

### Example 3: TOTP Code Generation

```kotlin
class TotpDisplayActivity : AppCompatActivity() {

    fun showCurrentCode() {
        // Get device secret
        val deviceSecret = PreferencesHelper.getDeviceSecretKey()

        if (deviceSecret != null) {
            // Generate current TOTP code
            val code = TotpManager.generateCode(deviceSecret)
            val remaining = TotpManager.getRemainingSeconds()

            binding.codeText.text = code
            binding.timerText.text = "Refreshes in $remaining seconds"
        } else {
            binding.codeText.text = "Device not initialized"
        }
    }

    fun verifyFriendCode() {
        // Get a friend's secret key
        val keys = PreferencesHelper.getSecretKeys()
        if (keys.isNotEmpty()) {
            val friendSecret = keys[0].secretKey
            val userInput = binding.inputCode.text.toString()

            if (TotpManager.verifyCode(friendSecret, userInput)) {
                Toast.makeText(this, "Code verified!", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Invalid code", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
```

---

## Storage Characteristics

### ✅ Advantages
- **Persistent:** Data survives app restarts and device reboots
- **Simple:** Easy to use key-value storage
- **Automatic:** Gson handles serialization/deserialization
- **Fast:** In-memory cache with disk persistence

### ⚠️ Limitations
- **Not Encrypted:** Data is stored in plain text XML
- **Lost on Uninstall:** All data is cleared when app is uninstalled
- **Size Limit:** ~1MB recommended (SharedPreferences not for large data)
- **No Transactions:** Not suitable for complex relational data

### 🔒 Security Notes
For production apps, consider:
- Encrypting secret keys using Android Keystore
- Using EncryptedSharedPreferences (Jetpack Security)
- Implementing secure key generation and storage

---

## Best Practices

### 1. Always Initialize First
```kotlin
// In Application class or first Activity
PreferencesHelper.init(context)
```

### 2. Check Before Reading Device Secret
```kotlin
val secret = PreferencesHelper.getDeviceSecretKey() ?: run {
    // Handle uninitialized state
    return
}
```

### 3. Validate Indices Before Removal
```kotlin
if (index >= 0 && index < PreferencesHelper.getSecretKeys().size) {
    PreferencesHelper.removeSecretKeyAt(index)
}
```

### 4. Use Labels for User-Friendly Display
```kotlin
// Good: "Alice's Phone (Instagram)"
PreferencesHelper.appendSecretKey("Alice's Phone (Instagram)", secret)

// Bad: "key1"
PreferencesHelper.appendSecretKey("key1", secret)
```

---

## Troubleshooting

### Data Not Persisting
**Problem:** Changes don't survive app restart
**Solution:** Ensure `PreferencesHelper.init(context)` is called before any operations

### NullPointerException
**Problem:** Crash when accessing preferences
**Solution:** Call `init()` in `Application.onCreate()` or `MainActivity.onCreate()`

### Device Secret Can't Be Set
**Problem:** `setDeviceSecretKey()` returns false
**Solution:** This is expected behavior after first launch. Use `getDeviceSecretKey()` to read the existing value.

### Empty List After Adding Keys
**Problem:** `getSecretKeys()` returns empty list after `appendSecretKey()`
**Solution:** Verify no exceptions are thrown. Check Logcat for Gson serialization errors.

---

## File References

| File | Purpose |
|------|---------|
| `utils/PreferencesHelper.kt` | All storage operations |
| `models/SecretKeyEntry.kt` | Secret key data model |
| `models/LockedApp.kt` | Locked app data model |
| `utils/TotpManager.kt` | TOTP generation/verification |

---

## Related Documentation

- [TOTP Implementation Guide](./TOTP_GUIDE.md) *(if exists)*
- [Architecture Overview](../ARCHITECTURE.md)
- [Android SharedPreferences Docs](https://developer.android.com/training/data-storage/shared-preferences)
