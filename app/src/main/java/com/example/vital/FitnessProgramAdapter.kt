package com.example.vital.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.vital.R

class FitnessProgramAdapter(
    private val programs: List<FitnessProgram>,
    private val onProgressClick: (FitnessProgram) -> Unit
) : RecyclerView.Adapter<FitnessProgramAdapter.ProgramViewHolder>() {

    inner class ProgramViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val name: TextView = view.findViewById(R.id.programName)
        val description: TextView = view.findViewById(R.id.programDescription)
        val difficulty: TextView = view.findViewById(R.id.programDifficulty)
        val progressBar: ProgressBar = view.findViewById(R.id.programProgressBar)
        val updateButton: Button = view.findViewById(R.id.updateProgressButton)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProgramViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_fitness_program, parent, false)
        return ProgramViewHolder(view)
    }

    override fun onBindViewHolder(holder: ProgramViewHolder, position: Int) {
        val program = programs[position]
        holder.name.text = program.name
        holder.description.text = program.description
        holder.difficulty.text = "Level: ${program.difficulty}"
        holder.progressBar.progress = program.progress

        holder.updateButton.text = if (program.progress >= 100) "Completed" else "Update Progress"
        holder.updateButton.isEnabled = program.progress < 100

        holder.updateButton.setOnClickListener { onProgressClick(program) }
    }

    override fun getItemCount() = programs.size
}
