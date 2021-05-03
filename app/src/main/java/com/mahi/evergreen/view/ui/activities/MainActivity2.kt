package com.mahi.evergreen.view.ui.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.viewpager.widget.ViewPager
import com.google.android.material.tabs.TabLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.*
import com.google.firebase.ktx.Firebase
import com.mahi.evergreen.R
import com.mahi.evergreen.model.User
import com.mahi.evergreen.network.FirebaseDatabaseService
import com.mahi.evergreen.network.USERS
import com.mahi.evergreen.view.ui.fragments.ChatListFragment
import com.mahi.evergreen.view.ui.fragments.SearchUsersFragment
import com.mahi.evergreen.view.ui.fragments.SettingsFragment
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView
import kotlin.collections.ArrayList

class MainActivity2 : AppCompatActivity() {

    private lateinit var buttonSignOut : Button
    private val databaseService: FirebaseDatabaseService = FirebaseDatabaseService()
    private var firebaseUser: FirebaseUser? = null
    private var currentUserReference: DatabaseReference? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main2)
        setSupportActionBar(findViewById(R.id.toolbar_main2))

        // First of all, after Authentication we need to get user's data from Realtime Database using the users's uid
        firebaseUser = Firebase.auth.currentUser
        currentUserReference = firebaseUser?.let { databaseService.database.getReference(USERS).child(it.uid) }


        val toolbar: androidx.appcompat.widget.Toolbar = findViewById(R.id.toolbar_main2)
        setSupportActionBar(toolbar)
        supportActionBar!!.title = ""
        val tabLayout: TabLayout = findViewById(R.id.tlMainTabLayout)
        val viewPager: ViewPager = findViewById(R.id.vpMainViewPager)
        val viewPagerAdapter = ViewPagerAdapter(supportFragmentManager)
        viewPagerAdapter.addFragment(ChatListFragment(), "Chats")
        viewPagerAdapter.addFragment(SearchUsersFragment(), "Search")
        viewPagerAdapter.addFragment(SettingsFragment(), "Settings")
        viewPager.adapter = viewPagerAdapter
        tabLayout.setupWithViewPager(viewPager)

        buttonSignOut = findViewById(R.id.buttonSignOut)
        buttonSignOut.setOnClickListener {
            signOut()
        }

        // Display username and profile picture
        currentUserReference?.addValueEventListener(object : ValueEventListener {

            override fun onDataChange(snapshot: DataSnapshot) {
                // OnDataChange: we need to make sure that the current user still exist
                if (snapshot.exists()){
                    val userDataSnapshot = snapshot.getValue(User::class.java)
                    val userProfile = userDataSnapshot?.profile
                    val tvMainUsername = findViewById<TextView>(R.id.tvMainUsername)
                    val civMainProfileImage = findViewById<CircleImageView>(R.id.civMainProfileImage)
                    if (userProfile != null) {
                        tvMainUsername.text = userProfile.username
                        Picasso.get().load(userProfile.profileImage).placeholder(R.drawable.user_default).into(civMainProfileImage)
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }

        })

    }

    private fun signOut() {
        FirebaseAuth.getInstance().signOut()
        val intent = Intent(this@MainActivity2, WelcomeActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
        finish()
    }

    // Adapter needed for the ViewPager and navegation
    internal class ViewPagerAdapter(fragmentManager: FragmentManager) :
        FragmentPagerAdapter(fragmentManager,  BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT)
    {

        private val fragments: ArrayList<Fragment>
        private val titles: ArrayList<String>

        init {
            fragments = ArrayList<Fragment>()
            titles = ArrayList<String>()
        }


        override fun getItem(position: Int): Fragment {
            return fragments[position]
        }

        override fun getCount(): Int {
            return fragments.size
        }

        fun addFragment(fragment: Fragment, title: String){
            fragments.add(fragment)
            titles.add(title)
        }


        override fun getPageTitle(i: Int): CharSequence? {
            return titles[i]
        }
    }




}