package com.hackathon.blockerapp.ui

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.hackathon.blockerapp.databinding.ActivityFriendsBinding
import com.hackathon.blockerapp.ui.adapters.PartnersListAdapter
import com.hackathon.blockerapp.utils.PreferencesHelper

class FriendsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityFriendsBinding
    private lateinit var adapter: PartnersListAdapter

    companion object {
        private const val TAG = "FriendsActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFriendsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(android.R.drawable.ic_menu_sort_by_size)
        supportActionBar?.title = "Manage Partners"

        setupDrawer()
        setupDeviceSecretDisplay()
        setupAddPartnerForm()
        setupPartnersList()
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

        binding.navManagePartners.setOnClickListener {
            binding.drawerLayout.closeDrawer(GravityCompat.START)
            // Already on this page
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
                val clip = ClipData.newPlainText("Device Secret Key", secret)
                clipboard.setPrimaryClip(clip)
                Toast.makeText(this, "Secret key copied to clipboard", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupAddPartnerForm() {
        binding.btnSave.setOnClickListener {
            val name = binding.nameInput.text.toString().trim()
            val secretKey = binding.secretKeyInput.text.toString().trim()

            if (validateAndSavePartner(name, secretKey)) {
                // Clear inputs on success
                binding.nameInput.text?.clear()
                binding.secretKeyInput.text?.clear()
                binding.nameInputLayout.error = null
                binding.secretKeyInputLayout.error = null

                // Refresh the list
                refreshPartnersList()

                Toast.makeText(this, "Partner added successfully!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun validateAndSavePartner(name: String, secretKey: String): Boolean {
        // Clear previous errors
        binding.nameInputLayout.error = null
        binding.secretKeyInputLayout.error = null

        // Validation 1: Check if fields are empty
        if (name.isEmpty()) {
            binding.nameInputLayout.error = "Name is required"
            return false
        }

        if (secretKey.isEmpty()) {
            binding.secretKeyInputLayout.error = "Secret key is required"
            return false
        }

        // Validation 2: Check for invalid characters in name
        // Allow only letters, numbers, spaces, and common punctuation
        val validNamePattern = "^[a-zA-Z0-9 .'\\-]+$".toRegex()
        if (!name.matches(validNamePattern)) {
            binding.nameInputLayout.error = "Name contains invalid characters"
            Log.w(TAG, "Invalid name: $name")
            return false
        }

        // Validation 3: Check if secret key is valid Base32 format
        // Base32 uses A-Z and 2-7 characters
        val validSecretPattern = "^[A-Z2-7]+$".toRegex()
        if (!secretKey.matches(validSecretPattern)) {
            binding.secretKeyInputLayout.error = "Invalid secret key format (use A-Z and 2-7)"
            Log.w(TAG, "Invalid secret key format: $secretKey")
            return false
        }

        // Validation 4: Check if it's not your own device key
        val deviceSecret = PreferencesHelper.getDeviceSecretKey()
        if (secretKey.equals(deviceSecret, ignoreCase = true)) {
            binding.secretKeyInputLayout.error = "Cannot add your own device key"
            Log.w(TAG, "Attempted to add own device key")
            return false
        }

        // Validation 5: Check if this label already exists
        val existingKeys = PreferencesHelper.getSecretKeys()
        if (existingKeys.any { it.label.equals(name, ignoreCase = true) }) {
            binding.nameInputLayout.error = "Partner with this name already exists"
            Log.w(TAG, "Duplicate partner name: $name")
            return false
        }

        // All validations passed - save to persistent storage
        try {
            PreferencesHelper.appendSecretKey(name, secretKey)
            Log.d(TAG, "Successfully added partner: $name")
            return true
        } catch (e: Exception) {
            // Unspecified error - log and show generic message
            Log.e(TAG, "Error saving partner", e)
            Toast.makeText(this, "Error saving partner. Please try again.", Toast.LENGTH_SHORT).show()
            return false
        }
    }

    private fun setupPartnersList() {
        adapter = PartnersListAdapter(
            partners = emptyList(),
            onDeleteClick = { index ->
                deletePartner(index)
            }
        )

        binding.partnersRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.partnersRecyclerView.adapter = adapter

        refreshPartnersList()
    }

    private fun refreshPartnersList() {
        val partners = PreferencesHelper.getSecretKeys()

        if (partners.isEmpty()) {
            binding.emptyStateText.visibility = android.view.View.VISIBLE
            binding.partnersRecyclerView.visibility = android.view.View.GONE
        } else {
            binding.emptyStateText.visibility = android.view.View.GONE
            binding.partnersRecyclerView.visibility = android.view.View.VISIBLE
            adapter.updatePartners(partners)
        }
    }

    private fun deletePartner(index: Int) {
        val partners = PreferencesHelper.getSecretKeys()
        if (index >= 0 && index < partners.size) {
            val partnerName = partners[index].label

            if (PreferencesHelper.removeSecretKeyAt(index)) {
                Toast.makeText(this, "Removed $partnerName", Toast.LENGTH_SHORT).show()
                refreshPartnersList()
            } else {
                Toast.makeText(this, "Error removing partner", Toast.LENGTH_SHORT).show()
            }
        }
    }

}

