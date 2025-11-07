package com.example.vitalapp

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.vitalapp.databinding.ActivityTogetherBinding

data class CommunityPost(
    val username: String,
    val message: String,
    var likes: Int
)

class TogetherActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTogetherBinding
    private lateinit var adapter: CommunityAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTogetherBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val posts = mutableListOf(
            CommunityPost("Thabo", "Just finished a 5km run! Feeling amazing 🏃‍♂️💪", 24),
            CommunityPost("Aisha", "Day 3 of my yoga challenge — flexibility improving 🧘‍♀️✨", 18),
            CommunityPost("Liam", "Hit 8 hours of sleep for the first time this week 😴", 30)
        )

        adapter = CommunityAdapter(posts)
        binding.recyclerViewCommunity.layoutManager = LinearLayoutManager(this)
        binding.recyclerViewCommunity.adapter = adapter

        binding.buttonAddPost.setOnClickListener {
            val message = binding.editTextPost.text.toString().trim()
            if (message.isNotEmpty()) {
                posts.add(0, CommunityPost("You", message, 0))
                adapter.notifyItemInserted(0)
                binding.recyclerViewCommunity.scrollToPosition(0)
                binding.editTextPost.text.clear()
            }
        }
    }
}
