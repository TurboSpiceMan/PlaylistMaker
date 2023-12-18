package com.example.playlistmaker

import android.annotation.SuppressLint
import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
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
    lateinit var historyAdapter: TracksAdapter

    //searchHistory class
    lateinit var searchHistory: SearchHistory

    //placeholders
    private lateinit var placeholderImageNothing: ImageView
    private lateinit var placeholderImageError: ImageView
    private lateinit var placeholderText: TextView
    private lateinit var refreshButton: Button

    //history textView && button
    private lateinit var youSearched: TextView
    private lateinit var deleteButton: Button


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
        fun historyViewVisibility(check: Boolean) {
            youSearched.isVisible = check
            deleteButton.isVisible = check
        }

        //deleteButton listener
        deleteButton.setOnClickListener {
            searchHistory.clearHistory(trackHistory)
            historyAdapter.notifyDataSetChanged()
            historyViewVisibility(false)
        }


        //recycler view
        val recyclerView = findViewById<RecyclerView>(R.id.trackRecycler)
        recyclerView.layoutManager = LinearLayoutManager(this)


        //add click listener to recycler view
        adapter.listener = OnItemClickListener { item ->
            searchHistory.addTrackToHistory(item, trackHistory)
            historyAdapter.notifyDataSetChanged()
        }

        //savedInstanceState for search line
        if (savedInstanceState != null) {
            constSearchValue = savedInstanceState.getString(STRING_VALUE, "")
            inputEditText.setText(constSearchValue)
        }

        //set history adapter if history list isNotEmpty
        if (trackHistory.isNotEmpty()) {
            recyclerView.adapter = historyAdapter
            historyViewVisibility(trackHistory.isNotEmpty())
        }

        //inputText clear button click listener
        clearButton.setOnClickListener {
            inputEditText.setText("")
            showMessage("")
            trackList.clear()
            adapter.notifyDataSetChanged()
            val inputMethodManager =
                getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
            inputMethodManager?.hideSoftInputFromWindow(it.windowToken, 0)
        }

        //back arrow button listener
        backButton.setOnClickListener {
            finish()
        }

        //edit text focus listener
        inputEditText.setOnFocusChangeListener { view, hasFocus ->
            if (hasFocus && inputEditText.text.isEmpty()) {
                recyclerView.adapter = historyAdapter
                historyViewVisibility(trackHistory.isNotEmpty())
            } else {
                recyclerView.adapter = adapter
                historyViewVisibility(false)
            }
        }

        val textWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (s != null) {
                    clearButton.isVisible = s.isNotEmpty()
                }

                //if clear edit text, placeHolders will gone
                if (s != null) {
                    if (s.isEmpty()) {
                        showMessage("")
                    }

                    //switch between history and search
                        if (inputEditText.hasFocus() && s?.isEmpty() == true) {
                            recyclerView.adapter = historyAdapter
                            historyViewVisibility(trackHistory.isNotEmpty())
                        } else {
                            historyViewVisibility(false)
                            recyclerView.adapter = adapter
                        }
                }
            }

            override fun afterTextChanged(s: Editable?) {
                constSearchValue = s.toString()
            }
        }

        //add listener to edit text
        inputEditText.addTextChangedListener(textWatcher)


        fun searchMusic() {
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
                            if (trackList.isEmpty() && inputEditText.text.isNotEmpty()) {
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
        refreshButton.setOnClickListener { searchMusic() }

        //keyboard listener for search
        inputEditText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                searchMusic()
                true
            } else {
                false
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(STRING_VALUE, constSearchValue)
    }


    //switch between placeholders abd visibility
    @SuppressLint("NotifyDataSetChanged")
    private fun showMessage(text: String) {
        if (text.isNotEmpty() && text == getString(R.string.nothingToFind)) {
            placeholderImageNothing.isVisible = true
            placeholderImageError.isVisible = false
            refreshButton.isVisible = false
            placeholderText.isVisible = true
            placeholderText.text = text
            trackList.clear()
            adapter.notifyDataSetChanged()
        } else if (text.isNotEmpty() && text == getString(R.string.noInternetError)) {
            placeholderImageError.isVisible = true
            refreshButton.isVisible = true
            placeholderImageNothing.isVisible = false
            placeholderText.isVisible = true
            placeholderText.text = text
            trackList.clear()
            adapter.notifyDataSetChanged()
        } else {
            placeholderImageNothing.isVisible = false
            placeholderImageError.isVisible = false
            placeholderText.isVisible = false
            refreshButton.isVisible = false
        }
    }

    private companion object {
        const val STRING_VALUE = "STRING_VALUE"
    }
}