package xyz.thingapps.cocoasearch.ui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import com.jakewharton.rxbinding3.view.clicks
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_image_detail.view.*
import kotlinx.android.synthetic.main.item_tag.view.*
import xyz.thingapps.cocoasearch.DetailViewModel
import xyz.thingapps.cocoasearch.R
import xyz.thingapps.cocoasearch.net.Document
import xyz.thingapps.cocoasearch.utils.GlideApp
import java.util.concurrent.TimeUnit

class ImageDetailFragment : Fragment() {

    companion object {
        private const val SEARCH_DOCUMENT = "search_document"
        private const val TYPE_TEXT_PLAIN = "text/plain"
        private const val WINDOW_DURATION = 600L

        fun newInstance(document: Document): ImageDetailFragment {
            val fragment = ImageDetailFragment()
            val args = Bundle()
            args.putParcelable(SEARCH_DOCUMENT, document)
            fragment.arguments = args
            return fragment
        }
    }

    private lateinit var viewModel: DetailViewModel
    private val disposeBag = CompositeDisposable()
    var listener: FragmentLifeListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(DetailViewModel::class.java)
        viewModel.document = arguments?.getParcelable<Document>(SEARCH_DOCUMENT)

        listener?.onBirth()
    }

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_image_detail, container, false)

        viewModel.document?.let { document ->
            GlideApp.with(view).load(document.imageUrl)
                    .placeholder(R.drawable.ic_insert_photo_black_48dp)
                    .into(view.detailImageView)
        }

        view.imageDateTextView.text = viewModel.getDocumentDate()
        view.siteInfoTextView.text = viewModel.document?.displaySiteName
        view.imageHeightTextView.text = viewModel.document?.height?.toString()
        view.imageWidthTextView.text = viewModel.document?.width?.toString()

        setupButtons(view)

        return view
    }

    private fun setupButtons(view: View) {
        viewModel.document?.docUrl?.let { url ->
            setButtonClick(view.websiteButton) {
                fragmentManager?.beginTransaction()
                        ?.replace(
                                R.id.fragmentContainer,
                                WebViewFragment.newInstance(url)
                        )
                        ?.addToBackStack(WebViewFragment::class.java.name)
                        ?.commit()
            }

            setButtonClick(view.shareButton) {
                val intent = Intent(Intent.ACTION_SEND)
                intent.type = TYPE_TEXT_PLAIN
                intent.putExtra(Intent.EXTRA_TEXT, getString(R.string.share_message, url))
                startActivity(Intent.createChooser(intent, getString(R.string.share)))
            }
        }
    }

    private fun setButtonClick(button: Button, onClick: (() -> Unit)) {
        button.clicks()
                .throttleFirst(WINDOW_DURATION, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    onClick.invoke()
                }.addTo(disposeBag)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        activity?.let { viewModel.setClassifier(it) }
        viewModel.document?.imageUrl?.let { url ->
            viewModel.getRecognitions(url).subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .map { recognitionList ->
                        recognitionList.map { it.title }
                    }
                    .subscribe({ labels ->
                        Log.d(
                                ImageDetailFragment::class.java.name,
                                "getRecognition success : $labels"
                        )
                        createTags(labels)
                    }, { e ->
                        Log.d(
                                ImageDetailFragment::class.java.name,
                                "getRecognition failed : ",
                                e
                        )
                    }).addTo(disposeBag)
        }

        super.onActivityCreated(savedInstanceState)
    }

    private fun createTags(labels: List<String>) {
        labels.forEach { label ->
            val tagView =
                    LayoutInflater.from(this@ImageDetailFragment.context)
                            .inflate(R.layout.item_tag, view?.tagLayout, false)
            tagView.tagTextView.text = getString(R.string.format_hash_tag, label)

            setHashTagOnClick(tagView, label)

            view?.tagLayout?.addView(tagView)
        }
    }

    private fun setHashTagOnClick(tagView: View, label: String) {
        tagView.clicks().observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    fragmentManager?.beginTransaction()
                            ?.replace(
                                    R.id.fragmentContainer,
                                    HashTagFragment.newInstance(label)
                            )
                            ?.addToBackStack(HashTagFragment::class.java.name)
                            ?.commit()
                }.addTo(disposeBag)
    }

    override fun onDestroy() {
        listener?.onDeath()
        disposeBag.dispose()
        super.onDestroy()
    }
}