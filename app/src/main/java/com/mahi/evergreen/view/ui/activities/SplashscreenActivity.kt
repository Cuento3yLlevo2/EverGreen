package com.mahi.evergreen.view.ui.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.ImageView
import com.google.android.gms.ads.MobileAds
import com.mahi.evergreen.R

/**
 * This is the LAUNCHER activity of the application
 * It shows the Official application logo with an animation while
 * charging the Google MobileAds and other app process
 */
class SplashscreenActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splashscreen)

        // User's consent status successfully updated.
        // This call initializes the SDK and calls back a completion listener
        // once initialization is complete (or after a 30-second timeout).
        // This method should be Called only once and as early as possible, ideally at app launch.
        MobileAds.initialize(applicationContext)

        val ivLogoEverGreen = findViewById<ImageView>(R.id.ivLogoEverGreen)

        val animation = AnimationUtils.loadAnimation(this, R.anim.splash_screen_animation)
        ivLogoEverGreen.startAnimation(animation)

        val intent = Intent(this, WelcomeActivity::class.java)

        animation.setAnimationListener(object: Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation?) {
            }
            override fun onAnimationEnd(animation: Animation?) {
                startActivity(intent)
                finish()
            }
            override fun onAnimationRepeat(animation: Animation?) {
            }
        })
    }
}