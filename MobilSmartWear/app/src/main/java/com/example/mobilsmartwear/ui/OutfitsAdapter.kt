package com.example.mobilsmartwear.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.mobilsmartwear.R
import com.example.mobilsmartwear.model.Outfit

class OutfitsAdapter : ListAdapter<Outfit, OutfitsAdapter.OutfitViewHolder>(OutfitDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OutfitViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_outfit, parent, false)
        return OutfitViewHolder(view)
    }

    override fun onBindViewHolder(holder: OutfitViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class OutfitViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val nameTextView: TextView = itemView.findViewById(R.id.outfitName)
        private val descriptionTextView: TextView = itemView.findViewById(R.id.outfitDescription)
        private val imageView: ImageView = itemView.findViewById(R.id.outfitImage)

        fun bind(outfit: Outfit) {
            nameTextView.text = outfit.name
            descriptionTextView.text = outfit.description
            Glide.with(itemView.context)
                .load(outfit.imageUrl)
                .into(imageView)
        }
    }

    private class OutfitDiffCallback : DiffUtil.ItemCallback<Outfit>() {
        override fun areItemsTheSame(oldItem: Outfit, newItem: Outfit): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Outfit, newItem: Outfit): Boolean {
            return oldItem == newItem
        }
    }
} 