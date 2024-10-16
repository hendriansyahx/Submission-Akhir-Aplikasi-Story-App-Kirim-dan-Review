package com.callcenter.storydicoding.data.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.callcenter.storydicoding.data.model.AddStoryResponse
import com.callcenter.storydicoding.data.model.Story
import com.callcenter.storydicoding.data.network.ApiClient
import com.callcenter.storydicoding.data.paging.StoryPagingSource
import com.callcenter.storydicoding.data.pref.UserPreference
import com.callcenter.storydicoding.data.repository.StoryRepository
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class StoryViewModel(private val userPreference: UserPreference, private val storyRepository: StoryRepository) : ViewModel() {

    private val _addStoryResponse = MutableLiveData<AddStoryResponse?>()
    val addStoryResponse: LiveData<AddStoryResponse?> get() = _addStoryResponse

    private var isLogin: Boolean = false

    init {
        viewModelScope.launch {
            userPreference.getSession().collect { user ->
                isLogin = user.isLogin
            }
        }
    }

    fun getSession() = userPreference.getSession()

    fun getStoriesFlow(token: String): LiveData<PagingData<Story>> {
        return storyRepository.fetchStories(token).also { liveData ->
            liveData.observeForever { pagingData ->
                println("Paging Data Emitted: $pagingData")
            }
        }
    }

    fun addNewStory(token: String, descriptionBody: RequestBody, body: MultipartBody.Part) {
        val call = if (isLogin) {
            ApiClient.apiService.addNewStory(token, descriptionBody, body)
        } else {
            ApiClient.apiService.addNewStoryAsGuest(descriptionBody, body)
        }

        call.enqueue(object : Callback<AddStoryResponse> {
            override fun onResponse(call: Call<AddStoryResponse>, response: Response<AddStoryResponse>) {
                if (response.isSuccessful) {
                    _addStoryResponse.value = response.body()
                } else {
                    _addStoryResponse.value = null
                }
            }

            override fun onFailure(call: Call<AddStoryResponse>, t: Throwable) {
                _addStoryResponse.value = null
            }
        })
    }
}
