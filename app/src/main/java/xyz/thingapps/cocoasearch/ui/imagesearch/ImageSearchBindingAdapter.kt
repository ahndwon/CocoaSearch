package xyz.thingapps.cocoasearch.ui.imagesearch

import android.widget.ImageView
import androidx.databinding.BindingAdapter
import xyz.thingapps.cocoasearch.utils.GlideUtil

object ImageSearchBindingAdapter {

    @BindingAdapter("app:loadImageWithCenterCrop")
    @JvmStatic
    fun loadImageWithCenterCrop(imageView: ImageView, imageUrl: String?) {
        GlideUtil.loadWithCenterCrop(imageView, imageUrl)
    }

    @BindingAdapter(value = ["bind:itemHeight", "bind:itemWidth"])
    @JvmStatic
    fun heightByImageRatio(imageView: ImageView, itemHeight: Int, itemWidth: Int) {
        val scale = itemHeight.toFloat() / itemWidth.toFloat()
        val displayMetrics = imageView.context.resources.displayMetrics
        val holderWidth = (displayMetrics.widthPixels - 20) / 2
        val viewHeight = holderWidth * scale
        imageView.layoutParams.height = viewHeight.toInt()
    }
}