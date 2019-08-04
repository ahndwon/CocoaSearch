package xyz.thingapps.cocoasearch.net

import android.util.Log
import okhttp3.HttpUrl
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import xyz.thingapps.cocoasearch.BuildConfig

interface ImageSearchApi {

    @GET("v2/search/image")
    fun getImage(
            @Query("query") query: String,
            @Query("page") page: Int = 1,
            @Query("size") size: Int = 80,
            @Query("sort") sort: String = SORT_ACCURACY
    ): Call<SearchResponse>

    companion object {
        const val SORT_ACCURACY = "accuracy"
        const val SORT_RECENCY = "recency"

        private const val BASE_URL = "https://dapi.kakao.com/"
        fun create(): ImageSearchApi = create(HttpUrl.parse(BASE_URL)!!)
        private fun create(httpUrl: HttpUrl): ImageSearchApi {
            val logger = HttpLoggingInterceptor(HttpLoggingInterceptor.Logger {
                Log.d("API", it)
            })
            logger.level = HttpLoggingInterceptor.Level.BASIC

            val client = OkHttpClient.Builder()
                    .addInterceptor(logger)
                    .addInterceptor(ApiKeyInterceptor())
                    .build()
            return Retrofit.Builder()
                    .baseUrl(httpUrl)
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()
                    .create(ImageSearchApi::class.java)
        }
    }

    class ApiKeyInterceptor : Interceptor {
        override fun intercept(chain: Interceptor.Chain): Response {
            val original = chain.request()
            val request = original.newBuilder().apply {
                addHeader("Authorization", "KakaoAK ${BuildConfig.API_KEY}")
            }.build()

            return chain.proceed(request)
        }
    }
}
