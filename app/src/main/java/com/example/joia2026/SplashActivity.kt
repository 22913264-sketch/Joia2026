package com.example.joia2026

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        lifecycleScope.launch {
            delay(900)
            val token = UserSession.getToken(this@SplashActivity)
            val validToken = if (!token.isNullOrBlank()) {
                try {
                    val response = RetrofitClient.instance.getMe("Bearer $token")
                    if (response.isSuccessful) {
                        UserSession.saveLoggedUser(this@SplashActivity, response.body())
                        true
                    } else {
                        false
                    }
                } catch (_: Exception) {
                    UserSession.isLoggedIn(this@SplashActivity)
                }
            } else {
                false
            }

            val intent = if (validToken) Intent(this@SplashActivity, MainActivity::class.java) else Intent(this@SplashActivity, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}
