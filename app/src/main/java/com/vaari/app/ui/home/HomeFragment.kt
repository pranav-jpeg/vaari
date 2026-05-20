package com.vaari.app.ui.home

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Toast
import androidx.core.content.getSystemService
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.vaari.app.R
import com.vaari.app.api.RetrofitClient
import com.vaari.app.databinding.FragmentHomeBinding
import com.vaari.app.model.AppDatabase
import com.vaari.app.model.HistoryEntity
import kotlinx.coroutines.launch

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val autoAdapter = object : ArrayAdapter<String>(
            requireContext(),
            R.layout.item_suggestion,
            mutableListOf()
        ) {
            override fun getFilter(): android.widget.Filter {
                return object : android.widget.Filter() {
                    override fun performFiltering(constraint: CharSequence?) = FilterResults()

                    override fun publishResults(
                        constraint: CharSequence?,
                        results: FilterResults?
                    ) {
                        notifyDataSetChanged()
                    }
                }
            }
        }

        (binding.etSearch as AutoCompleteTextView).setAdapter(autoAdapter)
        binding.etSearch.threshold = 1

        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                val query = s.toString().trim()
                if (query.isEmpty()) return

                lifecycleScope.launch {
                    try {
                        val response = RetrofitClient.apiService.getSuggestions(query)
                        if (response.isSuccessful) {
                            val list = response.body() ?: emptyList()
                            autoAdapter.clear()
                            autoAdapter.addAll(list)
                            autoAdapter.notifyDataSetChanged()
                        }
                    } catch (e: Exception) {
                    }
                }
            }
        })

        binding.etSearch.setOnItemClickListener { _, _, _, _ ->
            hideKeyboard()
            performSearch()
        }

        binding.btnSearch.setOnClickListener {
            performSearch()
        }

        binding.etSearch.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                performSearch()
                true
            } else {
                false
            }
        }

        arguments?.getString("scannedCrop")?.let { crop ->
            binding.etSearch.setText(crop)
            performSearch()
            arguments?.remove("scannedCrop")
        }

        binding.btnLanguage.setOnClickListener {
            findNavController().navigate(R.id.languageFragment)
        }
    }

    private fun performSearch() {
        val query = binding.etSearch.text.toString().trim()

        if (query.isEmpty()) {
            Toast.makeText(
                requireContext(),
                getString(R.string.error_enter_crop),
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        hideKeyboard()
        setLoading(true)
        binding.tvError.visibility = View.GONE

        lifecycleScope.launch {
            try {
                val response = RetrofitClient.apiService.searchCrop(query)
                setLoading(false)

                if (response.isSuccessful && response.body() != null) {
                    val crop = response.body()!!

                    val db = AppDatabase.getDatabase(requireContext())
                    db.historyDao().insert(
                        HistoryEntity(
                            cropName = crop.productName,
                            totalWater = crop.totalWater,
                            unit = crop.unit
                        )
                    )

                    val bundle = Bundle().apply {
                        putParcelable("cropResult", crop)
                    }

                    findNavController().navigate(R.id.resultFragment, bundle)
                } else {
                    showError(getString(R.string.error_crop_not_found, query))
                }
            } catch (e: Exception) {
                setLoading(false)
                showError(
                    getString(
                        R.string.error_connection,
                        e.message ?: "Unknown error"
                    )
                )
            }
        }
    }

    private fun setLoading(loading: Boolean) {
        binding.progressBar.visibility = if (loading) View.VISIBLE else View.GONE
        binding.btnSearch.isEnabled = !loading
    }

    private fun showError(msg: String) {
        binding.tvError.text = msg
        binding.tvError.visibility = View.VISIBLE
    }

    private fun hideKeyboard() {
        requireContext().getSystemService<InputMethodManager>()
            ?.hideSoftInputFromWindow(binding.etSearch.windowToken, 0)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
};