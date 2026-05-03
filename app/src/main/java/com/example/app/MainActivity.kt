package com.exemplo.app

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val textView = TextView(this)
        textView.text = "Ganha Cash"
        textView.textSize = 32f
        setContentView(textView)
    }
}
