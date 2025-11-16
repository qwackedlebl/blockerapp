# Persistent Storage Quick Reference

## Initialization (Required First)
```kotlin
PreferencesHelper.init(context)
```

---

## Secret Key List (Label + Secret Key Table)

| Operation | Code | Returns |
|-----------|------|---------|
| **Get all** | `PreferencesHelper.getSecretKeys()` | `List<SecretKeyEntry>` |
| **Append** | `PreferencesHelper.appendSecretKey(label, key)` | `Unit` |
| **Remove by index** | `PreferencesHelper.removeSecretKeyAt(index)` | `Boolean` |
| **Remove by label** | `PreferencesHelper.removeSecretKeyByLabel(label)` | `Boolean` |

---

## Device Secret Key (Write-Once)

| Operation | Code | Returns |
|-----------|------|---------|
| **Check if exists** | `PreferencesHelper.hasDeviceSecretKey()` | `Boolean` |
| **Get key** | `PreferencesHelper.getDeviceSecretKey()` | `String?` |
| **Set key (once)** | `PreferencesHelper.setDeviceSecretKey(key)` | `Boolean` |

---

## Common Patterns

### First Launch Setup
```kotlin
if (!PreferencesHelper.hasDeviceSecretKey()) {
    val secret = TotpManager.generateSecretKey()
    PreferencesHelper.setDeviceSecretKey(secret)
}
```

### Display All Secrets
```kotlin
PreferencesHelper.getSecretKeys().forEachIndexed { index, entry ->
    println("[$index] ${entry.label}: ${entry.secretKey}")
}
```

### Safe Removal
```kotlin
if (PreferencesHelper.removeSecretKeyAt(index)) {
    // Success
} else {
    // Index out of bounds
}
```

---

## Data Models

### SecretKeyEntry
```kotlin
data class SecretKeyEntry(
    val label: String,      // "Alice's Phone"
    val secretKey: String   // "JBSWY3DPEHPK3PXP"
)
```

---

## Notes
- ✅ Data persists across app restarts
- ⚠️ Data is NOT encrypted (plain text)
- ❌ Data lost on app uninstall
- Device secret can only be set **once** (write-once, read-many)
