package com.example.storyapp.data.preference

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.storyapp.data.model.UserModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_session")

class UserPreference private constructor(private val dataStore: DataStore<Preferences>) {

    suspend fun saveSession(user: UserModel) {
        dataStore.edit { preferences ->
            preferences[USER_TOKEN_KEY] = user.token
            preferences[USER_NAME_KEY] = user.name
            preferences[USER_ID_KEY] = user.userId
            preferences[LOGIN_STATUS_KEY] = true
        }
    }

    fun getSession(): Flow<UserModel> {
        return dataStore.data.map { preferences ->
            UserModel(
                preferences[USER_TOKEN_KEY] ?: "",
                preferences[USER_NAME_KEY] ?: "",
                preferences[USER_ID_KEY] ?: "",
                preferences[LOGIN_STATUS_KEY] ?: false
            )
        }
    }

    suspend fun logOut() {
        dataStore.edit { preferences ->
            preferences[USER_TOKEN_KEY] = ""
            preferences[USER_NAME_KEY] = ""
            preferences[USER_ID_KEY] = ""
            preferences[LOGIN_STATUS_KEY] = false
            preferences.clear()
        }
    }

    companion object {
        @Volatile
        private var INSTANCE: UserPreference? = null

        private val USER_NAME_KEY = stringPreferencesKey("user_name")
        private val USER_ID_KEY = stringPreferencesKey("user_id")
        private val USER_TOKEN_KEY = stringPreferencesKey("user_token")
        private val LOGIN_STATUS_KEY = booleanPreferencesKey("login_status")

        fun getInstance(dataStore: DataStore<Preferences>): UserPreference {
            return INSTANCE ?: synchronized(this) {
                val instance = UserPreference(dataStore)
                INSTANCE = instance
                instance
            }
        }
    }
}
