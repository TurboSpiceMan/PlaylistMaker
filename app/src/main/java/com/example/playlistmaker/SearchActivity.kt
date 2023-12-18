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
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


class SearchActivity : AppCompatActivity() {
    private var constSearchValue: String = ""

    //retrofit prefs
    private val iTunesBaseUrl = "https://itunes.apple.com"
    private val retrofit = Retrofit.Builder()
        .baseUrl(iTunesBaseUrl)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
    private val itunesService = retrofit.create(ItunesAPI::class.java)

    //lists of tracks
    private val trackList = mutableListOf<Track>()
    private var trackHistory = mutableListOf<Track>()

    //adapters
    private val adapter = TracksAdapter(trackList)
    lateinit var historyAdapter : TracksAdapter

    //searchHistory class
    lateinit var searchHistory: SearchHistory

    //placeholders
    private lateinit var placeholderImageNothing: ImageView
    private lateinit var placeholderImageError: ImageView
    private lateinit var placeholderText: TextView
    private lateinit var refreshButton: Button

    //history textView && button
    private lateinit var youSearched :TextView
    private lateinit var deleteButton : Button


    @SuppressLint("NotifyDataSetChanged")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)

        //shared prefs && searchHistory class object
        val sharedPreferences = getSharedPreferences(APP_PREFERENCES, MODE_PRIVATE)
        searchHistory = SearchHistory(sharedPreferences)

        //init history track list && history adapter
        trackHistory = searchHistory.readHistory()
        historyAdapter = TracksAdapter(trackHistory)

        val backButton = findViewById<ImageView>(R.id.back_search_button)
        val inputEditText = findViewById<EditText>(R.id.inputEditText)
        val clearButton = findViewById<ImageView>(R.id.clearIcon)

        //placeholder views
        placeholderText = findViewById(R.id.placeholder_message)
        placeholderImageNothing = findViewById(R.id.placeholder_image_nothing)
        placeholderImageError = findViewById(R.id.placeholder_image_error)
        refreshButton = findViewById(R.id.refresh_button)

        //history textView && button init
        youSearched = findViewById(R.id.yourSearch)
        deleteButton = findViewById(R.id.deleteHistory)

        //history Views visibility
        fun isVisibly(check : Boolean) {
                youSearched.isVisible = check
                deleteButton.isVisible = check
        }

        //delete button listener
        deleteButton.setOnClickListener {
            searchHistory.clearHistory(trackHistory)
            historyAdapter.notifyDataSetChanged()
            isVisibly(false)
        }


        //recycler view
        val recyclerView = findViewById<RecyclerView>(R.id.trackRecycler)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter


        //add click listener to recycler view
        adapter.listener = object : OnItemClickListener {
            override fun onItemClick(item: Track) {
                searchHistory.addTrackToHistory(item, trackHistory)
                historyAdapter.notifyDataSetChanged()
            }
        }

        //savedInstanceState for search line
        if (savedInstanceState != null) {
            constSearchValue = savedInstanceState.getString(STRING_VALUE, "")
            inputEditText.setText(constSearchValue)
        }

        //inputText clear button click listener
        clearButton.setOnClickListener {
            inputEditText.setText("")
            trackList.clear()
            adapter.notifyDataSetChanged()
            val inputMethodManager =
                getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
            inputMethodManager?.hideSoftInputFromWindow(it.windowToken, 0)
        }

        //back arrow button listener
        backButton.setOnClickListener {
            this.finish()
        }

        //edit text focus listener
        inputEditText.setOnFocusChangeListener { view, hasFocus ->
            if (hasFocus && inputEditText.text.isEmpty()) {
                recyclerView.adapter = historyAdapter
                isVisibly(trackHistory.isNotEmpty())
            }else {
                recyclerView.adapter = adapter
                isVisibly(false)
            }
        }

        val textWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                clearButton.visibility = clearButtonVisibility(s)

                //switch between history and search
                if (inputEditText.hasFocus() && s?.isEmpty() == true) {
                    recyclerView.adapter = historyAdapter
                    isVisibly(trackHistory.isNotEmpty())
                } else {
                    isVisibly(false)
                    recyclerView.adapter = adapter
                }

            }

            override fun afterTextChanged(s: Editable?) {
                constSearchValue = s.toString()
            }
        }

        //add listener to edit text
        inputEditText.addTextChangedListener(textWatcher)


        fun musicSearch() {
            itunesService.search(inputEditText.text.toString())
                .enqueue(object : Callback<TrackResponse> {

                    @SuppressLint("NotifyDataSetChanged")
                    override fun onResponse(
                        call: Call<TrackResponse>,
                        response: Response<TrackResponse>
                    ) {
                        if (response.code() == 200) {
                            trackList.clear()
                            if (response.body()?.results?.isNotEmpty() == true) {
                                trackList.addAll(response.body()?.results!!)
                                adapter.notifyDataSetChanged()
                            }
                            if (trackList.isEmpty()) {
                                showMessage(getString(R.string.nothingToFind))
                            } else {
                                showMessage("")
                            }
                        }
                    }

                    override fun onFailure(call: Call<TrackResponse>, t: Throwable) {
                        showMessage(getString(R.string.noInternetError))
                    }
                })
        }

        //no internet refresh button listener
        refreshButton.setOnClickListener { musicSearch() }

        //keyboard listener for search
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
        outState.putString(STRING_VALUE, constSearchValue)
    }


    private fun clearButtonVisibility(s: CharSequence?): Int {
        return if (s.isNullOrEmpty()) {
            View.GONE
        } else {
            View.VISIBLE
        }
    }

    //switch between placeholders abd visibility
    @SuppressLint("NotifyDataSetChanged")
    private fun showMessage(text: String) {
        if (text.isNotEmpty() && text == getString(R.string.nothingToFind)) {
            placeholderImageNothing.visibility = View.VISIBLE
            placeholderImageError.visibility = View.GONE
            refreshButton.visibility = View.GONE
            placeholderText.visibility = View.VISIBLE
            placeholderText.text = text
            trackList.clear()
            adapter.notifyDataSetChanged()
        } else if (text.isNotEmpty() && text == getString(R.string.noInternetError)) {
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

    private companion object {
        const val STRING_VALUE = "STRING_VALUE"
    }
}