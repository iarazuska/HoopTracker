package com.example.hooptracker.ui.player

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.hooptracker.R
import com.example.hooptracker.data.local.entity.Player
/**
 * Adaptador que muestra jugadores en lista normal o agrupados por equipo con secciones desplegables.
 */

class PlayerAdapter(
    private val onClick: (Player) -> Unit,
    private val onLongClick: (Player) -> Unit
) : ListAdapter<Player, PlayerAdapter.PlayerViewHolder>(DiffCallback) {

    private var originalPlayers: List<Player> = emptyList()
    private var isGrouped: Boolean = false
    private val expandedTeams = mutableSetOf<Long>()

    object DiffCallback : DiffUtil.ItemCallback<Player>() {
        override fun areItemsTheSame(oldItem: Player, newItem: Player): Boolean =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: Player, newItem: Player): Boolean =
            oldItem == newItem
    }

    fun submitFlatList(players: List<Player>) {
        isGrouped = false
        expandedTeams.clear()
        originalPlayers = players
        submitList(players)
    }

    fun submitGroupedList(players: List<Player>) {
        isGrouped = true
        originalPlayers = players
        expandedTeams.clear()
        rebuildGroupedList()
    }

    private fun rebuildGroupedList() {
        if (!isGrouped) {
            submitList(originalPlayers)
            return
        }

        val result = mutableListOf<Player>()
        val grouped = originalPlayers.groupBy { it.teamId }

        for ((teamId, playersOfTeam) in grouped) {
            val header = Player(
                id = -teamId - 1L,
                name = "Equipo $teamId",
                number = -1,
                position = "__HEADER__",
                teamId = teamId
            )
            result.add(header)

            if (expandedTeams.contains(teamId)) {
                result.addAll(playersOfTeam.sortedBy { it.number })
            }
        }

        submitList(result)
    }

    private fun Player.isHeader(): Boolean = this.position == "__HEADER__"

    inner class PlayerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val headerLayout: View = itemView.findViewById(R.id.layoutHeader)
        private val txtTeamHeader: TextView = itemView.findViewById(R.id.txtTeamHeader)
        private val txtArrow: TextView = itemView.findViewById(R.id.txtArrow)

        private val playerLayout: View = itemView.findViewById(R.id.layoutPlayer)
        private val txtName: TextView = itemView.findViewById(R.id.txtPlayerName)
        private val txtInfo: TextView = itemView.findViewById(R.id.txtPlayerInfo)

        fun bind(player: Player) {
            if (player.isHeader()) {
                headerLayout.visibility = View.VISIBLE
                playerLayout.visibility = View.GONE

                txtTeamHeader.text = player.name
                txtArrow.text = if (expandedTeams.contains(player.teamId)) "▲" else "▼"

                itemView.setOnClickListener {
                    toggleTeam(player.teamId)
                }
                itemView.setOnLongClickListener { false }

            } else {
                headerLayout.visibility = View.GONE
                playerLayout.visibility = View.VISIBLE

                txtName.text = player.name
                txtInfo.text = "#${player.number} · ${player.position}"

                itemView.setOnClickListener { onClick(player) }
                itemView.setOnLongClickListener {
                    onLongClick(player)
                    true
                }
            }
        }
    }

    private fun toggleTeam(teamId: Long) {
        if (expandedTeams.contains(teamId)) expandedTeams.remove(teamId)
        else expandedTeams.add(teamId)
        rebuildGroupedList()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlayerViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_player, parent, false)
        return PlayerViewHolder(view)
    }

    override fun onBindViewHolder(holder: PlayerViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}
