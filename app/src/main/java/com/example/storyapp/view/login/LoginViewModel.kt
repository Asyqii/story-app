package com.example.storyapp.view.login

import androidx.lifecycle.ViewModel
import com.example.storyapp.data.repository.UserRepository
import com.example.storyapp.data.model.UserModel

class LoginViewModel(private val repository: UserRepository) : ViewModel() {
    suspend fun login(email: String, password: String) = repository.login(email, password)
    suspend fun saveSession(user: UserModel) {
        repository.saveSession(user)
    }
}