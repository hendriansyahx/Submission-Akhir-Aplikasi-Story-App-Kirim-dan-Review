package com.callcenter.storydicoding.view.story

import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.callcenter.storydicoding.R
import com.callcenter.storydicoding.data.network.ApiClient
import com.callcenter.storydicoding.data.pref.UserPreference
import com.callcenter.storydicoding.data.viewmodel.StoryViewModel
import com.callcenter.storydicoding.data.viewmodel.StoryViewModelFactory
import com.callcenter.storydicoding.view.map.MapActivity
import com.callcenter.storydicoding.view.story.adapter.StoryAdapter
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.launch
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import androidx.datastore.preferences.preferencesDataStore
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build

val Context.dataStore by preferencesDataStore(name = "user_prefs")

class ListStoryActivity : AppCompatActivity() {

    private lateinit var storyAdapter: StoryAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private val viewModel: StoryViewModel by viewModels { StoryViewModelFactory(UserPreference.getInstance(dataStore), ApiClient.apiService) }
    private var errorDialog: Dialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_list_story)

        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "List Stories"

        toolbar.navigationIcon?.setColorFilter(getColor(R.color.white), PorterDuff.Mode.SRC_ATOP)

        recyclerView = findViewById(R.id.recyclerViewStories)
        progressBar = findViewById(R.id.progressBar)
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout)
        recyclerView.layoutManager = LinearLayoutManager(this)

        storyAdapter = StoryAdapter { storyId ->
            val intent = Intent(this@ListStoryActivity, DetailStoryActivity::class.java)
            intent.putExtra("story_id", storyId)
            startActivity(intent)
        }
        recyclerView.adapter = storyAdapter

        lifecycleScope.launch {
            viewModel.getSession().collect { userModel ->
                fetchStories(userModel.token)
            }
        }

        val fabAddStory: FloatingActionButton = findViewById(R.id.fabAddStory)
        fabAddStory.setOnClickListener {
            startActivity(Intent(this, AddStoryActivity::class.java))
        }

        swipeRefreshLayout.setOnRefreshListener {
            lifecycleScope.launch {
                viewModel.getSession().collect { userModel ->
                    fetchStories(userModel.token)
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        lifecycleScope.launch {
            viewModel.getSession().collect { userModel ->
                fetchStories(userModel.token)
            }
        }
    }

    object NetworkUtil {
        fun isInternetAvailable(context: Context): Boolean {
            val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                val networkCapabilities = connectivityManager.activeNetwork?.let {
                    connectivityManager.getNetworkCapabilities(it)
                }
                networkCapabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true
            } else {
                val activeNetworkInfo = connectivityManager.activeNetworkInfo
                activeNetworkInfo != null && activeNetworkInfo.isConnected
            }
        }
    }

    private fun fetchStories(token: String) {
        if (!NetworkUtil.isInternetAvailable(this)) {
            showErrorDialog("No internet connection.")
            return
        }

        progressBar.visibility = View.VISIBLE
        swipeRefreshLayout.isRefreshing = true

        viewModel.getStoriesFlow(token).observe(this) { pagingData ->
            storyAdapter.submitData(lifecycle, pagingData)
            progressBar.visibility = View.GONE
            swipeRefreshLayout.isRefreshing = false
        }
    }

    private fun showErrorDialog(message: String) {
        errorDialog?.dismiss()

        errorDialog = Dialog(this).apply {
            setContentView(R.layout.dialog_error)

            val textViewErrorMessage: TextView = findViewById(R.id.textViewErrorMessage)
            val buttonDismiss: Button = findViewById(R.id.buttonDismiss)

            textViewErrorMessage.text = message

            buttonDismiss.setOnClickListener {
                dismiss()
            }

            window?.setLayout(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

            show()
        }
    }


    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.toolbar_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_map -> {
                startActivity(Intent(this, MapActivity::class.java))
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        errorDialog?.dismiss()
    }
}
