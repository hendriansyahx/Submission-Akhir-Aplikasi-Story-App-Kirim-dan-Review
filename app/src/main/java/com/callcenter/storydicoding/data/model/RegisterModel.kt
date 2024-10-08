package com.callcenter.storydicoding.data.model

data class SignupRequest(
    val name: String,
    val email: String,
    val password: String
)

data class SignupResponse(
    val error: Boolean,
    val message: String
)
