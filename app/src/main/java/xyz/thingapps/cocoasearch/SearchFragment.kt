package xyz.thingapps.cocoasearch

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_search.view.*
import xyz.thingapps.cocoasearch.net.searchApi
import xyz.thingapps.cocoasearch.utils.GridItemDecoration

class SearchFragment : Fragment() {
    private val disposeBag = CompositeDisposable()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_search, container, false)

        val adapter = SearchListAdapter()
        view.searchRecyclerView.adapter = adapter
        view.searchRecyclerView.layoutManager = StaggeredGridLayoutManager(2, RecyclerView.VERTICAL)
        view.searchRecyclerView.addItemDecoration(GridItemDecoration(10, 2))

        searchApi.getImage("카카오").observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({ result ->
                    Log.d(MainActivity.TAG, "result: $result")
                    adapter.items = result.documents
                    adapter.notifyDataSetChanged()
                }, { e ->
                    Log.e(MainActivity.TAG, "error: ", e)
                }).addTo(disposeBag)

        return view
    }

    override fun onDestroy() {
        disposeBag.dispose()
        super.onDestroy()
    }
}