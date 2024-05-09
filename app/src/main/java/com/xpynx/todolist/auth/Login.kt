package com.xpynx.todolist.auth

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.xpynx.todolist.MainActivity
import com.xpynx.todolist.R

class Login : AppCompatActivity() {
    private lateinit var etEmail: TextInputEditText
    private lateinit var etPassword: TextInputEditText
    private lateinit var loginButton: MaterialButton
    private lateinit var signupButton: MaterialButton
    private lateinit var auth: FirebaseAuth
    private lateinit var tvForgotP: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        etEmail = findViewById(R.id.etEmail)
        etPassword = findViewById(R.id.etPassword)
        tvForgotP = findViewById(R.id.tvForgotP)
        loginButton = findViewById(R.id.material_button)
        signupButton = findViewById(R.id.signup)
        auth = FirebaseAuth.getInstance()
        val currentUser = auth.currentUser
        if (currentUser != null) {
            // User is already logged in, navigate to the MainActivity
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish() // Finish the Login activity to prevent going back to it
        }
        loginButton.setOnClickListener {
            val email = etEmail.text.toString()
            val password = etPassword.text.toString()
            if(email.isEmpty()){
                Toast.makeText(this,"Please Enter Email",Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if(password.isEmpty()){
                Toast.makeText(this,"Please Enter Password",Toast.LENGTH_SHORT).show()

                return@setOnClickListener
            }

            // Use Firebase Authentication to sign in the user
            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        val intent = Intent(this, MainActivity::class.java)
                        startActivity(intent)
                        finish()
                    } else {
                        Toast.makeText(this, "Login failed. Please check your credentials.", Toast.LENGTH_SHORT).show()
                    }
                }
        }

        signupButton.setOnClickListener {
            val intent=Intent(this,Signup::class.java)
            startActivity(intent)
            // Implement code to handle signup button click, e.g., navigate to the signup screen
        }
        tvForgotP.setOnClickListener{
            val i =Intent(this,ForgotPassword::class.java)
            startActivity(i)
        }
    }
}
