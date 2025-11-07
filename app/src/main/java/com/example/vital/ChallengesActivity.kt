package com.example.vital.ui

import android.annotation.SuppressLint
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.vital.R

class ChallengesActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ChallengeAdapter

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_challenges)

        recyclerView = findViewById(R.id.challengeRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        val challenges = listOf(
            Challenge("Step Challenge", "Complete 10,000 steps daily for 7 days", 30),
            Challenge("Cycling Streak", "Ride 15km per day for 5 days", 50),
            Challenge("Mindful Week", "Do 10 minutes of breathing exercises daily", 100),
            Challenge("Calorie Burn Race", "Burn 500 calories/day for a week", 60)
        )

        adapter = ChallengeAdapter(challenges) { challenge ->
            val newProgress = (challenge.progress + 20).coerceAtMost(100)
            challenge.progress = newProgress
            adapter.notifyDataSetChanged()

            if (newProgress == 100) {
                Toast.makeText(this, "${challenge.title} Completed 🏆", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "${challenge.title} Progress: $newProgress%", Toast.LENGTH_SHORT).show()
            }
        }

        recyclerView.adapter = adapter
    }
}

data class Challenge(val title: String, val description: String, var progress: Int)
