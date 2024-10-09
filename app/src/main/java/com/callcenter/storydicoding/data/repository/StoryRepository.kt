package com.callcenter.storydicoding.data.repository

import androidx.lifecycle.LiveData
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.liveData
import com.callcenter.storydicoding.data.model.Story
import com.callcenter.storydicoding.data.network.ApiService
import com.callcenter.storydicoding.data.paging.StoryPagingSource

class StoryRepository(private val apiService: ApiService) {

    fun fetchStories(token: String): LiveData<PagingData<Story>> {
        val pager = Pager(
            config = PagingConfig(pageSize = 20),
            pagingSourceFactory = { StoryPagingSource(apiService, token) }
        )
        return pager.liveData
    }
}
