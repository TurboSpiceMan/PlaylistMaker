package com.example.playlistmaker

import android.app.Application
import android.content.res.Configuration
import androidx.appcompat.app.AppCompatDelegate

const val APP_PREFERENCES = "AppPrefs"
const val SWITCHER_KEY = "SwitchKey"

class App : Application() {
    var darkTheme = false
        private set

    override fun onCreate() {
        super.onCreate()

        //set app theme as device theme
        val defaultTheme = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        val sharedPrefs = getSharedPreferences(APP_PREFERENCES, MODE_PRIVATE)
        darkTheme = sharedPrefs.getBoolean(
            SWITCHER_KEY,
            defaultTheme == Configuration.UI_MODE_NIGHT_YES
        )
        switchTheme(darkTheme)
    }

    fun switchTheme(darkThemeEnabled: Boolean) {
        darkTheme = darkThemeEnabled
        AppCompatDelegate.setDefaultNightMode(
            if (darkThemeEnabled) {
                AppCompatDelegate.MODE_NIGHT_YES
            } else {
                AppCompatDelegate.MODE_NIGHT_NO

            }
        )
    }
}