package com.mahi.evergreen.view.ui.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.Navigation
import androidx.navigation.ui.NavigationUI
import com.mahi.evergreen.R
import com.mahi.evergreen.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        setActionBar(findViewById(R.id.toolbarHome))
        title = ""


        // Set Navigation Config for app flow
        configNav()

    }

    private fun configNav() {
        NavigationUI.setupWithNavController(binding.bnvMenu, Navigation.findNavController(this, R.id.fragContent))
    }

    fun setBottomNavigationVisibility(visibility: Int) {
        // get the reference of the bottomNavigationView and set the visibility.
        binding.bnvMenu.visibility = visibility
    }

}