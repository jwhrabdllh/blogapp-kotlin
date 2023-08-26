package com.abdi.blogapp.ui.activity

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import com.abdi.blogapp.R

@SuppressLint("CustomSplashScreen")
class SplashScreenActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)

        Handler(Looper.getMainLooper()).postDelayed({
            val userPref = applicationContext.getSharedPreferences("user", MODE_PRIVATE)
            val isLogin = userPref.getBoolean("isLogin", false)

            if (isLogin) {
                startActivity(Intent(this@SplashScreenActivity, HomeActivity::class.java))
                finish()
            } else {
                isResume()
            }
        }, 2500)
    }

    private fun isResume() {
        val preferences = getSharedPreferences("onResume", MODE_PRIVATE)
        val splashScreen = preferences.getBoolean("splashScreen", true)

        if (splashScreen) {
            preferences.edit()
                .putBoolean("splashScreen", false)
                .apply()
        }

        startActivity(Intent(this@SplashScreenActivity, SignInActivity::class.java))
        finish()
    }
}