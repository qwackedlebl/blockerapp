package com.hackathon.blockerapp.ui

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.SearchView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.hackathon.blockerapp.R
import com.hackathon.blockerapp.databinding.ActivityMainBinding
import com.hackathon.blockerapp.models.SecretKeyEntry
import com.hackathon.blockerapp.ui.adapters.AccountabilityPartnerAdapter
import com.hackathon.blockerapp.utils.PreferencesHelper
import com.hackathon.blockerapp.utils.TotpManager
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var adapter: AccountabilityPartnerAdapter

    companion object {
        private const val TAG = "MainActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        PreferencesHelper.init(this)

        setupToolbar()
        setupDrawer()
        setupSearch()
        setupPartnersList()
        setupFab()
    }

    private fun setupFab() {
        binding.fabAddPartner.setOnClickListener {
            showAddPartnerDialog()
        }
    }

    override fun onResume() {
        super.onResume()
        refreshPartnersList()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::adapter.isInitialized) {
            adapter.cleanup()
        }
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.title = "Accountability Partners"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(android.R.drawable.ic_menu_sort_by_size)
    }

    override fun onSupportNavigateUp(): Boolean {
        binding.drawerLayout.openDrawer(GravityCompat.START)
        return true
    }

    private fun setupDrawer() {
        // Set drawer width to ~85% of screen width
        binding.leftDrawer.post {
            val displayMetrics = resources.displayMetrics
            val width = (displayMetrics.widthPixels * 0.85).toInt()
            binding.leftDrawer.layoutParams.width = width
            binding.leftDrawer.requestLayout()
        }

        binding.navAccountabilityPartners.setOnClickListener {
            binding.drawerLayout.closeDrawer(GravityCompat.START)
            // Already on this page
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
            startActivity(Intent(this, HowItWorksActivity::class.java))
            finish()
        }
    }

    private fun setupSearch() {
        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                adapter.filter(newText)
                return true
            }
        })
    }

    private fun setupPartnersList() {
        adapter = AccountabilityPartnerAdapter(
            partners = emptyList(),
            onSuperSecretClick = { partner, index ->
                showSuperSecretDialog(partner)
            },
            onDeleteClick = { partner, index ->
                showDeleteConfirmationDialog(partner)
            }
        )

        binding.partnersRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.partnersRecyclerView.adapter = adapter

        refreshPartnersList()
    }

    private fun refreshPartnersList() {
        val partners = PreferencesHelper.getSecretKeys()

        if (partners.isEmpty()) {
            binding.emptyStateText.visibility = View.VISIBLE
            binding.partnersRecyclerView.visibility = View.GONE
        } else {
            binding.emptyStateText.visibility = View.GONE
            binding.partnersRecyclerView.visibility = View.VISIBLE
            adapter.updatePartners(partners)
        }
    }

    private fun showSuperSecretDialog(partner: SecretKeyEntry) {
        showSuperSecretKey(partner)
    }

    private fun showSuperSecretKey(partner: SecretKeyEntry) {
        // Generate the super secret key using offset function
        val superSecretKey = TotpManager.generateSuperSecretKey(partner.secretKey)

        val container = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(40, 20, 40, 20)
        }

        val descriptionText = TextView(this).apply {
            text = "Share this key with ${partner.label} to permanently unlock their apps."
            textSize = 14f
            setTextColor(ContextCompat.getColor(this@MainActivity, android.R.color.darker_gray))
            setPadding(0, 0, 0, 16)
        }

        val keyText = TextView(this).apply {
            text = superSecretKey
            textSize = 20f
            setPadding(24, 24, 24, 24)
            setBackgroundColor(Color.LTGRAY)
            setTextIsSelectable(true)
            gravity = android.view.Gravity.CENTER
        }

        container.addView(descriptionText)
        container.addView(keyText)

        MaterialAlertDialogBuilder(this)
            .setTitle("Super Secret Key")
            .setView(container)
            .setPositiveButton("Copy Key") { dialog, _ ->
                val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                val clip = ClipData.newPlainText("Super Secret Key", superSecretKey)
                clipboard.setPrimaryClip(clip)
                Toast.makeText(this, "Copied to clipboard", Toast.LENGTH_SHORT).show()
                dialog.dismiss()
            }
            .setNegativeButton("Close") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun showDeleteConfirmationDialog(partner: SecretKeyEntry) {
        MaterialAlertDialogBuilder(this)
            .setTitle("Delete Partner")
            .setMessage("Are you sure you want to remove ${partner.label} as a Partner?")
            .setPositiveButton("Yes, Delete") { dialog, _ ->
                val success = PreferencesHelper.removeSecretKeyByLabel(partner.label)
                if (success) {
                    Toast.makeText(this, "Partner deleted successfully", Toast.LENGTH_SHORT).show()
                    refreshPartnersList()
                } else {
                    Toast.makeText(this, "Failed to delete partner", Toast.LENGTH_SHORT).show()
                }
                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun showAddPartnerDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_partner, null, false)
        val nameInputLayout = dialogView.findViewById<TextInputLayout>(R.id.nameInputLayout)
        val nameInput = dialogView.findViewById<TextInputEditText>(R.id.nameInput)
        val secretKeyInputLayout = dialogView.findViewById<TextInputLayout>(R.id.secretKeyInputLayout)
        val secretKeyInput = dialogView.findViewById<TextInputEditText>(R.id.secretKeyInput)

        val dialog = MaterialAlertDialogBuilder(this)
            .setTitle("Add Accountability Partner")
            .setView(dialogView)
            .setPositiveButton("Save", null)
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .create()

        dialog.window?.setDimAmount(0.7f) // Darken background
        // Prevent dialog from resizing/compressing when keyboard appears
        dialog.window?.setSoftInputMode(android.view.WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN)

        dialog.setOnShowListener {
            val saveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            val buttonPanel = saveButton.parent as? ViewGroup
            buttonPanel?.setPadding(
                buttonPanel.paddingLeft,
                0,
                buttonPanel.paddingRight,
                8
            )

            saveButton.setOnClickListener {
                val name = nameInput.text.toString().trim()
                val secretKey = secretKeyInput.text.toString().trim()

                if (validateAndSavePartner(name, secretKey, nameInputLayout, secretKeyInputLayout)) {
                    refreshPartnersList()
                    Toast.makeText(this, "Partner added successfully!", Toast.LENGTH_SHORT).show()
                    dialog.dismiss()
                }
            }
        }

        dialog.show()
    }

    private fun validateAndSavePartner(
        name: String,
        secretKey: String,
        nameInputLayout: TextInputLayout,
        secretKeyInputLayout: TextInputLayout
    ): Boolean {
        // Clear previous errors
        nameInputLayout.error = null
        secretKeyInputLayout.error = null

        // Validation 1: Check if fields are empty
        if (name.isEmpty()) {
            nameInputLayout.error = "Name is required"
            return false
        }

        if (secretKey.isEmpty()) {
            secretKeyInputLayout.error = "Secret key is required"
            return false
        }

        // Validation 2: Check for invalid characters in name
        val validNamePattern = "^[a-zA-Z0-9 .'\\-]+$".toRegex()
        if (!name.matches(validNamePattern)) {
            nameInputLayout.error = "Name contains invalid characters"
            Log.w(TAG, "Invalid name: $name")
            return false
        }

        // Validation 3: Check if secret key is valid Base32 format
        if (!secretKey.matches("""^[a-zA-Z0-9-]+$""".toRegex())) {
            secretKeyInputLayout.error = "Invalid secret key format (use A-Z and 2-7)"
            Log.w(TAG, "Invalid secret key format: $secretKey")
            return false
        }

        // Validation 4: Check minimum length for secret key
        if (secretKey.length < 16) {
            secretKeyInputLayout.error = "Secret key too short (minimum 16 characters)"
            Log.w(TAG, "Secret key too short: ${secretKey.length} characters")
            return false
        }

        // Validation 5: Check if it's not your own device key
        val deviceSecret = PreferencesHelper.getDeviceSecretKey()
        if (secretKey.equals(deviceSecret, ignoreCase = true)) {
            secretKeyInputLayout.error = "Cannot add your own device key"
            Log.w(TAG, "Attempted to add own device key")
            return false
        }

        // Validation 6: Check if this label already exists
        val existingKeys = PreferencesHelper.getSecretKeys()
        if (existingKeys.any { it.label.equals(name, ignoreCase = true) }) {
            nameInputLayout.error = "Partner with this name already exists"
            Log.w(TAG, "Duplicate partner name: $name")
            return false
        }

        // Validation 7: Check if this secret key already exists
        if (existingKeys.any { it.secretKey.equals(secretKey, ignoreCase = true) }) {
            secretKeyInputLayout.error = "This secret key is already added"
            Log.w(TAG, "Duplicate secret key")
            return false
        }

        // All validations passed - save to persistent storage
        try {
            PreferencesHelper.appendSecretKey(name, secretKey)
            Log.d(TAG, "Successfully added partner: $name")
            return true
        } catch (e: Exception) {
            Log.e(TAG, "Error saving partner", e)
            Toast.makeText(this, "Error saving partner. Please try again.", Toast.LENGTH_SHORT).show()
            return false
        }
    }
}
