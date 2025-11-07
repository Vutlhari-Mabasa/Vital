package com.example.vital.ui

import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.example.vital.R
import com.example.vital.data.StressData
import com.example.vital.viewmodel.StressViewModel
import kotlinx.coroutines.*

class StressFragment : Fragment() {

    private val viewModel: StressViewModel by viewModels()
    private lateinit var stressLevelText: TextView
    private lateinit var heartRateText: TextView
    private lateinit var hrvText: TextView
    private lateinit var checkStressButton: Button
    private lateinit var startBreathingButton: Button
    private lateinit var breathingView: BreathingView
    private var breathingJob: Job? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_stress, container, false)

        stressLevelText = view.findViewById(R.id.stressLevelText)
        heartRateText = view.findViewById(R.id.heartRateText)
        hrvText = view.findViewById(R.id.hrvText)
        checkStressButton = view.findViewById(R.id.checkStressButton)
        startBreathingButton = view.findViewById(R.id.startBreathingButton)
        breathingView = view.findViewById(R.id.breathingView)

        checkStressButton.setOnClickListener {
            val newStress = viewModel.analyzeStress()
            viewModel.addStressData(newStress)
            updateUI(newStress)
        }

        viewModel.getLatestStress(1).observe(viewLifecycleOwner, Observer {
            it?.let { updateUI(it) }
        })

        startBreathingButton.setOnClickListener {
            startGuidedBreathing()
        }

        return view
    }

    private fun updateUI(data: StressData) {
        stressLevelText.text = "Stress Level: ${data.stressLevel} (${data.stressIndex}/100)"
        heartRateText.text = "Heart Rate: ${data.heartRate} bpm"
        hrvText.text = "HRV: ${"%.1f".format(data.hrvScore)} ms"
    }

    private fun startGuidedBreathing() {
        breathingJob?.cancel()
        breathingJob = CoroutineScope(Dispatchers.Main).launch {
            val inhaleTime = 4000L
            val holdTime = 4000L
            val exhaleTime = 4000L
            val minRadius = 50f
            val maxRadius = 300f

            while (isActive) {
                // Inhale
                for (r in minRadius.toInt()..maxRadius.toInt() step 5) {
                    breathingView.setRadius(r.toFloat())
                    delay(inhaleTime / ((maxRadius - minRadius)/5))
                }
                // Hold
                delay(holdTime)
                // Exhale
                for (r in maxRadius.toInt() downTo minRadius.toInt() step 5) {
                    breathingView.setRadius(r.toFloat())
                    delay(exhaleTime / ((maxRadius - minRadius)/5))
                }
            }
        }
    }

    override fun onDestroyView() {
        breathingJob?.cancel()
        super.onDestroyView()
    }
}
