package com.callcenter.storydicoding.view.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.callcenter.storydicoding.data.UserRepository
import com.callcenter.storydicoding.data.pref.UserModel
import kotlinx.coroutines.launch

class LoginViewModel(private val repository: UserRepository) : ViewModel() {
    fun saveSession(user: UserModel) {
        viewModelScope.launch {
            repository.saveSession(user)
        }
    }
}