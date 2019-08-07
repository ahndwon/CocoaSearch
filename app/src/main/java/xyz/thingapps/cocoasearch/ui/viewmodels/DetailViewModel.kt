package xyz.thingapps.cocoasearch.ui.viewmodels

import android.app.Activity
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import io.reactivex.Single
import org.threeten.bp.LocalDateTime
import org.threeten.bp.format.DateTimeFormatter
import xyz.thingapps.cocoasearch.net.Document
import xyz.thingapps.cocoasearch.tflite.Classifier
import xyz.thingapps.cocoasearch.tflite.ImageRecognizer

class DetailViewModel(application: Application) : AndroidViewModel(application) {
    private val imageRecognizer = ImageRecognizer(application)
    var document: Document? = null

    fun getRecognitions(url: String): Single<List<Classifier.Recognition>> {
        return imageRecognizer.getRecognitions(url)
    }

    fun setClassifier(activity: Activity) {
        imageRecognizer.setClassifier(activity)
    }

    fun getDocumentDate(): String {
        val date = document?.datetime ?: ""
        val parsedDate = LocalDateTime.parse(date, DateTimeFormatter.ISO_OFFSET_DATE_TIME)
        return parsedDate.format(DateTimeFormatter.ofPattern("E, M월 yyyy년 HH시 mm분 ss초"))
    }

}