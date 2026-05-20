package com.vaari.app.utils

import android.content.Context
import android.os.Build
import java.util.Locale

object LocaleHelper {
    fun setLocale(context: Context, languageCode: String): Context {
        val locale = Locale(languageCode)
        Locale.setDefault(locale)

        val config = context.resources.configuration
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            config.setLocale(locale)
        } else {
            @Suppress("DEPRECATION")
            config.locale = locale
        }

        // ✅ Force Western Arabic numerals (0-9) regardless of language
        config.setLayoutDirection(locale)
        val baseContext = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            context.createConfigurationContext(config)
        } else context

        // Override digit substitution
        Locale.setDefault(Locale.Category.FORMAT, Locale.ENGLISH)

        return baseContext
    }

    fun getCurrentLanguage(context: Context): String {
        val prefs = context.getSharedPreferences("app_settings", Context.MODE_PRIVATE)
        return prefs.getString("language", "en") ?: "en"
    }

    fun saveLanguage(context: Context, languageCode: String) {
        context.getSharedPreferences("app_settings", Context.MODE_PRIVATE)
            .edit().putString("language", languageCode).apply()
    }
}