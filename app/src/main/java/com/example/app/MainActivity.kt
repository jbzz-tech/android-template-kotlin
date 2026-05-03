package com.example.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            AppTela()
        }
    }
}

@Composable
fun AppTela() {
    MaterialTheme {
        Surface {
            Text(text = "Olá, Android em Kotlin!")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewTela() {
    AppTela()
}
