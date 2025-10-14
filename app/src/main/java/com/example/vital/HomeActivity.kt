package com.example.vital

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.fitness.Fitness
import com.google.android.gms.fitness.FitnessOptions
import com.google.android.gms.fitness.data.DataType
import com.google.android.gms.fitness.data.Field
import com.google.android.material.bottomnavigation.BottomNavigationView

class HomeActivity : AppCompatActivity() {

    // UI components for displaying progress and fitness data
    private lateinit var progress: ProgressBar
    private lateinit var textSteps: TextView
    private lateinit var textCalories: TextView
    private lateinit var textDistance: TextView
    private lateinit var textActiveMinutes: TextView

    // Define which fitness data types to access from Google Fit
    private val fitnessOptions = FitnessOptions.builder()
        .addDataType(DataType.TYPE_STEP_COUNT_DELTA, FitnessOptions.ACCESS_READ)
        .addDataType(DataType.TYPE_CALORIES_EXPENDED, FitnessOptions.ACCESS_READ)
        .addDataType(DataType.TYPE_DISTANCE_DELTA, FitnessOptions.ACCESS_READ)
        .addDataType(DataType.TYPE_MOVE_MINUTES, FitnessOptions.ACCESS_READ)
        .build()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        // Initialize UI elements
        progress = findViewById(R.id.progress)
        textSteps = findViewById(R.id.textSteps)
        textCalories = findViewById(R.id.textCalories)
        textDistance = findViewById(R.id.textDistance)
        textActiveMinutes = findViewById(R.id.textActiveMinutes)

        // Button to navigate to Meals screen
        findViewById<Button>(R.id.btnGoMeals).setOnClickListener {
            startActivity(Intent(this, MealsActivity::class.java))
        }

        // Button to navigate to Profile screen
        findViewById<Button>(R.id.btnGoProfile).setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }

        // Fetch today's Google Fit metrics
        fetchTodayMetrics()

        // Setup bottom navigation bar
        val bottom = findViewById<BottomNavigationView>(R.id.bottomNav)
        bottom.selectedItemId = R.id.nav_home  // Highlight current tab
        bottom.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> true // Stay on current screen
                R.id.nav_profile -> { startActivity(Intent(this, ProfileActivity::class.java)); true }
                R.id.nav_meals -> { startActivity(Intent(this, MealsActivity::class.java)); true }
                R.id.nav_fitness -> { startActivity(Intent(this, FitnessActivity::class.java)); true }
                else -> false
            }
        }
    }

    // Function to retrieve today's activity metrics from Google Fit
    private fun fetchTodayMetrics() {
        val account = GoogleSignIn.getAccountForExtension(this, fitnessOptions)

        // Check if app has permission to access Google Fit data
        if (!GoogleSignIn.hasPermissions(account, fitnessOptions)) {
            textSteps.text = "Steps: connect Google Fit"
            textCalories.text = "Calories burned: connect Google Fit"
            textDistance.text = "Distance: connect Google Fit"
            textActiveMinutes.text = "Active minutes: connect Google Fit"
            return
        }

        setLoading(true)

        // Retrieve daily step count
        Fitness.getHistoryClient(this, account)
            .readDailyTotal(DataType.TYPE_STEP_COUNT_DELTA)
            .addOnSuccessListener { result ->
                val steps = if (!result.isEmpty) result.dataPoints.first().getValue(Field.FIELD_STEPS).asInt() else 0
                textSteps.text = "Steps: $steps"
            }
            .addOnFailureListener { textSteps.text = "Steps: unavailable" }

        // Retrieve daily calories burned
        Fitness.getHistoryClient(this, account)
            .readDailyTotal(DataType.TYPE_CALORIES_EXPENDED)
            .addOnSuccessListener { result ->
                val kcal = if (!result.isEmpty) result.dataPoints.first().getValue(Field.FIELD_CALORIES).asFloat().toInt() else 0
                textCalories.text = "Calories burned: $kcal kcal"
            }
            .addOnFailureListener { textCalories.text = "Calories burned: unavailable" }

        // Retrieve daily distance covered
        Fitness.getHistoryClient(this, account)
            .readDailyTotal(DataType.TYPE_DISTANCE_DELTA)
            .addOnSuccessListener { result ->
                val meters = if (!result.isEmpty) result.dataPoints.first().getValue(Field.FIELD_DISTANCE).asFloat() else 0f
                val km = meters / 1000f
                textDistance.text = "Distance: ${String.format("%.2f", km)} km"
            }
            .addOnFailureListener { textDistance.text = "Distance: unavailable" }

        // Retrieve daily active minutes
        Fitness.getHistoryClient(this, account)
            .readDailyTotal(DataType.TYPE_MOVE_MINUTES)
            .addOnSuccessListener { result ->
                val minutes = if (!result.isEmpty) result.dataPoints.first().getValue(Field.FIELD_DURATION).asInt() else 0
                textActiveMinutes.text = "Active minutes: $minutes"
                setLoading(false)
            }
            .addOnFailureListener {
                textActiveMinutes.text = "Active minutes: unavailable"
                setLoading(false)
            }
    }

    // Show or hide progress bar while loading data
    private fun setLoading(loading: Boolean) {
        progress.visibility = if (loading) View.VISIBLE else View.INVISIBLE
    }
}
