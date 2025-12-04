package com.example.fakeandroidcam.ui

import android.content.pm.ApplicationInfo
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.fakeandroidcam.databinding.FragmentScopeBinding
import com.example.fakeandroidcam.util.AppEntry
import com.example.fakeandroidcam.util.ConfigStore

class ScopeFragment : Fragment() {

    private var _binding: FragmentScopeBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentScopeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.appList.layoutManager = LinearLayoutManager(requireContext())
        binding.appList.adapter = AppScopeAdapter(loadApps(), ::isChecked) { pkg, enabled ->
            if (enabled) ConfigStore.scopedPackages.add(pkg) else ConfigStore.scopedPackages.remove(pkg)
        }
    }

    private fun isChecked(pkg: String): Boolean = ConfigStore.scopedPackages.contains(pkg)

    private fun loadApps(): List<AppEntry> {
        val pm = requireContext().packageManager
        val installed = pm.getInstalledApplications(0)
        return installed
            .filter { it.flags and ApplicationInfo.FLAG_SYSTEM == 0 }
            .map {
                val label = pm.getApplicationLabel(it).toString()
                AppEntry(it.packageName, label)
            }
            .sortedBy { it.label }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
