package com.example.budgeting.android

import android.os.Bundle
import android.widget.TextView
import androidx.activity.ComponentActivity



class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_layout)

        // get the username from the intent
        val username = intent.getStringExtra("USERNAME")

        val textView = findViewById<TextView>(R.id.textView)
        textView.text = "Hello ${username}!"
    }

}
