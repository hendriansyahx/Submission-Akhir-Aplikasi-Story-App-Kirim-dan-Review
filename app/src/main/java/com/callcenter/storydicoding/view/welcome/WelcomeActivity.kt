package com.callcenter.storydicoding.view.welcome

import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.view.WindowInsets
import android.view.WindowManager
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.callcenter.storydicoding.databinding.ActivityWelcomeBinding
import com.callcenter.storydicoding.view.login.LoginActivity
import com.callcenter.storydicoding.view.signup.SignupActivity
import com.callcenter.storydicoding.view.main.MainActivity
import com.callcenter.storydicoding.view.ViewModelFactory
import com.callcenter.storydicoding.view.main.MainViewModel
import com.callcenter.storydicoding.view.story.AddStoryActivity
import com.callcenter.storydicoding.view.story.ListStoryActivity

class WelcomeActivity : AppCompatActivity() {
    private lateinit var binding: ActivityWelcomeBinding
    private val viewModel by viewModels<MainViewModel> {
        ViewModelFactory.getInstance(this)
    }

    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        sharedPreferences = getSharedPreferences("AppSettingsPrefs", 0)
        val isDarkModeOn = sharedPreferences.getBoolean("DarkMode", false)
        setDarkMode(isDarkModeOn)

        binding = ActivityWelcomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        checkSession()

        setupView()
        setupAction()
    }

    private fun checkSession() {
        viewModel.getSession().observe(this) { user ->
            if (user.isLogin && user.token.isNotEmpty()) {

                startActivity(Intent(this, MainActivity::class.java))
                finish()
            }
        }
    }

    private fun setupView() {
        @Suppress("DEPRECATION")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.insetsController?.hide(WindowInsets.Type.statusBars())
        } else {
            window.setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
            )
        }
        supportActionBar?.hide()
    }

    private fun setupAction() {
        binding.loginButton.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }

        binding.signupButton.setOnClickListener {
            startActivity(Intent(this, SignupActivity::class.java))
        }

        binding.guestButton.setOnClickListener {
            startActivity(Intent(this, AddStoryActivity::class.java))
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
