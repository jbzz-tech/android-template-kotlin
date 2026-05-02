package com.exemplo.app

import android.app.Activity
import android.os.Bundle
import android.widget.TextView

class MainActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val textView = TextView(this)
        textView.text = "Template 🐴 Kotlin 2.0 + Gradle 8.12"
        textView.textSize = 18f
        setContentView(textView)
    }
}
