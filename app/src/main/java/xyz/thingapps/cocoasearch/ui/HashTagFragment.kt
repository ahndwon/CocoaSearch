package xyz.thingapps.cocoasearch.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.paging.PagedList
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.fragment_hash_tag.view.*
import xyz.thingapps.cocoasearch.R
import xyz.thingapps.cocoasearch.repository.NetworkState
import xyz.thingapps.cocoasearch.ui.imagedetail.ImageDetailFragment
import xyz.thingapps.cocoasearch.ui.imagesearch.ImageSearchResultAdapter
import xyz.thingapps.cocoasearch.ui.viewmodels.HashTagViewModel
import xyz.thingapps.cocoasearch.utils.GlideApp
import xyz.thingapps.cocoasearch.utils.GridItemDecoration
import xyz.thingapps.cocoasearch.utils.ServiceLocator
import xyz.thingapps.cocoasearch.vo.Document

class HashTagFragment : Fragment() {

    companion object {
        const val HASH_TAG = "hash_tag"

        fun newInstance(hashTag: String): HashTagFragment {
            val fragment = HashTagFragment()
            val args = Bundle()
            args.putString(HASH_TAG, hashTag)
            fragment.arguments = args
            return fragment
        }
    }

    private val disposeBag = CompositeDisposable()
    private lateinit var model: HashTagViewModel
    var listener: TitleFragmentLifeListener? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_search, container, false)
        setHasOptionsMenu(true)
        val hashTag = arguments?.getString(HASH_TAG) ?: ""
        listener?.onBirth(hashTag)
        model = getViewModel(hashTag)
        initAdapter(view)
        initSwipeToRefresh(view)
        return view
    }

    private fun getViewModel(searchWord: String): HashTagViewModel {
        return ViewModelProviders.of(this, object : ViewModelProvider.Factory {
            override fun <T : ViewModel?> create(modelClass: Class<T>): T {
                val repo =
                        ServiceLocator.instance()
                                .getRepository()

                @Suppress("UNCHECKED_CAST")
                return HashTagViewModel(searchWord, repo) as T
            }
        })[HashTagViewModel::class.java]
    }

    private fun initAdapter(view: View) {
        val glide = GlideApp.with(this)
        val adapter = ImageSearchResultAdapter(glide) {
            model.retry()
        }

        adapter.onClick = { document ->
            fragmentManager?.beginTransaction()
                    ?.replace(R.id.fragmentContainer, ImageDetailFragment.newInstance(document))
                    ?.addToBackStack(ImageDetailFragment::class.java.name)
                    ?.commit()
        }

        view.searchRecyclerView.adapter = adapter
        view.searchRecyclerView.layoutManager = StaggeredGridLayoutManager(2, RecyclerView.VERTICAL)
        view.searchRecyclerView.addItemDecoration(GridItemDecoration(10, 2))

        model.posts.observe(this, Observer<PagedList<Document>> {
            adapter.submitList(it)
        })
        model.networkState.observe(this, Observer {
            adapter.setNetworkState(it)
        })
    }

    private fun initSwipeToRefresh(view: View) {
        model.refreshState.observe(this, Observer {
            view.swipeRefreshLayout.isRefreshing = it == NetworkState.LOADING
        })
        view.swipeRefreshLayout.setOnRefreshListener {
            model.refresh()
        }
    }

    override fun onDestroy() {
        disposeBag.dispose()
        listener?.onDeath()
        super.onDestroy()
    }

    interface TitleFragmentLifeListener {
        fun onBirth(title: String)
        fun onDeath()
    }
}