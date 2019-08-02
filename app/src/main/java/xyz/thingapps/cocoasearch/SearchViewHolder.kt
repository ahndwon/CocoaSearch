package xyz.thingapps.cocoasearch

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.item_search_result.view.*
import xyz.thingapps.cocoasearch.net.Document
import java.util.*

class SearchViewHolder(private val view : View) : RecyclerView.ViewHolder(view) {
    private val random = Random()

    fun bind(item : Document) {
        view.searchImageView.layoutParams.height = getRandomIntInRange(400, 300)

        Glide.with(view)
                .load(item.imageUrl)
//                .load(item.thumbnailUrl)
                .into(view.searchImageView)
    }

    private fun getRandomIntInRange(max: Int, min: Int): Int {
        return random.nextInt(max - min + min) + min
    }
}