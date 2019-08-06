package xyz.thingapps.cocoasearch.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.load.resource.bitmap.BitmapTransitionOptions
import kotlinx.android.synthetic.main.item_search_result.view.*
import xyz.thingapps.cocoasearch.R
import xyz.thingapps.cocoasearch.net.Document
import xyz.thingapps.cocoasearch.utils.GlideRequests


class ImageSearchViewHolder(view: View, private val glide: GlideRequests)
    : RecyclerView.ViewHolder(view) {

    private val displayMetrics = view.context.resources.displayMetrics
    var onClick: ((Document) -> Unit)? = null

    fun bind(itemView: View, item: Document?) {
        with(itemView) {
            item?.let { item ->
                scaleHeight(searchImageView, item.height, item.width)
                showImage(searchImageView, item.imageUrl)

                setOnClickListener {
                    onClick?.invoke(item)
                }
            }
        }
    }

    private fun scaleHeight(imageView: ImageView, itemHeight: Int, itemWidth: Int) {
        val scale = itemHeight.toFloat() / itemWidth.toFloat()
        val holderWidth = (displayMetrics.widthPixels - 20) / 2
        val viewHeight = holderWidth * scale
        imageView.layoutParams.height = viewHeight.toInt()
    }

    private fun showImage(imageView: ImageView, url: String) {
        if (url.startsWith("http")) {
            glide.asBitmap()
                    .load(url)
                    .centerCrop()
                    .transition(BitmapTransitionOptions.withCrossFade())
                    .into(imageView)
        }
    }

    companion object {
        fun create(
                parent: ViewGroup,
                glide: GlideRequests,
                onClick: ((Document) -> Unit)?
        ): ImageSearchViewHolder {
            val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_search_result, parent, false)
            return ImageSearchViewHolder(view, glide).apply {
                this.onClick = onClick
            }
        }
    }
}