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
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.runningtracker.R
import com.example.runningtracker.databinding.FragmentHomeBinding
import com.example.runningtracker.ui.adapters.HomeRecyclerViewAdapter
import com.example.runningtracker.utils.SortTypes
import com.example.runningtracker.viewmodel.HomeFragmentViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.example.runningtracker.model.RunEntry
import com.google.android.material.snackbar.Snackbar


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
        subscribeToObserver()

        return binding.root
    }
    private fun setupSpinner() {
        val adapter = ArrayAdapter.createFromResource(
            requireContext(),
            R.array.sort_items,
            android.R.layout.simple_spinner_item
        )
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

    private fun subscribeToObserver() {
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

        binding.homeFragmentRv.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    binding.homeFragmentFab.show()
                }

                super.onScrollStateChanged(recyclerView, newState)
            }

            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                if (dy > 0 || dy < 0 && binding.homeFragmentFab.isShown) {
                    binding.homeFragmentFab.hide()
                }
            }
        })

        val helper = ItemTouchHelper(
            object : ItemTouchHelper.SimpleCallback(
                0,
                ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
            ) {
                // Not needed
                override fun onMove(
                    recyclerView: RecyclerView,
                    viewHolder: RecyclerView.ViewHolder,
                    target: RecyclerView.ViewHolder
                ): Boolean {
                    return false
                }

                // delete run from db when swiped
                override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                    val position = viewHolder.adapterPosition
                    val runEntry: RunEntry = homeRecyclerViewAdapter.getRunEntry(position)

                    viewLifecycleOwner.lifecycleScope.launch {
                        homeViewModel.deleteRun(runEntry)
                        showSnackBar("Run deleted successful!")
                    }
                }
            })

        helper.attachToRecyclerView(binding.homeFragmentRv)
    }

    private fun showSnackBar(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG).show()
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