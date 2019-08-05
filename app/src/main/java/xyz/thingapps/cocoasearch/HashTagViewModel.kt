package xyz.thingapps.cocoasearch

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import androidx.paging.PagedList
import xyz.thingapps.cocoasearch.net.Document
import xyz.thingapps.cocoasearch.repository.KakaoImageRepository
import xyz.thingapps.cocoasearch.repository.Listing
import xyz.thingapps.cocoasearch.repository.NetworkState

class HashTagViewModel(searchWord: String, repository: KakaoImageRepository) : ViewModel() {
    private val searchResult: MutableLiveData<Listing<Document>> = MutableLiveData()

    init {
        searchResult.value = repository.imageSearchResult(searchWord, 40)
    }

    val posts: LiveData<PagedList<Document>> = Transformations.switchMap(searchResult) { it.pagedList }
    val networkState: LiveData<NetworkState> = Transformations.switchMap(searchResult) { it.networkState }
    val refreshState: LiveData<NetworkState> = Transformations.switchMap(searchResult) { it.refreshState }

    fun refresh() {
        searchResult.value?.refresh?.invoke()
    }

    fun retry() {
        val listing = searchResult.value
        listing?.retry?.invoke()
    }
}