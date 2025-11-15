package com.hackathon.blockerapp

import android.app.Application
import com.hackathon.blockerapp.utils.PreferencesHelper

class BlockerApplication : Application() {

    companion object {
        lateinit var instance: BlockerApplication
            private set
    }

    override fun onCreate() {
        super.onCreate()
        instance = this

        // Initialize preferences helper
        PreferencesHelper.init(this)
    }
}

