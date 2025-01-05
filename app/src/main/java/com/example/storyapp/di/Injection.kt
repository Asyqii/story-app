package com.example.storyapp.di

import android.content.Context
import com.example.storyapp.data.repository.UserRepository
import com.example.storyapp.data.database.DatabaseStory
import com.example.storyapp.data.preference.UserPreference
import com.example.storyapp.data.preference.dataStore
import com.example.storyapp.data.remote.ApiConfig
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

object Injection {
    fun injectionRepository(context: Context): UserRepository = runBlocking {
        val pref = UserPreference.getInstance(context.dataStore)
        val user = pref.getSession().first()
        val apiService = ApiConfig.getApiService(user.token)
        val database = DatabaseStory.getDatabase(context)
        UserRepository.getInstance(database, apiService, pref)
    }
}