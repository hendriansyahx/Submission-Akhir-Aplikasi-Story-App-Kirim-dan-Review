package com.callcenter.storydicoding.data.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.callcenter.storydicoding.data.model.Story
import com.callcenter.storydicoding.data.network.ApiService

class StoryPagingSource(
    private val apiService: ApiService,
    private val token: String
) : PagingSource<Int, Story>() {

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Story> {
        val page = params.key ?: 1
        return try {
            val response = apiService.getAllStories("Bearer $token", page = page, size = params.loadSize)
            if (response.error) {
                LoadResult.Error(Exception(response.message))
            } else {
                LoadResult.Page(
                    data = response.listStory,
                    prevKey = if (page == 1) null else page - 1,
                    nextKey = if (response.listStory.isEmpty()) null else page + 1
                )
            }
        } catch (exception: Exception) {
            LoadResult.Error(exception)
        }
    }

    override fun getRefreshKey(state: PagingState<Int, Story>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            val page = state.closestPageToPosition(anchorPosition)
            page?.prevKey?.plus(1) ?: page?.nextKey?.minus(1)
        }
    }
}
