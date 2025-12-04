package com.example.fakeandroidcam.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.CompoundButton
import androidx.recyclerview.widget.RecyclerView
import com.example.fakeandroidcam.databinding.ItemAppBinding
import com.example.fakeandroidcam.util.AppEntry

class AppScopeAdapter(
    private val items: List<AppEntry>,
    private val isChecked: (String) -> Boolean,
    private val onToggle: (String, Boolean) -> Unit
) : RecyclerView.Adapter<AppScopeAdapter.ViewHolder>() {

    inner class ViewHolder(private val binding: ItemAppBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(entry: AppEntry) {
            binding.appName.text = entry.label
            binding.appToggle.setOnCheckedChangeListener(null)
            binding.appToggle.isChecked = isChecked(entry.packageName)
            binding.appToggle.setOnCheckedChangeListener { _: CompoundButton, checked: Boolean ->
                onToggle(entry.packageName, checked)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemAppBinding.inflate(inflater, parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size
}
