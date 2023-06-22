package com.example.musicplayer.artistdetail

import android.content.Context
import android.content.res.TypedArray
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.musicplayer.R
import com.example.musicplayer.databaseparts.AlbumData

class ArtistDetailRecyclerAdapter(private var context: Context?, album: List<AlbumData>) :
    RecyclerView.Adapter<ArtistDetailRecyclerAdapter.MyViewHolder>() {
    private var albums: List<AlbumData> = album
    private lateinit var mListener: OnItemClickListener

    interface OnItemClickListener {
        fun onItemClick(position: Int)
    }

    fun setOnItemClickListener(listener: OnItemClickListener) {
        mListener = listener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val v: View = LayoutInflater.from(context).inflate(R.layout.album_detail_item, parent, false)
        return MyViewHolder(v, mListener)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.name.text = albums[position].album_name
        if (albums[position].release_year == null) {
            holder.year.text = ""
        } else {
            holder.year.text = albums[position].release_year.toString()
        }
        if (albums[position].image_route != null) {
            holder.image.setImageURI(Uri.parse(albums[position].image_route))
        } else {
            val ta: TypedArray = context!!.obtainStyledAttributes(
                R.style.record,
                intArrayOf(android.R.attr.drawable)
            )
            val test = ta.getDrawable(0)
            ta.recycle()
            holder.image.setImageDrawable(test)
        }

    }

    override fun getItemCount(): Int {
        return albums.size
    }

    class MyViewHolder(itemView: View, listener: OnItemClickListener) : RecyclerView.ViewHolder(itemView) {
        var image: ImageView
        var name: TextView
        var year: TextView
        init {
            image = itemView.findViewById(R.id.artist_detail_album_image)
            image.layoutParams.height = 200
            image.layoutParams.width = 200
            name = itemView.findViewById(R.id.artist_detail_album_name)
            year = itemView.findViewById(R.id.artist_detail_album_year)

            itemView.setOnClickListener {
                listener.onItemClick(adapterPosition)
            }
        }
    }

}