package xyz.thingapps.cocoasearch.ui.webview

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.fragment_web_view.view.*
import xyz.thingapps.cocoasearch.R
import xyz.thingapps.cocoasearch.ui.FragmentLifeListener

class WebViewFragment : Fragment() {

    private var url: String? = null
    var listener: FragmentLifeListener? = null

    companion object {
        fun newInstance(url: String): WebViewFragment {
            val fragment = WebViewFragment()
            val args = Bundle()
            args.putString("URL", url)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        listener?.onBirth()

        arguments?.let {
            url = it.getString("URL")
        }

    }

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {

        val view = inflater.inflate(R.layout.fragment_web_view, container, false)

        url?.let { loadUrl(view, it) }

        setSettings(view.webView)

        return view
    }

    private fun loadUrl(view: View, url: String) {
        view.webView.apply {
            loadUrl(url)
            webViewClient = CustomWebView().apply {
                this.onStart = {
                    view.loadingProgressBar.visibility = View.VISIBLE
                }
                this.onFinish = {
                    view.loadingProgressBar.visibility = View.GONE
                }
            }
            scrollBarStyle = WebView.SCROLLBARS_OUTSIDE_OVERLAY
            isScrollbarFadingEnabled = true
            setLayerType(View.LAYER_TYPE_HARDWARE, null)
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun setSettings(webView: WebView) {
        webView.settings.apply {
            javaScriptEnabled = true
            loadWithOverviewMode = true
            useWideViewPort = true
            setSupportMultipleWindows(true)
            builtInZoomControls = false
            layoutAlgorithm = WebSettings.LayoutAlgorithm.NORMAL
            cacheMode = WebSettings.LOAD_NO_CACHE
            domStorageEnabled = true
        }
    }

    class CustomWebView : WebViewClient() {
        var onStart: (() -> Unit)? = null
        var onFinish: (() -> Unit)? = null

        private var loadingFinished = true
        private var redirect = false

        override fun shouldOverrideUrlLoading(
                view: WebView?, request: WebResourceRequest?
        ): Boolean {
            if (!loadingFinished) {
                redirect = true
            }

            loadingFinished = false
            view?.loadUrl(request?.url.toString())
            return true
        }

        override fun onPageStarted(
                view: WebView?, url: String?, favicon: Bitmap?
        ) {
            super.onPageStarted(view, url, favicon)
            loadingFinished = false
            onStart?.invoke()
        }

        override fun onPageFinished(view: WebView?, url: String?) {
            if (!redirect) {
                loadingFinished = true
                onFinish?.invoke()
            } else {
                redirect = false
            }
        }
    }

    override fun onDestroy() {
        listener?.onDeath()
        super.onDestroy()
    }
}