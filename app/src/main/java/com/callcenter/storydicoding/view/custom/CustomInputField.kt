package com.callcenter.storydicoding.view.custom

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import androidx.core.content.ContextCompat
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import android.widget.LinearLayout
import android.view.LayoutInflater
import com.callcenter.storydicoding.R

class CustomInputField @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    private val inputLayout: TextInputLayout
    private val inputEditText: TextInputEditText

    init {
        LayoutInflater.from(context).inflate(R.layout.custom_input_field, this, true)
        inputLayout = findViewById(R.id.textInputLayout)
        inputEditText = findViewById(R.id.textInputEditText)

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
    }

    fun getText(): String {
        return inputEditText.text.toString().trim()
    }

    fun setError(error: String?) {
        inputLayout.error = error
    }
}
