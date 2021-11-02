package com.example.runningtracker.ui

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.example.runningtracker.R
import com.example.runningtracker.databinding.FragmentBottomDialogBinding
import com.example.runningtracker.viewmodel.BottomDialogViewModel
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class BottomDialogFragment : BottomSheetDialogFragment() {
    private lateinit var binding: FragmentBottomDialogBinding
    private val bottomDialogViewModel by viewModels<BottomDialogViewModel>()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentBottomDialogBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = this
        binding.viewModel = bottomDialogViewModel
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setListeners()
    }

    private fun setListeners() {
        binding.stopWatchStartBtn.setOnClickListener {
            if (binding.stopWatchStartBtn.text == getString(R.string.start_text)) {
                bottomDialogViewModel.start()
                binding.stopWatchStartBtn.text = getString(R.string.pause_text)
            } else {
                bottomDialogViewModel.pause()
                hideStartButton()
                showButtons()
            }

        }

        binding.stopWatchResumeBtn.setOnClickListener {
            bottomDialogViewModel.resume()
        }

        binding.stopWatchFinishRun.setOnClickListener {
            bottomDialogViewModel.finish()
        }
    }

    private fun hideStartButton() {
        binding.stopWatchStartBtn.visibility = View.GONE
    }

    private fun showButtons() {
        binding.stopWatchResumeBtn.visibility = View.VISIBLE
        binding.stopWatchFinishRun.visibility = View.VISIBLE
    }

    companion object {
        @JvmStatic
        fun newInstance() = BottomDialogFragment()
    }
}