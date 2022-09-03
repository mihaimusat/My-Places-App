package com.example.myplaces.activities

import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.view.WindowInsets
import android.view.WindowManager
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.widget.AppCompatEditText
import androidx.appcompat.widget.Toolbar
import com.example.myplaces.R
import com.example.myplaces.database.DatabaseRepository
import com.example.myplaces.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

class LoginActivity : BaseActivity() {

    private lateinit var auth : FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        auth = FirebaseAuth.getInstance()

        @Suppress("DEPRECATION")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.insetsController?.hide(WindowInsets.Type.statusBars())
        } else {
            window.setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
            )
        }

        setupActionBar()
    }

    private fun setupActionBar() {
        val signInToolbar : Toolbar = findViewById(R.id.toolbar_sign_in_activity)
        setSupportActionBar(signInToolbar)

        val actionBar = supportActionBar
        if(actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.ic_baseline_arrow_back_24)
        }
        signInToolbar.setNavigationOnClickListener { onBackPressed() }

        val btnSignIn : Button = findViewById(R.id.btn_sign_in)
        btnSignIn.setOnClickListener {
            loginRegisteredUser()
        }
    }

    fun userLoginSuccess(user: User) {
        hideProgressDialog()
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }

    private fun loginRegisteredUser() {
        val email: String = findViewById<AppCompatEditText>(R.id.et_email_login).text
            .toString()
            .trim {it <= ' '}

        val password: String = findViewById<AppCompatEditText>(R.id.et_password_login).text
            .toString()
            .trim {it <= ' '}

        if(validateForm(email, password)) {
            showProgressDialog(resources.getString(R.string.please_wait))
            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    hideProgressDialog()
                    if (task.isSuccessful) {
                        DatabaseRepository().loadUserData(this)
                    } else {
                        Toast.makeText(
                            this,
                            task.exception!!.message,
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
        }
    }

    private fun validateForm(email: String, password: String) : Boolean {
        return when {
            TextUtils.isEmpty(email) -> {
                showErrorSnackBar("Please enter your e-mail address.")
                false
            }
            TextUtils.isEmpty(password) -> {
                showErrorSnackBar("Please enter your password.")
                false
            } else -> {
                true
            }
        }
    }
}