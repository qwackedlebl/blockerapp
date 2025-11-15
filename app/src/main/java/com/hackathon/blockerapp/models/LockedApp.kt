package com.hackathon.blockerapp.models

data class LockedApp(
    val packageName: String,
    val appName: String,
    val isLocked: Boolean = false,
    val isTotpEnabled: Boolean = false,
    val secretKey: String? = null,
    val lastUnlockTime: Long = 0L
) {
    /**
     * Check if app is temporarily unlocked (within 5 minutes)
     */
    fun isTemporarilyUnlocked(): Boolean {
        if (lastUnlockTime == 0L) return false
        val fiveMinutes = 5 * 60 * 1000L
        return System.currentTimeMillis() - lastUnlockTime < fiveMinutes
    }
}

