package com.example.vital.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.vital.R

class ChallengeAdapter(
    private val challenges: List<Challenge>,
    private val onProgressClick: (Challenge) -> Unit
) : RecyclerView.Adapter<ChallengeAdapter.ChallengeViewHolder>() {

    inner class ChallengeViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val title: TextView = view.findViewById(R.id.challengeTitle)
        val description: TextView = view.findViewById(R.id.challengeDescription)
        val progressBar: ProgressBar = view.findViewById(R.id.challengeProgressBar)
        val updateButton: Button = view.findViewById(R.id.updateChallengeButton)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChallengeViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_challenge, parent, false)
        return ChallengeViewHolder(view)
    }

    override fun onBindViewHolder(holder: ChallengeViewHolder, position: Int) {
        val challenge = challenges[position]
        holder.title.text = challenge.title
        holder.description.text = challenge.description
        holder.progressBar.progress = challenge.progress

        holder.updateButton.text = if (challenge.progress >= 100) "Completed" else "Update Progress"
        holder.updateButton.isEnabled = challenge.progress < 100

        holder.updateButton.setOnClickListener { onProgressClick(challenge) }
    }

    override fun getItemCount() = challenges.size
}
