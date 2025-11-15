package com.hackathon.blockerapp.service

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import com.hackathon.blockerapp.ui.BlockerOverlayActivity
import com.hackathon.blockerapp.utils.PreferencesHelper

class AppBlockerAccessibilityService : AccessibilityService() {

    companion object {
        private const val TAG = "AppBlockerService"
        private var lastBlockedPackage: String? = null
        private var lastBlockTime: Long = 0
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event?.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            val packageName = event.packageName?.toString() ?: return

            // Ignore our own app
            if (packageName.startsWith("com.hackathon.blockerapp")) {
                return
            }

            // Prevent blocking the same app multiple times rapidly
            val currentTime = System.currentTimeMillis()
            if (packageName == lastBlockedPackage && currentTime - lastBlockTime < 1000) {
                return
            }

            // Check if this app should be blocked
            if (PreferencesHelper.isLocked(packageName)) {
                Log.d(TAG, "Blocking app: $packageName")

                lastBlockedPackage = packageName
                lastBlockTime = currentTime

                // Launch blocking overlay - this will cover the blocked app
                val intent = Intent(this, BlockerOverlayActivity::class.java).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                    addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS)
                    putExtra("blocked_package", packageName)
                }
                startActivity(intent)
            }
        }
    }

    override fun onInterrupt() {
        Log.d(TAG, "Accessibility service interrupted")
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        Log.d(TAG, "Accessibility service connected")
    }
}

