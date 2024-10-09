package com.callcenter.storydicoding.data.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.callcenter.storydicoding.data.network.ApiService
import com.callcenter.storydicoding.data.pref.UserPreference
import com.callcenter.storydicoding.data.repository.StoryRepository

class StoryViewModelFactory(
    private val userPreference: UserPreference,
    private val apiService: ApiService
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(StoryViewModel::class.java)) {
            val storyRepository = StoryRepository(apiService)
            return StoryViewModel(userPreference, storyRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
