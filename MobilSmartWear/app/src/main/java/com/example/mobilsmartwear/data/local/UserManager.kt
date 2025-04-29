package com.example.mobilsmartwear.data.local

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class UserManager(context: Context) {
    private val sharedPreferences: SharedPreferences = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
    private val gson = Gson()

    companion object {
        private const val KEY_USERS = "users"
        private const val KEY_CURRENT_USER = "current_user"
    }

    data class User(
        val name: String,
        val email: String,
        val password: String
    )

    fun saveUser(user: User) {
        val users = getUsers().toMutableList()
        users.add(user)
        
        val json = gson.toJson(users)
        sharedPreferences.edit().putString(KEY_USERS, json).apply()
    }

    fun getUsers(): List<User> {
        val json = sharedPreferences.getString(KEY_USERS, null)
        return if (json != null) {
            val type = object : TypeToken<List<User>>() {}.type
            gson.fromJson(json, type)
        } else {
            emptyList()
        }
    }

    fun checkUserExists(email: String): Boolean {
        return getUsers().any { it.email == email }
    }

    fun validateUser(email: String, password: String): Boolean {
        return getUsers().any { it.email == email && it.password == password }
    }

    fun setCurrentUser(user: User) {
        val json = gson.toJson(user)
        sharedPreferences.edit().putString(KEY_CURRENT_USER, json).apply()
    }

    fun getCurrentUser(): User? {
        val json = sharedPreferences.getString(KEY_CURRENT_USER, null)
        return if (json != null) {
            gson.fromJson(json, User::class.java)
        } else {
            null
        }
    }

    fun clearCurrentUser() {
        sharedPreferences.edit().remove(KEY_CURRENT_USER).apply()
    }
} 