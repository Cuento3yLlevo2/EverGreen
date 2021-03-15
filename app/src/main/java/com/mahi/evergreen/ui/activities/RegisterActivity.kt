package com.mahi.evergreen.ui.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.mahi.evergreen.MainActivity2
import com.mahi.evergreen.R
import com.mahi.evergreen.model.User

class RegisterActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private lateinit var dbReference: DatabaseReference

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

        // Initialize Firebase Auth
        auth = Firebase.auth

        val buttonActivityRegister : Button = findViewById(R.id.buttonActivityRegister)
        buttonActivityRegister.setOnClickListener {
            registerUser()
        }
    }

    private fun registerUser() {
        val username: String = etRegisterUsername.text.toString()
        val userEmail: String = etRegisterEmail.text.toString()
        val userPassword: String = etRegisterPassword.text.toString()

        when {
            username == "" -> {
                Toast.makeText(this@RegisterActivity, "introduce un nombre de usuario", Toast.LENGTH_LONG).show()
            }
            userEmail == "" -> {
                Toast.makeText(this@RegisterActivity, "introduce un correo electrónico", Toast.LENGTH_LONG).show()
            }
            userPassword == "" -> {
                Toast.makeText(this@RegisterActivity, "introduce una contraseña", Toast.LENGTH_LONG).show()
            }
            else -> {
                auth.createUserWithEmailAndPassword(userEmail, userPassword)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful){
                            // Sign in success, update UI with the signed-in user's information
                            Log.d("FireBaseLogs", "createUserWithEmail:success")
                            val user = auth.currentUser
                            if (user != null) {
                                writeNewUser(user.uid, username, userEmail,
                                    "https://firebasestorage.googleapis.com/v0/b/evergreen-app-bdbc2.appspot.com/o/user_default.png?alt=media&token=53382a72-8fc1-4a11-b63f-0cb342bb02c6",
                                    "https://firebasestorage.googleapis.com/v0/b/evergreen-app-bdbc2.appspot.com/o/profile_cover_default.jpg?alt=media&token=49ff9c78-9714-45bf-9ff7-b0a605ba39b1",
                                    "offline",
                                    username.toLowerCase()
                                )
                            }
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w("FireBaseLogs", "createUserWithEmail:failure", task.exception)
                            Toast.makeText(baseContext, "Authentication failed.",
                                Toast.LENGTH_SHORT).show()
                        }
                    }
            }
        }
    }

    private fun writeNewUser(userId: String, username: String, email: String, profileImage: String, coverImage: String, status: String, search: String) {
        val user = User(username, email, profileImage, coverImage, status, search)
        Log.w("FireBaseLogs", "Start Writing $userId")

        dbReference.push().key
        dbReference.child(userId).setValue(user)
                .addOnSuccessListener {
                    // Write was successful!
                    Log.w("FireBaseLogs", "Write was successful")

                    val intent = Intent(this@RegisterActivity, MainActivity2::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                    startActivity(intent)
                    finish()
                }
                .addOnFailureListener {
                    // Write failed
                    Log.w("FireBaseLogs", "Write failed")
                }
    }
}