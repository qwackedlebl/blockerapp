package com.hackathon.blockerapp.ui

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import com.hackathon.blockerapp.databinding.ActivityHowItWorksBinding

class HowItWorksActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHowItWorksBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHowItWorksBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        setupDrawer()
        setupContent()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(android.R.drawable.ic_menu_sort_by_size)
        supportActionBar?.title = "How It Works"
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
            startActivity(Intent(this, MyAppsActivity::class.java))
            finish()
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
            // Already on this page
        }
    }

    private fun setupContent() {
        binding.contentText.text = """
            BlockerApp - Accountability Partner System

            HOW IT WORKS:

            1. DEVICE SECRET KEY
            Each device has a unique secret key generated on first launch. This key is used to generate time-based codes (TOTP) that change every 30 seconds.

            2. ACCOUNTABILITY PARTNERS
            Share your device secret key with a friend who will be your accountability partner. They can see your key on their "Manage Partners" page.

            3. LOCKING APPS
            Go to "My Apps" and toggle the lock switch for any app you want to block (YouTube, Instagram, Snapchat, TikTok, etc.).

            4. UNLOCKING LOCKED APPS
            When you try to open a locked app:
            - A fullscreen blocker appears
            - You need to enter the 6-digit code from your accountability partner
            - They generate this code using your device secret key
            - The code changes every 30 seconds
            - Or click "Unlock App" for a temporary 5-minute unlock

            5. ACCOUNTABILITY PARTNERS PAGE
            The main page shows all your partners with their current TOTP codes. Use these codes to unlock apps on their devices.

            6. SUPER SECRET KEY (Advanced)
            Each partner has a secret button that reveals a "super secret key" - this permanently unlocks apps but requires solving a calculus problem first!

            PERMISSIONS NEEDED:
            - Accessibility Service: Detects when locked apps are opened
            - Overlay Permission: Shows the fullscreen blocker
            - Usage Access: Tracks which apps are being used

            This system creates accountability - you can't unlock apps without your partner's help!
        """.trimIndent()
    }
}
