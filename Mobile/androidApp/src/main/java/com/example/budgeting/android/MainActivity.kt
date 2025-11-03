package com.example.budgeting.android

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.lifecycle.lifecycleScope
import com.example.budgeting.android.data.auth.TokenHolder
import com.example.budgeting.android.data.local.TokenDataStore
import kotlinx.coroutines.launch


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_layout)

        val textView = findViewById<TextView>(R.id.textView)
        textView.text = "Hello logged user!"

        // just to test the persistency of the user

        val logoutButton = findViewById<Button>(R.id.logoutButton)
        logoutButton.setOnClickListener {

            lifecycleScope.launch {
                val tokenDataStore = TokenDataStore(applicationContext)

                tokenDataStore.clearToken()

                TokenHolder.token = null

                val intent = Intent(this@MainActivity, LoginActivity::class.java)

                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK

                startActivity(intent)
                finish()
            }
        }

        // TODO: main page with all the functionalities
    }
}

