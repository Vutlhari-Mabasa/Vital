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

class HomeActivity : AppCompatActivity() {

    private lateinit var progress: ProgressBar
    private lateinit var textSteps: TextView
    private lateinit var textCalories: TextView
    private lateinit var textDistance: TextView
    private lateinit var textActiveMinutes: TextView

    private val fitnessOptions = FitnessOptions.builder()
        .addDataType(DataType.TYPE_STEP_COUNT_DELTA, FitnessOptions.ACCESS_READ)
        .addDataType(DataType.TYPE_CALORIES_EXPENDED, FitnessOptions.ACCESS_READ)
        .addDataType(DataType.TYPE_DISTANCE_DELTA, FitnessOptions.ACCESS_READ)
        .addDataType(DataType.TYPE_MOVE_MINUTES, FitnessOptions.ACCESS_READ)
        .build()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        progress = findViewById(R.id.progress)
        textSteps = findViewById(R.id.textSteps)
        textCalories = findViewById(R.id.textCalories)
        textDistance = findViewById(R.id.textDistance)
        textActiveMinutes = findViewById(R.id.textActiveMinutes)

        findViewById<Button>(R.id.btnGoMeals).setOnClickListener {
            startActivity(Intent(this, MealsActivity::class.java))
        }
        findViewById<Button>(R.id.btnGoProfile).setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }

        fetchTodayMetrics()
    }

    private fun fetchTodayMetrics() {
        val account = GoogleSignIn.getAccountForExtension(this, fitnessOptions)
        if (!GoogleSignIn.hasPermissions(account, fitnessOptions)) {
            textSteps.text = "Steps: connect Google Fit"
            textCalories.text = "Calories burned: connect Google Fit"
            textDistance.text = "Distance: connect Google Fit"
            textActiveMinutes.text = "Active minutes: connect Google Fit"
            return
        }

        setLoading(true)
        Fitness.getHistoryClient(this, account)
            .readDailyTotal(DataType.TYPE_STEP_COUNT_DELTA)
            .addOnSuccessListener { result ->
                val steps = if (!result.isEmpty) result.dataPoints.first().getValue(Field.FIELD_STEPS).asInt() else 0
                textSteps.text = "Steps: $steps"
            }
            .addOnFailureListener { textSteps.text = "Steps: unavailable" }

        Fitness.getHistoryClient(this, account)
            .readDailyTotal(DataType.TYPE_CALORIES_EXPENDED)
            .addOnSuccessListener { result ->
                val kcal = if (!result.isEmpty) result.dataPoints.first().getValue(Field.FIELD_CALORIES).asFloat().toInt() else 0
                textCalories.text = "Calories burned: $kcal kcal"
            }
            .addOnFailureListener { textCalories.text = "Calories burned: unavailable" }

        Fitness.getHistoryClient(this, account)
            .readDailyTotal(DataType.TYPE_DISTANCE_DELTA)
            .addOnSuccessListener { result ->
                val meters = if (!result.isEmpty) result.dataPoints.first().getValue(Field.FIELD_DISTANCE).asFloat() else 0f
                val km = meters / 1000f
                textDistance.text = "Distance: ${String.format("%.2f", km)} km"
            }
            .addOnFailureListener { textDistance.text = "Distance: unavailable" }

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

    private fun setLoading(loading: Boolean) {
        progress.visibility = if (loading) View.VISIBLE else View.INVISIBLE
    }
}



