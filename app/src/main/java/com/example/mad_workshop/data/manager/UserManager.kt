package com.example.mad_workshop.data.manager

import android.content.Context
import android.content.SharedPreferences
import com.example.mad_workshop.data.model.Address
import com.example.mad_workshop.data.model.Name
import com.example.mad_workshop.data.model.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object UserManager {

    private const val PREFS_NAME = "user_prefs"
    private const val KEY_ACTIVE_USER_ID = "active_user_id"

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    private val _availableUsers = MutableStateFlow<List<User>>(emptyList())
    val availableUsers: StateFlow<List<User>> = _availableUsers.asStateFlow()

    private var sharedPreferences: SharedPreferences? = null

    fun init(context: Context) {
        if (sharedPreferences == null) {
            sharedPreferences = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        }
    }

    fun setAvailableUsers(users: List<User>) {
        _availableUsers.value = users
        val savedUserId = sharedPreferences?.getInt(KEY_ACTIVE_USER_ID, -1) ?: -1
        val matchedUser = users.find { it.id == savedUserId } ?: users.firstOrNull() ?: createFallbackUser()
        _currentUser.value = matchedUser
    }

    fun selectUser(user: User) {
        _currentUser.value = user
        sharedPreferences?.edit()?.putInt(KEY_ACTIVE_USER_ID, user.id)?.apply()
    }

    private fun createFallbackUser(): User {
        return User(
            id = 1,
            email = "john@gmail.com",
            username = "johnd",
            name = Name("John", "Doe"),
            address = Address("Kilcoole", "7835 New Road", 3, "12926-3874"),
            phone = "1-570-236-7033"
        )
    }
}
