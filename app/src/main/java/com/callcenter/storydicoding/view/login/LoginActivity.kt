package com.callcenter.storydicoding.view.login

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowInsets
import android.view.WindowManager
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewModelScope
import com.callcenter.storydicoding.R
import com.callcenter.storydicoding.data.model.LoginRequest
import com.callcenter.storydicoding.data.network.ApiClient
import com.callcenter.storydicoding.data.pref.UserModel
import com.callcenter.storydicoding.databinding.ActivityLoginBinding
import com.callcenter.storydicoding.view.ViewModelFactory
import com.callcenter.storydicoding.view.main.MainActivity
import com.callcenter.storydicoding.view.story.ListStoryActivity.NetworkUtil.isInternetAvailable
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {
    private val viewModel by viewModels<LoginViewModel> {
        ViewModelFactory.getInstance(this)
    }
    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

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
    }

    private fun setupAction() {
        binding.loginButton.setOnClickListener {
            val emailInputView = binding.emailInputView
            val password = binding.passwordInputView?.getPassword()

            binding.emailInputView?.clearError()
            binding.passwordInputView?.clearError()

            var isValid = true
            val errorMessageBuilder = StringBuilder()

            if (!emailInputView.validateEmailDomain()) {
                errorMessageBuilder.append(getString(R.string.error_message_email_invalid)).append("\n")
                isValid = false
            }

            if (password.isNullOrEmpty()) {
                binding.passwordInputView?.setError(getString(R.string.error_message_password_required))
                errorMessageBuilder.append(getString(R.string.error_message_password_required))
                isValid = false
            }

            if (!isValid) {
                showErrorDialog(errorMessageBuilder.toString())
                return@setOnClickListener
            }

            if (!isInternetAvailable(this)) {
                showErrorDialog(getString(R.string.error_message_no_internet))
                return@setOnClickListener
            }

            loginUser(emailInputView.getEmail(), password ?: "")
        }
    }

    private fun loginUser(email: String, password: String) {
        binding.loadingSpinner.visibility = View.VISIBLE

        lifecycleScope.launch {
            try {
                val response = ApiClient.apiService.login(LoginRequest(email, password))

                if (response.error) {
                    showErrorDialog(response.message)
                } else {
                    response.loginResult?.let { loginResult ->
                        val user = UserModel(
                            email = email,
                            token = loginResult.token,
                            name = loginResult.name,
                            isLogin = true
                        )
                        viewModel.saveSession(user)

                        showSuccessDialog()
                    }
                }
            } catch (e: Exception) {
                showErrorDialog(getString(R.string.error_message_generic))
                Log.e("LoginActivity", "Login failed: ${e.message}")
            } finally {
                binding.loadingSpinner.visibility = View.GONE
            }
        }
    }

    private fun showSuccessDialog() {
        AlertDialog.Builder(this).apply {
            setTitle(getString(R.string.success_title))
            setMessage(getString(R.string.success_message))
            setPositiveButton(getString(R.string.btn_continue)) { _, _ ->
                val intent = Intent(this@LoginActivity, MainActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                startActivity(intent)
                finish()
            }
            create()
            show()
        }
    }

    private fun showErrorDialog(message: String) {
        AlertDialog.Builder(this).apply {
            setTitle(getString(R.string.error_title))
            setMessage(message)
            setPositiveButton(getString(R.string.btn_ok), null)
            create()
            show()
        }
    }

}
