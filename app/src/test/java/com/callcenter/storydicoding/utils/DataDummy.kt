package com.callcenter.storydicoding.utils

import com.callcenter.storydicoding.data.model.StoriesResponse
import com.callcenter.storydicoding.data.model.Story

object DataDummy {
    fun generateDummyStories(): StoriesResponse {
        val listStory: MutableList<Story> = arrayListOf()
        for (i in 1..20) {
            val story = Story(
                createdAt = "2022-02-22T22:22:22Z",
                description = "Description $i",
                id = "Story-$i",
                lat = i.toDouble() * 10,
                lon = i.toDouble() * 10,
                name = "Name-$i",
                photoUrl = "https://developer.android.com/static/codelabs/basic-android-kotlin-compose-test-viewmodel/img/bb1e97c357603a27.png"
            )
            listStory.add(story)
        }

        return StoriesResponse(
            error = false,
            message = "Stories fetched successfully",
            listStory = listStory
        )
    }
}
