package xyz.thingapps.cocoasearch.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.fragment_image_detail.view.*
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

    private var document: Document? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_image_detail, container, false)
        activity?.findViewById<FrameLayout>(R.id.filterBarContainer)?.visibility = View.GONE

        document = arguments?.getParcelable<Document>(SEARCH_DOCUMENT)

        document?.let {
            GlideApp.with(view).load(it.imageUrl)
                    .placeholder(R.drawable.ic_insert_photo_black_48dp)
                    .into(view.detailImageView)
        }


        return view
    }
}