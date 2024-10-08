package com.callcenter.storydicoding.view.custom

import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.util.AttributeSet
import android.view.View
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.callcenter.storydicoding.R

class CustomEditText @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private val editText: EditText
    private val floatingLabel: TextView

    init {
        inflate(context, R.layout.custom_edit_text, this)
        editText = findViewById(R.id.editText)
        floatingLabel = findViewById(R.id.floatingLabel)

        editText.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus || editText.text.isNotEmpty()) {
                floatingLabel.visibility = View.VISIBLE
                floatingLabel.setTextColor(ContextCompat.getColor(context, R.color.colorAccent))
            } else {
                floatingLabel.visibility = View.GONE
                floatingLabel.setTextColor(ContextCompat.getColor(context, R.color.gray))
            }
        }

        editText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // Not used
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (s != null && s.isNotEmpty()) {
                    floatingLabel.visibility = View.VISIBLE
                } else {
                    floatingLabel.visibility = View.GONE
                }
            }

            override fun afterTextChanged(s: Editable?) {
                // Not used
            }
        })
    }

    fun getText(): String {
        return editText.text.toString()
    }

    fun setText(text: String) {
        editText.setText(text)
        floatingLabel.visibility = if (text.isNotEmpty()) View.VISIBLE else View.GONE
    }
}