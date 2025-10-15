package com.example.budgeting.android

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import com.example.budgeting.android.ui.screens.LoginScreen

class LoginActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MaterialTheme {
                LoginScreen(
                    onLoginClick = { username, password ->
                        // TODO - check if the username and password are correct before login

                        val intent = Intent(this@LoginActivity, MainActivity::class.java)
                        intent.putExtra("USERNAME", username)
                        startActivity(intent)
                        // finish the activity so the user can't go back to the login screen
                        finish()
                    },
                    onSignUpClick = {
                        val intent = Intent(this@LoginActivity, SignUpActivity::class.java)
                        startActivity(intent)
                    }
                )
            }
        }
    }
}