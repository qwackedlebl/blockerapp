package com.hackathon.blockerapp.ui

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import com.hackathon.blockerapp.databinding.ActivityDeviceSecretBinding
import com.hackathon.blockerapp.utils.PreferencesHelper

class DeviceSecretActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDeviceSecretBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDeviceSecretBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        setupDrawer()
        setupDeviceSecretDisplay()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(android.R.drawable.ic_menu_sort_by_size)
        supportActionBar?.title = "My Secret Key"
    }

    override fun onSupportNavigateUp(): Boolean {
        binding.drawerLayout.openDrawer(GravityCompat.START)
        return true
    }

    private fun setupDrawer() {
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
            startActivity(Intent(this, MyAppsActivity::class.java))
            finish()
        }

        binding.navDeviceSecret.setOnClickListener {
            binding.drawerLayout.closeDrawer(GravityCompat.START)
            // Already on this page
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

    private fun setupDeviceSecretDisplay() {
        val deviceSecret = PreferencesHelper.getDeviceSecretKey()

        if (deviceSecret != null) {
            binding.deviceSecretKeyText.setText(deviceSecret)
        } else {
            binding.deviceSecretKeyText.setText("Not initialized")
        }

        binding.btnCopySecret.setOnClickListener {
            deviceSecret?.let { secret ->
                val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                val clip = ClipData.newPlainText("My Secret Key", secret)
                clipboard.setPrimaryClip(clip)
                Toast.makeText(this, "Secret key copied to clipboard", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
