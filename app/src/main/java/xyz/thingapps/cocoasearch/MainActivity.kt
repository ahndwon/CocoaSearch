package xyz.thingapps.cocoasearch

import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.activity_main.*
import xyz.thingapps.cocoasearch.ui.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        supportFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, SearchFragment())
                .commit()
    }

    companion object {
        val TAG = MainActivity::class.java.name
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun setFilterBarVisibility(visibility: Int) {
        filterBarContainer.visibility = visibility
    }

    private fun setActionBarHomeUp(enabled: Boolean) {
        supportActionBar?.setDisplayHomeAsUpEnabled(enabled)
    }

    private fun setActionBarTitle(title: String) {
        supportActionBar?.title = title
    }

    override fun onAttachFragment(fragment: Fragment) {
        when (fragment) {
            is ImageDetailFragment -> {
                fragment.listener = object : FragmentLifeListener {
                    override fun onBirth() {
                        setFilterBarVisibility(View.GONE)
                        setActionBarHomeUp(true)
                        setActionBarTitle(getString(R.string.see_detail))
                    }

                    override fun onDeath() {
                        setFilterBarVisibility(View.VISIBLE)
                        setActionBarHomeUp(false)
                        setActionBarTitle(getString(R.string.developer_name))
                    }

                }
            }

            is WebViewFragment -> {
                fragment.listener = object : FragmentLifeListener {
                    override fun onBirth() {
                        setFilterBarVisibility(View.GONE)
                        setActionBarHomeUp(true)
                        setActionBarTitle(getString(R.string.website))
                    }

                    override fun onDeath() {
                        setActionBarTitle(getString(R.string.see_detail))
                    }
                }
            }

            is HashTagFragment -> {
                fragment.listener = object : HashTagFragment.TitleFragmentLifeListener {
                    override fun onBirth(title: String) {
                        setFilterBarVisibility(View.GONE)
                        setActionBarHomeUp(true)
                        setActionBarTitle(getString(R.string.format_hash_tag, title))
                    }

                    override fun onDeath() {
                        setActionBarTitle(getString(R.string.see_detail))
                    }
                }
            }
        }
    }
}