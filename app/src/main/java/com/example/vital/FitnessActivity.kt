package com.example.vital

import android.os.Bundle
import android.webkit.WebSettings
import android.webkit.WebView
import androidx.appcompat.app.AppCompatActivity
import android.widget.Button
import com.google.android.material.bottomnavigation.BottomNavigationView

class FitnessActivity : AppCompatActivity() {

    private lateinit var webView: WebView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_fitness)

        webView = findViewById(R.id.webView)
        val settings: WebSettings = webView.settings
        settings.javaScriptEnabled = true
        settings.domStorageEnabled = true

        findViewById<Button>(R.id.btnPilates).setOnClickListener {
            loadYoutubePlaylist("PL8dDSKArO2-8qj5_7hU8wS23YV3G3k_B2")
        }
        findViewById<Button>(R.id.btnYoga).setOnClickListener {
            loadYoutubePlaylist("PLui6Eyny-UzwxbWX1Lr4KZ0NSC1GAA86r")
        }
        findViewById<Button>(R.id.btnHiit).setOnClickListener {
            loadYoutubePlaylist("PLDK8ZQfHC3F0G4mQUPe8QqXFz3vGN9VOG")
        }

        // Setup bottom navigation
        val bottom = findViewById<BottomNavigationView>(R.id.bottomNav)
        bottom.selectedItemId = R.id.nav_fitness
        bottom.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> { startActivity(android.content.Intent(this, HomeActivity::class.java)); true }
                R.id.nav_profile -> { startActivity(android.content.Intent(this, ProfileActivity::class.java)); true }
                R.id.nav_meals -> { startActivity(android.content.Intent(this, MealsActivity::class.java)); true }
                R.id.nav_fitness -> true
                else -> false
            }
        }

        // Default
        loadYoutubePlaylist("PLui6Eyny-UzwxbWX1Lr4KZ0NSC1GAA86r")
    }

    private fun loadYoutubePlaylist(playlistId: String) {
        val html = """
            <html>
            <body style='margin:0'>
              <iframe width='100%' height='100%' src='https://www.youtube.com/embed/videoseries?list=$playlistId' frameborder='0' allow='accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture' allowfullscreen></iframe>
            </body>
            </html>
        """.trimIndent()
        webView.loadDataWithBaseURL(null, html, "text/html", "UTF-8", null)
    }
}