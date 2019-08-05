package xyz.thingapps.cocoasearch

import android.app.Activity
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import io.reactivex.Observable
import xyz.thingapps.cocoasearch.tflite.Classifier
import xyz.thingapps.cocoasearch.tflite.ImageRecognizer

class DetailViewModel(application: Application) : AndroidViewModel(application) {
    private val imageRecognizer = ImageRecognizer(application)

    fun getRecognitions(url: String): Observable<List<Classifier.Recognition>> {
        return imageRecognizer.getRecognitions(url)
    }

    fun setClassifier(activity: Activity) {
        imageRecognizer.setClassifier(activity)
    }

}