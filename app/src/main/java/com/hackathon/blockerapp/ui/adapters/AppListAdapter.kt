package com.hackathon.blockerapp.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.hackathon.blockerapp.R
import com.hackathon.blockerapp.models.LockedApp

class AppListAdapter(
    private var apps: List<LockedApp>,
    private val onLockToggle: (LockedApp, Boolean) -> Unit
) : RecyclerView.Adapter<AppListAdapter.AppViewHolder>(), Filterable {

    private var filteredApps: List<LockedApp> = apps

    inner class AppViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val appName: TextView = view.findViewById(R.id.appName)
        val packageName: TextView = view.findViewById(R.id.packageName)
        val lockButton: MaterialButton = view.findViewById(R.id.lockButton)
        val lockDescription: TextView = view.findViewById(R.id.lockDescription)
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

        // Set lock button icon state and description text
        if (app.isLocked) {
            holder.lockButton.icon = holder.itemView.context.getDrawable(R.drawable.ic_lock)
            holder.lockButton.alpha = 1.0f
            holder.lockDescription.text = "Locked"
        } else {
            holder.lockButton.icon = holder.itemView.context.getDrawable(R.drawable.ic_lock_open)
            holder.lockButton.alpha = 0.8f
            holder.lockDescription.text = "Unlocked"
        }

        holder.lockButton.setOnClickListener {
            val newState = !app.isLocked
            onLockToggle(app, newState)
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
