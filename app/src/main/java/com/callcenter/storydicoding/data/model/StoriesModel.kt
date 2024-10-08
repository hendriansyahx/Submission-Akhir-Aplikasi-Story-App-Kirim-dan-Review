package com.callcenter.storydicoding.data.model

data class StoriesResponse(
    val error: Boolean,
    val message: String,
    val listStory: List<Story>
)

data class Story(
    val id: String,
    val name: String,
    val description: String,
    val photoUrl: String,
    val createdAt: String,
    val lat: Double,
    val lon: Double
)
