package com.example.nomadwork.Helpers

import android.content.Context
import android.view.View
import android.view.inputmethod.InputMethodManager

class SoftInput {
    companion object {
        fun show (view: View, context: Context) {
            if (view.requestFocus()) {
                val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT)
            }
        }

        fun hide (view: View, context: Context) {
            val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(view.windowToken, 0)
        }
    }
}