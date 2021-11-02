package com.example.runningtracker.ui

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.example.runningtracker.R
import com.example.runningtracker.databinding.FragmentSetupBinding
import com.example.runningtracker.viewmodel.SetupViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch


@AndroidEntryPoint
class SetupFragment : Fragment() {

    private lateinit var binding: FragmentSetupBinding
    private val setupViewModel by viewModels<SetupViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_setup, container, false)
        binding.setupProceedBtn.setOnClickListener { saveUser() }
        binding.setupNameEt.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                if (p0.toString().length > 3) {
                    binding.setupNameEt.error = null
                }
            }

            override fun afterTextChanged(p0: Editable?) {
            }

        })

        binding.setupWeightEt.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                if (p0.toString() != "0" && p0.toString().isEmpty()) {
                    binding.setupWeightEt.error = null
                }
            }

            override fun afterTextChanged(p0: Editable?) {

            }

        })
        return binding.root
    }

    private fun saveUser() {
        val name = binding.setupNameEt.text.toString()
        val weight = binding.setupWeightEt.text.toString()

        val errorMessage = setupViewModel.validateInput(name, weight)
        if (errorMessage.isNullOrEmpty()) {
            viewLifecycleOwner.lifecycleScope.launch {
                setupViewModel.storeData(name, weight)
            }
        } else {
            if (errorMessage.contains("name")) {
                binding.setupNameEt.error = errorMessage
            } else {
                binding.setupWeightEt.error = errorMessage
            }
        }
    }


}