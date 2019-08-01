package xyz.thingapps.cocoasearch.net

import io.reactivex.Observable
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Query
import xyz.thingapps.cocoasearch.BuildConfig

interface SearchApi {

    companion object {
        const val SORT_ACCURACY = "accuracy"
        const val SORT_RECENCY = "recency"
    }

    @GET("v2/search/image")
    @Headers("Authorization: KakaoAK ${BuildConfig.ApiKey}")
    fun getImage(@Query("query") query: String,
                 @Query("sort") sort: String = SORT_ACCURACY,
                 @Query("page") page: Int = 1,
                 @Query("size") size: Int = 80): Observable<Result>
}
