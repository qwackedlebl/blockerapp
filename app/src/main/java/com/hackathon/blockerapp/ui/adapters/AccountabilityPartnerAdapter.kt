package com.hackathon.blockerapp.ui.adapters

import android.animation.ObjectAnimator
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.hackathon.blockerapp.R
import com.hackathon.blockerapp.models.SecretKeyEntry
import com.hackathon.blockerapp.utils.TotpManager

class AccountabilityPartnerAdapter(
    private var partners: List<SecretKeyEntry>,
    private val onSuperSecretClick: (SecretKeyEntry, Int) -> Unit,
    private val onDeleteClick: (SecretKeyEntry, Int) -> Unit
) : RecyclerView.Adapter<AccountabilityPartnerAdapter.PartnerViewHolder>() {

    private var allPartners: List<SecretKeyEntry> = partners
    private var filteredPartners: List<SecretKeyEntry> = partners
    private val settingsOpenStates = mutableMapOf<String, Boolean>()

    private val handler = Handler(Looper.getMainLooper())
    private val updateRunnable = object : Runnable {
        override fun run() {
            notifyDataSetChanged()
            handler.postDelayed(this, 1000) // Update every second
        }
    }

    init {
        handler.post(updateRunnable)
    }

    class PartnerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val partnerName: TextView = itemView.findViewById(R.id.partnerName)
        val totpCode: TextView = itemView.findViewById(R.id.totpCode)
        val timerText: TextView = itemView.findViewById(R.id.timerText)
        val timerProgress: ProgressBar = itemView.findViewById(R.id.timerProgress)
        val btnSettings: View = itemView.findViewById(R.id.btnSettings)
        val mainContent: View = itemView.findViewById(R.id.mainContent)
        val settingsOverlay: View = itemView.findViewById(R.id.settingsOverlay)
        val btnSettingsOverlay: View = itemView.findViewById(R.id.btnSettingsOverlay)
        val btnSuperSecretOverlay: View = itemView.findViewById(R.id.btnSuperSecretOverlay)
        val btnDeletePartner: View = itemView.findViewById(R.id.btnDeletePartner)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PartnerViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_accountability_partner, parent, false)
        return PartnerViewHolder(view)
    }

    override fun onBindViewHolder(holder: PartnerViewHolder, position: Int) {
        val partner = filteredPartners[position]

        holder.partnerName.text = partner.label

        // Generate TOTP code
        val code = TotpManager.generateCode(partner.secretKey)
        holder.totpCode.text = code

        // Get remaining seconds
        val remaining = TotpManager.getRemainingSeconds()
        holder.timerText.text = "${remaining}s"
        holder.timerProgress.max = 30
        holder.timerProgress.progress = remaining

        // Restore settings overlay state
        val isSettingsOpen = settingsOpenStates[partner.label] ?: false
        if (isSettingsOpen) {
            holder.settingsOverlay.visibility = View.VISIBLE
        } else {
            holder.settingsOverlay.visibility = View.GONE
        }

        // Settings button - toggle overlay with fade animation (both buttons do the same thing)
        val toggleSettings = {
            val isCurrentlyOpen = holder.settingsOverlay.visibility == View.VISIBLE
            settingsOpenStates[partner.label] = !isCurrentlyOpen

            if (!isCurrentlyOpen) {
                // Opening: fade in only
                holder.settingsOverlay.visibility = View.VISIBLE
                holder.settingsOverlay.alpha = 0f

                holder.settingsOverlay.animate()
                    .alpha(1f)
                    .setDuration(200)
                    .start()
            } else {
                // Closing: fade out only
                holder.settingsOverlay.animate()
                    .alpha(0f)
                    .setDuration(200)
                    .withEndAction {
                        holder.settingsOverlay.visibility = View.GONE
                    }
                    .start()
            }
        }

        holder.btnSettings.setOnClickListener { toggleSettings() }
        holder.btnSettingsOverlay.setOnClickListener { toggleSettings() }

        // Super Secret button in overlay
        holder.btnSuperSecretOverlay.setOnClickListener {
            onSuperSecretClick(partner, position)
        }

        // Delete partner button
        holder.btnDeletePartner.setOnClickListener {
            onDeleteClick(partner, position)
        }
    }

    override fun getItemCount(): Int = filteredPartners.size

    fun updatePartners(newPartners: List<SecretKeyEntry>) {
        allPartners = newPartners
        filteredPartners = newPartners
        notifyDataSetChanged()
    }

    fun filter(query: String?) {
        filteredPartners = if (query.isNullOrEmpty()) {
            allPartners
        } else {
            allPartners.filter { partner ->
                partner.label.contains(query, ignoreCase = true)
            }
        }
        notifyDataSetChanged()
    }

    fun cleanup() {
        handler.removeCallbacks(updateRunnable)
    }
}
