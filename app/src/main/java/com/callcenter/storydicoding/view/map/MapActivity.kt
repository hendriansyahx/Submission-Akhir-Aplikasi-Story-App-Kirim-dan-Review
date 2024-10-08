package com.callcenter.storydicoding.view.map

import android.graphics.PorterDuff
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.callcenter.storydicoding.R
import com.callcenter.storydicoding.data.network.ApiClient
import com.callcenter.storydicoding.data.pref.UserPreference
import com.callcenter.storydicoding.data.pref.dataStore
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first

class MapActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var googleMap: GoogleMap
    private lateinit var userPreference: UserPreference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map)

        // Inisialisasi toolbar
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.apply {
            title = "Map Activity"
            setDisplayHomeAsUpEnabled(true)
        }

        toolbar.setTitleTextColor(resources.getColor(android.R.color.white, theme))

        toolbar.navigationIcon?.setColorFilter(resources.getColor(android.R.color.white, theme), PorterDuff.Mode.SRC_ATOP)

        toolbar.setNavigationOnClickListener { finish() }

        userPreference = UserPreference.getInstance(dataStore)

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map

        CoroutineScope(Dispatchers.Main).launch {
            try {
                val token = "Bearer ${userPreference.getSession().first().token}"
                val response = ApiClient.apiService.getAllStories(token, location = 1)

                if (!response.error) {
                    response.listStory.forEach { story ->
                        val position = LatLng(story.lat, story.lon)
                        googleMap.addMarker(
                            MarkerOptions()
                                .position(position)
                                .title(story.name)
                                .snippet(story.description)
                        )
                    }

                    if (response.listStory.isNotEmpty()) {
                        val firstStory = response.listStory.first()
                        val firstLocation = LatLng(firstStory.lat, firstStory.lon)
                        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(firstLocation, 10f))
                    }
                }
            } catch (e: Exception) {
                Log.e("MapActivity", "Error loading stories: ${e.message}")
            }
        }
    }
}
