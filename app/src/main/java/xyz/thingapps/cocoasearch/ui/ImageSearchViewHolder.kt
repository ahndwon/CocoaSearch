package xyz.thingapps.cocoasearch.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.item_search_result.view.*
import xyz.thingapps.cocoasearch.R
import xyz.thingapps.cocoasearch.net.Document
import xyz.thingapps.cocoasearch.utils.GlideRequests
import java.util.*


class ImageSearchViewHolder(view: View, private val glide: GlideRequests)
    : RecyclerView.ViewHolder(view) {

    private var item: Document? = null
    private val random = Random()
    var onClick: ((Document) -> Unit)? = null

    fun bind(itemView: View, item: Document?) {
        this.item = item
        with(itemView) {
            searchImageView.layoutParams.height = getRandomIntInRange(400, 300)

            item?.let { item ->
                if (item.imageUrl.startsWith("http")) {
                    glide.load(item.imageUrl)
                            .centerCrop()
                            .placeholder(R.drawable.ic_insert_photo_black_48dp)
                            .into(searchImageView)
                }

                setOnClickListener {
                    onClick?.invoke(item)
                }
            }


//                item?.docUrl?.let { url ->
//                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
//                    context.startActivity(intent)
//                }
//            }
        }
    }

    private fun getRandomIntInRange(max: Int, min: Int): Int {
        return random.nextInt(max - min + min) + min
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