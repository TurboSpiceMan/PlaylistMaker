package com.example.playlistmaker

import android.app.Activity
import android.content.Context
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.TypedValue
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import java.io.Serializable
import java.text.SimpleDateFormat
import java.util.Locale

class PlayerActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_player)

        val backIcon = findViewById<ImageView>(R.id.backIcon)
        backIcon.setOnClickListener {
            this.finish()
        }

        val track = getSerializable(this, "track", Track::class.java)
        val corners = dpToPx(8f,this)

        val trackImage = findViewById<ImageView>(R.id.trackImage)
        Glide.with(this)
            .load(track.getCoverArtwork())
            .placeholder(R.drawable.ic_placeholder)
            .centerCrop()
            .transform(RoundedCorners(corners))
            .into(trackImage)

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
}