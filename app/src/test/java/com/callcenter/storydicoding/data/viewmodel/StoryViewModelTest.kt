package com.callcenter.storydicoding.data.viewmodel

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.MutableLiveData
import androidx.paging.*
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
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.junit.MockitoJUnitRunner

@ExperimentalCoroutinesApi
@RunWith(MockitoJUnitRunner.Silent::class)
class StoryViewModelTestAlt {

    @get:Rule
    val instantExecutor = InstantTaskExecutorRule()

    @get:Rule
    val dispatcherRule = MainDispatcherRule()

    @Mock
    private lateinit var mockUserPreference: UserPreference

    @Mock
    private lateinit var mockStoryRepository: StoryRepository

    private lateinit var viewModel: StoryViewModel

    @Before
    fun setup() {

        Mockito.`when`(mockUserPreference.getSession())
            .thenReturn(flowOf(UserModel("tbtumbal123@gmail.com", "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VySWQiOiJ1c2VyLWFXZzNHMC1MOTI2UHRMQXUiLCJpYXQiOjE3MjgzMTcwOTJ9.kkI6AkH-vAK_z4LPFEQdlSv7Gf3_7MJ3RYHNURI55rc", "tests", true)))

        viewModel = StoryViewModel(mockUserPreference, mockStoryRepository)
    }

    @Test
    fun `verify fetching stories returns valid data`() = runTest {

        val testStories = DataDummy.generateDummyStories().listStory
        val pagingData = FakeStoryPagingSource.snapshot(testStories)
        val liveDataStories = MutableLiveData<PagingData<Story>>()
        liveDataStories.value = pagingData

        Mockito.`when`(mockStoryRepository.fetchStories(Mockito.anyString())).thenReturn(liveDataStories)

        println(" ")

        val token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VySWQiOiJ1c2VyLWFXZzNHMC1MOTI2UHRMQXUiLCJpYXQiOjE3MjgzMTcwOTJ9.kkI6AkH-vAK_z4LPFEQdlSv7Gf3_7MJ3RYHNURI55rc"
        println("Fetching stories with token: $token")

        val observedStories = viewModel.getStoriesFlow(token).getOrAwaitValue()

        val differ = AsyncPagingDataDiffer(
            diffCallback = StoryAdapter.DIFF_CALLBACK,
            updateCallback = dummyListUpdateCallback,
            workerDispatcher = Dispatchers.Main,
        )
        differ.submitData(observedStories)

        val snapshot = differ.snapshot()

        println("Observed stories snapshot size: ${snapshot.size}")
        snapshot.forEachIndexed { index, story ->
            println("Story at index $index: $story")
        }

        assertNotNull(snapshot)
        assertEquals(testStories.size, snapshot.size)
        assertEquals(testStories[0], snapshot[0])
    }

    @Test
    fun `verify fetching stories returns empty data`() = runTest {

        val pagingData = PagingData.from(emptyList<Story>())
        val liveDataEmptyStories = MutableLiveData<PagingData<Story>>()
        liveDataEmptyStories.value = pagingData

        println(" ")

        Mockito.`when`(mockStoryRepository.fetchStories(Mockito.anyString())).thenReturn(liveDataEmptyStories)

        val token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VySWQiOiJ1c2VyLWFXZzNHMC1MOTI2UHRMQXUiLCJpYXQiOjE3MjgzMTcwOTJ9.kkI6AkH-vAK_z4LPFEQdlSv7Gf3_7MJ3RYHNURI55rc"
        println("Fetching stories with token: $token (expecting empty data)")

        val observedEmptyStories = viewModel.getStoriesFlow(token).getOrAwaitValue()

        val differ = AsyncPagingDataDiffer(
            diffCallback = StoryAdapter.DIFF_CALLBACK,
            updateCallback = dummyListUpdateCallback,
            workerDispatcher = Dispatchers.Main,
        )
        differ.submitData(observedEmptyStories)

        val snapshotSize = differ.snapshot().size
        println("Observed empty stories snapshot size: $snapshotSize")

        assertEquals(0, snapshotSize)
    }

}

class FakeStoryPagingSource : PagingSource<Int, Story>() {
    companion object {
        fun snapshot(items: List<Story>): PagingData<Story> {
            return PagingData.from(items)
        }
    }

    override fun getRefreshKey(state: PagingState<Int, Story>): Int? = null

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Story> {
        return LoadResult.Page(emptyList(), null, null)
    }
}

val dummyListUpdateCallback = object : ListUpdateCallback {
    override fun onInserted(position: Int, count: Int) {}
    override fun onRemoved(position: Int, count: Int) {}
    override fun onMoved(fromPosition: Int, toPosition: Int) {}
    override fun onChanged(position: Int, count: Int, payload: Any?) {}
}
