package com.example.storyapp.data.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.liveData
import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.liveData
import com.example.storyapp.data.Result
import com.example.storyapp.data.model.UserModel
import com.example.storyapp.data.preference.UserPreference
import com.example.storyapp.data.remote.ApiService
import com.example.storyapp.data.database.DatabaseStory
import com.example.storyapp.data.paging.RemoteMediator
import com.example.storyapp.data.response.LoginResponse
import com.example.storyapp.data.response.ListStoryItem
import com.example.storyapp.data.response.RegisterResponse
import com.example.storyapp.data.response.StoryResponse
import com.example.storyapp.data.response.UploadResponse
import com.google.gson.Gson
import kotlinx.coroutines.flow.Flow
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.HttpException

class UserRepository private constructor(
    private val apiService: ApiService,
    private val userPreference: UserPreference,
    private val database: DatabaseStory,
) {
    private var _list = MutableLiveData<List<ListStoryItem>>()
    var list: MutableLiveData<List<ListStoryItem>> = _list

    var _isLoading = MutableLiveData<Boolean>()
    var isLoading: LiveData<Boolean> = _isLoading

    fun getStory(): LiveData<PagingData<ListStoryItem>> {
        @OptIn(ExperimentalPagingApi::class)
        return Pager(config = PagingConfig(
            pageSize = 5,
            enablePlaceholders = false
        ),
            remoteMediator = RemoteMediator(database, apiService),
            pagingSourceFactory = {
                database.storyDao().getAllStory()
            }
        ).liveData
    }

    fun register(
        name: String,
        email: String,
        password: String
    ): LiveData<Result<RegisterResponse>> = liveData {
        emit(Result.Loading)
        val response = apiService.register(name, email, password)
        if (!response.error) {
            emit(Result.Success(response))
        } else {
            emit(Result.Error(response.message))
        }
    }

    fun login(email: String, password: String): LiveData<Result<LoginResponse>> = liveData {
        emit(Result.Loading)
        try {
            val response = apiService.login(email, password)
            emit(Result.Success(response))
        } catch (e: HttpException) {
            val jsonInString = e.response()?.errorBody()?.string()
            val errorBody = Gson().fromJson(jsonInString, LoginResponse::class.java)
            emit(Result.Error(errorBody.message.toString()))
        }
    }

    fun  getStoriesWithLocation(): LiveData<Result<StoryResponse>> = liveData {
        emit(Result.Loading)
        try {
            val response = apiService.getStoriesWithLocation()
            if (response.error == false) {
                emit(Result.Success(response))
            } else {
                emit(Result.Error(response.message.toString()))
            }
        } catch (e: Exception) {
            emit(Result.Error(e.message.toString()))
        }
    }

    suspend fun saveSession(user: UserModel) {
        userPreference.saveSession(user)
    }

    fun getSession(): Flow<UserModel> {
        return userPreference.getSession()
    }

    suspend fun logout() {
        userPreference.logOut()
    }

    fun uploadImage(
        file: MultipartBody.Part,
        description: RequestBody,
        lat: Float,
        lon: Float
    ): LiveData<Result<UploadResponse>> = liveData {
        emit(Result.Loading)
        val response = if (lat != 0f && lon != 0f) {
            apiService.uploadImageWithLocation(file, description, lat, lon)
        } else {
            apiService.uploadImage(file, description)
        }
        if (response.error == false) {
            emit(Result.Success(response))
        } else {
            emit(Result.Error(response.message.toString()))
        }
    }

    companion object {
        @Volatile
        private var instance: UserRepository? = null

        fun clearInstance() {
            instance = null
        }

        fun getInstance(
            database: DatabaseStory,
            apiService: ApiService,
            userPreference: UserPreference
        ): UserRepository =
            instance ?: synchronized(this) {
                instance ?: UserRepository(apiService, userPreference, database)
            }.also { instance = it }
    }
}