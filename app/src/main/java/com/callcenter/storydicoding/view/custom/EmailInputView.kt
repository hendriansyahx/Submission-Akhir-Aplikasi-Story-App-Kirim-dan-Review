package com.callcenter.storydicoding.view.custom

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import com.callcenter.storydicoding.R
import com.callcenter.storydicoding.databinding.ViewEmailInputBinding

class EmailInputView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    private var binding: ViewEmailInputBinding
    private var allowedDomains: List<String> = emptyList()

    init {
        binding = ViewEmailInputBinding.inflate(LayoutInflater.from(context), this, true)
        attrs?.let {
            val typedArray = context.obtainStyledAttributes(it, R.styleable.EmailInputView)
            val domains = typedArray.getString(R.styleable.EmailInputView_allowedDomains)
            allowedDomains = domains?.split(",")?.map { it.trim() } ?: emptyList()
            typedArray.recycle()
        }
    }

    fun getEmail(): String {
        return binding.emailEditText.text.toString()
    }

    fun setError(error: String?) {
        binding.emailEditTextLayout.error = error
    }

    fun clearError() {
        binding.emailEditTextLayout.error = null
    }

    fun validateEmailDomain(): Boolean {
        val email = getEmail()
        return if (isEmailValid(email)) {
            val domain = email.substringAfterLast("@")
            if (allowedDomains.contains(domain)) {
                true
            } else {
                // Use string resource for error message
                setError(context.getString(R.string.error_invalid_domain, allowedDomains.joinToString(", ")))
                false
            }
        } else {
            // Use string resource for invalid email format
            setError(context.getString(R.string.error_invalid_email_format))
            false
        }
    }

    private fun isEmailValid(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    // Setter to allow dynamic domain configuration
    fun setAllowedDomains(domains: List<String>) {
        allowedDomains = domains.map { it.trim() }
    }
}
