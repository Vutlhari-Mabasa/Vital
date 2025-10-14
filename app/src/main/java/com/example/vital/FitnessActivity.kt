package com.example.vital

import android.os.Bundle
import android.webkit.WebSettings
import android.webkit.WebView
import androidx.appcompat.app.AppCompatActivity
import android.widget.Button
import com.google.android.material.bottomnavigation.BottomNavigationView

class FitnessActivity : AppCompatActivity() {

    // Declare WebView variable
    private lateinit var webView: WebView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_fitness)

        // Initialize WebView
        webView = findViewById(R.id.webView)
        val settings: WebSettings = webView.settings
        settings.javaScriptEnabled = true   // Enable JavaScript for YouTube playback
        settings.domStorageEnabled = true   // Enable DOM storage for web content

        // Button for Pilates playlist
        findViewById<Button>(R.id.btnPilates).setOnClickListener {
            loadYoutubePlaylist("PL8dDSKArO2-8qj5_7hU8wS23YV3G3k_B2")
        }

        // Button for Yoga playlist
        findViewById<Button>(R.id.btnYoga).setOnClickListener {
            loadYoutubePlaylist("PLui6Eyny-UzwxbWX1Lr4KZ0NSC1GAA86r")
        }

        // Button for HIIT playlist
        findViewById<Button>(R.id.btnHiit).setOnClickListener {
            loadYoutubePlaylist("PLDK8ZQfHC3F0G4mQUPe8QqXFz3vGN9VOG")
        }

        // Setup bottom navigation bar
        val bottom = findViewById<BottomNavigationView>(R.id.bottomNav)
        bottom.selectedItemId = R.id.nav_fitness  // Highlight current tab
        bottom.setOnItemSelectedListener { item ->
            when (item.itemId) {
                // Navigate to HomeActivity
                R.id.nav_home -> {
                    startActivity(android.content.Intent(this, HomeActivity::class.java))
                    true
                }
                // Navigate to ProfileActivity
                R.id.nav_profile -> {
                    startActivity(android.content.Intent(this, ProfileActivity::class.java))
                    true
                }
                // Navigate to MealsActivity
                R.id.nav_meals -> {
                    startActivity(android.content.Intent(this, MealsActivity::class.java))
                    true
                }
                // Stay on FitnessActivity
                R.id.nav_fitness -> true
                else -> false
            }
        }

        // Load default playlist (Yoga) when screen opens
        loadYoutubePlaylist("PLui6Eyny-UzwxbWX1Lr4KZ0NSC1GAA86r")
    }

    // Function to load a YouTube playlist inside WebView
    private fun loadYoutubePlaylist(playlistId: String) {
        val html = """
            <html>
            <body style='margin:0'>
              <iframe width='100%' height='100%' 
                src='https://www.youtube.com/embed/videoseries?list=$playlistId' 
                frameborder='0' 
                allow='accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture' 
                allowfullscreen>
              </iframe>
            </body>
            </html>
        """.trimIndent()

        // Load the YouTube playlist HTML into the WebView
        webView.loadDataWithBaseURL(null, html, "text/html", "UTF-8", null)
    }
}
