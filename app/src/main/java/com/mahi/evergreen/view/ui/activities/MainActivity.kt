package com.mahi.evergreen.view.ui.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.Navigation
import androidx.navigation.ui.NavigationUI
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.mahi.evergreen.R
import com.mahi.evergreen.databinding.ActivityMainBinding
import com.mahi.evergreen.network.DatabaseService

/**
 * This activity takes care of the main flow of the application
 * this activity sets a navigation controller and a customized toolbar
 */
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private var firebaseUser: FirebaseUser? = null
    private var databaseService = DatabaseService()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        // customized toolbar
        setActionBar(findViewById(R.id.toolbarHome))
        title = ""


        // Set Navigation Config for app flow
        configNav()

        // retrieve from Firebase.auth the current user UID
        firebaseUser = Firebase.auth.currentUser

        // Call User Online Listener
        firebaseUser?.let { databaseService.isUserOnline(it.uid, null) }
    }

    /**
     * Sets up a BottomNavigationView for use with a NavController.
     */
    private fun configNav() {
        NavigationUI.setupWithNavController(binding.bnvMenu, Navigation.findNavController(this, R.id.fragContent))
    }

    override fun onResume() {
        super.onResume()
        // Call User Online Listener
        firebaseUser?.let { databaseService.isUserOnline(it.uid, null) }
    }

    override fun onPause() {
        super.onPause()
        // Call User Online Listener
        firebaseUser?.let { databaseService.isUserOnline(it.uid, false) }
    }

    /**
     * Lets fragments take control of hidding or showing the BottomNavigationView when needed
     */
    fun setBottomNavigationVisibility(visibility: Int) {
        // get the reference of the bottomNavigationView and set the visibility.
        binding.bnvMenu.visibility = visibility
    }

}