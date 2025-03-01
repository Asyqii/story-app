package com.example.storyapp.view.maps

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.example.storyapp.data.repository.UserRepository
import com.example.storyapp.data.response.StoryResponse

class MapsViewModel(
    private val repository: UserRepository
) : ViewModel() {

    suspend fun getStoriesWithLocation(): LiveData<com.example.storyapp.data.Result<StoryResponse>> {
        return repository.getStoriesWithLocation()
    }

}