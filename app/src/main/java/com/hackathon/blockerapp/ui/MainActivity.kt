package com.hackathon.blockerapp.ui

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.hackathon.blockerapp.databinding.ActivityMainBinding
import com.hackathon.blockerapp.models.SecretKeyEntry
import com.hackathon.blockerapp.ui.adapters.AccountabilityPartnerAdapter
import com.hackathon.blockerapp.utils.PreferencesHelper

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var adapter: AccountabilityPartnerAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        PreferencesHelper.init(this)

        setupToolbar()
        setupDrawer()
        setupPartnersList()
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

        binding.navManagePartners.setOnClickListener {
            binding.drawerLayout.closeDrawer(GravityCompat.START)
            startActivity(Intent(this, FriendsActivity::class.java))
            finish()
        }

        binding.navHowItWorks.setOnClickListener {
            binding.drawerLayout.closeDrawer(GravityCompat.START)
            startActivity(Intent(this, HowItWorksActivity::class.java))
            finish()
        }
    }

    private fun setupPartnersList() {
        adapter = AccountabilityPartnerAdapter(
            partners = emptyList(),
            onSuperSecretClick = { partner, index ->
                showSuperSecretDialog(partner)
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
        // Create custom dialog view
        val dialogView = layoutInflater.inflate(android.R.layout.simple_list_item_1, null)
        val container = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(50, 50, 50, 50)
        }

        val titleText = TextView(this).apply {
            text = "Super Secret Key"
            textSize = 24f
            setTextColor(Color.RED)
            setPadding(0, 0, 0, 20)
        }

        val warningText = TextView(this).apply {
            text = "This lets your friend unblock the app forever. Are you sure you wish to proceed?"
            textSize = 16f
            setPadding(0, 0, 0, 30)
        }

        container.addView(titleText)
        container.addView(warningText)

        val builder = AlertDialog.Builder(this)
        builder.setView(container)
        builder.setPositiveButton("Yes") { dialog, _ ->
            dialog.dismiss()
            showCalculusProblem(partner)
        }
        builder.setNegativeButton("Cancel") { dialog, _ ->
            dialog.dismiss()
        }

        val dialog = builder.create()
        dialog.window?.setDimAmount(0.8f) // Darken background
        dialog.show()
    }

    private fun showCalculusProblem(partner: SecretKeyEntry) {
        val container = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(50, 50, 50, 50)
        }

        val problemText = TextView(this).apply {
            text = "Solve this calculus problem:\n\n∫(2x³ + 3x²) dx\n\nEnter the result (without +C):"
            textSize = 16f
            setPadding(0, 0, 0, 20)
        }

        val answerInput = EditText(this).apply {
            hint = "Your answer"
            setPadding(20, 20, 20, 20)
        }

        container.addView(problemText)
        container.addView(answerInput)

        val builder = AlertDialog.Builder(this)
        builder.setTitle("Math Challenge")
        builder.setView(container)
        builder.setPositiveButton("Submit") { dialog, _ ->
            val answer = answerInput.text.toString().trim()
            // Correct answer: (1/2)x^4 + x^3 or 0.5x^4 + x^3
            if (answer.contains("x^4") && answer.contains("x^3")) {
                dialog.dismiss()
                showSuperSecretKey(partner)
            } else {
                Toast.makeText(this, "Incorrect answer. Try again.", Toast.LENGTH_SHORT).show()
            }
        }
        builder.setNegativeButton("Cancel") { dialog, _ ->
            dialog.dismiss()
        }

        builder.create().show()
    }

    private fun showSuperSecretKey(partner: SecretKeyEntry) {
        // Reverse the secret key
        val reversedKey = partner.secretKey.reversed()

        val container = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(50, 50, 50, 50)
        }

        val titleText = TextView(this).apply {
            text = "Super Secret Key"
            textSize = 24f
            setTextColor(Color.RED)
            setPadding(0, 0, 0, 20)
        }

        val keyText = TextView(this).apply {
            text = reversedKey
            textSize = 18f
            setPadding(20, 20, 20, 20)
            setBackgroundColor(Color.LTGRAY)
            setTextIsSelectable(true)
        }

        val copyButton = Button(this).apply {
            text = "Copy Key"
            setOnClickListener {
                val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                val clip = ClipData.newPlainText("Super Secret Key", reversedKey)
                clipboard.setPrimaryClip(clip)
                Toast.makeText(this@MainActivity, "Copied to clipboard", Toast.LENGTH_SHORT).show()
            }
        }

        container.addView(titleText)
        container.addView(keyText)
        container.addView(copyButton)

        val builder = AlertDialog.Builder(this)
        builder.setView(container)
        builder.setPositiveButton("Close") { dialog, _ ->
            dialog.dismiss()
        }

        builder.create().show()
    }
}
