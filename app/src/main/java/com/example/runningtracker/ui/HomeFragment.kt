package com.example.runningtracker.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.AdapterView.OnItemSelectedListener
import android.widget.ArrayAdapter
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.runningtracker.R
import com.example.runningtracker.databinding.FragmentHomeBinding
import com.example.runningtracker.ui.adapters.HomeRecyclerViewAdapter
import com.example.runningtracker.utils.SortTypes
import com.example.runningtracker.viewmodel.HomeFragmentViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class HomeFragment : Fragment(), OnItemSelectedListener {
    private lateinit var binding: FragmentHomeBinding

    @Inject
    lateinit var homeRecyclerViewAdapter: HomeRecyclerViewAdapter

    private val homeViewModel by viewModels<HomeFragmentViewModel>()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_home, container, false)

        binding.homeFragmentFab.setOnClickListener { navigate() }
        setupRecyclerView()
        setupSpinner()
        getAllRuns()

        return binding.root
    }
    private fun setupSpinner() {
        val adapter = ArrayAdapter.createFromResource(requireContext(), R.array.sort_items ,android.R.layout.simple_spinner_item,)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.sortSpinner.adapter = adapter
        binding.sortSpinner.onItemSelectedListener = this

        when(homeViewModel.sortType) {
            SortTypes.DATE -> binding.sortSpinner.setSelection(0)
            SortTypes.DISTANCE -> binding.sortSpinner.setSelection(1)
            SortTypes.TIME -> binding.sortSpinner.setSelection(2)
            SortTypes.CALORIES_BURNED -> binding.sortSpinner.setSelection(3)
            SortTypes.AVG_SPEED -> binding.sortSpinner.setSelection(4)
        }
    }

    private fun getAllRuns() {
        viewLifecycleOwner.lifecycleScope.launch {
            homeViewModel.runs.observe(viewLifecycleOwner) {
                homeRecyclerViewAdapter.submitList(it)
            }
        }
    }

    private fun setupRecyclerView() {
        val layoutManager = LinearLayoutManager(requireActivity(), LinearLayoutManager.VERTICAL, false)

        binding.homeFragmentRv.adapter = homeRecyclerViewAdapter
        binding.homeFragmentRv.layoutManager = layoutManager
        binding.homeFragmentRv.setHasFixedSize(true)
    }

    private fun navigate() {
        findNavController().navigate(R.id.runFragment)
    }

    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        when(position) {
            0 -> homeViewModel.sortRuns(SortTypes.DATE)
            1 -> homeViewModel.sortRuns(SortTypes.DISTANCE)
            2 -> homeViewModel.sortRuns(SortTypes.TIME)
            3-> homeViewModel.sortRuns(SortTypes.CALORIES_BURNED)
            4 -> homeViewModel.sortRuns(SortTypes.AVG_SPEED)
        }
    }

    override fun onNothingSelected(parent: AdapterView<*>?) {

    }

}