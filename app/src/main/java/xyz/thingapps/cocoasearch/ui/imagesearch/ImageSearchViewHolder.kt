package xyz.thingapps.cocoasearch.ui.imagesearch

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import xyz.thingapps.cocoasearch.R
import xyz.thingapps.cocoasearch.databinding.ItemSearchResultBinding
import xyz.thingapps.cocoasearch.utils.GlideRequests
import xyz.thingapps.cocoasearch.vo.Document


class ImageSearchViewHolder(view: View, private val glide: GlideRequests)
    : RecyclerView.ViewHolder(view) {

    private val binding: ItemSearchResultBinding? = DataBindingUtil.bind(view)

    var item: Document? = null
    var onClick: ((Document) -> Unit)? = null

    fun bind(itemView: View, item: Document?) {
        binding?.document = item

        this.item = item
        with(itemView) {
            item?.let { item ->
                setOnClickListener {
                    onClick?.invoke(item)
                }
            }
        }
    }

//    fun updateImage(itemView: View, item: Document?) {
//        this.item = item
//        item?.let { showImage(itemView.searchImageView, item.imageUrl) }
//    }

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