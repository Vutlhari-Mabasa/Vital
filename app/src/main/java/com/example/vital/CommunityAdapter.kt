package com.example.vitalapp

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView

private val Unit.buttonLike: Any
private val Unit.textLikes: Any
    get() {
        TODO()
    }
private val Unit.textMessage: Any
    get() {
        TODO()
    }
private val Unit.textUsername: Any
    get() {
        TODO()
    }
private var Any.text: String
    get() {
        TODO()
    }
    set(value) {}
private val databinding: Any
    get() {
        TODO()
    }

class CommunityAdapter(private val posts: MutableList<CommunityPost>) :
    RecyclerView.Adapter<CommunityAdapter.CommunityViewHolder>() {

    inner class CommunityViewHolder(val binding: Unit) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommunityViewHolder {
        val binding = ItemCommunityPostBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return CommunityViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CommunityViewHolder, position: Int) {
        val post = posts[position]
        holder.binding.textUsername.text = post.username
        holder.binding.textMessage.text = post.message
        holder.binding.textLikes.text = "${post.likes} likes"

        holder.binding.buttonLike.setOnClickListener {
            post.likes++
            notifyItemChanged(position)
        }
    }

    override fun getItemCount(): Int = posts.size
}

private fun Any.setOnClickListener(function: () -> Unit) {
    TODO("Not yet implemented")
}

annotation class ItemCommunityPostBinding {
    val buttonLike: Any
    val textLikes: Any
    val textMessage: Any
    val textUsername: Any

    companion object {
        fun inflate(from: LayoutInflater, parent: ViewGroup, bool: Boolean) {
            TODO("Not yet implemented")
        }
    }
}
