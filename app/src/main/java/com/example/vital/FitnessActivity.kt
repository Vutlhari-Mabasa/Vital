package com.example.vital

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class FitnessActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_fitness)


        val video1: WebView = findViewById(R.id.youtubeVideo1)
        val video2: WebView = findViewById(R.id.youtubeVideo2)
        val video3: WebView = findViewById(R.id.youtubeVideo3)

        // Enable JavaScript for video playback
        val webSettings1: WebSettings = video1.settings
        val webSettings2: WebSettings = video2.settings
        val webSettings3: WebSettings = video3.settings

        webSettings1.javaScriptEnabled = true
        webSettings2.javaScriptEnabled = true
        webSettings3.javaScriptEnabled = true

        // Dummy YouTube fitness videos (replace with your own later)
        val videoHtml1 = """<iframe width="100%" height="100%" 
            src="https://www.youtube.com/embed/UItWltVZZmE" 
            frameborder="0" allowfullscreen></iframe>"""

        val videoHtml2 = """<iframe width="100%" height="100%" 
            src="https://www.youtube.com/embed/ml6cT4AZdqI" 
            frameborder="0" allowfullscreen></iframe>"""

        val videoHtml3 = """<iframe width="100%" height="100%" 
            src="https://www.youtube.com/embed/UBMk30rjy0o" 
            frameborder="0" allowfullscreen></iframe>"""

        // Load the HTML for each video
        video1.loadData(videoHtml1, "text/html", "utf-8")
        video2.loadData(videoHtml2, "text/html", "utf-8")
        video3.loadData(videoHtml3, "text/html", "utf-8")

        val whatsNewContainer = findViewById<LinearLayout>(R.id.hlWhatsNew)
        val knowHowContainer = findViewById<LinearLayout>(R.id.hlKnowHows)

        val sampleWorkouts = listOf(
            Workout("EASY TABATA! ALL-IN-ONE UPPER BODY", "07:28", "LILLIUS", R.drawable.sample_workout),
            Workout("EASY TABATA! ARM & CHEST STRENGTH", "07:28", "LILLIUS", R.drawable.sample_workout2),
            Workout("FULL BODY KETTLEBELL WORKOUT", "08:15", "LILLIUS", R.drawable.sample_workout3)
        )

        populateWorkoutSection(whatsNewContainer, sampleWorkouts)
        populateWorkoutSection(knowHowContainer, sampleWorkouts)

        // Navigation setup
        findViewById<LinearLayout>(R.id.navHome).setOnClickListener {
            startActivity(Intent(this, HomeActivity::class.java))
        }

        findViewById<LinearLayout>(R.id.navMeals).setOnClickListener {
            startActivity(Intent(this, MealsActivity::class.java))
        }

        findViewById<LinearLayout>(R.id.navProfile).setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }
    }

    private fun populateWorkoutSection(container: LinearLayout, workouts: List<Workout>) {
        workouts.forEach {
            val card = LayoutInflater.from(this).inflate(R.layout.simple_card, container, false)
            val img = card.findViewById<ImageView>(R.id.imgThumbnail)
            val title = card.findViewById<TextView>(R.id.tvTitle)
            val author = card.findViewById<TextView>(R.id.tvAuthor)

            img.setImageResource(it.thumbnailResId)
            title.text = it.title
            author.text = it.author

            container.addView(card)
        }
    }
}
