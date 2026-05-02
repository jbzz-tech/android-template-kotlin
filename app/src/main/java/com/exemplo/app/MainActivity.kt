package com.exemplo.app

import android.app.Activity
import android.graphics.Color
import android.os.Bundle
import android.widget.TextView

class MainActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val textView = TextView(this)
        textView.text = "Template 🐴 Kotlin"
        textView.textSize = 24f
        textView.setTextColor(Color.BLACK)
        setContentView(textView)
    }
}
