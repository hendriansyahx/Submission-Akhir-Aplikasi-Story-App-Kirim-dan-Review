package com.callcenter.storydicoding.data.network

import com.callcenter.storydicoding.data.model.AddStoryResponse
import com.callcenter.storydicoding.data.model.LoginRequest
import com.callcenter.storydicoding.data.model.LoginResponse
import com.callcenter.storydicoding.data.model.SignupRequest
import com.callcenter.storydicoding.data.model.SignupResponse
import com.callcenter.storydicoding.data.model.StoriesResponse
import com.callcenter.storydicoding.data.model.StoryDetailResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {
    @POST("login")
    suspend fun login(@Body request: LoginRequest): LoginResponse

    @POST("register")
    suspend fun signup(@Body request: SignupRequest): SignupResponse

    @GET("stories")
    suspend fun getAllStories(
        @Header("Authorization") token: String,
        @Query("page") page: Int? = null,
        @Query("size") size: Int? = null,
        @Query("location") location: Int? = null
    ): StoriesResponse

    @Multipart
    @POST("stories")
    fun addNewStory(
        @Header("Authorization") token: String,
        @Part("description") description: RequestBody,
        @Part photo: MultipartBody.Part
    ): Call<AddStoryResponse>

    @GET("stories/{id}")
    suspend fun getStoryDetails(
        @Header("Authorization") token: String,
        @Path("id") id: String
    ): StoryDetailResponse

    @Multipart
    @POST("stories/guest")
    fun addNewStoryAsGuest(
        @Part("description") description: RequestBody,
        @Part photo: MultipartBody.Part
    ): Call<AddStoryResponse>

}