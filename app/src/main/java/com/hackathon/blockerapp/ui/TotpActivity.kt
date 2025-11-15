package com.hackathon.blockerapp.ui

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.hackathon.blockerapp.databinding.ActivityTotpBinding
import com.hackathon.blockerapp.utils.PreferencesHelper
import com.hackathon.blockerapp.utils.TotpManager

class TotpActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTotpBinding
    private val handler = Handler(Looper.getMainLooper())
    private var updateRunnable: Runnable? = null
    private var currentSecretKey: String? = null

    // For app-specific TOTP setup
    private var targetPackageName: String? = null
    private var targetAppName: String? = null
    private var totpEnabled: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTotpBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        loadIntentData()
        setupButtons()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "TOTP Manager"
    }

    private fun loadIntentData() {
        targetPackageName = intent.getStringExtra("package_name")
        targetAppName = intent.getStringExtra("app_name")
        currentSecretKey = intent.getStringExtra("secret_key")
        totpEnabled = intent.getBooleanExtra("totp_enabled", false)

        if (targetAppName != null) {
            binding.appTitle.text = "TOTP for: $targetAppName"
            binding.appTitle.visibility = android.view.View.VISIBLE
        }

        if (currentSecretKey != null) {
            binding.secretKeyInput.setText(currentSecretKey)
            startTotpDisplay(currentSecretKey!!)
        }

        if (totpEnabled) {
            binding.enableTotpButton.text = "Disable TOTP"
        }
    }

    private fun setupButtons() {
        binding.generateButton.setOnClickListener {
            generateNewSecret()
        }

        binding.copySecretButton.setOnClickListener {
            copySecretToClipboard()
        }

        binding.importButton.setOnClickListener {
            importSecret()
        }

        binding.enableTotpButton.setOnClickListener {
            toggleTotpForApp()
        }

        // Show enable button only if we have an app context
        if (targetPackageName != null) {
            binding.enableTotpButton.visibility = android.view.View.VISIBLE
        } else {
            binding.enableTotpButton.visibility = android.view.View.GONE
        }
    }

    private fun generateNewSecret() {
        currentSecretKey = TotpManager.generateSecretKey()
        binding.secretKeyInput.setText(currentSecretKey)

        Toast.makeText(this, "New secret key generated", Toast.LENGTH_SHORT).show()

        startTotpDisplay(currentSecretKey!!)
    }

    private fun copySecretToClipboard() {
        val secretKey = binding.secretKeyInput.text.toString()

        if (secretKey.isEmpty()) {
            Toast.makeText(this, "No secret key to copy", Toast.LENGTH_SHORT).show()
            return
        }

        val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("TOTP Secret", secretKey)
        clipboard.setPrimaryClip(clip)

        Toast.makeText(this, "Secret key copied to clipboard", Toast.LENGTH_SHORT).show()
    }

    private fun importSecret() {
        val secretKey = binding.secretKeyInput.text.toString()

        if (secretKey.isEmpty()) {
            Toast.makeText(this, "Please enter a secret key", Toast.LENGTH_SHORT).show()
            return
        }

        currentSecretKey = secretKey

        try {
            // Test if the key is valid by generating a code
            TotpManager.generateCode(secretKey)

            Toast.makeText(this, "Secret key imported successfully", Toast.LENGTH_SHORT).show()
            startTotpDisplay(secretKey)
        } catch (e: Exception) {
            Toast.makeText(this, "Invalid secret key format", Toast.LENGTH_SHORT).show()
        }
    }

    private fun toggleTotpForApp() {
        if (targetPackageName == null) {
            Toast.makeText(this, "No app selected", Toast.LENGTH_SHORT).show()
            return
        }

        val secretKey = binding.secretKeyInput.text.toString()

        if (secretKey.isEmpty() && !totpEnabled) {
            Toast.makeText(this, "Please generate or import a secret key first", Toast.LENGTH_SHORT).show()
            return
        }

        val lockedApp = PreferencesHelper.getLockedApp(targetPackageName)

        if (lockedApp == null) {
            Toast.makeText(this, "App not found", Toast.LENGTH_SHORT).show()
            return
        }

        val updatedApp = if (totpEnabled) {
            // Disable TOTP
            lockedApp.copy(
                isTotpEnabled = false,
                secretKey = null
            )
        } else {
            // Enable TOTP
            lockedApp.copy(
                isTotpEnabled = true,
                secretKey = secretKey,
                isLocked = true // Auto-lock when enabling TOTP
            )
        }

        PreferencesHelper.updateApp(updatedApp)

        totpEnabled = !totpEnabled
        binding.enableTotpButton.text = if (totpEnabled) "Disable TOTP" else "Enable TOTP"

        Toast.makeText(
            this,
            if (totpEnabled) "TOTP enabled for ${lockedApp.appName}" else "TOTP disabled",
            Toast.LENGTH_SHORT
        ).show()
    }

    private fun startTotpDisplay(secretKey: String) {
        stopTotpDisplay()

        binding.totpDisplay.visibility = android.view.View.VISIBLE

        updateRunnable = object : Runnable {
            override fun run() {
                try {
                    val code = TotpManager.generateCode(secretKey)
                    val remaining = TotpManager.getRemainingSeconds()

                    binding.currentCodeText.text = code
                    binding.timeRemainingText.text = "Valid for $remaining seconds"

                    // Update progress bar
                    binding.progressBar.max = 30
                    binding.progressBar.progress = remaining

                    handler.postDelayed(this, 1000)
                } catch (e: Exception) {
                    binding.currentCodeText.text = "ERROR"
                    binding.timeRemainingText.text = "Invalid secret key"
                }
            }
        }

        handler.post(updateRunnable!!)
    }

    private fun stopTotpDisplay() {
        updateRunnable?.let {
            handler.removeCallbacks(it)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        stopTotpDisplay()
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}

