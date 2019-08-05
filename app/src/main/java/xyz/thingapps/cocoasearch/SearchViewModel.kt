package xyz.thingapps.cocoasearch

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import androidx.paging.PagedList
import xyz.thingapps.cocoasearch.net.Document
import xyz.thingapps.cocoasearch.repository.KakaoImageRepository
import xyz.thingapps.cocoasearch.repository.NetworkState

class SearchViewModel(private val repository: KakaoImageRepository) : ViewModel() {
    private val searchWord = MutableLiveData<String>()
    private val searchResult = Transformations.map(searchWord) {
        repository.imageSearchResult(it, 40)
    }

    val posts : LiveData<PagedList<Document>> = Transformations.switchMap(searchResult) { it.pagedList }
    val networkState : LiveData<NetworkState> = Transformations.switchMap(searchResult) { it.networkState }
    val refreshState : LiveData<NetworkState> = Transformations.switchMap(searchResult) { it.refreshState }

    fun refresh() {
        searchResult.value?.refresh?.invoke()
    }

    fun showSearchResult(searchWord: String): Boolean {
        if (this.searchWord.value == searchWord) {
            return false
        }
        this.searchWord.value = searchWord
        return true
    }

    fun retry() {
        val listing = searchResult?.value
        listing?.retry?.invoke()
    }

    fun currentSearchWord(): String? = searchWord.value
}