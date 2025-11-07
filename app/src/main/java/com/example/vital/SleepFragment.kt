package com.example.vital.ui

import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.example.vital.R
import com.example.vital.data.SleepData
import com.example.vital.viewmodel.SleepViewModel
import java.text.SimpleDateFormat
import java.util.*

class SleepFragment : Fragment() {

    private val viewModel: SleepViewModel by viewModels()
    private lateinit var durationText: TextView
    private lateinit var qualityText: TextView
    private lateinit var stagesText: TextView
    private lateinit var simulateButton: Button

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_sleep, container, false)
        durationText = view.findViewById(R.id.sleepDurationText)
        qualityText = view.findViewById(R.id.sleepQualityText)
        stagesText = view.findViewById(R.id.sleepStagesText)
        simulateButton = view.findViewById(R.id.analyzeSleepButton)

        simulateButton.setOnClickListener {
            val end = System.currentTimeMillis()
            val start = end - (7 * 60 * 60 * 1000) // simulate 7 hours ago
            val newSleep = viewModel.analyzeSleep(start, end)
            viewModel.addSleepRecord(newSleep)
            updateUI(newSleep)
        }

        // Observe latest sleep record (if available)
        viewModel.getLatestSleep(1).observe(viewLifecycleOwner, Observer {
            it?.let { updateUI(it) }
        })

        return view
    }

    private fun updateUI(data: SleepData) {
        durationText.text = "Duration: ${data.durationMinutes} mins"
        qualityText.text = "Quality Score: ${data.sleepQualityScore}/100"
        stagesText.text =
            "Deep: ${data.deepSleepMinutes}m | Light: ${data.lightSleepMinutes}m | REM: ${data.remSleepMinutes}m"
    }
}
