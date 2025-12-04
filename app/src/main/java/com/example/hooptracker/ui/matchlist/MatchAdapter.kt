package com.example.hooptracker.ui.matchlist

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.hooptracker.data.local.entity.Match
import com.example.hooptracker.data.local.entity.MatchStatus
import com.example.hooptracker.databinding.ItemMatchBinding
import java.text.SimpleDateFormat
import java.util.*
/**
 * Adaptador que muestra la lista de partidos con fecha, estado y acciones de clic y clic prolongado.
 */

class MatchAdapter(
    private var items: List<Match> = emptyList(),
    private val onClick: (Match) -> Unit,
    private val onLongClick: (Match) -> Unit
) : RecyclerView.Adapter<MatchAdapter.MatchViewHolder>() {

    inner class MatchViewHolder(val binding: ItemMatchBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MatchViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemMatchBinding.inflate(inflater, parent, false)
        return MatchViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MatchViewHolder, position: Int) {
        val item = items[position]

        holder.binding.txtTeams.text = "Partido ${item.id} · ${item.homeScore} - ${item.awayScore}"

        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        holder.binding.txtDate.text = dateFormat.format(Date(item.dateMillis))

        val statusText = when (item.status) {
            MatchStatus.NOT_STARTED -> "Pendiente"
            MatchStatus.IN_PROGRESS -> "En juego"
            MatchStatus.FINISHED -> "Finalizado"
        }
        holder.binding.txtStatus.text = statusText

        holder.itemView.setOnClickListener {
            onClick(item)
        }

        holder.itemView.setOnLongClickListener {
            onLongClick(item)
            true
        }
    }

    override fun getItemCount(): Int = items.size

    fun submitList(newList: List<Match>) {
        items = newList
        notifyDataSetChanged()
    }
}
