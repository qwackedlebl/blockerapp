package com.hackathon.blockerapp.ui

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.hackathon.blockerapp.databinding.ActivityBlockerOverlayBinding
import com.hackathon.blockerapp.utils.PreferencesHelper
import com.hackathon.blockerapp.utils.TotpManager

class BlockerOverlayActivity : AppCompatActivity() {

    private lateinit var binding: ActivityBlockerOverlayBinding
    private var blockedPackage: String? = null
    private val handler = Handler(Looper.getMainLooper())
    private var totpUpdateRunnable: Runnable? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Make this a fullscreen activity that appears over other apps
        window.addFlags(
            WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
            WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD or
            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or
            WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
        )

        binding = ActivityBlockerOverlayBinding.inflate(layoutInflater)
        setContentView(binding.root)

        blockedPackage = intent.getStringExtra("blocked_package")

        setupUI()
    }

    private fun setupUI() {
        val lockedApp = PreferencesHelper.getLockedApp(blockedPackage)

        if (lockedApp == null) {
            finish()
            return
        }

        binding.blockedAppName.text = lockedApp.appName
        binding.blockedPackageName.text = lockedApp.packageName

        // Get device secret key for TOTP verification
        val deviceSecret = PreferencesHelper.getDeviceSecretKey()

        if (deviceSecret != null) {
            // TOTP-protected mode using device secret
            setupTotpMode(deviceSecret)
        } else {
            // Fallback to manual unlock if device secret not initialized
            setupManualMode()
        }
    }

    private fun setupTotpMode(secretKey: String) {
        binding.totpInputLayout.visibility = android.view.View.VISIBLE

        binding.unlockMessage.text = "This app is locked.\nEnter the 6-digit code from your accountability partner's device."

        // Show current code for debugging (remove in production)
        updateTotpDisplay(secretKey)

        binding.verifyButton.setOnClickListener {
            val inputCode = binding.codeInput.text.toString()

            // Clear previous errors
            binding.totpInputLayout.error = null

            // Validate input
            if (!validateCode(inputCode)) {
                return@setOnClickListener
            }

            // Verify TOTP code
            if (TotpManager.verifyCode(secretKey, inputCode)) {
                unlockApp()
            } else {
                binding.totpInputLayout.error = "Invalid code"
                binding.codeInput.setText("")
                binding.codeInput.requestFocus()
            }
        }
    }

    private fun validateCode(code: String): Boolean {
        // Validation 1: Check if empty
        if (code.isEmpty()) {
            binding.totpInputLayout.error = "Code is required"
            return false
        }

        // Validation 2: Check if digits only
        if (!code.matches("^[0-9]+$".toRegex())) {
            binding.totpInputLayout.error = "Code must contain digits only"
            return false
        }

        // Validation 3: Check if too short
        if (code.length < 6) {
            binding.totpInputLayout.error = "Code is too short (6 digits required)"
            return false
        }

        return true
    }

    private fun updateTotpDisplay(secretKey: String) {
        totpUpdateRunnable = object : Runnable {
            override fun run() {
                val code = TotpManager.generateCode(secretKey)
                val remaining = TotpManager.getRemainingSeconds()

                binding.currentCode.text = "Debug: $code ($remaining s)"
                binding.currentCode.visibility = android.view.View.VISIBLE

                handler.postDelayed(this, 1000)
            }
        }
        handler.post(totpUpdateRunnable!!)
    }

    private fun setupManualMode() {
        binding.totpInputLayout.visibility = android.view.View.GONE
        binding.verifyButton.visibility = android.view.View.GONE
        binding.currentCode.visibility = android.view.View.GONE

        binding.unlockMessage.text = "This app is locked.\nDevice secret key not initialized.\nPlease configure the app first."
    }

    private fun unlockApp() {
        val lockedApp = PreferencesHelper.getLockedApp(blockedPackage)

        if (lockedApp != null) {
            // Temporarily unlock for 5 minutes
            val updatedApp = lockedApp.copy(
                lastUnlockTime = System.currentTimeMillis()
            )
            PreferencesHelper.updateApp(updatedApp)

            Toast.makeText(this, "Unlocked for 5 minutes", Toast.LENGTH_SHORT).show()
        }

        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        totpUpdateRunnable?.let { handler.removeCallbacks(it) }
    }

    override fun onBackPressed() {
        // Prevent back button from dismissing
        Toast.makeText(this, "App is locked", Toast.LENGTH_SHORT).show()
    }
}

