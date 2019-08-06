package xyz.thingapps.cocoasearch

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import androidx.paging.PagedList
import xyz.thingapps.cocoasearch.net.Document
import xyz.thingapps.cocoasearch.net.ImageSearchApi
import xyz.thingapps.cocoasearch.repository.KakaoImageRepository
import xyz.thingapps.cocoasearch.repository.NetworkState

class SearchViewModel(private val repository: KakaoImageRepository) : ViewModel() {
    private val searchWord = MutableLiveData<String>()
    private val searchResult =
            Transformations.map(searchWord) {
                Log.d(SearchViewModel::class.java.name, "searchSort : $searchSort")
                repository.imageSearchResult(it, searchSort, PAGE_SIZE)
            }

    var searchSort = ImageSearchApi.SORT_ACCURACY

    val posts: LiveData<PagedList<Document>> =
            Transformations.switchMap(searchResult) { it.pagedList }

    val networkState: LiveData<NetworkState> =
            Transformations.switchMap(searchResult) { it.networkState }

    val refreshState: LiveData<NetworkState> =
            Transformations.switchMap(searchResult) { it.refreshState }

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

    companion object {
        const val PAGE_SIZE = 40
    }

}