package com.mahi.evergreen

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.viewpager.widget.ViewPager
import com.google.android.material.tabs.TabLayout
import com.google.firebase.auth.FirebaseAuth
import com.mahi.evergreen.ui.activities.WelcomeActivity
import com.mahi.evergreen.ui.fragments.ChatsFragment
import com.mahi.evergreen.ui.fragments.SearchFragment
import com.mahi.evergreen.ui.fragments.SettingsFragment
import kotlin.collections.ArrayList

class MainActivity2 : AppCompatActivity() {

    private lateinit var buttonSignOut : Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main2)
        setSupportActionBar(findViewById(R.id.toolbar_main2))



        val toolbar: androidx.appcompat.widget.Toolbar = findViewById(R.id.toolbar_main2)
        setSupportActionBar(toolbar)
        supportActionBar!!.title = ""

        val tabLayout: TabLayout = findViewById(R.id.tlMainTabLayout)
        val viewPager: ViewPager = findViewById(R.id.vpMainViewPager)
        val viewPagerAdapter = ViewPagerAdapter(supportFragmentManager)

        viewPagerAdapter.addFragment(ChatsFragment(), "Chats")
        viewPagerAdapter.addFragment(SearchFragment(), "Search")
        viewPagerAdapter.addFragment(SettingsFragment(), "Settings")

        viewPager.adapter = viewPagerAdapter
        tabLayout.setupWithViewPager(viewPager)

        buttonSignOut = findViewById(R.id.buttonSignOut)
        buttonSignOut.setOnClickListener {
            signOut()
        }

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