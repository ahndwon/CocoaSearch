package xyz.thingapps.cocoasearch

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import xyz.thingapps.cocoasearch.net.searchApi

class MainActivity : AppCompatActivity() {

    private val disposeBag = CompositeDisposable()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        searchApi.getImage("설현").subscribeOn(AndroidSchedulers.mainThread())
                .subscribe({ result ->
                    Log.d(TAG, "result: $result")

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