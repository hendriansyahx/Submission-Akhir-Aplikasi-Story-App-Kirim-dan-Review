package com.callcenter.storydicoding.data.viewmodel

import android.annotation.SuppressLint
import android.util.Log
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.paging.AsyncPagingDataDiffer
import androidx.paging.PagingData
import androidx.recyclerview.widget.ListUpdateCallback
import com.callcenter.storydicoding.data.model.Story
import com.callcenter.storydicoding.data.pref.UserModel
import com.callcenter.storydicoding.data.pref.UserPreference
import com.callcenter.storydicoding.data.repository.StoryRepository
import com.callcenter.storydicoding.utils.DataDummy
import com.callcenter.storydicoding.utils.MainDispatcherRule
import com.callcenter.storydicoding.utils.getOrAwaitValue
import com.callcenter.storydicoding.view.story.adapter.StoryAdapter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers.anyInt
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.junit.MockitoJUnitRunner
import java.util.concurrent.TimeUnit

@ExperimentalCoroutinesApi
@RunWith(MockitoJUnitRunner.Silent::class)
class StoryViewModelTest {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Mock private lateinit var userPreference: UserPreference

    private lateinit var storyRepository: StoryRepository
    private lateinit var storyViewModel: StoryViewModel

    @SuppressLint("CheckResult")
    @Before
    fun setup() {
        Mockito.mockStatic(Log::class.java)
        storyRepository = Mockito.mock(StoryRepository::class.java)

        val flowUserModel = flow {
            emit(UserModel("tbtumbal123@gmail.com", "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VySWQiOiJ1c2VyLWFXZzNHMC1MOTI2UHRMQXUiLCJpYXQiOjE3MjgzMTcwOTJ9.kkI6AkH-vAK_z4LPFEQdlSv7Gf3_7MJ3RYHNURI55rc", "tests", true))
        }
        Mockito.`when`(userPreference.getSession()).thenReturn(flowUserModel)

        storyViewModel = StoryViewModel(userPreference, storyRepository)
    }

    @After
    fun tearDown() {
        Mockito.clearAllCaches()
    }

    @Test
    fun `when stories loaded should trigger diff callback correctly`() = runTest {
        val dummyStoriesResponse = DataDummy.generateDummyStories()
        val expectedPagingData: PagingData<Story> = PagingData.from(dummyStoriesResponse.listStory)

        Mockito.`when`(storyRepository.fetchStories("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VySWQiOiJ1c2VyLWFXZzNHMC1MOTI2UHRMQXUiLCJpYXQiOjE3MjgzMTcwOTJ9.kkI6AkH-vAK_z4LPFEQdlSv7Gf3_7MJ3RYHNURI55rc")).thenReturn(flow { emit(expectedPagingData) })

        val actualPagingData = storyViewModel.getStoriesFlow("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VySWQiOiJ1c2VyLWFXZzNHMC1MOTI2UHRMQXUiLCJpYXQiOjE3MjgzMTcwOTJ9.kkI6AkH-vAK_z4LPFEQdlSv7Gf3_7MJ3RYHNURI55rc").getOrAwaitValue(timeUnit = TimeUnit.SECONDS)

        val differ = AsyncPagingDataDiffer(
            diffCallback = StoryAdapter.DIFF_CALLBACK,
            updateCallback = noopListUpdateCallback,
            workerDispatcher = Dispatchers.Main
        )
        differ.submitData(actualPagingData)

        advanceUntilIdle()

        println("Expected Size: ${dummyStoriesResponse.listStory.size}")
        println("Actual Size: ${differ.snapshot().size}")

        assertNotNull(differ.snapshot())
        assertEquals(dummyStoriesResponse.listStory.size, differ.snapshot().size)

        val firstStory = differ.snapshot().first()
        assertEquals(dummyStoriesResponse.listStory.first().id, firstStory?.id)
        assertEquals(dummyStoriesResponse.listStory.first().name, firstStory?.name)
        assertEquals(dummyStoriesResponse.listStory.first().description, firstStory?.description)
        assertEquals(dummyStoriesResponse.listStory.first().createdAt, firstStory?.createdAt)
        assertEquals(dummyStoriesResponse.listStory.first().photoUrl, firstStory?.photoUrl)
    }

    @Test
    fun `when no stories loaded should return zero size`() = runTest {
        val expectedPagingData: PagingData<Story> = PagingData.empty()

        Mockito.`when`(storyRepository.fetchStories("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VySWQiOiJ1c2VyLWFXZzNHMC1MOTI2UHRMQXUiLCJpYXQiOjE3MjgzMTcwOTJ9.kkI6AkH-vAK_z4LPFEQdlSv7Gf3_7MJ3RYHNURI55rc")).thenReturn(flow { emit(expectedPagingData) })

        val actualPagingData = storyViewModel.getStoriesFlow("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VySWQiOiJ1c2VyLWFXZzNHMC1MOTI2UHRMQXUiLCJpYXQiOjE3MjgzMTcwOTJ9.kkI6AkH-vAK_z4LPFEQdlSv7Gf3_7MJ3RYHNURI55rc").getOrAwaitValue()

        val differ = AsyncPagingDataDiffer(
            diffCallback = StoryAdapter.DIFF_CALLBACK,
            updateCallback = noopListUpdateCallback,
            workerDispatcher = Dispatchers.Main
        )
        differ.submitData(actualPagingData)

        advanceUntilIdle()

        assertNotNull(differ.snapshot())
        assertEquals(0, differ.snapshot().size)
    }

    @Test
    fun `when stories loaded should ensure data is not null and first story is correct`() = runTest {
        val dummyStoriesResponse = DataDummy.generateDummyStories()
        val expectedPagingData: PagingData<Story> = PagingData.from(dummyStoriesResponse.listStory)

        println("Dummy Stories Response: $dummyStoriesResponse")

        Mockito.`when`(storyRepository.fetchStories(anyString(), anyInt())).thenReturn(flow {
            println("Emitting expectedPagingData")
            emit(expectedPagingData)
        })

        storyViewModel.getStoriesFlow("dummy_token").observeForever { pagingData ->
            println("Collected Paging Data: $pagingData")
        }

        val actualPagingData = storyViewModel.getStoriesFlow("dummy_token").getOrAwaitValue()

        val differ = AsyncPagingDataDiffer(
            diffCallback = StoryAdapter.DIFF_CALLBACK,
            updateCallback = noopListUpdateCallback,
            workerDispatcher = Dispatchers.Main
        )
        differ.submitData(actualPagingData)

        advanceUntilIdle()

        println("Snapshot after submitData: ${differ.snapshot()}")

        assertNotNull(differ.snapshot())
        assertEquals(dummyStoriesResponse.listStory.size, differ.snapshot().size)

        val firstStory = differ.snapshot().first()
        println("First Story: $firstStory")

        assertEquals(dummyStoriesResponse.listStory.first().id, firstStory?.id)
        assertEquals(dummyStoriesResponse.listStory.first().name, firstStory?.name)
        assertEquals(dummyStoriesResponse.listStory.first().description, firstStory?.description)
        assertEquals(dummyStoriesResponse.listStory.first().createdAt, firstStory?.createdAt)
        assertEquals(dummyStoriesResponse.listStory.first().photoUrl, firstStory?.photoUrl)
    }

}

val noopListUpdateCallback = object : ListUpdateCallback {
    override fun onInserted(position: Int, count: Int) {}
    override fun onRemoved(position: Int, count: Int) {}
    override fun onMoved(fromPosition: Int, toPosition: Int) {}
    override fun onChanged(position: Int, count: Int, payload: Any?) {}
}
