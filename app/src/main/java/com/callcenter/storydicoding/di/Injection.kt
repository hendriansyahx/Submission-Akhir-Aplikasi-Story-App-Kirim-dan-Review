package com.callcenter.storydicoding.di

import android.content.Context
import com.callcenter.storydicoding.data.UserRepository
import com.callcenter.storydicoding.data.pref.UserPreference
import com.callcenter.storydicoding.data.pref.dataStore

object Injection {
    fun provideRepository(context: Context): UserRepository {
        val pref = UserPreference.getInstance(context.dataStore)
        return UserRepository.getInstance(pref)
    }
}