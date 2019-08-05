package xyz.thingapps.cocoasearch

import android.app.Application
import com.jakewharton.threetenabp.AndroidThreeTen

class SharedApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        AndroidThreeTen.init(this)
    }
}