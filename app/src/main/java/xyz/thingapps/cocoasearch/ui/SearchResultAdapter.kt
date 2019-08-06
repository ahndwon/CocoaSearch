package xyz.thingapps.cocoasearch.ui

import android.view.ViewGroup
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import xyz.thingapps.cocoasearch.R
import xyz.thingapps.cocoasearch.net.Document
import xyz.thingapps.cocoasearch.repository.NetworkState
import xyz.thingapps.cocoasearch.utils.GlideRequests


class SearchResultAdapter(
        private val glide: GlideRequests,
        private val retryCallback: () -> Unit)
    : PagedListAdapter<Document, RecyclerView.ViewHolder>(POST_COMPARATOR) {
    private var networkState: NetworkState? = null
    var onClick: ((Document) -> Unit)? = null

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (getItemViewType(position)) {
            R.layout.item_search_result -> (holder as ImageSearchViewHolder).bind(holder.itemView, getItem(position))
            R.layout.item_network_state -> (holder as NetworkStateItemViewHolder).bind(holder.itemView,
                    networkState)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            R.layout.item_search_result -> ImageSearchViewHolder.create(parent, glide, onClick)
            R.layout.item_network_state -> NetworkStateItemViewHolder.create(parent, retryCallback)
            else -> throw IllegalArgumentException("unknown view type $viewType")
        }
    }

    private fun hasExtraRow() = networkState != null && networkState != NetworkState.LOADED

    override fun getItemViewType(position: Int): Int {
        return if (hasExtraRow() && position == itemCount - 1) {
            R.layout.item_network_state
        } else {
            R.layout.item_search_result
        }
    }

    override fun getItemCount(): Int {
        return super.getItemCount() + if (hasExtraRow()) 1 else 0
    }

    fun setNetworkState(newNetworkState: NetworkState?) {
        val previousState = this.networkState
        val hadExtraRow = hasExtraRow()
        this.networkState = newNetworkState
        val hasExtraRow = hasExtraRow()
        if (hadExtraRow != hasExtraRow) {
            if (hadExtraRow) {
                notifyItemRemoved(super.getItemCount())
            } else {
                notifyItemInserted(super.getItemCount())
            }
        } else if (hasExtraRow && previousState != newNetworkState) {
            notifyItemChanged(itemCount - 1)
        }
    }

    companion object {
        val POST_COMPARATOR = object : DiffUtil.ItemCallback<Document>() {
            override fun areContentsTheSame(oldItem: Document, newItem: Document): Boolean =
                    oldItem == newItem

            override fun areItemsTheSame(oldItem: Document, newItem: Document): Boolean =
                    oldItem.imageUrl == newItem.imageUrl
        }
    }
}
