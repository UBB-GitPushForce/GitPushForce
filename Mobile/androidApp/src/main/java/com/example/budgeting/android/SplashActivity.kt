package com.example.budgeting.android

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.lifecycle.lifecycleScope
import com.example.budgeting.android.data.auth.TokenHolder
import com.example.budgeting.android.data.local.TokenDataStore
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

@SuppressLint("CustomSplashScreen")
class SplashActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        checkLoginStatus()
    }

    private fun checkLoginStatus() {
        val tokenDataStore = TokenDataStore(applicationContext)

        // Use lifecycleScope to launch a coroutine tied to this Activity's lifecycle
        lifecycleScope.launch {
            val savedToken = tokenDataStore.tokenFlow.firstOrNull()

            val nextIntent: Intent
            if (!savedToken.isNullOrBlank()) {
                TokenHolder.token = savedToken

                nextIntent = Intent(this@SplashActivity, ExpenseActivity::class.java)
            } else {
                nextIntent = Intent(this@SplashActivity, LoginActivity::class.java)
            }

            startActivity(nextIntent)
            finish()
        }
    }
}
