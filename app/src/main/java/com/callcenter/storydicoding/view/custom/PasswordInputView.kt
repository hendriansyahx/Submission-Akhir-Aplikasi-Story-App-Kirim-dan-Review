package com.callcenter.storydicoding.view.custom

import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import com.callcenter.storydicoding.R
import com.callcenter.storydicoding.databinding.ViewPasswordInputBinding

class PasswordInputView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    private var binding: ViewPasswordInputBinding

    init {
        binding = ViewPasswordInputBinding.inflate(LayoutInflater.from(context), this, true)
        setupTextWatcher()
    }

    fun getPassword(): String {
        return binding.passwordEditText.text.toString().trim()
    }

    fun setError(errorMessage: String?) {
        binding.passwordInputLayout.error = errorMessage
    }

    fun clearError() {
        binding.passwordInputLayout.error = null
    }

    private fun setupTextWatcher() {
        binding.passwordEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                validatePassword(s.toString())
            }

            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun validatePassword(password: String) {
        if (password.length < 8) {
            setError(context.getString(R.string.password_too_short))
        } else {
            clearError()
        }
    }
}
