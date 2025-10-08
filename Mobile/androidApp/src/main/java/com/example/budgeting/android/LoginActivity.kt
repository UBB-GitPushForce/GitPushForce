package com.example.budgeting.android

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.activity.ComponentActivity

class LoginActivity : ComponentActivity(){
    private lateinit var button1: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.login_layout)
        button1 = findViewById(R.id.loginButton)

        button1.setOnClickListener{
            // get the username from the EditText before starting the MainActivity
            val username = findViewById<EditText>(R.id.usernameEditText).text.toString()

            val intent = Intent(this, MainActivity::class.java)
            intent.putExtra("USERNAME", username)
            startActivity(intent)
            // finish the activity so the user can't go back to the login screen
            finish()
        }
    }
}