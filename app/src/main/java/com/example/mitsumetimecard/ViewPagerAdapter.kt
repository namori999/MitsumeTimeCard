package com.example.mitsumetimecard

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.mitsumetimecard.calendar.CalenderFragment
import com.example.mitsumetimecard.kintaitable.KintaiTableFragment
import com.example.mitsumetimecard.ui.main.MainFragment


class ViewPagerFragmentAdapter(fragmentActivity: FragmentActivity) :
    FragmentStateAdapter(fragmentActivity) {
    // tab titles
    override fun createFragment(position: Int): Fragment {
        when (position) {
            0 -> return MainFragment()
            1 -> return CalenderFragment()
            2 -> return KintaiTableFragment()
        }
        return MainFragment()
    }

    override fun getItemCount(): Int {
        return 3
    }
}
