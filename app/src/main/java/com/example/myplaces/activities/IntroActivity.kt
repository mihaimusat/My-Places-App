package com.example.myplaces.activities

import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.WindowInsets
import android.view.WindowManager
import android.widget.Button
import com.example.myplaces.R

class IntroActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_intro)

        @Suppress("DEPRECATION")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.insetsController?.hide(WindowInsets.Type.statusBars())
        } else {
            window.setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
            )
        }

        val btnSignUpIntro : Button = findViewById(R.id.btn_sign_up_intro)
        btnSignUpIntro.setOnClickListener {
            startActivity(Intent(this, SignUpActivity::class.java))
        }

        val btnSignInIntro : Button = findViewById(R.id.btn_sign_in_intro)
        btnSignInIntro.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }
    }
}