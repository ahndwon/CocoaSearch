package xyz.thingapps.cocoasearch.ui

import android.content.Context
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
import xyz.thingapps.cocoasearch.R
import xyz.thingapps.cocoasearch.net.ImageSearchApi
import xyz.thingapps.cocoasearch.repository.NetworkState
import xyz.thingapps.cocoasearch.ui.viewmodels.SearchViewModel
import xyz.thingapps.cocoasearch.utils.GlideApp
import xyz.thingapps.cocoasearch.utils.GridItemDecoration
import xyz.thingapps.cocoasearch.utils.ServiceLocator
import xyz.thingapps.cocoasearch.vo.Document
import java.util.concurrent.TimeUnit

class SearchFragment : Fragment() {

    companion object {
        const val KEY_SEARCH_WORD = "search_word"
        const val DEFAULT_SEARCH_WORD = "카카오페이"
        const val GRID_SPACING_PX = 10
        const val GRID_SIZE = 2
        const val QUERY_TIMEOUT = 500L
        const val WINDOW_DURATION = 600L
    }

    private val disposeBag = CompositeDisposable()
    private lateinit var searchViewModel: SearchViewModel

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
        setSortBarClick(sortBar)
        super.onActivityCreated(savedInstanceState)
    }

    private fun setSortBarClick(sortBar: View?) {
        sortBar?.clicks()?.throttleFirst(WINDOW_DURATION, TimeUnit.MILLISECONDS)
                ?.observeOn(AndroidSchedulers.mainThread())
                ?.subscribe {
                    val dialog = getSortDialog(sortBar.context)
                    setupSortDialog(dialog, sortBar.bottom).show()
                }?.addTo(disposeBag)
    }

    private fun setupSortDialog(dialog: AlertDialog, positionY: Int): AlertDialog {
        return dialog.apply {
            window?.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
            window?.setGravity(Gravity.TOP)
            val layoutParams = window?.attributes
            layoutParams?.let {
                it.y = positionY
                window?.attributes = it
            }
        }
    }

    private fun getSortDialog(context: Context): AlertDialog {
        val array = resources.getStringArray(R.array.sort_list)

        val builder =
                AlertDialog.Builder(
                        context,
                        android.R.style.Theme_Material_Light_Dialog_NoActionBar
                )

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
        return builder.create()
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
        view.searchRecyclerView.layoutManager =
                StaggeredGridLayoutManager(2, RecyclerView.VERTICAL)
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