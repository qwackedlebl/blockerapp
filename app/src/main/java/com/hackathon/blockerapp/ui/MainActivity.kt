package com.hackathon.blockerapp.ui

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.accessibility.AccessibilityManager
import android.widget.SearchView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.hackathon.blockerapp.R
import com.hackathon.blockerapp.databinding.ActivityMainBinding
import com.hackathon.blockerapp.models.LockedApp
import com.hackathon.blockerapp.ui.adapters.AppListAdapter
import com.hackathon.blockerapp.utils.PermissionHelper
import com.hackathon.blockerapp.utils.PreferencesHelper

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var adapter: AppListAdapter
    private var allApps = mutableListOf<LockedApp>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        PreferencesHelper.init(this)
        verifyAccessibilityFramework()

        setupToolbar()
        setupRecyclerView()
        loadInstalledApps()

        binding.fabTotp.setOnClickListener {
            startActivity(Intent(this, TotpActivity::class.java))
        }
    }

    private fun verifyAccessibilityFramework() {
        Log.d("AccessibilityVerification", "--- QUERYING PACKAGE MANAGER FOR ACCESSIBILITY SERVICES ---");
        val packageManager = packageManager
        val intent = Intent("android.accessibility.AccessibilityService")
        val serviceInfos = packageManager.queryIntentServices(intent, PackageManager.MATCH_ALL)
        if (serviceInfos.isEmpty()) {
            Log.e("AccessibilityVerification", "CRITICAL: No services on the device can handle the AccessibilityService intent.")
        } else {
            Log.d("AccessibilityVerification", "Found ${serviceInfos.size} services capable of being an Accessibility Service:")
            for (info in serviceInfos) {
                Log.d("AccessibilityVerification", "  - Service: ${info.serviceInfo.name}, Package: ${info.serviceInfo.packageName}")
            }
        }
        Log.d("AccessibilityVerification", "---------------------------------------------------------");
    }

    override fun onResume() {
        super.onResume()
        checkPermissions()
        updatePermissionStatus()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.title = "BlockerApp"
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

        Log.d("MainActivity", "Overlay permission: $hasOverlay")
        Log.d("MainActivity", "Accessibility service enabled: $hasAccessibility")
        Log.d("MainActivity", "Usage access permission: $hasUsageAccess")

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
        binding.progressBar.visibility = android.view.View.VISIBLE

        Thread {
            val packageManager = packageManager
            val installedApps = packageManager.getInstalledApplications(PackageManager.GET_META_DATA)
            val savedApps = PreferencesHelper.getLockedApps().associateBy { it.packageName }

            allApps.clear()

            for (appInfo in installedApps) {
                if (appInfo.packageName == packageName) continue

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
                binding.progressBar.visibility = android.view.View.GONE
            }
        }.start()
    }

    private fun handleLockToggle(app: LockedApp, isLocked: Boolean) {
        val updatedApp = app.copy(isLocked = isLocked)
        updateApp(updatedApp)
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

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)

        val searchItem = menu.findItem(R.id.action_search)
        val searchView = searchItem.actionView as SearchView

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                adapter.filter.filter(newText)
                return true
            }
        })

        return true
    }
}
