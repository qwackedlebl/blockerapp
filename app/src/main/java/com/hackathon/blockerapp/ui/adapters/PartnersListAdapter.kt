package com.hackathon.blockerapp.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.hackathon.blockerapp.R
import com.hackathon.blockerapp.models.SecretKeyEntry

class PartnersListAdapter(
    private var partners: List<SecretKeyEntry>,
    private val onDeleteClick: (Int) -> Unit
) : RecyclerView.Adapter<PartnersListAdapter.PartnerViewHolder>() {

    class PartnerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val nameText: TextView = itemView.findViewById(R.id.partnerName)
        val secretText: TextView = itemView.findViewById(R.id.partnerSecret)
        val deleteButton: ImageButton = itemView.findViewById(R.id.btnDelete)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PartnerViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_partner, parent, false)
        return PartnerViewHolder(view)
    }

    override fun onBindViewHolder(holder: PartnerViewHolder, position: Int) {
        val partner = partners[position]

        holder.nameText.text = partner.label
        holder.secretText.text = partner.secretKey

        holder.deleteButton.setOnClickListener {
            onDeleteClick(position)
        }
    }

    override fun getItemCount(): Int = partners.size

    fun updatePartners(newPartners: List<SecretKeyEntry>) {
        partners = newPartners
        notifyDataSetChanged()
    }
}
