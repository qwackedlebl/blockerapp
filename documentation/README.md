# BlockerApp Documentation

Welcome to the BlockerApp technical documentation.

## 📚 Documentation Index

### Storage & Data
- **[Persistent Storage Guide](PERSISTENT_STORAGE.md)** - Complete guide to data storage, including secret keys and device configuration
- **[Quick Reference](QUICK_REFERENCE.md)** - Cheat sheet for common storage operations

### Architecture & Implementation
- **[Architecture Overview](../ARCHITECTURE.md)** - System design and component overview
- **[Implementation Guide](../IMPLEMENTATION.md)** - Step-by-step implementation details
- **[Project Summary](../PROJECT_SUMMARY.md)** - High-level project description

### Getting Started
- **[README](../README.md)** - Main project README with features and setup
- **[Quick Start](../QUICKSTART.md)** - Fast setup guide for development

---

## Quick Links

### Data Storage
```kotlin
// Initialize (required first)
PreferencesHelper.init(context)

// Device secret (write-once)
PreferencesHelper.setDeviceSecretKey(secret)
val key = PreferencesHelper.getDeviceSecretKey()

// Secret key list
PreferencesHelper.appendSecretKey("Label", "Secret")
val keys = PreferencesHelper.getSecretKeys()
PreferencesHelper.removeSecretKeyAt(index)
```

### Key Files
| Path | Purpose |
|------|---------|
| `utils/PreferencesHelper.kt` | Storage API |
| `models/SecretKeyEntry.kt` | Secret key model |
| `models/LockedApp.kt` | Locked app model |
| `service/AppBlockerAccessibilityService.kt` | App detection |
| `ui/BlockerOverlayActivity.kt` | Lock screen |

---

## Need Help?

1. Check the [Quick Reference](QUICK_REFERENCE.md) for common operations
2. Read the [Persistent Storage Guide](PERSISTENT_STORAGE.md) for detailed examples
3. Review existing code in the `utils/` and `models/` directories
4. Check Android Studio's autocomplete for available methods

---

**Last Updated:** 2025-11-15
