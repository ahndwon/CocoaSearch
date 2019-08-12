package xyz.thingapps.cocoasearch.utils

import android.graphics.drawable.Drawable
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target
import xyz.thingapps.cocoasearch.R

class GlideUtil {
    companion object {

        private const val THUMBNAIL_VALUE = 0.3f

        private val CENTER_CROP_REQUEST_OPTIONS = RequestOptions()
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .error(R.drawable.ic_insert_photo_black_48dp)

        private val CENTER_INSIDE_REQUEST_OPTIONS = RequestOptions()
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .error(R.drawable.ic_insert_photo_black_48dp)

        fun cancel(imageView: ImageView) {
            Glide.with(imageView.context).clear(imageView)
        }

        fun loadWithCenterCrop(imageView: ImageView, imageUrl: String?) {
            load(imageView, imageUrl, CENTER_CROP_REQUEST_OPTIONS, null)
        }

        fun loadWithCenterInside(imageView: ImageView, imageUrl: String?) {
            load(imageView, imageUrl, CENTER_INSIDE_REQUEST_OPTIONS, null)
        }

        private fun load(imageView: ImageView,
                         imageUrl: String?,
                         requestOptions: RequestOptions,
                         progressBar: ProgressBar?) {

            progressBar?.let { it.visibility = View.VISIBLE }

            Glide.with(imageView.context)
                    .load(imageUrl)
                    .thumbnail(THUMBNAIL_VALUE)
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .apply(requestOptions)
                    .listener(object : RequestListener<Drawable> {
                        override fun onLoadFailed(
                                e: GlideException?,
                                model: Any?,
                                target: Target<Drawable>?,
                                isFirstResource: Boolean
                        ): Boolean {
                            progressBar?.let { it.visibility = View.GONE }
                            imageView.scaleType = ImageView.ScaleType.CENTER

                            return false
                        }

                        override fun onResourceReady(
                                resource: Drawable?,
                                model: Any?,
                                target: Target<Drawable>?,
                                dataSource: DataSource?,
                                isFirstResource: Boolean
                        ): Boolean {
                            progressBar?.let { it.visibility = View.GONE }
                            imageView.scaleType = ImageView.ScaleType.FIT_XY

                            return false
                        }
                    }).into(imageView)
        }
    }
}