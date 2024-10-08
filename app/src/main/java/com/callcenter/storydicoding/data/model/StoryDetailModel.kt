package com.callcenter.storydicoding.data.model

data class StoryDetailResponse(
    val error: Boolean,
    val message: String,
    val story: Story
)
