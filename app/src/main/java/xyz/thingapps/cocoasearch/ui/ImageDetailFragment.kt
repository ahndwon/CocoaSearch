package xyz.thingapps.cocoasearch.ui

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_image_detail.view.*
import xyz.thingapps.cocoasearch.DetailViewModel
import xyz.thingapps.cocoasearch.R
import xyz.thingapps.cocoasearch.net.Document
import xyz.thingapps.cocoasearch.utils.GlideApp

class ImageDetailFragment : Fragment() {

    companion object {
        private const val SEARCH_DOCUMENT = "search_document"

        fun newInstance(document: Document): ImageDetailFragment {
            val fragment = ImageDetailFragment()
            val args = Bundle()
            args.putParcelable(SEARCH_DOCUMENT, document)
            fragment.arguments = args
            return fragment
        }
    }

    private lateinit var viewModel: DetailViewModel
    private var document: Document? = null
    private val disposeBag = CompositeDisposable()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(DetailViewModel::class.java)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_image_detail, container, false)
        activity?.findViewById<FrameLayout>(R.id.filterBarContainer)?.visibility = View.GONE

        document = arguments?.getParcelable<Document>(SEARCH_DOCUMENT)

        document?.let { document ->
            GlideApp.with(view).load(document.imageUrl)
                    .placeholder(R.drawable.ic_insert_photo_black_48dp)
                    .into(view.detailImageView)
        }

        return view
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        activity?.let { viewModel.setClassifier(it) }
        document?.imageUrl?.let { url ->
            viewModel.getRecognitions(url).subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .map { recognitionList ->
                        recognitionList.map { it.title }
                    }
                    .subscribe({ labels ->
                        Log.d(ImageDetailFragment::class.java.name, "getRecognition success : $labels")
                        createTags(labels)
                    }, { e ->
                        Log.d(ImageDetailFragment::class.java.name, "getRecognition failed : ", e)
                    }).addTo(disposeBag)
        }

        super.onActivityCreated(savedInstanceState)
    }

    private fun createTags(labels: List<String>) {
        labels.forEach {
            val tagView = TextView(this@ImageDetailFragment.context)
            tagView.text = it
            view?.tagLayout?.addView(tagView)
        }
    }

    override fun onDestroy() {
        activity?.findViewById<FrameLayout>(R.id.filterBarContainer)?.visibility = View.VISIBLE
        disposeBag.dispose()
        super.onDestroy()
    }
}