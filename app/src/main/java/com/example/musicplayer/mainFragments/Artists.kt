package com.example.musicplayer.mainFragments

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.media.MediaPlayer
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.musicplayer.PlayerQueue
import com.example.musicplayer.artistdetail.ArtistDetailActivity
import com.example.musicplayer.R
import com.example.musicplayer.databaseparts.MusicDatabase
import com.example.musicplayer.databinding.FragmentArtistsBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class Artists(private val musicPlayer: MediaPlayer, private val playerQueue: PlayerQueue) : Fragment() {
    private lateinit var binding: FragmentArtistsBinding
    private lateinit var musicDatabase: MusicDatabase

    private lateinit var resultLauncher: ActivityResultLauncher<Intent>
    private lateinit var recyclerView: RecyclerView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = FragmentArtistsBinding.inflate(layoutInflater)
        musicDatabase = MusicDatabase.getDatabase(requireContext())
        resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
                result ->

            if (result.resultCode == Activity.RESULT_OK) {
                if (result.data!!.getBooleanExtra("selected", false)) {
                    playerQueue.readSavedQueue()
                    playerQueue.setUpdatePlayerStart(true)
                    musicPlayer.reset()
                }
            } else {
                playerQueue.readUpdate()
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_artists, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        recyclerView = view.findViewById(R.id.artist_recycler)
        update()
    }

    @SuppressLint("NotifyDataSetChanged")
    fun update() {
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.setHasFixedSize(true)
        GlobalScope.launch(Dispatchers.IO) {
            val artists = musicDatabase.artistDao().getAll()
            val adapteris = ArtistsRecyclerAdapter(context, artists)
            withContext(Dispatchers.Main) {
                recyclerView.adapter = adapteris
                adapteris.setOnItemClickListener(object : ArtistsRecyclerAdapter.OnItemClickListener {
                    override fun onItemClick(position: Int) {
                        val intent = Intent(context, ArtistDetailActivity::class.java)
                        val artistName = artists[position].name

                        val bundle = Bundle()
                        bundle.putString("artist_name", artistName)
                        bundle.putInt("artist_num_of_tracks", artists[position].total_num_of_tracks)
                        bundle.putInt("artist_num_of_albums", artists[position].num_of_albums)
                        bundle.putInt("artist_duration", artists[position].duration)
                        bundle.putSerializable("queue", playerQueue)
                        intent.putExtras(bundle)
                        resultLauncher.launch(intent)
                    }
                })
                adapteris.notifyDataSetChanged()
            }
        }
    }
}