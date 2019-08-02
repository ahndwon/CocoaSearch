package xyz.thingapps.cocoasearch

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_main.*
import xyz.thingapps.cocoasearch.net.searchApi
import xyz.thingapps.cocoasearch.utils.GridItemDecoration

class MainActivity : AppCompatActivity() {

    private val disposeBag = CompositeDisposable()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val adapter = SearchListAdapter()
        searchRecyclerView.adapter = adapter
        searchRecyclerView.layoutManager = StaggeredGridLayoutManager(2, RecyclerView.VERTICAL)
        searchRecyclerView.addItemDecoration(GridItemDecoration(10, 2))


        searchApi.getImage("카카오").observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({ result ->
                    Log.d(TAG, "result: $result")
                    adapter.items = result.documents
                    adapter.notifyDataSetChanged()
                }, { e ->
                    Log.e(TAG, "error: ", e)
                }).addTo(disposeBag)

    }

    override fun onDestroy() {
        disposeBag.dispose()
        super.onDestroy()
    }

    companion object {
        val TAG = MainActivity::class.java.name
    }
}