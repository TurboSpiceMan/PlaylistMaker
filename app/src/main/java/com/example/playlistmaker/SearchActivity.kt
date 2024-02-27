package com.example.playlistmaker

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.ProgressBar
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

    //handler
    private var mainThreadHandler: Handler? = null

    //click debounce boolean
    private var isClickAllowed = true

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

    //progress bar
    private lateinit var progressBar: ProgressBar


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

        mainThreadHandler = Handler(Looper.getMainLooper())


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

        //progress bar init
        progressBar = findViewById(R.id.progressBar)

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
        adapter.listener = OnItemClickListener { item: Track ->
            if (clickDebounce()) {
                searchHistory.addTrackToHistory(item, trackHistory)
                historyAdapter.notifyDataSetChanged()
                val intent = Intent(this, PlayerActivity::class.java).apply {
                    putExtra("track", item)
                }
                startActivity(intent)

            }
        }

        historyAdapter.listener = OnItemClickListener { item: Track ->
            if (clickDebounce()) {
                val intent = Intent(this, PlayerActivity::class.java).apply {
                    putExtra("track", item)
                }
                startActivity(intent)
            }

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
            recyclerView.isVisible = true
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

        fun searchMusic() {
            if (inputEditText.text.isNotEmpty()) {
                showMessage("")
                recyclerView.isVisible = false
                progressBar.isVisible = true

                itunesService.search(inputEditText.text.toString())
                    .enqueue(object : Callback<TrackResponse> {

                        @SuppressLint("NotifyDataSetChanged")
                        override fun onResponse(
                            call: Call<TrackResponse>,
                            response: Response<TrackResponse>
                        ) {
                            progressBar.isVisible = false
                            if (response.code() == 200) {
                                trackList.clear()
                                val results = response.body()?.results ?: listOf()
                                if (results.isNotEmpty()) {
                                    recyclerView.isVisible = true
                                    trackList.addAll(results)
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
                            progressBar.isVisible = false
                            showMessage(getString(R.string.noInternetError))
                        }
                    })
            }
        }

        //search with debounce
        val searchRunnable = Runnable { searchMusic() }
        fun searchDebounce () {
            mainThreadHandler?.removeCallbacks(searchRunnable)
            mainThreadHandler?.postDelayed(searchRunnable, SEARCH_DELAY)
        }

        val textWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                searchDebounce()
                if (s != null) {
                    clearButton.isVisible = s.isNotEmpty()

                    //if clear edit text, placeHolders will gone
                    if (s.isEmpty()) {
                        showMessage("")
                    }

                    //switch between history and search
                    if (inputEditText.hasFocus() && s?.isEmpty() == true) {
                        recyclerView.isVisible = true
                        recyclerView.adapter = historyAdapter
                        trackList.clear()
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

        //no internet refresh button listener
        refreshButton.setOnClickListener { searchMusic() }
    }

    private fun clickDebounce () : Boolean {
        val current = isClickAllowed
        if (isClickAllowed) {
            isClickAllowed = false
            mainThreadHandler?.postDelayed({isClickAllowed = true}, CLICK_DELAY)
        }
        return current
    }

    override fun onDestroy() {
        super.onDestroy()
        mainThreadHandler?.removeCallbacksAndMessages(null)
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
        private const val SEARCH_DELAY = 2000L
        private const val CLICK_DELAY = 1000L
    }
}

