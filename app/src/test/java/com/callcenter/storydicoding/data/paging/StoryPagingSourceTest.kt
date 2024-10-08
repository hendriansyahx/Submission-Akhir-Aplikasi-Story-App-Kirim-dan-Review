package com.callcenter.storydicoding.data.paging

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.paging.PagingSource
import com.callcenter.storydicoding.data.model.StoriesResponse
import com.callcenter.storydicoding.data.model.Story
import com.callcenter.storydicoding.data.network.ApiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.*
import org.junit.rules.TestRule
import org.mockito.Mockito.*
import org.robolectric.RobolectricTestRunner
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@ExperimentalCoroutinesApi
@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE)
class StoryPagingSourceTest {

    @get:Rule
    var rule: TestRule = InstantTaskExecutorRule()

    private lateinit var apiService: ApiService
    private lateinit var pagingSource: StoryPagingSource

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        apiService = mock(ApiService::class.java)
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `load should return stories when data is available`() = runBlocking {

        val token = "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VySWQiOiJ1c2VyLWFXZzNHMC1MOTI2UHRMQXUiLCJpYXQiOjE3Mjc5MzA4OTJ9.HZddwjZQo8DEbeasMGxr1mQ3A2FjN_ruORbGaQ28lJk"
        val mockStoriesResponse = StoriesResponse(
            error = false,
            message = "Success",
            listStory = listOf(
                Story(id = "1", name = "Story 1", description = "Description 1", photoUrl = "http://example.com/photo1.jpg", createdAt = "2023-01-01T00:00:00Z", lat = 0.0, lon = 0.0),
                Story(id = "2", name = "Story 2", description = "Description 2", photoUrl = "http://example.com/photo2.jpg", createdAt = "2023-01-02T00:00:00Z", lat = 1.0, lon = 1.0)
            )
        )

        `when`(apiService.getAllStories(token, 1, 20)).thenReturn(mockStoriesResponse)
        pagingSource = StoryPagingSource(apiService, "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VySWQiOiJ1c2VyLWFXZzNHMC1MOTI2UHRMQXUiLCJpYXQiOjE3Mjc5MzA4OTJ9.HZddwjZQo8DEbeasMGxr1mQ3A2FjN_ruORbGaQ28lJk")

        val loadResult = pagingSource.load(PagingSource.LoadParams.Refresh(key = 1, loadSize = 20, placeholdersEnabled = false))

        Assert.assertTrue(loadResult is PagingSource.LoadResult.Page)
        val page = loadResult as PagingSource.LoadResult.Page
        Assert.assertEquals(2, page.data.size)
        Assert.assertEquals("Story 1", page.data[0].name)
        Assert.assertNull(page.prevKey)
        Assert.assertEquals(2, page.nextKey)
    }

    @Test
    fun `load should return empty list when no data is available`() = runBlocking {

        val token = "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VySWQiOiJ1c2VyLWFXZzNHMC1MOTI2UHRMQXUiLCJpYXQiOjE3Mjc5MzA4OTJ9.HZddwjZQo8DEbeasMGxr1mQ3A2FjN_ruORbGaQ28lJk"
        val mockEmptyStoriesResponse = StoriesResponse(
            error = false,
            message = "No stories available",
            listStory = emptyList()
        )

        `when`(apiService.getAllStories(token, 1, 20)).thenReturn(mockEmptyStoriesResponse)
        pagingSource = StoryPagingSource(apiService, "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VySWQiOiJ1c2VyLWFXZzNHMC1MOTI2UHRMQXUiLCJpYXQiOjE3Mjc5MzA4OTJ9.HZddwjZQo8DEbeasMGxr1mQ3A2FjN_ruORbGaQ28lJk")

        val loadResult = pagingSource.load(PagingSource.LoadParams.Refresh(key = 1, loadSize = 20, placeholdersEnabled = false))

        Assert.assertTrue(loadResult is PagingSource.LoadResult.Page)
        val page = loadResult as PagingSource.LoadResult.Page
        Assert.assertEquals(0, page.data.size)
        Assert.assertNull(page.prevKey)
        Assert.assertNull(page.nextKey)
    }

    @Test
    fun `load should return error when API call fails`() = runBlocking {

        val token = "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VySWQiOiJ1c2VyLWFXZzNHMC1MOTI2UHRMQXUiLCJpYXQiOjE3Mjc5MzA4OTJ9.HZddwjZQo8DEbeasMGxr1mQ3A2FjN_ruORbGaQ28lJk"
        `when`(apiService.getAllStories(token, 1, 20)).thenThrow(RuntimeException("Network error"))
        pagingSource = StoryPagingSource(apiService, "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VySWQiOiJ1c2VyLWFXZzNHMC1MOTI2UHRMQXUiLCJpYXQiOjE3Mjc5MzA4OTJ9.HZddwjZQo8DEbeasMGxr1mQ3A2FjN_ruORbGaQ28lJk")

        val loadResult = pagingSource.load(PagingSource.LoadParams.Refresh(key = 1, loadSize = 20, placeholdersEnabled = false))

        Assert.assertTrue(loadResult is PagingSource.LoadResult.Error)
        val error = loadResult as PagingSource.LoadResult.Error
        Assert.assertTrue(error.throwable is RuntimeException)
        Assert.assertEquals("Network error", error.throwable.message)
    }
}