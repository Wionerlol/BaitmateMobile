package com.example.baitmatemobile.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.baitmatemobile.databinding.ItemCatchRecordBinding
import com.example.baitmatemobile.model.CatchRecordDTO

class CatchRecordAdapter(private val catchRecords: List<CatchRecordDTO>) :
    RecyclerView.Adapter<CatchRecordAdapter.CatchRecordViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CatchRecordViewHolder {
        val binding = ItemCatchRecordBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CatchRecordViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CatchRecordViewHolder, position: Int) {
        val catchRecord = catchRecords[position]
        holder.bind(catchRecord)
    }

    override fun getItemCount(): Int = catchRecords.size

    class CatchRecordViewHolder(private val binding: ItemCatchRecordBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(catchRecord: CatchRecordDTO) {
            // Bind data to the views in the layout
            binding.tvCatchTime.text = catchRecord.time
            binding.tvCatchLength.text = "Length: ${catchRecord.length} cm"
            binding.tvCatchWeight.text = "Weight: ${catchRecord.weight} kg"
            binding.tvCatchRemark.text = catchRecord.remark

            // Optionally load the image using a library like Glide or Picasso
            Glide.with(binding.root.context)
                .load(catchRecord.image)
                .into(binding.ivCatchImage)
        }
    }
}