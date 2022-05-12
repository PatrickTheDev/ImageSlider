package com.patrickpaul.imageslider

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.patrickpaul.imageslider.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val images = ArrayList<ImageSliderItem>().apply {
            addAll(
                listOf(
                    ImageSliderItem(
                        "https://images.pexels.com/photos/54186/tulips-flowers-tulip-bouquet-violet-54186.jpeg?auto=compress&cs=tinysrgb&dpr=2&w=500",
                        "Tulips"
                    ),
                    ImageSliderItem(
                        "https://images.pexels.com/photos/7427460/pexels-photo-7427460.jpeg?auto=compress&cs=tinysrgb&dpr=2&w=500",
                        "Geraniums"
                    ),
                    ImageSliderItem(
                        "https://images.pexels.com/photos/2898430/pexels-photo-2898430.jpeg?auto=compress&cs=tinysrgb&dpr=2&w=500",
                        "Chrysanthemums"
                    )
                )
            )
        }

        with(binding) {
            imageSlider.apply {
                setImageList(images)
                setItemOnClickListener(object: ItemOnClickListener {
                    override fun onClick(item: ImageSliderItem, position: Int) {
                        TODO("Not yet implemented")
                    }
                })
                setItemOnLongClickListener(object: ItemOnLongClickListener {
                    override fun onLongClick(item: ImageSliderItem, position: Int) {
                        Toast.makeText(context, item.description, Toast.LENGTH_LONG).show()
                    }
                })
            }
        }
    }
}