package com.example.fakeandroidcam.ui

import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.MediaController
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.fakeandroidcam.databinding.FragmentMediaBinding
import com.example.fakeandroidcam.util.ConfigStore
import com.example.fakeandroidcam.util.ConfigWriter

class MediaFragment : Fragment() {

    private var _binding: FragmentMediaBinding? = null
    private val binding get() = _binding!!

    private val pickMedia = registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
        uri?.let { handleMediaSelection(it) }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMediaBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.selectMediaButton.setOnClickListener { requestPermissionAndPick() }
        binding.enableSwitch.isChecked = ConfigStore.enabled
        binding.enableSwitch.setOnCheckedChangeListener { _, checked -> ConfigStore.enabled = checked }
        binding.saveConfigButton.setOnClickListener { saveConfig() }
    }

    private fun requestPermissionAndPick() {
        val permission = android.Manifest.permission.READ_EXTERNAL_STORAGE
        if (ContextCompat.checkSelfPermission(requireContext(), permission) == PackageManager.PERMISSION_GRANTED) {
            pickMedia.launch(arrayOf("image/*", "video/*"))
        } else {
            requestPermissions(arrayOf(permission), 101)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 101 && grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
            pickMedia.launch(arrayOf("image/*", "video/*"))
        } else {
            Toast.makeText(requireContext(), "Permission needed to select media", Toast.LENGTH_SHORT).show()
        }
    }

    private fun handleMediaSelection(uri: Uri) {
        ConfigStore.selectedMedia = uri
        val mime = requireContext().contentResolver.getType(uri) ?: ""
        if (mime.startsWith("image/")) {
            binding.imagePreview.visibility = View.VISIBLE
            binding.videoPreview.visibility = View.GONE
            binding.imagePreview.setImageURI(uri)
        } else if (mime.startsWith("video/")) {
            binding.videoPreview.visibility = View.VISIBLE
            binding.imagePreview.visibility = View.GONE
            binding.videoPreview.setVideoURI(uri)
            val controller = MediaController(requireContext())
            controller.setAnchorView(binding.videoPreview)
            binding.videoPreview.setMediaController(controller)
            binding.videoPreview.start()
        }
    }

    private fun saveConfig() {
        val config = ConfigWriter.buildConfig(ConfigStore.selectedMedia, ConfigStore.enabled, ConfigStore.scopedPackages)
        val success = ConfigWriter.writeConfig(requireContext(), config)
        val message = if (success) "Config saved to system" else "Config broadcasted"
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
