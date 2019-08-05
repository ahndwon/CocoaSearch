package xyz.thingapps.cocoasearch.tflite

import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.drawable.Drawable
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DecodeFormat
import com.bumptech.glide.request.target.SimpleTarget
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import java.io.IOException

@Suppress("DEPRECATION")
class ImageRecognizer(context: Context) {
    private var classifier: Classifier? = null
    private var rgbFrameBitmap: Bitmap? = null
    private var frameToCropTransform: Matrix? = null
    private var cropToFrameTransform: Matrix? = null
    private val glideRequest = Glide.with(context)

    private fun processImage(bitmap: Bitmap, pixels: IntArray): List<Classifier.Recognition> {

        rgbFrameBitmap?.setPixels(pixels, 0, bitmap.width, 0, 0, bitmap.width, bitmap.height)

        val croppedBitmap =
                classifier?.let {
                    Bitmap.createBitmap(
                            it.imageSizeX,
                            it.imageSizeY,
                            Bitmap.Config.ARGB_8888
                    )
                }
        val canvas = croppedBitmap?.let { Canvas(it) }
        rgbFrameBitmap?.let { rgbFrameBitmap ->
            frameToCropTransform?.let { frameToCropTransform ->
                canvas?.drawBitmap(
                        rgbFrameBitmap,
                        frameToCropTransform, null
                )
            }
        }

        return classifier?.recognizeImage(croppedBitmap)?.toList() ?: emptyList()

    }

    fun getRecognitions(url: String): Single<List<Classifier.Recognition>> {
        return Single.create { emitter ->
            getBitmap(url)
                    .subscribeOn(Schedulers.io())
                    .subscribe({
                        glideRequest.asBitmap()
                                .format(DecodeFormat.PREFER_ARGB_8888)
                                .load(url)
                                .into(object : SimpleTarget<Bitmap>() {
                                    override fun onResourceReady(
                                            resource: Bitmap,
                                            transition: com.bumptech.glide.request.transition.Transition<in Bitmap>?
                                    ) {
                                        val results = recognizeImage(resource)
                                        emitter.onSuccess(results)
                                    }

                                    override fun onLoadFailed(errorDrawable: Drawable?) {
                                        emitter.onError(Throwable("image recognition failed"))
                                        super.onLoadFailed(errorDrawable)
                                    }
                                })
                    }, { e ->
                        e.printStackTrace()
                    })
        }
    }

    private fun recognizeImage(resource: Bitmap): List<Classifier.Recognition> {
        val pixels = IntArray(resource.width * resource.height)
        resource.getPixels(pixels, 0, resource.width, 0, 0, resource.width, resource.height)
        rgbFrameBitmap =
                Bitmap.createBitmap(resource.width, resource.height, Bitmap.Config.ARGB_8888)

        frameToCropTransform = classifier?.let {
            ImageUtils.getTransformationMatrix(
                    resource.width,
                    resource.height,
                    it.imageSizeX,
                    it.imageSizeY,
                    0,
                    true
            )
        }
        cropToFrameTransform = Matrix()
        frameToCropTransform?.invert(cropToFrameTransform)
        return processImage(resource, pixels)
    }

    private fun getBitmap(url: String): Single<Bitmap> {
        return Single.create { emitter ->
            glideRequest.asBitmap()
                    .format(DecodeFormat.PREFER_ARGB_8888)
                    .load(url)
                    .into(object : SimpleTarget<Bitmap>() {
                        override fun onResourceReady(
                                resource: Bitmap,
                                transition: com.bumptech.glide.request.transition.Transition<in Bitmap>?
                        ) {
                            emitter.onSuccess(resource)
                        }

                        override fun onLoadFailed(errorDrawable: Drawable?) {
                            emitter.onError(Throwable("getBitmap load failed"))
                            super.onLoadFailed(errorDrawable)
                        }
                    })

        }
    }

    fun setClassifier(
            activity: Activity
    ) {
        try {
            classifier =
                    Classifier.create(activity, Classifier.Device.CPU, 1)
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }
}