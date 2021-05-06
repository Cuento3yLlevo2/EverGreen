package com.mahi.evergreen.view.ui.activities

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.Navigation
import androidx.navigation.ui.NavigationUI
import com.google.firebase.auth.FirebaseAuth
import com.mahi.evergreen.R
import com.mahi.evergreen.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        setActionBar(findViewById(R.id.toolbarMain))
        title = ""

        // Set Navigation Config for app flow
        configNav()

        binding.buttonSignOut.setOnClickListener {
            signOut()
        }

    }

    private fun configNav() {
        NavigationUI.setupWithNavController(binding.bnvMenu, Navigation.findNavController(this, R.id.fragContent))
    }

    private fun signOut() {
        FirebaseAuth.getInstance().signOut()
        val intent = Intent(this@MainActivity, WelcomeActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
        finish()
    }

}