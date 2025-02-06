package com.example.baitmatemobile.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import androidx.core.net.toUri
import com.bumptech.glide.Glide
import com.example.baitmatemobile.R

data class FishResult(val fishId: Long, val fishName: String, val confidence: Int, val imageUrl: String)

class FishResultAdapter(context: Context, private val fishResults: List<FishResult>) :
    ArrayAdapter<FishResult>(context, 0, fishResults) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: LayoutInflater.from(context).inflate(R.layout.list_item, parent, false)
        val fishResult = getItem(position)

        val imageView = view.findViewById<ImageView>(R.id.imageView)
        val textView = view.findViewById<TextView>(R.id.textView)

        textView.text = "${fishResult?.fishName}: ${fishResult?.confidence}%"
        Glide.with(context).load(fishResult?.imageUrl?.toUri()).into(imageView)

        return view
    }
}