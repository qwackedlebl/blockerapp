package com.hackathon.blockerapp.utils

import org.apache.commons.codec.binary.Base32
import java.nio.ByteBuffer
import java.security.SecureRandom
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

object TotpManager {
    private const val TIME_STEP = 30L // 30 seconds

    /**
     * Generate a random Base32-encoded secret key
     */
    fun generateSecretKey(): String {
        val random = SecureRandom()
        val bytes = ByteArray(20) // 160 bits
        random.nextBytes(bytes)
        return Base32().encodeToString(bytes).replace("=", "")
    }

    /**
     * Generate a 6-digit TOTP code from a secret key
     */
    fun generateCode(secretKey: String, timeStep: Long = TIME_STEP): String {
        try {
            val base32 = Base32()
            val key = base32.decode(secretKey)
            val time = System.currentTimeMillis() / 1000 / timeStep
            val msg = ByteBuffer.allocate(8).putLong(time).array()

            val mac = Mac.getInstance("HmacSHA1")
            mac.init(SecretKeySpec(key, "HmacSHA1"))
            val hash = mac.doFinal(msg)

            val offset = hash[hash.size - 1].toInt() and 0x0f
            val binary = ((hash[offset].toInt() and 0x7f) shl 24) or
                         ((hash[offset + 1].toInt() and 0xff) shl 16) or
                         ((hash[offset + 2].toInt() and 0xff) shl 8) or
                         (hash[offset + 3].toInt() and 0xff)

            val otp = binary % 1000000
            return otp.toString().padStart(6, '0')
        } catch (e: Exception) {
            e.printStackTrace()
            return "000000"
        }
    }

    /**
     * Verify if the input code matches the current TOTP code
     * Checks current time step ±1 (90 seconds total window)
     */
    fun verifyCode(secretKey: String, inputCode: String): Boolean {
        if (inputCode.length != 6) return false

        // Check current time step
        val currentCode = generateCode(secretKey)
        if (currentCode == inputCode) return true

        // Check previous time step (allow 30 second clock skew)
        val time = System.currentTimeMillis() / 1000
        val previousTime = (time - TIME_STEP) / TIME_STEP
        val previousCode = generateCodeForTime(secretKey, previousTime)
        if (previousCode == inputCode) return true

        // Check next time step (allow 30 second clock skew)
        val nextTime = (time + TIME_STEP) / TIME_STEP
        val nextCode = generateCodeForTime(secretKey, nextTime)
        if (nextCode == inputCode) return true

        return false
    }

    private fun generateCodeForTime(secretKey: String, timeValue: Long): String {
        try {
            val base32 = Base32()
            val key = base32.decode(secretKey)
            val msg = ByteBuffer.allocate(8).putLong(timeValue).array()

            val mac = Mac.getInstance("HmacSHA1")
            mac.init(SecretKeySpec(key, "HmacSHA1"))
            val hash = mac.doFinal(msg)

            val offset = hash[hash.size - 1].toInt() and 0x0f
            val binary = ((hash[offset].toInt() and 0x7f) shl 24) or
                         ((hash[offset + 1].toInt() and 0xff) shl 16) or
                         ((hash[offset + 2].toInt() and 0xff) shl 8) or
                         (hash[offset + 3].toInt() and 0xff)

            val otp = binary % 1000000
            return otp.toString().padStart(6, '0')
        } catch (e: Exception) {
            return "000000"
        }
    }

    /**
     * Get remaining seconds until code changes
     */
    fun getRemainingSeconds(): Int {
        val time = System.currentTimeMillis() / 1000
        return (TIME_STEP - (time % TIME_STEP)).toInt()
    }
}

