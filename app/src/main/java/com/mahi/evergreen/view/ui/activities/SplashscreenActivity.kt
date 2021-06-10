package com.mahi.evergreen.view.ui.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.ImageView
import com.mahi.evergreen.R

class SplashscreenActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splashscreen)

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