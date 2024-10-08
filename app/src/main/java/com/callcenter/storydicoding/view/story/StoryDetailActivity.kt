package com.callcenter.storydicoding.view.story

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.lifecycleScope
import com.callcenter.storydicoding.R
import com.callcenter.storydicoding.data.network.ApiClient
import com.callcenter.storydicoding.data.network.ApiClient.apiService
import com.callcenter.storydicoding.data.pref.UserPreference
import com.callcenter.storydicoding.data.pref.dataStore
import com.callcenter.storydicoding.data.viewmodel.StoryViewModel
import com.callcenter.storydicoding.data.viewmodel.StoryViewModelFactory
import com.squareup.picasso.Picasso
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException

class DetailStoryActivity : AppCompatActivity() {

    private lateinit var progressBar: ProgressBar
    private lateinit var tvName: TextView
    private lateinit var tvDescription: TextView
    private lateinit var ivPhoto: ImageView
    private lateinit var toolbar: Toolbar
    private lateinit var cardView: androidx.cardview.widget.CardView

    private val storyViewModel: StoryViewModel by viewModels {
        StoryViewModelFactory(UserPreference.getInstance(dataStore), apiService)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail_story)

        progressBar = findViewById(R.id.progressBar)
        tvName = findViewById(R.id.tvName)
        tvDescription = findViewById(R.id.tvDescription)
        ivPhoto = findViewById(R.id.ivPhoto)
        toolbar = findViewById(R.id.toolbar)
        cardView = findViewById(R.id.cardView)

        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(true)
        supportActionBar?.title = "Detail Story"

        lifecycleScope.launch {
            storyViewModel.getSession().collect { userModel ->
                val token = userModel.token
                val storyId = intent.getStringExtra("story_id")

                Log.d("DetailStoryActivity", "Story ID: $storyId")
                Log.d("DetailStoryActivity", "Token: $token")

                if (storyId != null && token.isNotEmpty()) {
                    fetchStoryDetails(storyId, token)
                } else {
                    Log.e("DetailStoryActivity", "Story ID or token is invalid.")
                }
            }
        }
    }

    private fun hasInternetConnection(): Boolean {
        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork = connectivityManager.activeNetworkInfo
        return activeNetwork != null && activeNetwork.isConnected
    }

    private fun fetchStoryDetails(storyId: String, token: String) {
        progressBar.visibility = View.VISIBLE
        cardView.visibility = View.GONE

        if (!hasInternetConnection()) {
            showError("No internet connection. Please check your network.")
            progressBar.visibility = View.GONE
            return
        }

        lifecycleScope.launch {
            try {
                val response = ApiClient.apiService.getStoryDetails("Bearer $token", storyId)

                Log.d("DetailStoryActivity", "Response: $response")

                if (!response.error) {
                    Log.d("DetailStoryActivity", "Story Name: ${response.story.name}")
                    tvName.text = response.story.name
                    tvDescription.text = response.story.description
                    Picasso.get().load(response.story.photoUrl).into(ivPhoto)

                    cardView.visibility = View.VISIBLE // Show CardView
                    tvName.visibility = View.VISIBLE
                    tvDescription.visibility = View.VISIBLE
                    ivPhoto.visibility = View.VISIBLE
                } else {
                    showError("Error fetching story details: ${response.message}")
                }
            } catch (e: HttpException) {
                Log.e("DetailStoryActivity", "HTTP Exception: ${e.message()}")
                showError("Failed to fetch story details: ${e.message()}")
            } catch (e: IOException) {
                Log.e("DetailStoryActivity", "IOException: ${e.message}")
                showError("Unable to connect to server: ${e.message}")
            } finally {
                progressBar.visibility = View.GONE
            }
        }
    }

    private fun showErrorDialog(message: String) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_error, null)

        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .setCancelable(false)
            .create()

        val textViewErrorMessage = dialogView.findViewById<TextView>(R.id.textViewErrorMessage)
        val buttonDismiss = dialogView.findViewById<Button>(R.id.buttonDismiss)

        textViewErrorMessage.text = message

        buttonDismiss.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun showError(message: String) {
        Log.e("DetailStoryActivity", "Error: $message")
        showErrorDialog(message)
        cardView.visibility = View.GONE
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}
