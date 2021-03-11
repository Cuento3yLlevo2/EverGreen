package com.mahi.evergreen.ui.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.mahi.evergreen.R

class RegisterActivity : AppCompatActivity() {

    private lateinit var firebaseAuthEntryPoint: FirebaseAuth
    private lateinit var usersReferences: DatabaseReference
    private lateinit var etRegisterUsername: EditText
    private lateinit var etRegisterEmail: EditText
    private lateinit var etRegisterPassword: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        etRegisterUsername = findViewById(R.id.etRegisterUsername)
        etRegisterEmail = findViewById(R.id.etRegisterEmail)
        etRegisterPassword = findViewById(R.id.etRegisterPassword)

        val tvLinkFromRegisterToLogin : TextView = findViewById(R.id.tvLinkFromRegisterToLogin)

        tvLinkFromRegisterToLogin.setOnClickListener {
            val intent = Intent(this@RegisterActivity, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }

        firebaseAuthEntryPoint = FirebaseAuth.getInstance()

        val buttonActivityRegister : Button = findViewById(R.id.buttonActivityRegister)
        buttonActivityRegister.setOnClickListener {
            registerUser()
        }
    }

    private fun registerUser() {
        val username: String = etRegisterUsername.text.toString()
        val userEmail: String = etRegisterEmail.text.toString()
        val userPassword: String = etRegisterPassword.text.toString()
    }
}