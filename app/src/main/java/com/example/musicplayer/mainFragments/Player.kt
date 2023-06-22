package com.example.musicplayer.mainFragments

import android.content.Context
import android.content.res.Resources
import android.content.res.TypedArray
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.SeekBar
import androidx.fragment.app.Fragment
import com.example.musicplayer.PlayerQueue
import com.example.musicplayer.R
import com.example.musicplayer.databaseparts.MusicDatabase
import com.example.musicplayer.databinding.FragmentPlayerBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class Player(private val musicPlayer: MediaPlayer, private val playerQueue: PlayerQueue) : Fragment() {

    private lateinit var binding: FragmentPlayerBinding

    private var seekChanging = false
    private var progress1 = 0
    var audioManager: AudioManager? = null
    var audioFocusRequest: Int = 0

    val audioFocusChangeListener =
        AudioManager.OnAudioFocusChangeListener { focusChange ->
            if (focusChange == AudioManager.AUDIOFOCUS_GAIN) {
                musicPlayer.start()
            } else if (focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT) {
                musicPlayer.pause()
            } else {
                musicPlayer.pause()
            }
        }

    val playbackAttributes = AudioAttributes.Builder()
        .setUsage(AudioAttributes.USAGE_MEDIA)
        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
        .build()

    val focusRequest = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
        .setAudioAttributes(playbackAttributes)
        .setAcceptsDelayedFocusGain(true)
        .setOnAudioFocusChangeListener(audioFocusChangeListener)
        .build()


    private lateinit var layout: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentPlayerBinding.inflate(inflater, container, false)

        setAudioFocus()
        GlobalScope.launch(Dispatchers.IO) {
            playerQueue.readDatabase(MusicDatabase.getDatabase(requireContext()))
            withContext(Dispatchers.Main) {
                start()
            }
        }

        return binding.root
    }

    private fun setAudioFocus() {
        audioManager = requireContext().getSystemService(Context.AUDIO_SERVICE) as AudioManager
        audioFocusRequest  = audioManager!!.requestAudioFocus(focusRequest)
    }

    override fun onResume() {
        super.onResume()
        GlobalScope.launch(Dispatchers.IO) {
            if (playerQueue.getUpdatePlayerStart() || playerQueue.getUpdate()) {
                playerQueue.readSavedQueue()
                playerQueue.readDatabase(MusicDatabase.getDatabase(requireContext()))
                withContext(Dispatchers.Main) {
                    if (playerQueue.getUpdatePlayerStart()) {
                        start()
                        playerQueue.setUpdatePlayerStart(false)
                    } else if (playerQueue.getUpdate()) {
                        update()
                        playerQueue.setUpdate(false)
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        musicPlayer.release()
        audioManager!!.abandonAudioFocusRequest(focusRequest)
    }

    private fun start() {
        binding.previousButton.setOnClickListener { previousSong() }
        binding.nextButton.setOnClickListener { nextSong() }
        update()
        if (playerQueue.getSize() != 0) {
            if (!musicPlayer.isPlaying) {
                musicPlayer.setDataSource(playerQueue.getTrackData().track_route)
                musicPlayer.prepareAsync()
                musicPlayer.setOnPreparedListener { playerPrepared(true) }
            }
            musicPlayer.setOnCompletionListener { nextSong() }
        } else {
            binding.artist.text = ""
            binding.name.text = ""
            binding.totalTime.text = getString(R.string.duration,"0", "00")
            binding.nowTime.text = getString(R.string.duration, "0", "00")
            binding.seekBar.max = 0
        }
    }

    private fun playerPrepared(onStart: Boolean) {
        if (!onStart) {
            if (audioFocusRequest == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
                musicPlayer.start()
            }
        }
        binding.nowTime.post(mUpdateTime)
        if (musicPlayer.isPlaying) {
            if (resources.configuration.isNightModeActive) {
                binding.playPauseButton.setImageResource(R.drawable.pause)
            }
            else {
                binding.playPauseButton.setImageResource(R.drawable.pause_light)
            }
        } else {
            musicPlayer.seekTo(playerQueue.getPosition())
            if (resources.configuration.isNightModeActive) {
                binding.playPauseButton.setImageResource(R.drawable.play)
            } else {
                binding.playPauseButton.setImageResource(R.drawable.play_light)
            }
        }
    }

    private fun update() {
        layout = binding.imageL
        if (playerQueue.getSize() != 0) {
            if (playerQueue.getTrackData().track_route != null) {
                val artistG = playerQueue.getTrackData().artist_name
                val nameG = playerQueue.getTrackData().title
                val durationG = playerQueue.getTrackData().duration

                val tmp = Resources.getSystem().getDisplayMetrics().widthPixels
                val params: ViewGroup.LayoutParams = layout.layoutParams
                params.width = tmp
                params.height = tmp
                layout.layoutParams = params
                binding.artist.text = artistG
                binding.name.text = nameG
                binding.totalTime.text =
                    getString(R.string.duration, convert(durationG.toString())[0], convert(durationG.toString())[1])
                binding.nowTime.text = getString(
                    R.string.duration,
                    convert(playerQueue.getPosition().toString())[0],
                    convert(playerQueue.getPosition().toString())[1]
                )
                binding.seekBar.max = durationG
                binding.seekBar.progress = playerQueue.getPosition()
                if (playerQueue.getTrackData().image_route == null) {
                    val ta: TypedArray = requireContext().obtainStyledAttributes(
                        R.style.record,
                        intArrayOf(android.R.attr.drawable)
                    )
                    val test = ta.getDrawable(0)
                    ta.recycle()
                    binding.Cover.setImageDrawable(test)
                } else {
                    binding.Cover.setImageURI(Uri.parse(playerQueue.getTrackData().image_route))
                }
                binding.seekBar.setOnSeekBarChangeListener(object :
                    SeekBar.OnSeekBarChangeListener {
                    override fun onProgressChanged(
                        seekBar: SeekBar,
                        progress: Int,
                        fromUser: Boolean
                    ) {
                        if (fromUser) {
                            progress1 = progress
                            binding.nowTime.text = getString(
                                R.string.duration,
                                convert(binding.seekBar.progress.toString())[0],
                                convert(binding.seekBar.progress.toString())[1]
                            )
                        }
                    }

                    override fun onStartTrackingTouch(seekBar: SeekBar) {
                        seekChanging = true
                    }

                    override fun onStopTrackingTouch(seekBar: SeekBar) {
                        musicPlayer.seekTo(progress1)
                        seekChanging = false
                    }
                })
                binding.playPauseButton.setOnClickListener {
                    playPause(binding.playPauseButton, false)
                }
            }
        } else {
            musicPlayer.reset()
            playPause(binding.playPauseButton, true)
            val artistG = ""
            val nameG = ""
            val durationG = "0"

            val tmp = Resources.getSystem().getDisplayMetrics().widthPixels
            val params: ViewGroup.LayoutParams = layout.layoutParams
            params.width = tmp
            params.height = tmp
            layout.layoutParams = params
            binding.artist.text = artistG
            binding.name.text = nameG
            binding.totalTime.text = getString(R.string.duration, convert(durationG)[0], convert(durationG)[1])
            binding.nowTime.text = getString(R.string.duration, convert(durationG)[0], convert(durationG)[1])
            binding.seekBar.max = durationG.toInt()
            binding.seekBar.progress = playerQueue.getPosition()

            val ta: TypedArray = requireContext().obtainStyledAttributes(
                R.style.record,
                intArrayOf(android.R.attr.drawable)
            )
            val test = ta.getDrawable(0)
            ta.recycle()
            binding.Cover.setImageDrawable(test)
        }
    }

    private fun nextSong() {
        if (playerQueue.getSize() != 0) {
            playerQueue.nextSong()
            playerQueue.setPosition(0)
            playerQueue.saveState()
            musicPlayer.stop()
            musicPlayer.reset()
            musicPlayer.setDataSource(playerQueue.getTrackData().track_route)
            musicPlayer.prepareAsync()
            musicPlayer.setOnPreparedListener { playerPrepared(false) }
            update()
        }
    }

    private fun previousSong() {
        if (playerQueue.getSize() != 0) {
            if (musicPlayer.currentPosition > 10000) {
                musicPlayer.seekTo(0)
                playerQueue.setPosition(0)
                playerQueue.saveState()
            } else {
                playerQueue.previousSong()
                playerQueue.setPosition(0)
                playerQueue.saveState()
                musicPlayer.stop()
                musicPlayer.reset()
                musicPlayer.setDataSource(playerQueue.getTrackData().track_route)
                musicPlayer.prepareAsync()
                musicPlayer.setOnPreparedListener { playerPrepared(false) }
                update()
            }
        }
    }

    private val mUpdateTime: Runnable = object : Runnable {
        override fun run() {
            val currentDuration: Int
            if (this@Player.musicPlayer.isPlaying()) {
                currentDuration = this@Player.musicPlayer.getCurrentPosition()
                if (!seekChanging) {
                    updatePosition(currentDuration)
                    binding.seekBar.progress = currentDuration
                    playerQueue.setPosition(currentDuration)
                    playerQueue.saveState()
                }
                binding.nowTime.postDelayed(this, 10)
            } else {
                binding.nowTime.removeCallbacks(this)
            }
        }
    }

    private fun updatePosition(currentDuration: Int) {
        binding.nowTime.text = getString(R.string.duration, convert(currentDuration.toString())[0], convert(currentDuration.toString())[1])
    }

    private fun convert(time: String?): List<String> {
        val milis = time?.toLong()
        val minutes =  (milis?.div(1000) ?: 0) / 60
        val seconds = (milis?.div(1000) ?: 0) % 60
        if (seconds < 10) return listOf(minutes.toString(), "0$seconds") else return listOf(minutes.toString(), seconds.toString())
    }

    private fun playPause(playPauseButton: ImageButton, stop: Boolean) {
        if (musicPlayer.isPlaying) {
            if (resources.configuration.isNightModeActive) playPauseButton.setImageResource(R.drawable.play) else playPauseButton.setImageResource(R.drawable.play_light)
            musicPlayer.pause()
        } else {
            if (stop || playerQueue.getSize() == 0) {
                if (resources.configuration.isNightModeActive) playPauseButton.setImageResource(R.drawable.play) else playPauseButton.setImageResource(R.drawable.play_light)
                binding.nowTime.text = getString(R.string.duration, "0", "00")
                binding.seekBar.progress = 0
            } else {
                if (resources.configuration.isNightModeActive) playPauseButton.setImageResource(R.drawable.pause) else playPauseButton.setImageResource(
                    R.drawable.pause_light
                )
                binding.nowTime.post(mUpdateTime)
                if (audioFocusRequest == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
                    musicPlayer.start()
                }
            }
        }
    }
}