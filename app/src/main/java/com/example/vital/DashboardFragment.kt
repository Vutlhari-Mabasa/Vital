package com.example.vital.ui

import android.os.Bundle
import android.view.*
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.example.vital.R
import com.example.vital.data.SleepData
import com.example.vital.data.StressData
import com.example.vital.viewmodel.EnergyScoreViewModel
import com.example.vital.viewmodel.SleepViewModel
import com.example.vital.viewmodel.StressViewModel

private val EnergyScoreViewModel.latestScore: Any

class DashboardFragment : Fragment() {

    private lateinit var energyProgressBar: ProgressBar
    private lateinit var energyScoreText: TextView
    private lateinit var sleepSummaryText: TextView
    private lateinit var stressSummaryText: TextView
    private lateinit var activitySummaryText: TextView

    private val energyViewModel: EnergyScoreViewModel by viewModels()

    private fun viewModels() {
        TODO("Not yet implemented")
    }

    private val sleepViewModel: SleepViewModel by viewModels()
    private val stressViewModel: StressViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_dashboard, container, false)

        energyProgressBar = view.findViewById(R.id.energyProgressBar)
        energyScoreText = view.findViewById(R.id.energyScoreText)
        sleepSummaryText = view.findViewById(R.id.sleepSummaryText)
        stressSummaryText = view.findViewById(R.id.stressSummaryText)
        activitySummaryText = view.findViewById(R.id.activitySummaryText)

        // Observe Energy Score
        energyViewModel.latestScore.observe(viewLifecycleOwner, Observer { score ->
            score?.let {
                energyProgressBar.progress = it.score
                energyScoreText.text = "${it.score}/100"
            }
        })

        // Observe latest sleep
        sleepViewModel.getLatestSleep(1).observe(viewLifecycleOwner, Observer { sleep ->
            updateSleepUI(sleep)
        })

        // Observe latest stress
        stressViewModel.getLatestStress(1).observe(viewLifecycleOwner, Observer { stress ->
            updateStressUI(stress)
        })

        // Simulated Activity
        updateActivityUI(steps = 4500, workouts = 2) // Replace with actual data later

        return view
    }

    private fun updateSleepUI(sleep: SleepData?) {
        sleep?.let {
            sleepSummaryText.text = "Duration: ${it.durationMinutes} mins | Quality: ${it.sleepQualityScore}/100"
        }
    }

    private fun updateStressUI(stress: StressData?) {
        stress?.let {
            stressSummaryText.text = "${it.stressLevel} (${it.stressIndex}/100)"
        }
    }

    private fun updateActivityUI(steps: Int, workouts: Int) {
        activitySummaryText.text = "Steps: $steps | Workouts: $workouts"
    }
}
