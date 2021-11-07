package com.example.runningtracker.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.example.runningtracker.R
import com.example.runningtracker.databinding.FragmentSettingsBinding
import com.example.runningtracker.viewmodel.SettingsViewModel
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SettingsFragment : Fragment() {

    private lateinit var settingBinding: FragmentSettingsBinding
    private val settingsViewModel: SettingsViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        settingBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_settings, container, false)
        readData()
        return settingBinding.root
    }

    private fun showSnackbar(view: View, message: String) {
        Snackbar.make(view, message, Snackbar.LENGTH_LONG).show()
    }

    private fun readData() {
        viewLifecycleOwner.lifecycleScope.launch {
            settingsViewModel.getUser().flowWithLifecycle(viewLifecycleOwner.lifecycle).collect {
                settingBinding.settingsNameEt.setText(it.name)
                settingBinding.settingsWeightEt.setText(it.weight.toString())
            }
        }
    }

    private fun applyChanges(): Boolean {
        val name = settingBinding.settingsNameEt.text.toString()
        val weight = settingBinding.settingsWeightEt.text.toString()

        return settingsViewModel.saveChanges(name, weight)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        settingBinding.settingsSaveBtn.setOnClickListener {
            val success = applyChanges()
            if (success) {
                showSnackbar(view, "Changes saved!")
            } else {
                showSnackbar(view, "Please fill all field.")
            }
        }
    }
}