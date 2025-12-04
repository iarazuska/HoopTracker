package com.example.hooptracker.ui.match

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.hooptracker.R
/**
 * Adaptador para mostrar las estadísticas resumidas de cada jugador en el resumen del partido.
 */

class PlayerSummaryAdapter :
    ListAdapter<PlayerWithStats, PlayerSummaryAdapter.ViewHolder>(DiffCallback) {

    object DiffCallback : DiffUtil.ItemCallback<PlayerWithStats>() {
        override fun areItemsTheSame(oldItem: PlayerWithStats, newItem: PlayerWithStats): Boolean =
            oldItem.player.id == newItem.player.id

        override fun areContentsTheSame(oldItem: PlayerWithStats, newItem: PlayerWithStats): Boolean =
            oldItem == newItem
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val txtName: TextView = view.findViewById(R.id.txtPlayerName)
        private val txtStatsLine: TextView = view.findViewById(R.id.txtStatsLine)

        fun bind(item: PlayerWithStats) {
            val s = item.stats
            txtName.text = item.player.name
            txtStatsLine.text =
                "PTS ${s.points}   REB ${s.rebounds}   AST ${s.assists}   ROB ${s.steals}   PER ${s.turnovers}"
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_player_summary, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}
