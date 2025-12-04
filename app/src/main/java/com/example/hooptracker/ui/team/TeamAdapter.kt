package com.example.hooptracker.ui.team

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.hooptracker.R
import com.example.hooptracker.data.local.entity.Team
/**
 * Adaptador que muestra equipos y permite clic y clic prolongado para editar o eliminar.
 */

class TeamAdapter(
    private val onClick: (Team) -> Unit,
    private val onLongClick: (Team) -> Unit   // ⬅️ NUEVO
) : ListAdapter<Team, TeamAdapter.TeamViewHolder>(DiffCallback) {

    object DiffCallback : DiffUtil.ItemCallback<Team>() {
        override fun areItemsTheSame(oldItem: Team, newItem: Team): Boolean =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: Team, newItem: Team): Boolean =
            oldItem == newItem
    }

    inner class TeamViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val txtName: TextView = itemView.findViewById(R.id.txtTeamName)

        fun bind(team: Team) {
            txtName.text = team.name

            itemView.setOnClickListener { onClick(team) }

            // 👇 LONG PRESS
            itemView.setOnLongClickListener {
                onLongClick(team)
                true
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TeamViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_team, parent, false)
        return TeamViewHolder(view)
    }

    override fun onBindViewHolder(holder: TeamViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}
