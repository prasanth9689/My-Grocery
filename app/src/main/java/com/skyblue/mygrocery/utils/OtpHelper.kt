package com.skyblue.mygrocery.utils

import android.content.ClipboardManager
import android.content.Context
import android.view.KeyEvent
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import androidx.core.widget.doOnTextChanged

class OtpHelper(
    private val context: Context,
    private val editTexts: List<EditText>,
    private val onComplete: (code: String) -> Unit
) {
    init {
        require(editTexts.size == 6) { "Requires exactly 6 EditTexts" }
        setup()
    }

    private fun setup() {
        editTexts.forEachIndexed { index, editText ->
            // ensure single char max (we set in XML too)
            editText.isCursorVisible = false

            // move to next when typed
            editText.doOnTextChanged { text, _, _, _ ->
                if (!text.isNullOrEmpty()) {
                    // if user pasted whole code, handle paste below
                    if (text.length > 1) {
                        handlePaste(text.toString())
                        return@doOnTextChanged
                    }
                    if (index < editTexts.size - 1) {
                        editTexts[index + 1].requestFocus()
                    } else {
                        editText.clearFocus()
                    }
                }
                checkComplete()
            }

            // handle delete/backspace to move focus back
            editText.setOnKeyListener { v, keyCode, event ->
                if (event.action == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_DEL) {
                    if (editText.text.isNullOrEmpty() && index > 0) {
                        editTexts[index - 1].requestFocus()
                        editTexts[index - 1].setSelection(editTexts[index - 1].text?.length ?: 0)
                    }
                }
                false
            }

            // handle "done" from keyboard on last
            editText.setOnEditorActionListener { _, actionId, _ ->
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    checkComplete()
                    true
                } else false
            }

            // allow long-press paste or clipboard paste: detect paste via text length >1
            editText.setOnLongClickListener {
                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                val clip = clipboard.primaryClip
                if (clip != null && clip.itemCount > 0) {
                    val clipText = clip.getItemAt(0).coerceToText(context).toString()
                    if (clipText.length >= 6 && clipText.all { it.isDigit() }) {
                        handlePaste(clipText)
                        return@setOnLongClickListener true
                    }
                }
                false
            }
        }
    }

    fun handlePaste(pasted: String) {
        // extract digits and fill boxes left-to-right
        val digits = pasted.filter { it.isDigit() }.take(6)
        digits.forEachIndexed { i, ch ->
            editTexts[i].setText(ch.toString())
        }
        if (digits.length < 6) {
            editTexts[digits.length].requestFocus()
        } else {
            editTexts.last().clearFocus()
            checkComplete()
        }
    }

    private fun checkComplete() {
        val code = editTexts.joinToString("") { it.text.toString().trim() }
        if (code.length == 6 && code.all { it.isDigit() }) {
            onComplete(code)
        }
    }

    fun clear() {
        editTexts.forEach { it.text?.clear() }
        editTexts[0].requestFocus()
    }
}
