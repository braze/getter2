package com.example.getter2.ui.main

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.example.getter2.R
import com.example.getter2.flip.FlipFragment
import com.example.getter2.name.NameFragment
import com.example.getter2.sunset.SunsetFragment

private val TAB_TITLES = arrayOf(
    R.string.tab_text_1,
    R.string.tab_text_2,
    R.string.tab_text_3
)

/**
 * A [FragmentPagerAdapter] that returns a fragment corresponding to
 * one of the sections/tabs/pages.
 */
class SectionsPagerAdapter(private val context: Context, fm: FragmentManager) :
    FragmentPagerAdapter(fm) {

    override fun getItem(position: Int): Fragment {
        // getItem is called to instantiate the fragment for the given page.
        // Return a PlaceholderFragment (defined as a static inner class below).
        when (position) {
            0 -> return FlipFragment.newInstance(1)
            1 -> return NameFragment.newInstance(2)
            2 -> return SunsetFragment.newInstance(3)
        }
        return PlaceholderFragment.newInstance(1)
    }

    override fun getPageTitle(position: Int): CharSequence {
        return context.resources.getString(TAB_TITLES[position])
    }

    override fun getCount(): Int {
        // Shows total pages.
        return TAB_TITLES.size
    }
}