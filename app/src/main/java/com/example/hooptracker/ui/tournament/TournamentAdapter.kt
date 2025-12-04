package com.example.hooptracker.ui.tournament

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.hooptracker.data.local.entity.Tournament
import com.example.hooptracker.databinding.ItemTournamentBinding
import java.text.SimpleDateFormat
import java.util.*
/**
 * Adaptador que muestra torneos con su fecha y permite clic y clic prolongado.
 */

class TournamentAdapter(
    private var items: List<Tournament> = emptyList(),
    private val onClick: (Tournament) -> Unit,
    private val onLongClick: (Tournament) -> Unit
) : RecyclerView.Adapter<TournamentAdapter.TournamentViewHolder>() {

    inner class TournamentViewHolder(val binding: ItemTournamentBinding)
        : RecyclerView.ViewHolder(binding.root)

    private val dateFormatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TournamentViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemTournamentBinding.inflate(inflater, parent, false)
        return TournamentViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TournamentViewHolder, position: Int) {
        val item = items[position]

        holder.binding.txtTournamentName.text = item.name
        holder.binding.txtTournamentDate.text =
            dateFormatter.format(Date(item.startDateMillis))

        holder.itemView.setOnClickListener {
            onClick(item)
        }

        holder.itemView.setOnLongClickListener {
            onLongClick(item)
            true
        }
    }

    override fun getItemCount(): Int = items.size

    fun submitList(list: List<Tournament>) {
        items = list
        notifyDataSetChanged()
    }
}
