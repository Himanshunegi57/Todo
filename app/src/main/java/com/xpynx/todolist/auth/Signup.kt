package com.xpynx.todolist.auth

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.xpynx.todolist.R

class Signup : AppCompatActivity() {
    private lateinit var etName: TextInputEditText
    private lateinit var etEmail: TextInputEditText
    private lateinit var etPassword: TextInputEditText
    private lateinit var btnSignup: MaterialButton
    private lateinit var btnLogin: MaterialButton


    private lateinit var mAuth: FirebaseAuth

    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)

        etName = findViewById(R.id.etName)
        etEmail = findViewById(R.id.etEmail)
        etPassword = findViewById(R.id.etPassword)
        btnSignup = findViewById(R.id.btnSignup)
        btnLogin = findViewById(R.id.btnLogin)

        mAuth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        btnSignup.setOnClickListener {
            signUp()
        }
        btnLogin.setOnClickListener{
            val i=Intent(this,Login::class.java)
            startActivity(i)
        }
    }

    private fun signUp() {
        val name = etName.text.toString()
        val email = etEmail.text.toString()
        val password = etPassword.text.toString()
        if(name.isEmpty()||email.isEmpty()||password.isEmpty()){
            Toast.makeText(this,"All Filed required",Toast.LENGTH_SHORT).show()
            return
        }

        mAuth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // User registration successful
                    val uid = mAuth.currentUser?.uid
                    val user = hashMapOf(
                        "name" to name,
                        "email" to email
                    )

                    if (uid != null) {
                        db.collection("users")
                            .document(uid)
                            .set(user)
                            .addOnSuccessListener {
                                // User data added to Firestore
                                val i=Intent(this,Login::class.java)
                                startActivity(i)
                                Toast.makeText(this, "Signup successful!", Toast.LENGTH_SHORT).show()
                                // Navigate to your main activity or login screen
                            }
                            .addOnFailureListener { e ->
                                // Handle Firestore user data write failure
                                Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                    }
                } else {
                    // Handle user registration failure
                    Toast.makeText(this, "Error: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }
}
