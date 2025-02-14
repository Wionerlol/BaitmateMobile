package com.example.baitmatemobile.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.baitmatemobile.databinding.ItemCatchRecordBinding
import com.example.baitmatemobile.model.CatchRecord
import com.example.baitmatemobile.network.RetrofitClient

class CatchRecordAdapter(private val catchRecords: List<CatchRecord>) :
    RecyclerView.Adapter<CatchRecordAdapter.CatchRecordViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CatchRecordViewHolder {
        val binding = ItemCatchRecordBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CatchRecordViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CatchRecordViewHolder, position: Int) {
        val catchRecord = catchRecords[position]
        Log.d("CatchRecordAdapter", "Binding data for record ID: ${catchRecord.id}")
        holder.bind(catchRecord)
    }

    override fun getItemCount(): Int = catchRecords.size

    class CatchRecordViewHolder(private val binding: ItemCatchRecordBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(catchRecord: CatchRecord) {
            binding.tvFishName.text = catchRecord.fishName?: "Unknown"
            binding.tvLocation.text = catchRecord.locationName?: "Unknown"
            binding.tvCatchTime.text = catchRecord.time
            binding.tvCatchLength.text = "Length: ${catchRecord.length} cm"
            binding.tvCatchWeight.text = "Weight: ${catchRecord.weight} kg"

            Glide.with(binding.root.context)
                .load(RetrofitClient.retrofit.baseUrl().toString() +"catch-records/${catchRecord.id}/image")
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(binding.ivCatchImage)
        }
    }
}