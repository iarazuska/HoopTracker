package com.example.hooptracker.ui.match

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.hooptracker.R
/**
 * Adaptador para mostrar jugadores en el partido en vivo y permitir sumar estadísticas con botones.
 */

class PlayerStatAdapter(
    private val isHomeTeam: Boolean,
    private val onAddPoints: (playerId: Long, isHomeTeam: Boolean, delta: Int) -> Unit,
    private val onAddRebound: (playerId: Long, isHomeTeam: Boolean) -> Unit,
    private val onAddAssist: (playerId: Long, isHomeTeam: Boolean) -> Unit,
    private val onAddSteal: (playerId: Long, isHomeTeam: Boolean) -> Unit,
    private val onAddTurnover: (playerId: Long, isHomeTeam: Boolean) -> Unit,
    private val onAddFoul: (playerId: Long, isHomeTeam: Boolean) -> Unit
) : ListAdapter<PlayerWithStats, PlayerStatAdapter.ViewHolder>(DiffCallback) {

    private var buttonsEnabled: Boolean = true
    private val expandedPlayerIds = mutableSetOf<Long>()

    fun setButtonsEnabled(enabled: Boolean) {
        buttonsEnabled = enabled
        notifyDataSetChanged()
    }

    object DiffCallback : DiffUtil.ItemCallback<PlayerWithStats>() {
        override fun areItemsTheSame(oldItem: PlayerWithStats, newItem: PlayerWithStats): Boolean =
            oldItem.player.id == newItem.player.id

        override fun areContentsTheSame(oldItem: PlayerWithStats, newItem: PlayerWithStats): Boolean =
            oldItem == newItem
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val txtName: TextView = view.findViewById(R.id.txtPlayerName)
        private val txtPoints: TextView = view.findViewById(R.id.txtPoints)
        private val txtRebounds: TextView = view.findViewById(R.id.txtRebounds)
        private val txtAssists: TextView = view.findViewById(R.id.txtAssists)
        private val txtSteals: TextView = view.findViewById(R.id.txtSteals)
        private val txtTurnovers: TextView = view.findViewById(R.id.txtTurnovers)
        private val txtFouls: TextView = view.findViewById(R.id.txtFouls)

        private val btnPlus1: Button = view.findViewById(R.id.btnPlus1)
        private val btnPlus2: Button = view.findViewById(R.id.btnPlus2)
        private val btnPlus3: Button = view.findViewById(R.id.btnPlus3)
        private val btnRebound: Button = view.findViewById(R.id.btnRebound)
        private val btnAssist: Button = view.findViewById(R.id.btnAssist)
        private val btnSteal: Button = view.findViewById(R.id.btnSteal)
        private val btnTurnover: Button = view.findViewById(R.id.btnTurnover)
        private val btnFoul: Button = view.findViewById(R.id.btnFoul)

        private val layoutButtons: View = view.findViewById(R.id.layoutButtons)

        fun bind(item: PlayerWithStats, enabled: Boolean) {
            val playerId = item.player.id

            txtName.text = item.player.name
            txtPoints.text = "PTS: ${item.stats.points}"
            txtRebounds.text = "REB: ${item.stats.rebounds}"
            txtAssists.text = "AST: ${item.stats.assists}"
            txtSteals.text = "ROB: ${item.stats.steals}"
            txtTurnovers.text = "PER: ${item.stats.turnovers}"
            txtFouls.text = "FAL: ${item.stats.fouls}"

            val foulLimitReached = item.stats.fouls >= 5

            val isExpanded = expandedPlayerIds.contains(playerId)
            layoutButtons.visibility =
                if (isExpanded && !foulLimitReached) View.VISIBLE else View.GONE

            itemView.setOnClickListener {
                if (foulLimitReached) return@setOnClickListener
                if (expandedPlayerIds.contains(playerId)) {
                    expandedPlayerIds.remove(playerId)
                } else {
                    expandedPlayerIds.add(playerId)
                }
                notifyItemChanged(adapterPosition)
            }

            if (foulLimitReached) {
                val allButtons = listOf(
                    btnPlus1, btnPlus2, btnPlus3,
                    btnRebound, btnAssist, btnSteal,
                    btnTurnover, btnFoul
                )
                allButtons.forEach { it.isEnabled = false }
                return
            }

            btnPlus1.setOnClickListener {
                if (enabled) onAddPoints(playerId, isHomeTeam, 1)
            }
            btnPlus2.setOnClickListener {
                if (enabled) onAddPoints(playerId, isHomeTeam, 2)
            }
            btnPlus3.setOnClickListener {
                if (enabled) onAddPoints(playerId, isHomeTeam, 3)
            }

            btnRebound.setOnClickListener {
                if (enabled) onAddRebound(playerId, isHomeTeam)
            }
            btnAssist.setOnClickListener {
                if (enabled) onAddAssist(playerId, isHomeTeam)
            }
            btnSteal.setOnClickListener {
                if (enabled) onAddSteal(playerId, isHomeTeam)
            }
            btnTurnover.setOnClickListener {
                if (enabled) onAddTurnover(playerId, isHomeTeam)
            }

            btnFoul.setOnClickListener {
                if (enabled && item.stats.fouls < 5) {
                    onAddFoul(playerId, isHomeTeam)
                }
            }

            val allButtons = listOf(
                btnPlus1, btnPlus2, btnPlus3,
                btnRebound, btnAssist, btnSteal,
                btnTurnover, btnFoul
            )
            allButtons.forEach { it.isEnabled = enabled && item.stats.fouls < 5 }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_player_stat, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item, buttonsEnabled)
    }
}
