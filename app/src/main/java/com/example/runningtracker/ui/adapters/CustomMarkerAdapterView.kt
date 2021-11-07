package com.example.runningtracker.ui.adapters

import android.content.Context
import android.widget.TextView
import com.example.runningtracker.R
import com.example.runningtracker.model.RunEntry
import com.example.runningtracker.utils.TimestampMillisecondsFormatter
import com.github.mikephil.charting.components.MarkerView
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.utils.MPPointF
import dagger.hilt.android.AndroidEntryPoint
import java.util.*
import javax.inject.Inject

@AndroidEntryPoint
class CustomMarkerAdapterView(
    private val runList: List<RunEntry>, context: Context, layoutResource: Int
) : MarkerView(context, layoutResource) {

    @Inject
    lateinit var timestampMillisecondsFormatter: TimestampMillisecondsFormatter

    override fun getOffset(): MPPointF {
        return MPPointF(-width / 2f, -height.toFloat())
    }

    override fun refreshContent(e: Entry?, highlight: Highlight?) {
        if (e == null) {
            return
        }

        val currentRunId = e.x.toInt()

        with(runList[currentRunId]) {
            val distance = "${distanceInMeters/1000f}km"
            val speed = "${averageSpeedKMH}km/h"
            val calories = "${caloriesBurned}kcal"
            val formattedTime = timestampMillisecondsFormatter.format(timeInMilliseconds, false)
            val date = getDateFromTimeStamp(timeStamp)

            rootView.findViewById<TextView>(R.id.popup_date).text = date
            rootView.findViewById<TextView>(R.id.popup_distance).text = distance
            rootView.findViewById<TextView>(R.id.popup_avg_speed).text = speed
            rootView.findViewById<TextView>(R.id.popup_calories).text = calories
            rootView.findViewById<TextView>(R.id.popup_duration).text = formattedTime


        }
        super.refreshContent(e, highlight)
    }

    private fun getDateFromTimeStamp(timeStamp: Long): String {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = timeStamp
        val day = calendar.get(Calendar.DAY_OF_MONTH)
        val month = calendar.get(Calendar.MONTH)
        val year = calendar.get(Calendar.YEAR)
        return "$day.$month.$year"
    }
}