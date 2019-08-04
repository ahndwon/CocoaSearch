package xyz.thingapps.cocoasearch.utils

import xyz.thingapps.cocoasearch.net.ImageSearchApi
import xyz.thingapps.cocoasearch.repository.KakaoImageRepository
import java.util.concurrent.Executor
import java.util.concurrent.Executors

interface ServiceLocator {
    companion object {
        private val LOCK = Any()
        private var instance: ServiceLocator? = null
        fun instance(): ServiceLocator {
            synchronized(LOCK) {
                if (instance == null) {
                    instance = DefaultServiceLocator()
                }
                return instance!!
            }
        }
    }

    fun getRepository(): KakaoImageRepository

    fun getNetworkExecutor(): Executor

    fun getDiskIOExecutor(): Executor

    fun getImageSearchApi(): ImageSearchApi
}


open class DefaultServiceLocator : ServiceLocator {
    @Suppress("PrivatePropertyName")
    private val DISK_IO = Executors.newSingleThreadExecutor()

    @Suppress("PrivatePropertyName")
    private val NETWORK_IO = Executors.newFixedThreadPool(5)

    private val api by lazy {
        ImageSearchApi.create()
    }

    override fun getRepository(): KakaoImageRepository {
        return KakaoImageRepository(getImageSearchApi(), getNetworkExecutor())
    }

    override fun getNetworkExecutor(): Executor = NETWORK_IO

    override fun getDiskIOExecutor(): Executor = DISK_IO

    override fun getImageSearchApi(): ImageSearchApi = api
}