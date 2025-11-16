package com.hackathon.blockerapp.utils

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.hackathon.blockerapp.models.LockedApp
import com.hackathon.blockerapp.models.SecretKeyEntry

object PreferencesHelper {

    private const val PREFS_NAME = "blocker_prefs"
    private const val KEY_LOCKED_APPS = "locked_apps"
    private const val KEY_SECRET_KEYS = "secret_keys"
    private const val KEY_DEVICE_SECRET = "device_secret"

    private lateinit var prefs: SharedPreferences
    private val gson = Gson()

    fun init(context: Context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun getLockedApps(): List<LockedApp> {
        val json = prefs.getString(KEY_LOCKED_APPS, "[]") ?: "[]"
        val type = object : TypeToken<List<LockedApp>>() {}.type
        return gson.fromJson(json, type)
    }

    fun saveLockedApps(apps: List<LockedApp>) {
        val json = gson.toJson(apps)
        prefs.edit().putString(KEY_LOCKED_APPS, json).apply()
    }

    fun isLocked(packageName: String?): Boolean {
        if (packageName == null) return false
        return getLockedApps().any {
            it.packageName == packageName && it.isLocked && !it.isTemporarilyUnlocked()
        }
    }

    fun getLockedApp(packageName: String?): LockedApp? {
        if (packageName == null) return null
        return getLockedApps().find { it.packageName == packageName }
    }

    fun updateApp(updatedApp: LockedApp) {
        val apps = getLockedApps().toMutableList()
        val index = apps.indexOfFirst { it.packageName == updatedApp.packageName }
        if (index != -1) {
            apps[index] = updatedApp
            saveLockedApps(apps)
        }
    }

    // ==================== Secret Key List Management ====================

    /**
     * Get all stored secret keys
     */
    fun getSecretKeys(): List<SecretKeyEntry> {
        val json = prefs.getString(KEY_SECRET_KEYS, "[]") ?: "[]"
        val type = object : TypeToken<List<SecretKeyEntry>>() {}.type
        return gson.fromJson(json, type)
    }

    /**
     * Save the entire secret keys list
     */
    private fun saveSecretKeys(keys: List<SecretKeyEntry>) {
        val json = gson.toJson(keys)
        prefs.edit().putString(KEY_SECRET_KEYS, json).apply()
    }

    /**
     * Append a new secret key to the end of the list
     */
    fun appendSecretKey(label: String, secretKey: String) {
        val keys = getSecretKeys().toMutableList()
        keys.add(SecretKeyEntry(label, secretKey))
        saveSecretKeys(keys)
    }

    /**
     * Remove a secret key at the specified index
     * @return true if removed successfully, false if index out of bounds
     */
    fun removeSecretKeyAt(index: Int): Boolean {
        val keys = getSecretKeys().toMutableList()
        if (index < 0 || index >= keys.size) {
            return false
        }
        keys.removeAt(index)
        saveSecretKeys(keys)
        return true
    }

    /**
     * Remove a secret key by label
     * @return true if found and removed, false otherwise
     */
    fun removeSecretKeyByLabel(label: String): Boolean {
        val keys = getSecretKeys().toMutableList()
        val removed = keys.removeIf { it.label == label }
        if (removed) {
            saveSecretKeys(keys)
        }
        return removed
    }

    // ==================== Device Secret Key (Write Once) ====================

    /**
     * Get the device-specific secret key
     * @return The device secret key, or null if not yet set
     */
    fun getDeviceSecretKey(): String? {
        return prefs.getString(KEY_DEVICE_SECRET, null)
    }

    /**
     * Set the device-specific secret key (can only be set once)
     * @return true if set successfully, false if already exists
     */
    fun setDeviceSecretKey(secretKey: String): Boolean {
        // Check if already set
        if (getDeviceSecretKey() != null) {
            return false
        }
        prefs.edit().putString(KEY_DEVICE_SECRET, secretKey).apply()
        return true
    }

    /**
     * Check if device secret key has been initialized
     */
    fun hasDeviceSecretKey(): Boolean {
        return getDeviceSecretKey() != null
    }
}
