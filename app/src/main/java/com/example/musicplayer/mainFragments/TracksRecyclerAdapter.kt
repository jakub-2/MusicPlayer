package com.example.musicplayer.mainFragments

import android.content.Context
import android.content.res.TypedArray
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.musicplayer.PlayerQueue
import com.example.musicplayer.R
import com.example.musicplayer.databaseparts.AlbumData
import com.example.musicplayer.databaseparts.MusicDatabase
import com.example.musicplayer.databaseparts.TrackData


class TracksRecyclerAdapter(
    private var context: Context?,
    track: MutableList<TrackData>,
    album: MutableList<AlbumData>,
    private var playerQueue: PlayerQueue,
    private var musicDatabase: MusicDatabase
) : RecyclerView.Adapter<TracksRecyclerAdapter.MyViewHolder>() {

    private var tracks: MutableList<TrackData> = track
    private var albums: MutableList<AlbumData> = album
    private lateinit var mListener: OnItemClickListener
    private lateinit var optionsMenuClickListener: OptionsMenuClickListener

    interface OnItemClickListener {
        fun onItemClick(position: Int)
    }

    fun setOnItemClickListener(listener: OnItemClickListener) {
        mListener = listener
    }

    interface OptionsMenuClickListener {
        fun onOptionsMenuClicked(position: Int, tracks: MutableList<TrackData>)
    }
    fun setOnOptionsMenuClickListener(listener: OptionsMenuClickListener) {
        optionsMenuClickListener = listener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val v: View = LayoutInflater.from(context).inflate(R.layout.track_item, parent, false)
        return MyViewHolder(v, mListener, optionsMenuClickListener, tracks)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.trackName.text = tracks[position].title
        holder.artistName.text = tracks[position].artist_name
        if (tracks[position].image_route != null) {
            holder.image.setImageURI(Uri.parse(tracks[position].image_route))
        } else {
            val ta: TypedArray = context!!.obtainStyledAttributes(
                R.style.record,
                intArrayOf(android.R.attr.drawable)
            )
            holder.image.setImageDrawable(ta.getDrawable(0))
            ta.recycle()
        }
    }

    override fun getItemCount(): Int {
        return tracks.size
    }

    class MyViewHolder(itemView: View, listener: OnItemClickListener, optionListener: OptionsMenuClickListener, tracks: MutableList<TrackData>) : RecyclerView.ViewHolder(itemView) {
        var image: ImageView
        var trackName: TextView
        var artistName: TextView
        private var optionsButton: TextView
        init {
            image = itemView.findViewById(R.id.track_image)
            image.layoutParams.height = 200
            image.layoutParams.width = 200
            trackName = itemView.findViewById(R.id.track_name)
            artistName = itemView.findViewById(R.id.artist_name_track)
            optionsButton = itemView.findViewById(R.id.track_option_button)

            itemView.setOnClickListener {
                listener.onItemClick(adapterPosition)
            }
            optionsButton.setOnClickListener {
                optionListener.onOptionsMenuClicked(adapterPosition, tracks)
            }
        }
    }
}