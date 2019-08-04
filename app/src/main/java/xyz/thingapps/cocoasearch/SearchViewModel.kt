package xyz.thingapps.cocoasearch

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import xyz.thingapps.cocoasearch.repository.KakaoImageRepository

class SearchViewModel(private val repository: KakaoImageRepository) : ViewModel() {
    private val queryLiveData = MutableLiveData<String>()
    private val repoResult = Transformations.map(queryLiveData) {
        repository.imageSearchResults(it, 1, 30)
    }
    val posts = Transformations.switchMap(repoResult) { it.pagedList }
    val networkState = Transformations.switchMap(repoResult) { it.networkState }
    val refreshState = Transformations.switchMap(repoResult) { it.refreshState }

    fun refresh() {
        repoResult.value?.refresh?.invoke()
    }

    fun showQuery(query: String): Boolean {
        if (queryLiveData.value == query) {
            return false
        }
        queryLiveData.value = query
        return true
    }

    fun retry() {
        val listing = repoResult?.value
        listing?.retry?.invoke()
    }

    fun currentQuery(): String? = queryLiveData.value

}