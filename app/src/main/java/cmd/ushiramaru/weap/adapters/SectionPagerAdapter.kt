package cmd.ushiramaru.weap.adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import cmd.ushiramaru.weap.fragments.ChatsFragment
import cmd.ushiramaru.weap.fragments.StatusListFragment
import cmd.ushiramaru.weap.fragments.StatusUpdateFragment

class SectionPagerAdapter(fm: FragmentManager) : FragmentPagerAdapter(fm) {

    private val chatsFragment = ChatsFragment()
    private val statusUpdateFragment =
        StatusUpdateFragment()
    private val statusFragment =
        StatusListFragment()

    override fun getItem(position: Int): Fragment {
        return when (position) {
            0 -> statusUpdateFragment
            1 -> chatsFragment
            2 -> statusFragment
            else -> chatsFragment
        }
    }

    override fun getCount(): Int {
        return 3
    }
}