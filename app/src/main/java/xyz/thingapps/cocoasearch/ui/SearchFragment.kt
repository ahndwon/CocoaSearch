package xyz.thingapps.cocoasearch.ui

import android.os.Bundle
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.paging.PagedList
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import kotlinx.android.synthetic.main.fragment_search.*
import kotlinx.android.synthetic.main.fragment_search.view.*
import xyz.thingapps.cocoasearch.R
import xyz.thingapps.cocoasearch.SearchViewModel
import xyz.thingapps.cocoasearch.net.Document
import xyz.thingapps.cocoasearch.repository.NetworkState
import xyz.thingapps.cocoasearch.utils.GlideApp
import xyz.thingapps.cocoasearch.utils.GridItemDecoration
import xyz.thingapps.cocoasearch.utils.ServiceLocator

class SearchFragment : Fragment() {

    companion object {
        const val KEY_SEARCH_WORD = "search_word"
        const val DEFAULT_SEARCH_WORD = "kakao"
    }

    private lateinit var model: SearchViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_search, container, false)

        model = getViewModel()
        initAdapter(view)
        initSwipeToRefresh(view)
        initSearch(view)
        val searchWord = savedInstanceState?.getString(KEY_SEARCH_WORD) ?: DEFAULT_SEARCH_WORD
        model.showSearchResult(searchWord)
        return view
    }

    private fun getViewModel(): SearchViewModel {
        return ViewModelProviders.of(this, object : ViewModelProvider.Factory {
            override fun <T : ViewModel?> create(modelClass: Class<T>): T {
                val repo =
                        ServiceLocator.instance()
                                .getRepository()

                @Suppress("UNCHECKED_CAST")
                return SearchViewModel(repo) as T
            }
        })[SearchViewModel::class.java]
    }

    private fun initAdapter(view: View) {
        val glide = GlideApp.with(this)
        val adapter = SearchResultAdapter(glide) {
            model.retry()
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

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(KEY_SEARCH_WORD, model.currentSearchWord())
    }

    private fun initSearch(view : View) {
        view.searchEditText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_GO) {
                updatedSubredditFromInput(view)
                true
            } else {
                false
            }
        }
        view.searchEditText.setOnKeyListener { _, keyCode, event ->
            if (event.action == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_ENTER) {
                updatedSubredditFromInput(view)
                true
            } else {
                false
            }
        }
    }

    private fun updatedSubredditFromInput(view: View) {
        view.searchEditText.text.trim().toString().let {
            if (it.isNotEmpty()) {
                if (model.showSearchResult(it)) {
                    searchRecyclerView.scrollToPosition(0)
                    (searchRecyclerView.adapter as? SearchResultAdapter)?.submitList(null)
                }
            }
        }
    }
}