package com.example.storyapp.view.uploadstory

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.example.storyapp.data.repository.UserRepository
import com.example.storyapp.data.response.UploadResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody

class UploadStoryViewModel(
    private val repository: UserRepository
) : ViewModel() {

    fun uploadStory(
        file: MultipartBody.Part,
        description: RequestBody,
        lat: Float,
        long: Float
    ): LiveData<com.example.storyapp.data.Result<UploadResponse>> {
        return repository.uploadImage(file, description, lat, long)
    }
}