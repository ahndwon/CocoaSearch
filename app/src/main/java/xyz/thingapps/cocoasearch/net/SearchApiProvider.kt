package xyz.thingapps.cocoasearch.net

import android.util.Log
import com.google.gson.GsonBuilder
import com.google.gson.JsonParser
import com.google.gson.JsonSyntaxException
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import xyz.thingapps.cocoasearch.BuildConfig

private val httpClient: OkHttpClient = OkHttpClient.Builder().apply {
    val loggingInterceptor = HttpLoggingInterceptor(ApiLogger()).apply {
        level = HttpLoggingInterceptor.Level.BODY
    }
    addInterceptor(loggingInterceptor)
    addInterceptor(ApiKeyInterceptor())
}.build()


class ApiLogger : HttpLoggingInterceptor.Logger {
    override fun log(message: String) {
        val logName = "ApiLogger"
        if (message.startsWith("{") || message.startsWith("[")) {
            try {
                val prettyPrintJson = GsonBuilder().setPrettyPrinting()
                        .create().toJson(JsonParser().parse(message))
                Log.d(logName, prettyPrintJson)
            } catch (m: JsonSyntaxException) {
                Log.d(logName, message)
            }
        } else {
            Log.d(logName, message)
            return
        }
    }
}

class ApiKeyInterceptor() : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val original = chain.request()
        val request = original.newBuilder().apply {
            addHeader("Authorization", "KakaoAK ${BuildConfig.API_KEY}")

        }.build()

        return chain.proceed(request)
    }
}

val searchApi: SearchApi = Retrofit.Builder().apply {
    baseUrl("https://dapi.kakao.com/")
    client(httpClient)
    addConverterFactory(GsonConverterFactory.create())
    addCallAdapterFactory(RxJava2CallAdapterFactory.createAsync())
}.build().create(SearchApi::class.java)