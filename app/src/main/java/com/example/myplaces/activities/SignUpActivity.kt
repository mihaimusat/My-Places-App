package com.example.myplaces.activities

import android.os.Build
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

class SignUpActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)

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
        val signUpToolbar : Toolbar = findViewById(R.id.toolbar_sign_up_activity)
        setSupportActionBar(signUpToolbar)

        val actionBar = supportActionBar
        if(actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.ic_baseline_arrow_back_24)
        }
        signUpToolbar.setNavigationOnClickListener { onBackPressed() }

        val btnSignUp : Button = findViewById(R.id.btn_sign_up)
        btnSignUp.setOnClickListener {
            registerUser()
        }
    }

    fun userRegisteredSuccess() {
        Toast.makeText(
            this,
            "You have successfully registered",
            Toast.LENGTH_LONG
        ).show()
        hideProgressDialog()
        FirebaseAuth.getInstance().signOut()
        finish()
    }

    private fun registerUser() {
        val name: String = findViewById<AppCompatEditText>(R.id.et_name).text
            .toString()
            .trim {it <= ' '}

        val email: String = findViewById<AppCompatEditText>(R.id.et_email).text
            .toString()
            .trim {it <= ' '}

        val password: String = findViewById<AppCompatEditText>(R.id.et_password).text
            .toString()
            .trim {it <= ' '}

        if(validateForm(name, email, password)) {
            showProgressDialog(resources.getString(R.string.please_wait))
            FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val firebaseUser: FirebaseUser = task.result!!.user!!
                        val registeredEmail = firebaseUser.email!!
                        val user = User(firebaseUser.uid, name, registeredEmail)
                        DatabaseRepository().registerUser(this, user)
                    } else {
                        Toast.makeText(this, "Registration failed!", Toast.LENGTH_SHORT)
                            .show()
                    }
                }
        }
    }

    private fun validateForm(name: String, email: String, password: String) : Boolean {
        return when {
            TextUtils.isEmpty(name) -> {
                showErrorSnackBar("Please enter a name.")
                false
            }
            TextUtils.isEmpty(email) -> {
                showErrorSnackBar("Please enter an e-mail address.")
                false
            }
            TextUtils.isEmpty(password) -> {
                showErrorSnackBar("Please enter a password.")
                false
            } else -> {
                true
            }
        }
    }
}