package xyz.thingapps.cocoasearch

import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.activity_main.*
import xyz.thingapps.cocoasearch.ui.FragmentLifeListener
import xyz.thingapps.cocoasearch.ui.ImageDetailFragment
import xyz.thingapps.cocoasearch.ui.SearchFragment

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

    override fun onAttachFragment(fragment: Fragment) {
        if (fragment is ImageDetailFragment) {
            fragment.listener = object : FragmentLifeListener {
                override fun onBirth() {
                    filterBarContainer.visibility = View.GONE
                    supportActionBar?.setDisplayHomeAsUpEnabled(true)
                    supportActionBar?.title = "상세보기"
                }

                override fun onDeath() {
                    filterBarContainer.visibility = View.VISIBLE
                    supportActionBar?.setDisplayHomeAsUpEnabled(false)
                    supportActionBar?.title = getString(R.string.developer_name)
                }

            }
        }
    }

}