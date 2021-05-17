package com.mahi.evergreen.view.ui.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.*
import com.google.firebase.ktx.Firebase
import com.mahi.evergreen.R

class WelcomeActivity : AppCompatActivity() {

    private var firebaseUser: FirebaseUser? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_welcome)

        val buttonWelcomeRegister: Button = findViewById(R.id.buttonWelcomeRegister)
        val buttonWelcomeLogin: Button = findViewById(R.id.buttonWelcomeLogin)

        // When the user press the register button it starts the Loginactivity
        buttonWelcomeRegister.setOnClickListener {
            val intent = Intent(this@WelcomeActivity, RegisterActivity::class.java)
            startActivity(intent)
            finish()
        }
        // When the user press the login button it starts the RegisterActivity
        buttonWelcomeLogin.setOnClickListener {
            val intent = Intent(this@WelcomeActivity, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    override fun onStart() {
        super.onStart()

        // setPersistenceEnabled to true at the beginning of the application
        FirebaseDatabase.getInstance().setPersistenceEnabled(true)

        // When the Activity starts we need to know whether the user has already login or not.
        // therefore we call the FirebaseAuth.getInstance().currentUser to return the user if there is one
        firebaseUser = Firebase.auth.currentUser
        // if there is not user, we need to show the WelcomeActivity to ask for login or singin
        // If there is a user logged we need directly to show the MainActivity
        if(firebaseUser != null)
        {
            val intent = Intent(this@WelcomeActivity, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}