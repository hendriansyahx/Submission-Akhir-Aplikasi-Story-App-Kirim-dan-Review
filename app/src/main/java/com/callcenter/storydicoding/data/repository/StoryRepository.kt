package com.callcenter.storydicoding.data.repository

import androidx.paging.PagingData
import com.callcenter.storydicoding.data.model.Story
import com.callcenter.storydicoding.data.network.ApiService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class StoryRepository(private val apiService: ApiService) {

    fun fetchStories(token: String, page: Int = 1): Flow<PagingData<Story>> = flow {
        try {
            val response = apiService.getAllStories("Bearer $token", page)
            emit(PagingData.from(response.listStory))
        } catch (e: Exception) {
            emit(PagingData.empty())
        }
    }
}
