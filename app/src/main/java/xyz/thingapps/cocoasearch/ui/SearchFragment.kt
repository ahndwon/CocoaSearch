package xyz.thingapps.cocoasearch.ui

import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.SearchView
import androidx.appcompat.app.AlertDialog
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.paging.PagedList
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.jakewharton.rxbinding3.view.clicks
import com.jakewharton.rxbinding3.widget.queryTextChanges
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_search.view.*
import xyz.thingapps.cocoasearch.MainActivity
import xyz.thingapps.cocoasearch.R
import xyz.thingapps.cocoasearch.SearchViewModel
import xyz.thingapps.cocoasearch.net.Document
import xyz.thingapps.cocoasearch.net.ImageSearchApi
import xyz.thingapps.cocoasearch.repository.NetworkState
import xyz.thingapps.cocoasearch.utils.GlideApp
import xyz.thingapps.cocoasearch.utils.GridItemDecoration
import xyz.thingapps.cocoasearch.utils.ServiceLocator
import java.util.concurrent.TimeUnit

class SearchFragment : Fragment() {

    companion object {
        const val KEY_SEARCH_WORD = "search_word"
        const val DEFAULT_SEARCH_WORD = "kakao"
        const val GRID_SPACING_PX = 10
        const val GRID_SIZE = 2
        const val QUERY_TIMEOUT = 500L
    }

    private val disposeBag = CompositeDisposable()
    lateinit var searchViewModel: SearchViewModel

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_search, container, false)
        setHasOptionsMenu(true)

        searchViewModel = getViewModel()
        initAdapter(view)
        initSwipeToRefresh(view)

        val searchWord = savedInstanceState?.getString(KEY_SEARCH_WORD) ?: DEFAULT_SEARCH_WORD
        searchViewModel.showSearchResult(searchWord)
        return view
    }


    override fun onActivityCreated(savedInstanceState: Bundle?) {
        val sortBar = (activity as MainActivity).findViewById<CardView>(R.id.sortBar)
        setupSortDialog(sortBar)
        super.onActivityCreated(savedInstanceState)
    }

    private fun setupSortDialog(view: View?) {
        view?.clicks()?.throttleFirst(600L, TimeUnit.MILLISECONDS)
                ?.observeOn(AndroidSchedulers.mainThread())
                ?.subscribe {
                    val array = resources.getStringArray(R.array.sort_list)

                    val builder = AlertDialog.Builder(view.context)
                    builder.setItems(array) { dialog, which ->
                        dialog.dismiss()
                        val mainActivity = (activity as MainActivity)
                        when (which) {
                            0 -> {
                                mainActivity.sortTypeSpinner.text = getString(R.string.sort_accuracy)
                                searchViewModel.searchSort = ImageSearchApi.SORT_ACCURACY
                            }
                            1 -> {
                                mainActivity.sortTypeSpinner.text = getString(R.string.sort_recency)
                                searchViewModel.searchSort = ImageSearchApi.SORT_RECENCY
                            }
                        }
                    }
                    val dialog = builder.create()
                    dialog.window?.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
                    dialog.window?.setGravity(Gravity.TOP)
                    val layoutParams = dialog.window?.attributes
                    layoutParams?.let {
                        it.x = -view.left
                        it.y = view.bottom
                        dialog.window?.attributes = it
                    }
                    dialog.show()
                }?.addTo(disposeBag)
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
            searchViewModel.retry()
        }

        adapter.onClick = { document ->
            fragmentManager?.beginTransaction()
                    ?.replace(R.id.fragmentContainer, ImageDetailFragment.newInstance(document))
                    ?.addToBackStack(ImageDetailFragment::class.java.name)
                    ?.commit()
        }

        view.searchRecyclerView.adapter = adapter
        view.searchRecyclerView.layoutManager = StaggeredGridLayoutManager(2, RecyclerView.VERTICAL)
        view.searchRecyclerView.addItemDecoration(GridItemDecoration(GRID_SPACING_PX, GRID_SIZE))

        searchViewModel.posts.observe(this, Observer<PagedList<Document>> {
            adapter.submitList(it)
        })
        searchViewModel.networkState.observe(this, Observer {
            adapter.setNetworkState(it)
        })
    }

    private fun initSwipeToRefresh(view: View) {
        searchViewModel.refreshState.observe(this, Observer {
            view.swipeRefreshLayout.isRefreshing = it == NetworkState.LOADING
        })
        view.swipeRefreshLayout.setOnRefreshListener {
            searchViewModel.refresh()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(KEY_SEARCH_WORD, searchViewModel.currentSearchWord())
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

        searchView?.isIconifiedByDefault = false
//        searchView?.setQuery(getString(R.string.default_search_word), false)

        searchView?.queryTextChanges()
                ?.debounce(QUERY_TIMEOUT, TimeUnit.MILLISECONDS)
                ?.observeOn(AndroidSchedulers.mainThread())
                ?.subscribe({ search ->
                    Log.d(SearchFragment::class.java.name, "search - $search")
                    search.trim().toString().let {
                        if (it.isNotEmpty()) {
                            if (searchViewModel.showSearchResult(it)) {
                                view?.searchRecyclerView?.scrollToPosition(0)
//                                (view?.searchRecyclerView?.adapter as? SearchResultAdapter)
//                                        ?.submitList(null)
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