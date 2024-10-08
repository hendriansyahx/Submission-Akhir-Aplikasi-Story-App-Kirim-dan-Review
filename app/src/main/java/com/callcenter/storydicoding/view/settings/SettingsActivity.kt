package com.callcenter.storydicoding.view.settings

import android.content.SharedPreferences
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.Toolbar
import android.widget.Switch
import com.callcenter.storydicoding.R

class SettingsActivity : AppCompatActivity() {

    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var darkModeSwitch: Switch

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        sharedPreferences = getSharedPreferences("AppSettingsPrefs", 0)
        val isDarkModeOn = sharedPreferences.getBoolean("DarkMode", false)
        setDarkMode(isDarkModeOn)

        setContentView(R.layout.activity_settings)

        val toolbar: Toolbar = findViewById(R.id.settingsToolbar)
        setSupportActionBar(toolbar)

        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            title = "Settings"
        }

        toolbar.setTitleTextColor(resources.getColor(android.R.color.white, theme))
        toolbar.navigationIcon?.setTint(resources.getColor(android.R.color.white, theme))
        toolbar.setNavigationOnClickListener {
            onBackPressed()
        }

        darkModeSwitch = findViewById(R.id.darkModeSwitch)

        darkModeSwitch.isChecked = isDarkModeOn

        darkModeSwitch.setOnCheckedChangeListener { _, isChecked ->

            val editor = sharedPreferences.edit()
            editor.putBoolean("DarkMode", isChecked)
            editor.apply()

            setDarkMode(isChecked)
        }
    }

    private fun setDarkMode(isDarkModeOn: Boolean) {
        if (isDarkModeOn) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }
    }
}
