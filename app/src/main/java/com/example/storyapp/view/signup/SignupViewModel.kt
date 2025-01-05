package com.example.storyapp.view.signup

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.example.storyapp.data.repository.UserRepository
import com.example.storyapp.data.response.RegisterResponse

class SignupViewModel(private var repository: UserRepository) : ViewModel() {

    fun register(name: String, email: String, password: String): LiveData<com.example.storyapp.data.Result<RegisterResponse>> {
        return repository.register(name, email, password)
    }

}