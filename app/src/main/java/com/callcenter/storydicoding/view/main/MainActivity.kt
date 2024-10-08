package com.callcenter.storydicoding.view.main

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.WindowInsets
import android.view.WindowManager
import android.view.animation.AnimationUtils
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.callcenter.storydicoding.R
import com.callcenter.storydicoding.databinding.ActivityMainBinding
import com.callcenter.storydicoding.view.ViewModelFactory
import com.callcenter.storydicoding.view.settings.SettingsActivity
import com.callcenter.storydicoding.view.welcome.WelcomeActivity
import com.callcenter.storydicoding.view.story.ListStoryActivity

class MainActivity : AppCompatActivity() {
    private val viewModel by viewModels<MainViewModel> {
        ViewModelFactory.getInstance(this)
    }
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel.getSession().observe(this) { user ->
            if (!user.isLogin || user.token.isEmpty()) {
                startActivity(Intent(this, WelcomeActivity::class.java))
                finish()
            } else {
                binding.messageTextView.text = getString(R.string.welcome_message_home, user.name, user.email)
            }
        }

        setupView()
        setupAction()
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

        val imageView = binding.imageView
        val scaleAnimation = AnimationUtils.loadAnimation(this, R.anim.scale_animation)
        imageView.startAnimation(scaleAnimation)

        val nameTextView = binding.nameTextView
        val fadeInAnimation = AnimationUtils.loadAnimation(this, R.anim.fade_in)
        nameTextView.startAnimation(fadeInAnimation)

        val listStoryButton = binding.listStoryButton
        val bounceAnimation = AnimationUtils.loadAnimation(this, R.anim.bounce)
        listStoryButton.startAnimation(bounceAnimation)
    }

    private fun setupAction() {
        binding.logoutButton.setOnClickListener {
            viewModel.logout()
        }

        binding.listStoryButton.setOnClickListener {
            val intent = Intent(this, ListStoryActivity::class.java)
            startActivity(intent)
        }

        binding.settingsButton.setOnClickListener {
            val intent = Intent(this, SettingsActivity::class.java)
            startActivity(intent)
        }
    }
}
