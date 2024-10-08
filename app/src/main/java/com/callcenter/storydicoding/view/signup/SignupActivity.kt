package com.callcenter.storydicoding.view.signup

import android.content.Context
import android.net.ConnectivityManager
import android.os.Build
import android.os.Bundle
import android.view.WindowInsets
import android.view.WindowManager
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.callcenter.storydicoding.data.model.SignupRequest
import com.callcenter.storydicoding.data.network.ApiClient
import com.callcenter.storydicoding.data.network.ApiService
import com.callcenter.storydicoding.databinding.ActivitySignupBinding
import kotlinx.coroutines.launch
import retrofit2.HttpException

class SignupActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySignupBinding
    private val apiService: ApiService by lazy { ApiClient.apiService }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignupBinding.inflate(layoutInflater)
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

    private fun isInternetAvailable(): Boolean {
        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkInfo = connectivityManager.activeNetworkInfo
        return networkInfo != null && networkInfo.isConnected
    }

    private fun setupAction() {
        binding.signupButton.setOnClickListener {

            val name = binding.nameInputField?.getText() ?: ""
            val emailInputView = binding.emailInputView
            val email = emailInputView.getEmail()
            val password = binding.passwordInputField?.getText() ?: ""

            binding.nameInputField?.setError(null)
            emailInputView.clearError()
            binding.passwordInputField?.setError(null)

            var isValid = true
            var errorMessage = ""

            if (name.isEmpty()) {
                binding.nameInputField?.setError("Nama harus diisi.")
                errorMessage += "Nama harus diisi.\n"
                isValid = false
            }

            if (!emailInputView.validateEmailDomain()) {
                errorMessage += "Domain email tidak valid.\n"
                isValid = false
            }

            if (password.length < 8) {
                binding.passwordInputField?.setError("Password harus terdiri dari setidaknya 8 karakter.")
                errorMessage += "Password harus terdiri dari setidaknya 8 karakter."
                isValid = false
            }

            if (!isValid) {
                AlertDialog.Builder(this@SignupActivity).apply {
                    setTitle("Error")
                    setMessage(errorMessage.trim())
                    setPositiveButton("OK") { _, _ -> }
                    create()
                    show()
                }
                return@setOnClickListener
            }

            if (!isInternetAvailable()) {
                AlertDialog.Builder(this@SignupActivity).apply {
                    setTitle("Error")
                    setMessage("Tidak ada koneksi internet. Silakan periksa pengaturan jaringan Anda.")
                    setPositiveButton("OK") { _, _ -> }
                    create()
                    show()
                }
                return@setOnClickListener
            }

            binding.loadingSpinner.visibility = android.view.View.VISIBLE

            lifecycleScope.launch {
                try {
                    apiService.signup(SignupRequest(name, email, password))
                    binding.loadingSpinner.visibility = android.view.View.GONE

                    AlertDialog.Builder(this@SignupActivity).apply {
                        setTitle("Sukses!")
                        setMessage("Akun dengan $email telah berhasil dibuat! Yuk, login dan mulai buat cerita di Story Dicoding.")
                        setPositiveButton("Lanjut") { _, _ -> finish() }
                        create()
                        show()
                    }
                } catch (e: HttpException) {
                    binding.loadingSpinner.visibility = android.view.View.GONE

                    AlertDialog.Builder(this@SignupActivity).apply {
                        setTitle("Error")
                        setMessage("Terjadi kesalahan: ${e.message}")
                        setPositiveButton("OK") { _, _ -> }
                        create()
                        show()
                    }
                } catch (e: Exception) {
                    binding.loadingSpinner.visibility = android.view.View.GONE

                    AlertDialog.Builder(this@SignupActivity).apply {
                        setTitle("Error")
                        setMessage("Terjadi kesalahan, silakan coba lagi.")
                        setPositiveButton("OK") { _, _ -> }
                        create()
                        show()
                    }
                }
            }
        }
    }
}
