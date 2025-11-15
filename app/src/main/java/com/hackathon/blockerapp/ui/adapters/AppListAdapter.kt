package com.hackathon.blockerapp.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.switchmaterial.SwitchMaterial
import com.hackathon.blockerapp.R
import com.hackathon.blockerapp.models.LockedApp

class AppListAdapter(
    private var apps: List<LockedApp>,
    private val onLockToggle: (LockedApp, Boolean) -> Unit,
    private val onTotpClick: (LockedApp) -> Unit
) : RecyclerView.Adapter<AppListAdapter.AppViewHolder>(), Filterable {

    private var filteredApps: List<LockedApp> = apps

    inner class AppViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val appName: TextView = view.findViewById(R.id.appName)
        val packageName: TextView = view.findViewById(R.id.packageName)
        val lockSwitch: SwitchMaterial = view.findViewById(R.id.lockSwitch)
        val totpButton: View = view.findViewById(R.id.totpButton)
        val totpIcon: ImageView = view.findViewById(R.id.totpIcon)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AppViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_app, parent, false)
        return AppViewHolder(view)
    }

    override fun onBindViewHolder(holder: AppViewHolder, position: Int) {
        val app = filteredApps[position]

        holder.appName.text = app.appName
        holder.packageName.text = app.packageName

        // Set lock switch state
        holder.lockSwitch.setOnCheckedChangeListener(null)
        holder.lockSwitch.isChecked = app.isLocked
        holder.lockSwitch.setOnCheckedChangeListener { _, isChecked ->
            onLockToggle(app, isChecked)
        }

        // Show TOTP button only if app is locked
        holder.totpButton.visibility = if (app.isLocked) View.VISIBLE else View.GONE

        // Update TOTP icon based on state
        if (app.isTotpEnabled) {
            holder.totpIcon.setImageResource(R.drawable.ic_lock)
            holder.totpButton.alpha = 1.0f
        } else {
            holder.totpIcon.setImageResource(R.drawable.ic_lock_open)
            holder.totpButton.alpha = 0.5f
        }

        holder.totpButton.setOnClickListener {
            onTotpClick(app)
        }
    }

    override fun getItemCount(): Int = filteredApps.size

    fun updateApps(newApps: List<LockedApp>) {
        apps = newApps
        filteredApps = newApps
        notifyDataSetChanged()
    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val query = constraint?.toString()?.lowercase() ?: ""

                filteredApps = if (query.isEmpty()) {
                    apps
                } else {
                    apps.filter {
                        it.appName.lowercase().contains(query) ||
                        it.packageName.lowercase().contains(query)
                    }
                }

                return FilterResults().apply {
                    values = filteredApps
                }
            }

            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                @Suppress("UNCHECKED_CAST")
                filteredApps = results?.values as? List<LockedApp> ?: emptyList()
                notifyDataSetChanged()
            }
        }
    }
}

