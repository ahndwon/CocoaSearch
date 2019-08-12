package xyz.thingapps.cocoasearch.repository

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.paging.ItemKeyedDataSource
import retrofit2.Call
import retrofit2.Response
import xyz.thingapps.cocoasearch.net.ImageSearchApi
import xyz.thingapps.cocoasearch.vo.Document
import java.io.IOException
import java.util.concurrent.Executor

class KakaoImageDataSource(
        private val imageSearchApi: ImageSearchApi,
        private val searchWord: String,
        private val sortType: String,
        private val retryExecutor: Executor)
    : ItemKeyedDataSource<Int, Document>() {

    private var retry: (() -> Any)? = null

    private var pageNum = -1

    val networkState = MutableLiveData<NetworkState>()

    val initialLoad = MutableLiveData<NetworkState>()
    fun retryAllFailed() {
        val prevRetry = retry
        retry = null
        prevRetry?.let {
            retryExecutor.execute {
                it.invoke()
            }
        }
    }

    override fun loadBefore(params: LoadParams<Int>, callback: LoadCallback<Document>) {

    }

    override fun loadAfter(params: LoadParams<Int>, callback: LoadCallback<Document>) {
        pageNum++

        networkState.postValue(NetworkState.LOADING)

        imageSearchApi.getImage(
                query = searchWord,
                page = pageNum,
                size = params.requestedLoadSize,
                sort = sortType
        ).enqueue(
                object : retrofit2.Callback<ImageSearchApi.SearchResponse> {
                    override fun onFailure(call: Call<ImageSearchApi.SearchResponse>, t: Throwable) {
                        retry = {
                            loadAfter(params, callback)
                        }

                        networkState.postValue(NetworkState.error(t.message ?: "unknown err"))
                    }

                    override fun onResponse(
                            call: Call<ImageSearchApi.SearchResponse>,
                            response: Response<ImageSearchApi.SearchResponse>) {
                        if (response.isSuccessful) {
                            val items = response.body()?.documents ?: emptyList()
                            retry = null
                            callback.onResult(items)
                            networkState.postValue(NetworkState.LOADED)
                        } else {
                            retry = {
                                loadAfter(params, callback)
                            }
                            networkState.postValue(
                                    NetworkState.error("error code: ${response.code()}"))
                        }
                    }
                }
        )
    }

    override fun getKey(item: Document): Int = pageNum

    override fun loadInitial(
            params: LoadInitialParams<Int>,
            callback: LoadInitialCallback<Document>) {
        pageNum = 0
        val request = imageSearchApi.getImage(
                query = searchWord,
                size = params.requestedLoadSize
        )

        networkState.postValue(NetworkState.LOADING)
        initialLoad.postValue(NetworkState.LOADING)

        try {
            val response = request.execute()
            val items = response.body()?.documents ?: emptyList()

            retry = null
            networkState.postValue(NetworkState.LOADED)
            initialLoad.postValue(NetworkState.LOADED)
            Log.d(KakaoImageDataSource::class.java.name, "items : $items")
            callback.onResult(items)
        } catch (ioException: IOException) {
            retry = {
                loadInitial(params, callback)
            }
            val error = NetworkState.error(ioException.message ?: "unknown error")
            networkState.postValue(error)
            initialLoad.postValue(error)
        }
    }
}