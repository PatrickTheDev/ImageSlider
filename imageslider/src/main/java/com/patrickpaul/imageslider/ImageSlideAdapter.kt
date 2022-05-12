package com.patrickpaul.imageslider

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.patrickpaul.imageslider.databinding.ImageSliderItemBinding

class ImageSlideAdapter : RecyclerView.Adapter<ImageSlideAdapter.ViewHolder>() {

    var items: MutableList<ImageSliderItem> = mutableListOf()
        @SuppressLint("NotifyDataSetChanged")
        set(value) {
            field.clear()
            field.addAll(value)
            notifyDataSetChanged()
        }

    var clickListener: ItemOnClickListener? = null
    var longClickListener: ItemOnLongClickListener? = null

    var errorImage: Int = 0
    var placeholder: Int = 0
    var background: Int = 0
    var withTitle: Boolean = false
    var withBackground: Boolean = false

    var titleAlignment: Int = 0
    var titleColor: Int = -0x10000
    var scaleType: ImageView.ScaleType? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ImageSliderItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int {
        return items.size
    }

    inner class ViewHolder(private val binding: ImageSliderItemBinding)
        : RecyclerView.ViewHolder(binding.root) {

            fun bind(item: ImageSliderItem) {
                with(binding) {
                    slideImage.load(item.imageUri) {
                        placeholder(placeholder)
                        error(errorImage)
                    }
                    if (this@ImageSlideAdapter.scaleType != null)
                        slideImage.scaleType = this@ImageSlideAdapter.scaleType

                    if (withTitle && !(item.description.isNullOrEmpty())) {
                        titleText.apply {
                            visibility = View.VISIBLE
                            text = item.description
                            gravity = textAlignment
                            setTextColor(titleColor)
                        }
                    } else
                        titleText.visibility = View.GONE

                    if (withBackground) {
                        titleContainer.apply {
                            visibility = View.VISIBLE
                            setBackgroundResource(this@ImageSlideAdapter.background)
                        }
                    } else
                        titleContainer.visibility = View.GONE

                    root.setOnClickListener {
                        clickListener?.onClick(item, adapterPosition)
                    }
                }
            }

    }

}