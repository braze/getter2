package com.example.getter2

import android.os.Bundle
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.tabs.TabLayout
import androidx.viewpager.widget.ViewPager
import androidx.appcompat.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import androidx.core.content.ContextCompat
import androidx.viewpager.widget.ViewPager.OnPageChangeListener
import com.example.getter2.ui.main.SectionsPagerAdapter
import com.example.getter2.databinding.ActivityMainBinding
import com.google.android.material.internal.ContextUtils.getActivity

class MainActivity : AppCompatActivity() {

    lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val sectionsPagerAdapter = SectionsPagerAdapter(this, supportFragmentManager)
        val viewPager: ViewPager = binding.viewPager
        viewPager.adapter = sectionsPagerAdapter
        val tabs: TabLayout = binding.tabs
        tabs.setupWithViewPager(viewPager)
//        val fab: FloatingActionButton = binding.fab
        val fab: FloatingActionButton = findViewById(R.id.fab)
        fab.hide()

//        fab.setOnClickListener { view ->
//            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                .setAction("Action", null).show()
//        }

        viewPager.addOnPageChangeListener(object : OnPageChangeListener {
            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int
            ) {

            }

            override fun onPageSelected(position: Int) {
                when (position) {
                    0 -> fab.hide()
                    1 -> {
                        fab.show()
                        fab.setImageResource(R.drawable.ic_search)
                    }
                    2 -> {
                        fab.show()
                        fab.setImageResource(R.drawable.ic_sunny)
                    }
                }
            }
            override fun onPageScrollStateChanged(state: Int) {
//                when (state) {
//                    ViewPager.SCROLL_STATE_IDLE -> fab.show()
//                    ViewPager.SCROLL_STATE_DRAGGING, ViewPager.SCROLL_STATE_SETTLING -> fab.hide()
//                }
            }
        })
    }
}