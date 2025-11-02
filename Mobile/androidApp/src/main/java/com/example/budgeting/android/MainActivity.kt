package com.example.budgeting.android

import android.os.Bundle
import android.widget.TextView
import androidx.activity.ComponentActivity



class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_layout)

        val textView = findViewById<TextView>(R.id.textView)
        textView.text = "Hello logged user!"
    }

}
