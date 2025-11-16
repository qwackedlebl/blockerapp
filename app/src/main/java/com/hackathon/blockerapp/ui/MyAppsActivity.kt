package com.hackathon.blockerapp.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.hackathon.blockerapp.databinding.ActivityMyAppsBinding
import com.hackathon.blockerapp.models.LockedApp
import com.hackathon.blockerapp.ui.adapters.AppListAdapter
import com.hackathon.blockerapp.utils.PreferencesHelper

class MyAppsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMyAppsBinding
    private lateinit var adapter: AppListAdapter
    private var apps = mutableListOf<LockedApp>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMyAppsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        setupRecyclerView()
        loadApps()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "My Apps"
    }

    private fun setupRecyclerView() {
        adapter = AppListAdapter(apps = emptyList(), onLockToggle = { app, isLocked ->
            // update locally and persist
            val updated = app.copy(isLocked = isLocked)
            PreferencesHelper.updateApp(updated)
            loadApps()
        }, onTotpClick = { app ->
            // open TotpActivity for this app
            TotpActivity::class.java.let {
                val intent = android.content.Intent(this, it)
                intent.putExtra("package_name", app.packageName)
                intent.putExtra("app_name", app.appName)
                intent.putExtra("secret_key", app.secretKey)
                intent.putExtra("totp_enabled", app.isTotpEnabled)
                startActivity(intent)
            }
        })

        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = adapter
    }

    private fun loadApps() {
        apps.clear()
        apps.addAll(PreferencesHelper.getLockedApps())
        apps.sortBy { it.appName.lowercase() }
        adapter.updateApps(apps)
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}

