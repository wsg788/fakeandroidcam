package com.example.fakeandroidcam.ui

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.example.fakeandroidcam.R
import com.example.fakeandroidcam.databinding.FragmentStatusBinding
import com.example.fakeandroidcam.util.ModuleInstaller
import com.example.fakeandroidcam.util.RootHelper

class StatusFragment : Fragment() {

    private var _binding: FragmentStatusBinding? = null
    private val binding get() = _binding!!
    private var selectedZip: Uri? = null

    private val chooseZip = registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
        selectedZip = uri
        uri?.let { binding.statusLog.text = "Selected module: $it" }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentStatusBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        renderRootStatus()
        binding.installModuleButton.setOnClickListener { installModule() }
        binding.chooseModuleZip.setOnClickListener { chooseZip.launch(arrayOf("application/zip")) }
    }

    private fun renderRootStatus() {
        val rootStatus = if (RootHelper.isDeviceRooted()) R.string.root_detected else R.string.root_not_detected
        val magiskStatus = if (RootHelper.isMagiskInstalled(requireContext())) R.string.magisk_detected else R.string.magisk_not_detected
        binding.rootStatus.setText(rootStatus)
        binding.magiskStatus.setText(magiskStatus)
    }

    private fun installModule() {
        val uri = selectedZip
        if (uri == null) {
            Toast.makeText(requireContext(), "Select a module ZIP first", Toast.LENGTH_SHORT).show()
            return
        }
        val installer = ModuleInstaller(requireContext())
        val result = installer.installModule(uri)
        val message = if (result) "Module installation requested" else "Module install failed"
        binding.statusLog.text = message
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
