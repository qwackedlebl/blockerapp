package com.hackathon.blockerapp.utils
}
    }
        }
            saveLockedApps(apps)
            apps[index] = updatedApp
        if (index != -1) {
        val index = apps.indexOfFirst { it.packageName == updatedApp.packageName }
        val apps = getLockedApps().toMutableList()
    fun updateApp(updatedApp: LockedApp) {

    }
        return getLockedApps().find { it.packageName == packageName }
        if (packageName == null) return null
    fun getLockedApp(packageName: String?): LockedApp? {

    }
        }
            it.packageName == packageName && it.isLocked && !it.isTemporarilyUnlocked()
        return getLockedApps().any {
        if (packageName == null) return false
    fun isLocked(packageName: String?): Boolean {

    }
        prefs.edit().putString(KEY_LOCKED_APPS, json).apply()
        val json = gson.toJson(apps)
    fun saveLockedApps(apps: List<LockedApp>) {

    }
        return gson.fromJson(json, type)
        val type = object : TypeToken<List<LockedApp>>() {}.type
        val json = prefs.getString(KEY_LOCKED_APPS, "[]") ?: "[]"
    fun getLockedApps(): List<LockedApp> {

    }
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    fun init(context: Context) {

    private val gson = Gson()
    private lateinit var prefs: SharedPreferences

    private const val KEY_LOCKED_APPS = "locked_apps"
    private const val PREFS_NAME = "blocker_prefs"
object PreferencesHelper {

import com.hackathon.blockerapp.models.LockedApp
import com.google.gson.reflect.TypeToken
import com.google.gson.Gson
import android.content.SharedPreferences
import android.content.Context


