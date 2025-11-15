package com.hackathon.blockerapp.utils

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.hackathon.blockerapp.models.LockedApp

object PreferencesHelper {

    private const val PREFS_NAME = "blocker_prefs"
    private const val KEY_LOCKED_APPS = "locked_apps"

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
}
