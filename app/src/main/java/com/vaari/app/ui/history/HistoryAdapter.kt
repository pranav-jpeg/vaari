package com.vaari.app.ui.history

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.vaari.app.R
import com.vaari.app.model.HistoryItem
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class HistoryAdapter(private val items: List<HistoryItem>) :
    RecyclerView.Adapter<HistoryAdapter.ViewHolder>() {

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvCrop: TextView = view.findViewById(R.id.tvHistoryCrop)
        val tvWater: TextView = view.findViewById(R.id.tvHistoryWater)
        val tvDate: TextView = view.findViewById(R.id.tvHistoryDate)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_history, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        val sdf = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
        holder.tvCrop.text = item.cropName
        holder.tvWater.text = "${String.format("%.0f", item.totalWater)} ${item.unit}"
        holder.tvDate.text = sdf.format(Date(item.timestamp))
    }

    override fun getItemCount() = items.size
}