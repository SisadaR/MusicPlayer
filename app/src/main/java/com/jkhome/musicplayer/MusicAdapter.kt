package com.jkhome.musicplayer

import android.view.LayoutInflater
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView.*

class MusicAdapter (var musicList: MutableList<Music>) : Adapter<MusicAdapter.MusicViewHolder>() {


    class MusicViewHolder(v:View): ViewHolder(v), OnClickListener{
        private var view:View = v
        private lateinit var music : Music
        private var artisName : TextView
        private var songName: TextView

        init {
            artisName = view.findViewById(R.id.artist_text_view)
            songName = view.findViewById(R.id.song_test_view)
            view.setOnClickListener(this)
        }

        fun bindMusic(music:Music){
            this.music = music
            artisName.text = music.artistName
            songName.text = music.songName
        }

        override fun onClick(v: View?) {

        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MusicViewHolder {
        val context = parent.context
        val inflater = LayoutInflater.from(context)
        val shouldAttach = false

        val view = inflater.inflate(R.layout.music_item,parent, shouldAttach)
        return  MusicViewHolder(view)


    }

    override fun onBindViewHolder(holder: MusicViewHolder, position: Int) {
        val item = musicList[position]
        holder.bindMusic(item)

    }

    override fun getItemCount(): Int {
        return musicList.size
    }
}