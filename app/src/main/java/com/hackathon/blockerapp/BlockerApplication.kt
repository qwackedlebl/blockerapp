package com.hackathon.blockerapp

import android.app.Application
import android.util.Log
import com.hackathon.blockerapp.utils.PreferencesHelper
import com.hackathon.blockerapp.utils.TotpManager

class BlockerApplication : Application() {

    companion object {
        lateinit var instance: BlockerApplication
            private set
        private const val TAG = "BlockerApplication"
    }

    override fun onCreate() {
        super.onCreate()
        instance = this

        // Initialize preferences helper
        PreferencesHelper.init(this)

        // Generate device-specific secret key on first launch
        initializeDeviceSecret()
    }

    private fun initializeDeviceSecret() {
        if (!PreferencesHelper.hasDeviceSecretKey()) {
            // First launch - generate and store device secret
            val deviceSecret = TotpManager.generateSecretKey()
            val success = PreferencesHelper.setDeviceSecretKey(deviceSecret)

            if (success) {
                Log.d(TAG, "Device secret key generated and stored")
                Log.d(TAG, "Device Secret: $deviceSecret")
            } else {
                Log.e(TAG, "Failed to store device secret key")
            }
        } else {
            Log.d(TAG, "Device secret key already exists")
        }
    }
}

