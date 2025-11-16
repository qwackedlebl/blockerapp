package com.hackathon.blockerapp.ui.adapters

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
    private val onSuperSecretClick: (SecretKeyEntry, Int) -> Unit
) : RecyclerView.Adapter<AccountabilityPartnerAdapter.PartnerViewHolder>() {

    private var allPartners: List<SecretKeyEntry> = partners
    private var filteredPartners: List<SecretKeyEntry> = partners

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
        val btnSuperSecret: ImageButton = itemView.findViewById(R.id.btnSuperSecret)
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

        // Super Secret button
        holder.btnSuperSecret.setOnClickListener {
            onSuperSecretClick(partner, position)
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
