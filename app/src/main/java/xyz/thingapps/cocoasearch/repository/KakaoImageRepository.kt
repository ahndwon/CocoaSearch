package xyz.thingapps.cocoasearch.repository

import androidx.annotation.MainThread
import androidx.lifecycle.Transformations
import androidx.paging.Config
import androidx.paging.toLiveData
import xyz.thingapps.cocoasearch.net.Document
import xyz.thingapps.cocoasearch.net.ImageSearchApi
import java.util.concurrent.Executor


class KakaoImageRepository(
        private val imageSearch: ImageSearchApi,
        private val networkExecutor: Executor) : ImageRepository {
    @MainThread
    override fun imageSearchResult(searchWord: String, pageSize: Int): Listing<Document> {
        val sourceFactory = KakaoImageDataSourceFactory(imageSearch, searchWord, networkExecutor)

        val livePagedList = sourceFactory.toLiveData(
                config = Config(
                        pageSize = pageSize,
                        enablePlaceholders = false,
                        initialLoadSizeHint = pageSize),
                fetchExecutor = networkExecutor)

        val refreshState = Transformations.switchMap(sourceFactory.sourceLiveData) {
            it.initialLoad
        }

        return Listing(
                pagedList = livePagedList,
                networkState = Transformations.switchMap(sourceFactory.sourceLiveData) {
                  it.networkState
                },
                retry = {
                    sourceFactory.sourceLiveData.value?.retryAllFailed()
                },
                refresh = {
                    sourceFactory.sourceLiveData.value?.invalidate()
                },
                refreshState = refreshState
        )
    }
}

