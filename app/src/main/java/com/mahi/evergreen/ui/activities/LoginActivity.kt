package com.mahi.evergreen.ui.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.mahi.evergreen.MainActivity2
import com.mahi.evergreen.R

class LoginActivity : AppCompatActivity() {

    private lateinit var firebaseAuthEntryPoint: FirebaseAuth

    private lateinit var etLoginEmail: EditText
    private lateinit var etLoginPassword: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        etLoginEmail = findViewById(R.id.etLoginEmail)
        etLoginPassword = findViewById(R.id.etLoginPassword)
        val tvLinkFromLoginToRegister : TextView = findViewById(R.id.tvLinkFromLoginToRegister)

        tvLinkFromLoginToRegister.setOnClickListener {
            val intent = Intent(this@LoginActivity, RegisterActivity::class.java)
            startActivity(intent)
            finish()
        }

        firebaseAuthEntryPoint = FirebaseAuth.getInstance()

        val buttonActivityLogin : Button = findViewById(R.id.buttonActivityLogin)
        buttonActivityLogin.setOnClickListener {
            loginUser()
        }
    }



    private fun loginUser() {
        val userEmail: String = etLoginEmail.text.toString()
        val userPassword: String = etLoginPassword.text.toString()

        when {
            userEmail == "" -> {
                Toast.makeText(this@LoginActivity, "introduce tu correo electrónico", Toast.LENGTH_LONG).show()
            }
            userPassword == "" -> {
                Toast.makeText(this@LoginActivity, "introduce tu contraseña", Toast.LENGTH_LONG).show()
            }
            else -> {
                firebaseAuthEntryPoint.signInWithEmailAndPassword(userEmail, userPassword)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful){
                            val intent = Intent(this@LoginActivity, MainActivity2::class.java)
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                            startActivity(intent)
                            finish()
                        } else {
                            Toast.makeText(this@LoginActivity, "Error: " + task.exception!!.message.toString(), Toast.LENGTH_LONG).show()
                        }
                    }
            }
        }
    }
}