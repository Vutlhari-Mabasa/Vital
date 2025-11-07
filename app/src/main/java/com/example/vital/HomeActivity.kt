package com.example.vitalapp

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.vitalapp.databinding.ActivityHomeBinding
import com.google.android.material.bottomnavigation.BottomNavigationView

class HomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHomeBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Dashboard cards
        binding.cardSleep.setOnClickListener {
            startActivity(Intent(this, SleepTrackingActivity::class.java))
        }

        binding.cardStress.setOnClickListener {
            startActivity(Intent(this, StressMonitoringActivity::class.java))
        }

        binding.cardEnergy.setOnClickListener {
            startActivity(Intent(this, EnergyScoreActivity::class.java))
        }

        binding.cardFitness.setOnClickListener {
            startActivity(Intent(this, FitnessProgramsActivity::class.java))
        }

        binding.cardTogether.setOnClickListener {
            startActivity(Intent(this, TogetherActivity::class.java))
        }

        // Bottom navigation setup
        val bottomNav: BottomNavigationView = binding.bottomNavigation
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> true // Already on home
                R.id.nav_fitness -> {
                    startActivity(Intent(this, FitnessProgramsActivity::class.java))
                    true
                }
                R.id.nav_challenges -> {
                    startActivity(Intent(this, ChallengesActivity::class.java))
                    true
                }
                R.id.nav_profile -> {
                    startActivity(Intent(this, ProfileActivity::class.java))
                    true
                }
                else -> false
            }
        }
    }
}
