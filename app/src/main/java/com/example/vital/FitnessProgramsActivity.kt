package com.example.vital.ui

import android.annotation.SuppressLint
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.vital.R
import com.example.vital.R.id.programRecyclerView

class FitnessProgramsActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: FitnessProgramAdapter

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_fitness_programs)

        recyclerView = findViewById(programRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        val programs = listOf(
            FitnessProgram("Endurance Training", "Improve stamina with cardio routines", "Intermediate", 40),
            FitnessProgram("Weight Loss", "Burn fat with guided HIIT sessions", "Beginner", 75),
            FitnessProgram("Balance & Stability", "Enhance coordination with yoga-inspired moves", "Advanced", 20),
            FitnessProgram("Mindfulness & Recovery", "Stretching and guided breathing exercises", "All Levels", 90)
        )

        adapter = FitnessProgramAdapter(programs) { program ->
            val newProgress = (program.progress + 10).coerceAtMost(100)
            program.progress = newProgress
            adapter.notifyDataSetChanged()

            if (newProgress == 100) {
                Toast.makeText(this, "${program.name} Completed 🎉", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "${program.name} Progress: $newProgress%", Toast.LENGTH_SHORT).show()
            }
        }

        recyclerView.adapter = adapter
    }
}

data class FitnessProgram(
    val name: String,
    val description: String,
    val difficulty: String,
    var progress: Int
)
