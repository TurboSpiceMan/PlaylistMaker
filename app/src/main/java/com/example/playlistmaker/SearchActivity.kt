package com.example.playlistmaker

import android.annotation.SuppressLint
import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class SearchActivity : AppCompatActivity() {
    private var constSearchValue: String = ""
    private val ItunesBaseUrl = "https://itunes.apple.com"

    private val retrofit = Retrofit.Builder()
        .baseUrl(ItunesBaseUrl)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val itunesService = retrofit.create(ItunesAPI::class.java)

    private val trackList =  ArrayList<Track>()
    private val adapter = TracksAdapter()

    private lateinit var placeholderImageNothing: ImageView
    private lateinit var placeholderImageError: ImageView
    private lateinit var placeholderText: TextView
    private lateinit var refreshButton: Button


    @SuppressLint("NotifyDataSetChanged")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)

        val backButton = findViewById<ImageView>(R.id.back_search_button)
        val inputEditText = findViewById<EditText>(R.id.inputEditText)
        val clearButton = findViewById<ImageView>(R.id.clearIcon)

        placeholderText = findViewById(R.id.placeholder_message)
        placeholderImageNothing = findViewById(R.id.placeholder_image_nothing)
        placeholderImageError = findViewById(R.id.placeholder_image_error)
        refreshButton = findViewById(R.id.refresh_button)


        val recyclerView = findViewById<RecyclerView>(R.id.trackRecycler)

        adapter.tracks = trackList

        recyclerView.layoutManager = LinearLayoutManager(this)

        recyclerView.adapter = adapter

        if(savedInstanceState != null){
            constSearchValue = savedInstanceState.getString(STRING_VALUE, "")
            inputEditText.setText(constSearchValue)
        }

        clearButton.setOnClickListener {
            inputEditText.setText("")
            trackList.clear()
            adapter.notifyDataSetChanged()
            val inputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
            inputMethodManager?.hideSoftInputFromWindow(it.windowToken, 0)
        }

        backButton.setOnClickListener{
            this.finish()
        }


        val textWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                clearButton.visibility = clearButtonVisibility(s)
            }

            override fun afterTextChanged(s: Editable?) {
                constSearchValue = s.toString()
            }
        }

        inputEditText.addTextChangedListener(textWatcher)

        fun musicSearch(){
            itunesService.search(inputEditText.text.toString()).enqueue(object: Callback<TrackResponse>{

                @SuppressLint("NotifyDataSetChanged")
                override fun onResponse(
                    call: Call<TrackResponse>,
                    response: Response<TrackResponse>
                ) {
                    if(response.code() == 200){
                        trackList.clear()
                        if (response.body()?.results?.isNotEmpty() == true){
                            trackList.addAll(response.body()?.results!!)
                            adapter.notifyDataSetChanged()
                        }
                        if(trackList.isEmpty()){
                            showMessage(getString(R.string.nothingToFind))
                        } else{
                           showMessage("")
                        }
                    }
                }
                override fun onFailure(call: Call<TrackResponse>, t: Throwable) {
                    showMessage(getString(R.string.noInternetError))
                }
            })
        }
        refreshButton.setOnClickListener { musicSearch() }

        inputEditText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                musicSearch()
                true
            }
            false
        }
    }


    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(STRING_VALUE,constSearchValue)
    }


    private fun clearButtonVisibility(s: CharSequence?): Int {
        return if (s.isNullOrEmpty()) {
            View.GONE
        } else {
            View.VISIBLE
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun showMessage(text: String){
        if(text.isNotEmpty() && text == getString(R.string.nothingToFind)){
            placeholderImageNothing.visibility = View.VISIBLE
            placeholderImageError.visibility = View.GONE
            refreshButton.visibility = View.GONE
            placeholderText.visibility = View.VISIBLE
            placeholderText.text = text
            trackList.clear()
            adapter.notifyDataSetChanged()
        }else if (text.isNotEmpty() && text == getString(R.string.noInternetError)){
            placeholderImageError.visibility = View.VISIBLE
            refreshButton.visibility = View.VISIBLE
            placeholderImageNothing.visibility = View.GONE
            placeholderText.visibility = View.VISIBLE
            placeholderText.text = text
            trackList.clear()
            adapter.notifyDataSetChanged()
        } else {
            placeholderImageNothing.visibility = View.GONE
            placeholderImageError.visibility = View.GONE
            placeholderText.visibility = View.GONE
            refreshButton.visibility = View.GONE
        }
    }

   private companion object{
        const val STRING_VALUE = "STRING_VALUE"
    }
}