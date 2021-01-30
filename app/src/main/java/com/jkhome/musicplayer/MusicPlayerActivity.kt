package com.jkhome.musicplayer

import android.annotation.SuppressLint
import android.app.Activity
import android.content.pm.PackageManager
import android.database.Cursor
import android.media.AudioManager
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.provider.MediaStore
import android.util.Log
import android.webkit.MimeTypeMap
import android.widget.MediaController
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.CodeBoy.MediaFacer.AudioGet
import com.CodeBoy.MediaFacer.MediaFacer
import com.CodeBoy.MediaFacer.mediaHolders.audioContent
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.lang.StringBuilder
import java.util.concurrent.TimeUnit

class MusicPlayerActivity : AppCompatActivity() , ItemCLicked {

    private var musicList: MutableList<Music> = mutableListOf()
    private lateinit var linearLayoutManager : LinearLayoutManager
    private lateinit var adapter: MusicAdapter
    private var currentPosition: Int = 0
    private var state:Boolean = false
    private var mediaPlayer : MediaPlayer? = null
    companion object{
        private const val REQUEST_CODE_EXTERNAL_STORAGE = 1
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_music_player)

        if (Build.VERSION.SDK_INT >= 23)
            checkPermissions()

        findViewById<FloatingActionButton>(R.id.fab_play).setOnClickListener{
            play(currentPosition)
        }

        findViewById<FloatingActionButton>(R.id.fab_next).setOnClickListener{
            mediaPlayer!!.stop()
            state = false

            if(currentPosition == musicList.size - 1)
            {

            }
            else {
                currentPosition++
                play(currentPosition)
            }
        }

        findViewById<FloatingActionButton>(R.id.fab_previous).setOnClickListener{
            mediaPlayer!!.stop()
            state = false

            if(currentPosition == 0)
            {
                
            }
            else {
                currentPosition--
                play(currentPosition)
            }
        }
    }

    private fun play(currentPosition: Int)
    {
        if(!state){
            findViewById<FloatingActionButton>(R.id.fab_play).setImageDrawable( resources.getDrawable(R.drawable.ic_stop))
            state = true
            mediaPlayer = MediaPlayer().apply {
                setAudioStreamType(AudioManager.STREAM_MUSIC)
                setDataSource(this@MusicPlayerActivity, Uri.parse(musicList[currentPosition].songUri))
                prepare()
                start()
            }
        }
        else{
            state = false
            mediaPlayer!!.stop()
            findViewById<FloatingActionButton>(R.id.fab_play).setImageDrawable( resources.getDrawable(R.drawable.ic_play_arrow))
        }

        val mHandler = Handler()
        this@MusicPlayerActivity.runOnUiThread(object : Runnable{
            override fun run() {
                val playerPosition = mediaPlayer?.currentPosition!! / 1000
                val totalDuration = mediaPlayer?.duration!! / 1000

                val seekBar = findViewById<SeekBar>(R.id.seek_bar)
                seekBar.max = totalDuration
                seekBar.progress = playerPosition

                val pastTextView = findViewById<TextView>(R.id.past_text_view)
                pastTextView.text = timerFormat( playerPosition.toLong())

                val remainTextView = findViewById<TextView>(R.id.remain_text_view)
                remainTextView.text = timerFormat(totalDuration - playerPosition.toLong())

                mHandler.postDelayed(this,1000)
            }

        })
    }

    fun timerFormat(time:Long):String{
        val result = String.format("%02d:%02d",
                TimeUnit.SECONDS.toMinutes(time),
                TimeUnit.SECONDS.toSeconds(time) - TimeUnit.MINUTES.toSeconds(TimeUnit.SECONDS.toMinutes(time)))
        var convert = ""
        for(element in result)
            convert += element

        return convert
    }

    private fun getSongsUsingLibMediaFacer(){
                var audioContents: ArrayList<audioContent> = MediaFacer
                .withAudioContex(this)
                .getAllAudioContent(AudioGet.externalContentUri);

        Log.d("DEBUG_MUSIC", audioContents.size.toString())
        for (i in audioContents) {
            var t = i.title;

            Log.d("DEBUG_MUSIC", "$t")
        }

        var audioContentsEx: ArrayList<audioContent> = MediaFacer
                .withAudioContex(this)
                .getAllAudioContent(AudioGet.externalContentUri);

        Log.d("DEBUG_MUSIC", audioContentsEx.size.toString())
        for (i in audioContentsEx) {
            var t = i.title;

            Log.d("DEBUG_MUSIC", "$t")
        }

        return;
    }

    private fun getSongs(){
        val selection = MediaStore.Audio.Media.IS_MUSIC + "=1";
        //val sortOrder = MediaStore.Audio.Media.TITLE + " ASC"
        val projection = arrayOf(
                MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.DATA,
        )
        var args = arrayOf(
                MimeTypeMap.getSingleton().getMimeTypeFromExtension("mp3")
        )

        val cursor : Cursor? = contentResolver.query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                projection,
                selection,
                null,
               null)


        while (cursor!!.moveToNext()){
            var music = Music(cursor.getString(0), cursor.getString(1),cursor.getString(2))
            musicList.add(music)
        }
        cursor.close()

        linearLayoutManager = LinearLayoutManager(this)
        adapter = MusicAdapter(musicList,this)
        findViewById<RecyclerView>(R.id.recyclerView).layoutManager = linearLayoutManager
        findViewById<RecyclerView>(R.id.recyclerView).adapter = adapter

    }

    private fun checkPermissions(){
        if (ActivityCompat.checkSelfPermission(this,android.Manifest.permission.READ_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED){
            getSongs()
        }else{
                //false -> user asked not to ask me any more/permission disalbed
                //true ->  rejected before want to use this feature again
            if(ActivityCompat.shouldShowRequestPermissionRationale(this,android.Manifest.permission.READ_EXTERNAL_STORAGE))
            {
                Toast.makeText(this,"Music Player need to access to your files", Toast.LENGTH_SHORT).show()
            }
            ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE),
                    REQUEST_CODE_EXTERNAL_STORAGE)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {

        when(requestCode){
            REQUEST_CODE_EXTERNAL_STORAGE ->
                if(grantResults[0] == PackageManager.PERMISSION_GRANTED){

                } else {
                    Toast.makeText(this,"Permission not granted", Toast.LENGTH_SHORT).show()
                }
            else ->   super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }

    }

    override fun itemClicked(position: Int) {
        state = false
        mediaPlayer?.stop()
        this.currentPosition = position
        play(this.currentPosition)
    }
}