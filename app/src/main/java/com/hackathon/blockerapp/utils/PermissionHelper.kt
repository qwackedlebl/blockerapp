package com.hackathon.blockerapp.utils

import android.accessibilityservice.AccessibilityServiceInfo
import android.app.Activity
import android.app.AppOpsManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Process
import android.provider.Settings
import android.view.accessibility.AccessibilityManager
import android.widget.Toast

object PermissionHelper {

    fun hasOverlayPermission(context: Context): Boolean {
        return Settings.canDrawOverlays(context)
    }

    fun requestOverlayPermission(activity: Activity) {
        try {
            // Try to open app-specific overlay permission settings
            val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION)
            intent.data = Uri.parse("package:${activity.packageName}")
            Toast.makeText(
                activity,
                "Enable \"Display over other apps\" permission for RotLocker",
                Toast.LENGTH_LONG
            ).show()
            activity.startActivity(intent)
        } catch (e: Exception) {
            // Fallback to general settings if app-specific doesn't work
            val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION)
            Toast.makeText(
                activity,
                "Find and enable RotLocker in the list",
                Toast.LENGTH_LONG
            ).show()
            activity.startActivity(intent)
        }
    }

    fun isAccessibilityServiceEnabled(context: Context): Boolean {
        val am = context.getSystemService(Context.ACCESSIBILITY_SERVICE) as AccessibilityManager
        val enabledServices = am.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_ALL_MASK)
        for (service in enabledServices) {
            if (service.id.startsWith(context.packageName)) {
                return true
            }
        }
        return false
    }

    fun requestAccessibilityPermission(activity: Activity) {
        val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
        Toast.makeText(
            activity,
            "Find and enable \"RotLocker\" under \"Downloaded apps\"",
            Toast.LENGTH_LONG
        ).show()
        activity.startActivity(intent)
    }

    fun hasUsageAccessPermission(context: Context): Boolean {
        val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        val mode = appOps.checkOpNoThrow(
            AppOpsManager.OPSTR_GET_USAGE_STATS,
            Process.myUid(),
            context.packageName
        )
        return mode == AppOpsManager.MODE_ALLOWED
    }

    fun requestUsageAccessPermission(activity: Activity) {
        try {
            // Try to open app-specific usage access settings
            val intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)
            intent.data = Uri.parse("package:${activity.packageName}")
            Toast.makeText(
                activity,
                "Enable Usage Access for RotLocker",
                Toast.LENGTH_LONG
            ).show()
            activity.startActivity(intent)
        } catch (e: Exception) {
            // Fallback to general settings if app-specific doesn't work
            val intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)
            Toast.makeText(
                activity,
                "Find and enable RotLocker for Usage Access",
                Toast.LENGTH_LONG
            ).show()
            activity.startActivity(intent)
        }
    }
}
