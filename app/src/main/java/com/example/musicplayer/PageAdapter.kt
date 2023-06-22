package com.example.musicplayer

import android.media.MediaPlayer
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.musicplayer.mainFragments.Albums
import com.example.musicplayer.mainFragments.Artists
import com.example.musicplayer.mainFragments.Player
import com.example.musicplayer.mainFragments.Tracks

class PageAdapter(
    fragmentActivity: FragmentActivity,
    private val musicPlayer: MediaPlayer,
    private val playerQueue: PlayerQueue
) : FragmentStateAdapter(fragmentActivity) {

    override fun createFragment(position: Int): Fragment {
        when(position) {
            0 -> return Player(musicPlayer, playerQueue)
            1 -> return Artists(musicPlayer, playerQueue)
            2 -> return Albums(musicPlayer, playerQueue)
            3 -> return Tracks(musicPlayer, playerQueue)
        }
        return Player(musicPlayer, playerQueue)
    }

    override fun getItemCount(): Int {
        return 4
    }
}