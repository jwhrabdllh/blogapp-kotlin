package com.abdi.blogapp.ui.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.fragment.app.FragmentManager
import com.abdi.blogapp.ui.fragment.HomeFragment
import com.abdi.blogapp.ui.fragment.ProfileFragment
import com.abdi.blogapp.R
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton

class HomeActivity : AppCompatActivity() {
    private lateinit var fragmentManager: FragmentManager
    private lateinit var btnAddPost: FloatingActionButton
    private lateinit var navigationView: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        fragmentManager = supportFragmentManager
        fragmentManager.beginTransaction().replace(R.id.frameHomeContainer, HomeFragment(), HomeFragment::class.java.simpleName).commit()
        init()
    }

    private fun init() {
        navigationView = findViewById(R.id.bottom_nav)
        btnAddPost = findViewById(R.id.btnAddPost)

        btnAddPost.setOnClickListener {
            val intent = Intent(this@HomeActivity, AddPostActivity::class.java)
            startActivity(intent)
        }

        navigationView.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.item_home -> {
                    // Handle navigation to HomeFragment
                    val home = fragmentManager.findFragmentByTag(ProfileFragment::class.java.simpleName)
                    if (home != null) {
                        fragmentManager.beginTransaction().hide(fragmentManager.findFragmentByTag(
                            ProfileFragment::class.java.simpleName)!!).commit()
                        fragmentManager.beginTransaction().show(fragmentManager.findFragmentByTag(
                            HomeFragment::class.java.simpleName)!!).commit()
                    }
                }
                R.id.item_profile -> {
                    // Handle navigation to ProfileFragment
                    fragmentManager.beginTransaction().hide(fragmentManager.findFragmentByTag(
                        HomeFragment::class.java.simpleName)!!).commit()
                    val profile = fragmentManager.findFragmentByTag(ProfileFragment::class.java.simpleName)
                    if (profile != null) {
                        fragmentManager.beginTransaction().show(profile).commit()
                    } else {
                        fragmentManager.beginTransaction().add(R.id.frameHomeContainer, ProfileFragment(), ProfileFragment::class.java.simpleName).commit()
                    }
                }
            }
            true
        }
    }
}