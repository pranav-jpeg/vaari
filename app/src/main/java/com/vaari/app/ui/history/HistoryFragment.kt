package com.vaari.app.ui.history

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.vaari.app.R
import com.vaari.app.api.RetrofitClient
import com.vaari.app.model.AppDatabase
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class HistoryFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_history, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadHistory(view)

        view.findViewById<Button>(R.id.btnClearHistory)?.setOnClickListener {
            lifecycleScope.launch {
                val db = AppDatabase.getDatabase(requireContext())
                db.historyDao().clearAll()
                loadHistory(view)
            }
        }
    }

    private fun loadHistory(view: View) {
        val container = view.findViewById<LinearLayout>(R.id.historyContainer)
        val tvEmpty = view.findViewById<TextView>(R.id.tvEmpty)

        lifecycleScope.launch {
            val db = AppDatabase.getDatabase(requireContext())
            val items = db.historyDao().getAll()
            container.removeAllViews()

            if (items.isEmpty()) {
                tvEmpty.visibility = View.VISIBLE
                container.visibility = View.GONE
            } else {
                tvEmpty.visibility = View.GONE
                container.visibility = View.VISIBLE

                val sdf = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())

                items.forEach { item ->
                    val row = layoutInflater.inflate(R.layout.item_history, container, false)

                    row.findViewById<TextView>(R.id.tvHistoryCrop).text = item.cropName
                    row.findViewById<TextView>(R.id.tvHistoryWater).text =
                        String.format("%.0f %s", item.totalWater, item.unit)
                    row.findViewById<TextView>(R.id.tvHistoryDate).text =
                        sdf.format(Date(item.timestamp))

                    row.setOnClickListener {
                        lifecycleScope.launch {
                            try {
                                val response = RetrofitClient.apiService.searchCrop(item.cropName)
                                if (response.isSuccessful && response.body() != null) {
                                    val crop = response.body()!!
                                    val bundle = Bundle().apply {
                                        putParcelable("cropResult", crop)
                                    }
                                    findNavController().navigate(R.id.resultFragment, bundle)
                                } else {
                                    Toast.makeText(
                                        requireContext(),
                                        getString(R.string.error_crop_not_found, item.cropName),
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            } catch (e: Exception) {
                                Toast.makeText(
                                    requireContext(),
                                    getString(
                                        R.string.error_connection,
                                        e.message ?: "Unknown error"
                                    ),
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    }

                    container.addView(row)
                }
            }
        }
    }
}