package com.example.runningtracker.ui.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.runningtracker.databinding.RecyclerViewItemBinding
import com.example.runningtracker.model.RunEntry
import com.example.runningtracker.utils.TimestampMillisecondsFormatter
import java.util.*
import javax.inject.Inject

class HomeRecyclerViewAdapter @Inject constructor(
    private val context: Context,
    private val timestampMillisecondsFormatter: TimestampMillisecondsFormatter
) :
    RecyclerView.Adapter<HomeRecyclerViewAdapter.HomeRecyclerViewHolder>() {

    private val diffCallback = object : DiffUtil.ItemCallback<RunEntry>() {
        override fun areItemsTheSame(oldItem: RunEntry, newItem: RunEntry): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: RunEntry, newItem: RunEntry): Boolean {
            return oldItem.hashCode() == newItem.hashCode()
        }
    }

    private val differ = AsyncListDiffer(this, diffCallback)
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HomeRecyclerViewHolder {
        val binding = RecyclerViewItemBinding.inflate(LayoutInflater.from(context), parent, false)
        return HomeRecyclerViewHolder(binding)
    }

    override fun onBindViewHolder(holder: HomeRecyclerViewHolder, position: Int) {
        with(differ.currentList[position]) {
            with(holder.binding) {

                val distance = "${distanceInMeters/1000f}km"
                val speed = "${averageSpeedKMH}km/h"
                val calories = "${caloriesBurned}kcal"
                val formattedTime = timestampMillisecondsFormatter.format(timeInMilliseconds, false)
                val date = getDateFromTimeStamp(timeStamp)

                Glide.with(context)
                    .load(image)
                    .centerCrop()
                    .into(runItemImg)

                runStartTimeTv.text = date
                runEndTimeTv.text = formattedTime
                runDistanceTv.text = distance
                runAvgSpeedTv.text = speed
                runCalTv.text = calories
            }
        }
    }

    private fun getDateFromTimeStamp(timeStamp: Long): String {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = timeStamp
        val day = calendar.get(Calendar.DAY_OF_MONTH)
        val month = calendar.get(Calendar.MONTH)
        val year = calendar.get(Calendar.YEAR)
        return "$day.$month.$year"
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }

    fun submitList(list: List<RunEntry>) = differ.submitList(list)

    class HomeRecyclerViewHolder(val binding: RecyclerViewItemBinding) :
        RecyclerView.ViewHolder(binding.root)

}