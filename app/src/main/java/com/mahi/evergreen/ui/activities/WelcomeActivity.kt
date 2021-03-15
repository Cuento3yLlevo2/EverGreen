package com.mahi.evergreen.ui.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import com.google.android.gms.tasks.Task
import com.google.firebase.FirebaseError
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.*
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.mahi.evergreen.MainActivity2
import com.mahi.evergreen.R

class WelcomeActivity : AppCompatActivity() {

    var firebaseUser: FirebaseUser? = null

    private lateinit var database: DatabaseReference


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

        database = Firebase.database.getReference("users")


        Log.w("firebase", "Start Writing Prueba")


        /*database.child("xoxo2").setValue("dodo2")
                .addOnCompleteListener { Log.w("firebase", " completed ${it.toString()}") }
            .addOnSuccessListener {
                // Write was successful!
                Log.w("firebase", "Write was successful!")
            }
            .addOnFailureListener {
                // Write failed
                Log.w("firebase", " Write failed ${it.toString()}" )
            }
            .addOnCanceledListener { Log.w("firebase", "canceled") }*/


        Log.w("firebase", "finished Writing Prueba")


        val connectedRef = Firebase.database.getReference(".info/connected")
        connectedRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val connected = snapshot.getValue(Boolean::class.java) ?: false
                if (connected) {
                    Log.d("firebase", "connected")
                } else {
                    Log.d("firebase", "not connected")
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.w("firebase", "Listener was cancelled")
            }
        })

        database.get().addOnSuccessListener {
            Log.i("firebase", "Got value ${it.value}")
        }.addOnFailureListener{
            Log.e("firebase", "Error getting data", it)
        }

    }

    override fun onStart() {
        super.onStart()

        // When the Activity starts we need to know whether the user has already login or not.
        // therefore we call the FirebaseAuth.getInstance().currentUser to return the user if there is one
        firebaseUser = Firebase.auth.currentUser
        // if there is not user, we need to show the WelcomeActivity to ask for login or singin
        // If there is a user logged we need directly to show the MainActivity
        if(firebaseUser != null)
        {
            val intent = Intent(this@WelcomeActivity, MainActivity2::class.java)
            startActivity(intent)
            finish()
        }
    }
}