package com.callcenter.storydicoding.view.custom

import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.util.AttributeSet
import android.widget.LinearLayout
import android.view.LayoutInflater
import androidx.core.content.ContextCompat
import com.callcenter.storydicoding.R
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

class CustomPasswordInputField @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    private val inputLayout: TextInputLayout
    private val inputEditText: TextInputEditText

    init {
        LayoutInflater.from(context).inflate(R.layout.custom_password_input_field, this, true)
        inputLayout = findViewById(R.id.passwordInputLayout)
        inputEditText = findViewById(R.id.passwordEditText)

        attrs?.let {
            val typedArray = context.obtainStyledAttributes(it, R.styleable.CustomInputField)
            val hint = typedArray.getString(R.styleable.CustomInputField_hint)
            val icon = typedArray.getResourceId(R.styleable.CustomInputField_icon, -1)

            inputEditText.hint = hint
            if (icon != -1) {
                inputLayout.startIconDrawable = ContextCompat.getDrawable(context, icon)
            }

            typedArray.recycle()
        }

        inputEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                val password = s.toString()
                if (password.length < 8) {
                    setError(context.getString(R.string.password_too_short))
                } else {
                    setError(null)
                }
            }
        })
    }

    fun getText(): String {
        return inputEditText.text.toString().trim()
    }

    fun setError(error: String?) {
        inputLayout.error = error
    }
}
