package xyz.thingapps.cocoasearch.repository

import androidx.lifecycle.MutableLiveData
import androidx.paging.DataSource
import xyz.thingapps.cocoasearch.net.Document
import xyz.thingapps.cocoasearch.net.ImageSearchApi
import java.util.concurrent.Executor


class KakaoImageDataSourceFactory(
        private val imageSearchApi: ImageSearchApi,
        private val searchWord: String,
        private val sortType: String,
        private val retryExecutor: Executor) : DataSource.Factory<Int, Document>() {
    val sourceLiveData = MutableLiveData<KakaoImageDataSource>()
    override fun create(): DataSource<Int, Document> {
        val source = KakaoImageDataSource(imageSearchApi, searchWord, sortType, retryExecutor)
        sourceLiveData.postValue(source)
        return source
    }
}
