package com.patrickpaul.imageslider

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.view.get
import androidx.viewpager2.widget.ViewPager2
import com.patrickpaul.imageslider.databinding.ImageSliderBinding
import java.util.*
import kotlin.concurrent.timerTask


class ImageSlider @JvmOverloads constructor(
    context: Context,
    attributeSet: AttributeSet,
    defStyleAttr: Int = 0
) : ConstraintLayout(context, attributeSet, defStyleAttr) {

    private var binding: ImageSliderBinding =
        ImageSliderBinding.inflate(LayoutInflater.from(context), this, true)
    private var viewpager: ViewPager2? = null
    private var adapter: ImageSlideAdapter? = null

    private var pagerdots: LinearLayout? = null
    private var dots: Array<ImageView?>? = null
    private var currentPage = 0
    private var imageCount = 0
    private var indicatorGravity: Int = 0x11
    private var selectedDot: Int = 0
    private var unselectedDot: Int = 0

    private var placeholder: Int = 0
    private var errorImage: Int = 0
    private var scaleType: ImageView.ScaleType = ImageView.ScaleType.FIT_CENTER
    private var withTitle: Boolean = false
    private var withBackground: Boolean = false
    private var titleColor: Int = -0x1
    private var titleGravity: Int = 0x00800003 or 0x10
    private var titleBackground: Int = 0

    private var autoCycle: Boolean = false
    private var period: Long = 0L
    private var delay: Long = 0L
    private var timer = Timer()

    private val scaleTypeArray = arrayOf(
        ImageView.ScaleType.MATRIX,
        ImageView.ScaleType.FIT_XY,
        ImageView.ScaleType.FIT_START,
        ImageView.ScaleType.FIT_CENTER,
        ImageView.ScaleType.FIT_END,
        ImageView.ScaleType.CENTER,
        ImageView.ScaleType.CENTER_CROP,
        ImageView.ScaleType.CENTER_INSIDE
    )

    init {
        viewpager = findViewById(R.id.view_pager)
        pagerdots = findViewById(R.id.pager_dots)
        adapter = ImageSlideAdapter()

        val typedArray = context.theme.obtainStyledAttributes(
            attributeSet,
            R.styleable.ImageSlider,
            defStyleAttr,
            defStyleAttr
        )

        with(typedArray) {
            selectedDot = getResourceId(R.styleable.ImageSlider_selectedDot, R.drawable.indicator_active)
            unselectedDot = getResourceId(R.styleable.ImageSlider_unselectedDot, R.drawable.indicator_inactive)
            indicatorGravity = getInt(R.styleable.ImageSlider_indicatorGravity, 0x11)

            placeholder = getResourceId(R.styleable.ImageSlider_placeholder, R.drawable.ic_image_placeholder)
            errorImage = getResourceId(R.styleable.ImageSlider_errorImage, R.drawable.ic_image_broken)
            scaleType = scaleTypeArray[getInt(R.styleable.ImageSlider_scaleType, ImageView.ScaleType.FIT_CENTER.ordinal)]
            withTitle = getBoolean(R.styleable.ImageSlider_withTitle, false)
            withBackground = getBoolean(R.styleable.ImageSlider_withBackground, false)
            titleColor = getColor(R.styleable.ImageSlider_titleColor, -0x1)
            titleGravity = getInt(R.styleable.ImageSlider_titleGravity, 0x00800003 or 0x10)

            autoCycle = getBoolean(R.styleable.ImageSlider_autoCycle, false)
            period = getInt(R.styleable.ImageSlider_cyclePeriod, 1000).toLong()
            delay = getInt(R.styleable.ImageSlider_cycleDelay, 1000).toLong()
        }
    }

    fun setImageList(list: List<ImageSliderItem>) {
        viewpager?.adapter = adapter
        if (list.isNotEmpty()) {
            imageCount = list.size
            Log.d("LIST", "List contained ${list.size} items.")
            adapter?.apply {
                items = list as MutableList<ImageSliderItem>
                errorImage = this@ImageSlider.errorImage
                placeholder = this@ImageSlider.placeholder
                withTitle = this@ImageSlider.withTitle
                withBackground = this@ImageSlider.withBackground
                background = this@ImageSlider.titleBackground
                titleAlignment = this@ImageSlider.titleGravity
                titleColor = this@ImageSlider.titleColor
                scaleType = this@ImageSlider.scaleType
            }
            setupDots(list.size)
            setCurrentIndicator(currentPage)
            if (autoCycle) {
                stopSliding()
                startSliding()
            }
        }
    }

    private fun setupDots(size: Int) {
        /*
        println(indicatorGravity)
        pagerdots?.gravity = indicatorGravity
        pagerdots?.removeAllViews()
        dots = arrayOfNulls(size)
        val params: LinearLayout.LayoutParams =
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        params.setMargins(8, 0, 8, 0)

        for (i in 0 until size) {
            dots!![i] = ImageView(context)
            pagerdots?.addView(dots!![i], params)
        }
        viewpager?.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                currentPage = position
                setCurrentIndicator(currentPage)
            }
        })
         */
        pagerdots?.apply {
            gravity = indicatorGravity
            removeAllViews()
        }
        dots = arrayOfNulls(size)
        val params = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(8, 0, 8, 0)
            }
        for (i in 0 until size) {
            dots?.let {
                it[i] = ImageView(context)
                pagerdots?.addView(it[i], params)
            }
        }
        viewpager?.registerOnPageChangeCallback(object: ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                currentPage = position
                setCurrentIndicator(currentPage)
            }
        })
    }

    private fun setCurrentIndicator(index: Int) {
        val childCount = pagerdots!!.childCount

        for (i in 0 until childCount) {
            val imageView = pagerdots!![i] as ImageView
            imageView.setImageDrawable(
                ContextCompat.getDrawable(
                    context,
                    R.drawable.indicator_selector
                )
            )
            imageView.isSelected = i == index
        }
    }

    private fun scheduleTimer(period: Long) {
        val handler = Handler(Looper.getMainLooper())
        val update = Runnable {
            if (currentPage == imageCount) {
                currentPage = 0
            }
            viewpager?.setCurrentItem(currentPage++, true)
        }
        timer = Timer().apply {
            schedule(
                timerTask { handler.post(update) },
                delay,
                period
            )
        }
    }

    private fun startSliding(changeablePeriod: Long = period) {
        stopSliding()
        scheduleTimer(changeablePeriod)
    }

    private fun stopSliding() {
        timer.run {
            cancel()
            purge()
        }
    }

    fun setItemOnClickListener(listener: ItemOnClickListener) {
        adapter?.clickListener = listener
    }

    fun setItemOnLongClickListener(listener: ItemOnLongClickListener) {
        adapter?.longClickListener = listener
    }

}