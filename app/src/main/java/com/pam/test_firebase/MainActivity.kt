package com.pam.test_firebase

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)

        auth = FirebaseAuth.getInstance()
        val currentUser = auth.currentUser

        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_navigation)
//        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottomNavigationView)
//
//        bottomNavigationView.background = null
//        bottomNavigationView.menu.getItem(2).isEnabled = false

        val email = currentUser?.email

        // Atur fragment default ke HomeFragment dan kirim email ke fragment
        val homeFragment = HomeFragment()
        val bundle = Bundle()
        bundle.putString("userEmail", email)
        homeFragment.arguments = bundle
        loadFragment(homeFragment)

        bottomNavigationView.setOnNavigationItemSelectedListener { item ->
            var selectedFragment: Fragment? = null
            when (item.itemId) {
                R.id.nav_home -> {
                    selectedFragment = HomeFragment()
                    selectedFragment.arguments = bundle
                }

                R.id.nav_search -> selectedFragment = SearchFragment()
                R.id.nav_add_recipe -> selectedFragment = AddFragment()
                R.id.nav_bookmarks -> selectedFragment = BookmarksFragment()
                R.id.nav_profile -> selectedFragment = ProfileFragment()

            }
            selectedFragment?.let {
                loadFragment(it)
            }
            true
        }
    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.nav_host_fragment, fragment)
            .commit()
    }
}
