package com.vaari.app.ui.settings

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import androidx.fragment.app.Fragment
import com.vaari.app.R
import com.vaari.app.ui.MainActivity
import com.vaari.app.utils.LocaleHelper

class LanguageFragment : Fragment() {

    private val names = arrayOf(
        "English","हिंदी","বাংলা","తెలుగు","मराठी",
        "தமிழ்","ગુજરાતી","ಕನ್ನಡ","മലയാളം","ਪੰਜਾਬੀ","ଓଡ଼ିଆ"
    )
    private val codes = arrayOf(
        "en","hi","bn","te","mr","ta","gu","kn","ml","pa","or"
    )

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.fragment_language, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val spinner = view.findViewById<Spinner>(R.id.spinnerLanguage)
        val btnConfirm = view.findViewById<Button>(R.id.btnConfirmLanguage)

        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, names)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter

        // Pre-select current language
        val current = LocaleHelper.getCurrentLanguage(requireContext())
        val index = codes.indexOf(current).coerceAtLeast(0)
        spinner.setSelection(index, false)

        btnConfirm.setOnClickListener {
            val selectedCode = codes[spinner.selectedItemPosition]
            LocaleHelper.saveLanguage(requireContext(), selectedCode)
            // Full activity restart to re-run attachBaseContext
            val intent = requireActivity().intent
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
            requireActivity().finish()
            startActivity(intent)
        }
    }
}