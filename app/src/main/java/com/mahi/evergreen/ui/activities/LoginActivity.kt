package com.mahi.evergreen.ui.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.mahi.evergreen.MainActivity2
import com.mahi.evergreen.R

class LoginActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

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

        // Initialize Firebase Auth
        auth = Firebase.auth

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
                auth.signInWithEmailAndPassword(userEmail, userPassword)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful){
                            val intent = Intent(this@LoginActivity, MainActivity2::class.java)
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                            startActivity(intent)
                            finish()
                        } else {
                            lateinit var errorCode: String
                            when(task.exception as FirebaseException){
                                is FirebaseAuthException -> {
                                    errorCode = (task.exception as FirebaseAuthException).errorCode
                                    when(errorCode){
                                        "ERROR_WRONG_PASSWORD" -> Toast.makeText(this@LoginActivity, "La contraseña que ingresaste es incorrecta.", Toast.LENGTH_LONG).show()
                                        "ERROR_USER_NOT_FOUND" -> Toast.makeText(this@LoginActivity, "El correo electrónico que ingresaste no pertenece a ninguna cuenta.", Toast.LENGTH_LONG).show()
                                        "ERROR_USER_DISABLED" -> Toast.makeText(this@LoginActivity, "La cuenta de usuario ha sido deshabilitada", Toast.LENGTH_LONG).show()
                                        "ERROR_USER_TOKEN_EXPIRED" -> Toast.makeText(this@LoginActivity, "El inicio de sesión ha caducado.", Toast.LENGTH_LONG).show()
                                        "ERROR_INVALID_USER_TOKEN" -> Toast.makeText(this@LoginActivity, "Error desconocido, imposible iniciar sesión.", Toast.LENGTH_LONG).show()
                                        else -> Toast.makeText(this@LoginActivity, "Error: $errorCode", Toast.LENGTH_LONG).show()
                                    }
                                }
                                is FirebaseTooManyRequestsException -> Toast.makeText(this@LoginActivity, "Demasiados intentos consecutivos. Vuelve a intentarlo más tarde.", Toast.LENGTH_LONG).show()
                                else -> Toast.makeText(this@LoginActivity, "Error: ${(task.exception as FirebaseException).message}", Toast.LENGTH_LONG).show()
                            }
                        }
                    }
            }
        }
    }
}