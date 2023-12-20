package com.example.playlistmaker

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView

class TracksAdapter(private val tracks: MutableList<Track>) :
    RecyclerView.Adapter<TracksViewHolder>() {
    var listener: OnItemClickListener? = null


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TracksViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.track_view, parent, false)
        return TracksViewHolder(view)
    }

    override fun onBindViewHolder(holder: TracksViewHolder, position: Int) {
        val item = tracks[position]
        holder.bind(item)

        holder.itemView.setOnClickListener {
            listener?.onItemClick(item)
        }

    }

    override fun getItemCount(): Int {
        return tracks.size
    }
}

fun interface OnItemClickListener {
    fun onItemClick(item: Track)
}