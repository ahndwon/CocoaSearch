package xyz.thingapps.cocoasearch.ui

import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.paging.PagedList
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.jakewharton.rxbinding3.widget.queryTextChanges
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import kotlinx.android.synthetic.main.fragment_search.view.*
import xyz.thingapps.cocoasearch.R
import xyz.thingapps.cocoasearch.SearchViewModel
import xyz.thingapps.cocoasearch.net.Document
import xyz.thingapps.cocoasearch.repository.NetworkState
import xyz.thingapps.cocoasearch.utils.GlideApp
import xyz.thingapps.cocoasearch.utils.GridItemDecoration
import xyz.thingapps.cocoasearch.utils.ServiceLocator
import java.util.concurrent.TimeUnit

class SearchFragment : Fragment() {

    companion object {
        const val KEY_SEARCH_WORD = "search_word"
        const val DEFAULT_SEARCH_WORD = "kakao"
        const val QUERY_TIMEOUT = 500L
    }

    private val disposeBag = CompositeDisposable()
    private lateinit var model: SearchViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_search, container, false)
        setHasOptionsMenu(true)

        model = getViewModel()
        initAdapter(view)
        initSwipeToRefresh(view)
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

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(KEY_SEARCH_WORD, model.currentSearchWord())
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        menu.clear()
        inflater.inflate(R.menu.options_menu, menu)
        setupSearchView(menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    private fun setupSearchView(menu: Menu) {
        val searchItem = menu.findItem(R.id.search)
        val searchView = searchItem.actionView as? SearchView

        searchView?.queryTextChanges()
                ?.debounce(QUERY_TIMEOUT, TimeUnit.MILLISECONDS)
                ?.observeOn(AndroidSchedulers.mainThread())
                ?.subscribe({ search ->
                    Log.d(SearchFragment::class.java.name, "search - $search")
                    search.trim().toString().let {
                        if (it.isNotEmpty()) {
                            if (model.showSearchResult(it)) {
                                view?.searchRecyclerView?.scrollToPosition(0)
                                (view?.searchRecyclerView?.adapter as? SearchResultAdapter)?.submitList(null)
                            }
                        }
                    }
                }, { e ->
                    e.printStackTrace()
                })?.addTo(disposeBag)
    }

    override fun onDestroy() {
        disposeBag.dispose()
        super.onDestroy()
    }
}