package com.example.runningtracker.ui

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.runningtracker.R
import com.example.runningtracker.databinding.FragmentStatisticsBinding
import com.example.runningtracker.ui.adapters.CustomMarkerAdapterView
import com.example.runningtracker.utils.TimestampMillisecondsFormatter
import com.example.runningtracker.viewmodel.StatisticsViewModel
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import dagger.hilt.android.AndroidEntryPoint
import java.text.DecimalFormat
import javax.inject.Inject


@AndroidEntryPoint
class StatisticsFragment : Fragment() {
    private lateinit var statsBinding: FragmentStatisticsBinding
    private val statsViewModel: StatisticsViewModel by viewModels()
    private val df = DecimalFormat("#.#")

    @Inject
    lateinit var timestampMillisecondsFormatter: TimestampMillisecondsFormatter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        statsBinding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_statistics, container, false)
        return statsBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setUpBarChart()
        subscribeToObservers()
    }

    private fun setUpBarChart() {
        statsBinding.barChart.xAxis.apply {
            position = XAxis.XAxisPosition.BOTTOM
            setDrawLabels(false)
            axisLineColor = Color.BLACK
            textColor = Color.BLACK
            setDrawGridLines(false)
        }

        statsBinding.barChart.axisLeft.apply {
            axisLineColor = Color.BLACK
            textColor = Color.BLACK
            setDrawGridLines(false)
        }

        statsBinding.barChart.axisRight.apply {
            axisLineColor = Color.BLACK
            textColor = Color.BLACK
            setDrawGridLines(false)
        }

        statsBinding.barChart.apply {
            description.text = "Average Speed Over Time"
            legend.isEnabled = false
        }
    }

    private fun subscribeToObservers() {
        statsViewModel.totalRunTime.observe(viewLifecycleOwner) {
            it?.let {
                val totalTime = timestampMillisecondsFormatter.format(it, false)
                statsBinding.statsTotalTimeValue.text = totalTime
            } ?: kotlin.run {
                statsBinding.statsTotalTimeValue.text = "00:00:00"
            }
        }

        statsViewModel.totalDistance.observe(viewLifecycleOwner) {
            it?.let {
                val distance = "${df.format(it / 1000f)}km"
                statsBinding.statsDistanceValue.text = distance
            } ?: kotlin.run {
                statsBinding.statsDistanceValue.text = "${0.0}km"
            }
        }

        statsViewModel.totalCaloriesBurned.observe(viewLifecycleOwner) {
            it?.let {
                val caloriesBurned = "${it}kcal"
                statsBinding.statsCaloriesValue.text = caloriesBurned
            } ?: kotlin.run {
                statsBinding.statsCaloriesValue.text = "0kcal"
            }
        }

        statsViewModel.avgSpeed.observe(viewLifecycleOwner) {
            it?.let {
                val avgSpeed = "${df.format(it)}km/h"
                statsBinding.statsAvgSpeedValue.text = avgSpeed
            } ?: kotlin.run {
                statsBinding.statsAvgSpeedValue.text = "0.0km/h"
            }
        }

        statsViewModel.runsSortedByDate.observe(viewLifecycleOwner) {
            it?.let {
                val avgSpeeds = it.indices.map { i -> BarEntry(i.toFloat(), it[i].averageSpeedKMH) }
                val barDataSet = BarDataSet(avgSpeeds, "Average Speed Over Time").apply {
                    valueTextColor = ContextCompat.getColor(requireContext(), R.color.purple_500)
                    color = ContextCompat.getColor(requireContext(), R.color.purple_500)
                }
                statsBinding.barChart.data = BarData(barDataSet)
                statsBinding.barChart.marker = CustomMarkerAdapterView(it.reversed(), requireContext(), R.layout.bar_chart_popup)
                statsBinding.barChart.invalidate()
            }
        }
    }
}