package xyz.thingapps.cocoasearch.ui

import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.item_network_state.view.*
import xyz.thingapps.cocoasearch.R
import xyz.thingapps.cocoasearch.repository.NetworkState
import xyz.thingapps.cocoasearch.repository.Status


class NetworkStateItemViewHolder(view: View,
                                 private val retryCallback: () -> Unit)
    : RecyclerView.ViewHolder(view) {

    fun bind(itemView: View, networkState: NetworkState?) {
        with(itemView) {
            loadingProgressBar.visibility = toVisibility(networkState?.status == Status.RUNNING)
            retryLoadingButton.visibility = toVisibility(networkState?.status == Status.FAILED)
            errorMessageTextView.visibility = toVisibility(networkState?.msg != null)
            errorMessageTextView.text = networkState?.msg

            retryLoadingButton.setOnClickListener {
                retryCallback()
            }
        }
    }

    companion object {
        fun create(parent: ViewGroup, retryCallback: () -> Unit): NetworkStateItemViewHolder {
            val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_network_state, parent, false)
            return NetworkStateItemViewHolder(view, retryCallback)
        }

        fun toVisibility(constraint: Boolean): Int {
            return if (constraint) {
                View.VISIBLE
            } else {
                View.GONE
            }
        }
    }
}
