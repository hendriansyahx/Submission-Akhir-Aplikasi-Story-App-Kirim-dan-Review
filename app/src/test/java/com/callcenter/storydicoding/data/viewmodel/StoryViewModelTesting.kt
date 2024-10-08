package com.callcenter.storydicoding.data.viewmodel

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Observer
import androidx.paging.PagingData
import com.callcenter.storydicoding.data.model.StoriesResponse
import com.callcenter.storydicoding.data.model.Story
import com.callcenter.storydicoding.data.network.ApiService
import com.callcenter.storydicoding.data.pref.UserPreference
import com.callcenter.storydicoding.data.repository.StoryRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.*
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.junit.MockitoJUnitRunner

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(MockitoJUnitRunner::class)
class StoryViewModelTesting {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    @Mock
    private lateinit var userPreference: UserPreference

    @Mock
    private lateinit var apiService: ApiService

    @Mock
    private lateinit var observer: Observer<PagingData<Story>>

    private lateinit var storyRepository: StoryRepository
    private lateinit var storyViewModel: StoryViewModel

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        storyRepository = StoryRepository(apiService)
        storyViewModel = StoryViewModel(userPreference, storyRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `getStoriesFlow returns expected data`() = runTest {
        val token = "test_token"
        val storyList = listOf(
            Story(
                id = "1",
                name = "Story 1",
                description = "Description of Story 1",
                photoUrl = "http://example.com/photo1.jpg",
                createdAt = "2024-01-01T00:00:00Z",
                lat = 0.0,
                lon = 0.0
            ),
            Story(
                id = "2",
                name = "Story 2",
                description = "Description of Story 2",
                photoUrl = "http://example.com/photo2.jpg",
                createdAt = "2024-01-02T00:00:00Z",
                lat = 0.0,
                lon = 0.0
            )
        )

        println("Story List: $storyList")

        val pagingData = PagingData.from(storyList)

        `when`(apiService.getAllStories(anyString(), anyInt(), anyInt(), anyInt())).thenReturn(
            StoriesResponse(false, "", storyList)
        )

        val liveData = storyViewModel.getStoriesFlow(token)
        liveData.observeForever(observer)

        println("Paging Data: $pagingData")

        verify(observer).onChanged(pagingData)
        liveData.removeObserver(observer)
    }

    @Test
    fun `getStoriesFlow handles error correctly`() = runTest {
        val token = "test_token"

        `when`(apiService.getAllStories(anyString(), anyInt(), anyInt(), anyInt())).thenThrow(RuntimeException("Network error"))

        val liveData = storyViewModel.getStoriesFlow(token)
        liveData.observeForever(observer)

        verify(observer).onChanged(PagingData.empty())
        liveData.removeObserver(observer)
    }
}

