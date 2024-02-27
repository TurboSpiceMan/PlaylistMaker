package com.example.playlistmaker

import android.app.Activity
import android.content.Context
import android.media.MediaPlayer
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.TypedValue
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import java.io.Serializable
import java.text.SimpleDateFormat
import java.util.Locale
import kotlin.properties.Delegates

class PlayerActivity : AppCompatActivity() {

    private var playerState = STATE_DEFAULT
    private lateinit var playButton : ImageView
    private var mediaPlayer  = MediaPlayer()
    private lateinit var trackPreview : String
    private lateinit var trackTime : TextView
    private var mainThreadHandler: Handler? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_player)

        val backIcon = findViewById<ImageView>(R.id.backIcon)
        backIcon.setOnClickListener {
            this.finish()
        }

        //handler on main thread
        mainThreadHandler = Handler(Looper.getMainLooper())

        val track = getSerializable(this, "track", Track::class.java)
        val corners = dpToPx(8f,this)

        //track source
        trackPreview = track.previewUrl

        //player button logic
        preparePlayer()
        playButton = findViewById(R.id.playButton)
        playButton.setOnClickListener {
            playBackControl()
            when (playerState) {
                STATE_PLAYING -> {
                    mainThreadHandler?.post (trackTimer())
                }
                STATE_PAUSED -> {
                    mainThreadHandler?.removeCallbacks(trackTimer())
                }
                STATE_PREPARED -> {
                    mainThreadHandler?.removeCallbacks (trackTimer())
                    trackTime.text = SimpleDateFormat("mm:ss", Locale.getDefault()).format(0)
                }
            }
        }


        val trackImage = findViewById<ImageView>(R.id.trackImage)
        Glide.with(this)
            .load(track.getCoverArtwork())
            .placeholder(R.drawable.ic_placeholder)
            .centerCrop()
            .transform(RoundedCorners(corners))
            .into(trackImage)


        trackTime = findViewById(R.id.playedTime)
        trackTime.text = SimpleDateFormat("mm:ss", Locale.getDefault()).format(0)

        val trackName = findViewById<TextView>(R.id.trackName)
        trackName.text = track.trackName

        val artistName = findViewById<TextView>(R.id.artistName)
        artistName.text = track.artistName

        val duration = findViewById<TextView>(R.id.durationText)
        duration.text = SimpleDateFormat("mm:ss", Locale.getDefault()).format(track.trackTimeMillis)

        val album = findViewById<TextView>(R.id.albumText)
        album.text = track.collectionName

        val year = findViewById<TextView>(R.id.yearText)
        year.text = track.releaseDate.take(4)

        val style = findViewById<TextView>(R.id.styleText)
        style.text = track.primaryGenreName

        val country = findViewById<TextView>(R.id.countryText)
        country.text = track.country

        mediaPlayer.setOnCompletionListener {
            mainThreadHandler?.removeCallbacksAndMessages(null)
            trackTime.text = SimpleDateFormat("mm:ss", Locale.getDefault()).format(0)
            playButton.setImageResource(R.drawable.ic_play_button)
        }

    }

    override fun onPause() {
        super.onPause()
        pausePlayer()
        mainThreadHandler?.removeCallbacks (trackTimer())
    }

    override fun onDestroy() {
        super.onDestroy()
        mainThreadHandler?.removeCallbacksAndMessages (null)
        mediaPlayer.release()
    }

    fun <T : Serializable?> getSerializable(activity: Activity, name: String, clazz: Class<T>): T
    {
        return if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
            activity.intent.getSerializableExtra(name, clazz)!!
        else
            activity.intent.getSerializableExtra(name) as T
    }

    private fun dpToPx(dp: Float, context: Context): Int {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            dp,
            context.resources.displayMetrics).toInt()
    }

    private fun trackTimer () : Runnable {
        return object : Runnable {
            override fun run() {
                trackTime.text = SimpleDateFormat("mm:ss", Locale.getDefault()).format(mediaPlayer.currentPosition)
                mainThreadHandler?.postDelayed (this, DELAY)
            }
        }
    }

    //checking player state
    private fun playBackControl(){
        when(playerState) {
            STATE_PLAYING -> {
                pausePlayer()
            }
            STATE_PREPARED, STATE_PAUSED -> {
                startPlayer()
            }
        }
    }

    //player preparing function
    private fun preparePlayer() {
        mediaPlayer.setDataSource(trackPreview)
        mediaPlayer.prepareAsync()
        mediaPlayer.setOnPreparedListener{
            playerState = STATE_PREPARED
        }
        mediaPlayer.setOnSeekCompleteListener {
            playerState = STATE_PREPARED
        }
    }

    //start player function
    private fun startPlayer() {
        mediaPlayer.start()
        playButton.setImageResource(R.drawable.ic_pause_button)
        playerState = STATE_PLAYING
    }

    //pause player function
    private fun pausePlayer() {
        mediaPlayer.pause()
        playButton.setImageResource(R.drawable.ic_play_button)
        playerState = STATE_PAUSED
    }

    companion object {
        private const val STATE_DEFAULT = 0
        private const val STATE_PREPARED = 1
        private const val STATE_PLAYING = 2
        private const val STATE_PAUSED = 3
        private const val DELAY = 300L
    }
}