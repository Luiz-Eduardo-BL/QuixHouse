package com.example.quixhouse

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI
import com.example.quixhouse.databinding.ActivityFeedBinding

class FeedActivity : AppCompatActivity() {

    private lateinit var binding: ActivityFeedBinding
    private lateinit var navController: NavController
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFeedBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initNavigation()

//        replaceFragment(HomeFragment())
//
//        binding.bottomNavigationView.setOnItemSelectedListener {
//            when(it.itemId){
//                R.id.home -> replaceFragment(HomeFragment())
//                R.id.profile -> replaceFragment(ProfileFragment())
//                R.id.settings -> replaceFragment(SettingsFragment())
//            }
//            true
//        }

    }

    private fun initNavigation() {
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.fragment_container) as NavHostFragment
        navController = navHostFragment.navController
        NavigationUI.setupWithNavController(binding.bottomNavigationView, navController)
    }

//    private fun replaceFragment(fragment: androidx.fragment.app.Fragment) {
//        val fragmentTransaction = supportFragmentManager.beginTransaction()
//        fragmentTransaction.replace(R.id.fragment_container, fragment)
//        fragmentTransaction.commit()
//    }
}