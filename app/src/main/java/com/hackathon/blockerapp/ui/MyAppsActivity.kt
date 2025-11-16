package com.hackathon.blockerapp.ui

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import android.widget.SearchView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.hackathon.blockerapp.R
import com.hackathon.blockerapp.databinding.ActivityMyAppsBinding
import com.hackathon.blockerapp.models.LockedApp
import com.hackathon.blockerapp.ui.adapters.AppListAdapter
import com.hackathon.blockerapp.utils.PermissionHelper
import com.hackathon.blockerapp.utils.PreferencesHelper
import com.hackathon.blockerapp.utils.TotpManager

class MyAppsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMyAppsBinding
    private lateinit var adapter: AppListAdapter
    private var allApps = mutableListOf<LockedApp>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMyAppsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        setupDrawer()
        setupRecyclerView()
        setupSearch()
        loadInstalledApps()
    }

    override fun onResume() {
        super.onResume()
        checkPermissions()
        updatePermissionStatus()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.title = "My Apps"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(android.R.drawable.ic_menu_sort_by_size)
    }

    override fun onSupportNavigateUp(): Boolean {
        binding.drawerLayout.openDrawer(GravityCompat.START)
        return true
    }

    private fun setupDrawer() {
        // Set drawer width
        binding.leftDrawer.post {
            val displayMetrics = resources.displayMetrics
            val width = (displayMetrics.widthPixels * 0.85).toInt()
            binding.leftDrawer.layoutParams.width = width
            binding.leftDrawer.requestLayout()
        }

        binding.navAccountabilityPartners.setOnClickListener {
            binding.drawerLayout.closeDrawer(GravityCompat.START)
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }

        binding.navMyApps.setOnClickListener {
            binding.drawerLayout.closeDrawer(GravityCompat.START)
            // Already on this page
        }

        binding.navDeviceSecret.setOnClickListener {
            binding.drawerLayout.closeDrawer(GravityCompat.START)
            startActivity(Intent(this, DeviceSecretActivity::class.java))
            finish()
        }

        binding.navAddPartner.setOnClickListener {
            binding.drawerLayout.closeDrawer(GravityCompat.START)
            startActivity(Intent(this, AddPartnerActivity::class.java))
            finish()
        }

        binding.navHowItWorks.setOnClickListener {
            binding.drawerLayout.closeDrawer(GravityCompat.START)
            startActivity(Intent(this, HowItWorksActivity::class.java))
            finish()
        }
    }

    private fun setupRecyclerView() {
        adapter = AppListAdapter(
            apps = emptyList(),
            onLockToggle = { app, isLocked ->
                handleLockToggle(app, isLocked)
            },
            onTotpClick = { app ->
                handleTotpClick(app)
            }
        )

        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = adapter
    }

    private fun setupSearch() {
        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                adapter.filter.filter(newText)
                return true
            }
        })
    }

    private fun checkPermissions() {
        val missingPermissions = mutableListOf<String>()

        if (!PermissionHelper.hasUsageAccessPermission(this)) {
            missingPermissions.add("Usage Access")
        }
        if (!PermissionHelper.hasOverlayPermission(this)) {
            missingPermissions.add("Overlay Permission")
        }
        if (!PermissionHelper.isAccessibilityServiceEnabled(this)) {
            missingPermissions.add("Accessibility Service")
        }

        if (missingPermissions.isNotEmpty()) {
            showPermissionDialog(missingPermissions)
        }
    }

    private fun updatePermissionStatus() {
        val hasOverlay = PermissionHelper.hasOverlayPermission(this)
        val hasAccessibility = PermissionHelper.isAccessibilityServiceEnabled(this)
        val hasUsageAccess = PermissionHelper.hasUsageAccessPermission(this)

        binding.permissionStatus.text = buildString {
            append("Status: ")
            if (hasOverlay && hasAccessibility && hasUsageAccess) {
                append("✓ All permissions granted")
            } else {
                append("⚠ Missing permissions")
                if (!hasUsageAccess) append("\n• Usage Access")
                if (!hasOverlay) append("\n• Overlay permission")
                if (!hasAccessibility) append("\n• Accessibility service")
            }
        }
    }

    private fun showPermissionDialog(missingPermissions: List<String>) {
        MaterialAlertDialogBuilder(this)
            .setTitle("Permissions Required (${missingPermissions.size})")
            .setMessage(
                "BlockerApp needs the following permissions:\n\n" +
                        missingPermissions.joinToString("\n• ", "• ") +
                        "\n\nYou will be guided through each permission."
            )
            .setPositiveButton("Start Setup") { _, _ ->
                requestNextPermission(missingPermissions)
            }
            .setNegativeButton("Later", null)
            .setCancelable(false)
            .show()
    }

    private fun requestNextPermission(missingPermissions: List<String>) {
        when (missingPermissions.firstOrNull()) {
            "Usage Access" -> PermissionHelper.requestUsageAccessPermission(this)
            "Overlay Permission" -> PermissionHelper.requestOverlayPermission(this)
            "Accessibility Service" -> PermissionHelper.requestAccessibilityPermission(this)
        }
    }

    private fun loadInstalledApps() {
        binding.progressBar.visibility = View.VISIBLE

        Thread {
            val packageManager = packageManager
            val installedApps = packageManager.getInstalledApplications(PackageManager.GET_META_DATA)
            val savedApps = PreferencesHelper.getLockedApps().associateBy { it.packageName }

            allApps.clear()

            for (appInfo in installedApps) {
                if (appInfo.packageName == packageName) continue

                if(appInfo.packageName != "com.google.android.youtube" &&
                    appInfo.packageName != "com.instagram.android" &&
                    appInfo.packageName != "com.snapchat.android" &&
                    appInfo.packageName != "com.musically.android"
                ) {
                    continue
                }

                val appName = packageManager.getApplicationLabel(appInfo).toString()
                val packageName = appInfo.packageName

                val lockedApp = savedApps[packageName] ?: LockedApp(
                    packageName = packageName,
                    appName = appName
                )

                allApps.add(lockedApp)
            }

            allApps.sortBy { it.appName.lowercase() }

            runOnUiThread {
                adapter.updateApps(allApps)
                binding.progressBar.visibility = View.GONE
            }
        }.start()
    }

    private fun handleLockToggle(app: LockedApp, isLocked: Boolean) {
        if (isLocked && !app.isLocked) {
            // Confirm before locking an app
            MaterialAlertDialogBuilder(this)
                .setTitle("Lock this app?")
                .setMessage("You will need to unlock it to use it. Do you want to continue?")
                .setPositiveButton("Lock") { _, _ ->
                    // Ensure a secret exists so future unlock requires code
                    val newSecret = if (app.secretKey.isNullOrEmpty()) TotpManager.generateSecretKey() else app.secretKey
                    val updatedApp = app.copy(isLocked = true, secretKey = newSecret)
                    updateApp(updatedApp)
                }
                .setNegativeButton("Cancel", null)
                .show()
        } else if (!isLocked && app.isLocked) {
            // Unlock requires confirmation code ALWAYS
            showUnlockDialog(app)
        } else {
            val updatedApp = app.copy(isLocked = isLocked)
            updateApp(updatedApp)
        }
    }

    private fun showUnlockDialog(app: LockedApp) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_unlock_confirm, null)
        val codeInput = dialogView.findViewById<EditText>(R.id.codeInput)
        val errorText = dialogView.findViewById<TextView>(R.id.errorText)
        val originalColor = codeInput.currentTextColor

        codeInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                errorText.visibility = View.GONE
                codeInput.setTextColor(originalColor)
                codeInput.alpha = 1f
                codeInput.translationX = 0f
            }
        })

        val dialog = MaterialAlertDialogBuilder(this)
            .setView(dialogView)
            .setCancelable(false)
            .setPositiveButton("Unlock", null)
            .setNegativeButton("Cancel", null)
            .create()

        dialog.setOnShowListener {
            val positive = dialog.getButton(android.app.AlertDialog.BUTTON_POSITIVE)
            positive.setOnClickListener {
                val entered = codeInput.text.toString().trim()
                val allDigits = entered.length == 6 && entered.all { it.isDigit() }
                if (!allDigits) {
                    showFailureAnimation(codeInput, errorText, "Enter 6 digits")
                    return@setOnClickListener
                }
                val secret = app.secretKey
                if (secret.isNullOrEmpty()) {
                    showFailureAnimation(codeInput, errorText, "TOTP not configured")
                    return@setOnClickListener
                }
                val reversed = entered.reversed()
                val valid = TotpManager.verifyCode(secret, reversed)
                if (valid) {
                    dialog.dismiss()
                    updateApp(app.copy(isLocked = false))
                } else {
                    showFailureAnimation(codeInput, errorText, "Invalid code")
                }
            }
        }

        dialog.show()
    }

    private fun showFailureAnimation(codeInput: EditText, errorText: TextView, message: String) {
        errorText.text = message
        errorText.visibility = View.VISIBLE
        codeInput.setTextColor(ContextCompat.getColor(this, android.R.color.holo_red_dark))

        // Shake animation with sequence of translations
        val shake = AnimatorSet()
        shake.playSequentially(
            ObjectAnimator.ofFloat(codeInput, "translationX", 0f, -20f),
            ObjectAnimator.ofFloat(codeInput, "translationX", -20f, 20f),
            ObjectAnimator.ofFloat(codeInput, "translationX", 20f, -15f),
            ObjectAnimator.ofFloat(codeInput, "translationX", -15f, 15f),
            ObjectAnimator.ofFloat(codeInput, "translationX", 15f, 0f)
        )

        // Fade animation
        val fade = ObjectAnimator.ofFloat(codeInput, "alpha", 1f, 0.3f, 1f)
        fade.duration = 200

        val set = AnimatorSet()
        set.playTogether(shake, fade)
        set.duration = 200
        set.start()
    }

    private fun handleTotpClick(app: LockedApp) {
        val intent = Intent(this, TotpActivity::class.java).apply {
            putExtra("package_name", app.packageName)
            putExtra("app_name", app.appName)
            putExtra("secret_key", app.secretKey)
            putExtra("totp_enabled", app.isTotpEnabled)
        }
        startActivity(intent)
    }

    private fun updateApp(updatedApp: LockedApp) {
        val index = allApps.indexOfFirst { it.packageName == updatedApp.packageName }
        if (index != -1) {
            allApps[index] = updatedApp
        }

        val savedApps = PreferencesHelper.getLockedApps().toMutableList()
        val savedIndex = savedApps.indexOfFirst { it.packageName == updatedApp.packageName }

        if (savedIndex != -1) {
            savedApps[savedIndex] = updatedApp
        } else {
            savedApps.add(updatedApp)
        }

        PreferencesHelper.saveLockedApps(savedApps)

        adapter.updateApps(allApps)
    }
}
