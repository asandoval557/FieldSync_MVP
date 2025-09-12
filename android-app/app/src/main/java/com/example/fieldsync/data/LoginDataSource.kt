package com.example.fieldsync.data

import com.example.fieldsync.data.model.LoggedInUser
import java.util.UUID

class LoginDataSource {

    companion object {
        // MVP default credentials (change if you want)
        const val DEFAULT_EMAIL = "test@example.com"
        const val DEFAULT_PASSWORD = "Passw0rd!"
    }

    fun login(username: String, password: String): Result<LoggedInUser> {
        return try {
            if (username.equals(DEFAULT_EMAIL, ignoreCase = true) && password == DEFAULT_PASSWORD) {
                val fakeUser = LoggedInUser(
                    userId = UUID.randomUUID().toString(),
                    displayName = "FieldSync User"
                )
                Result.Success(fakeUser)
            } else {
                Result.Error(Exception("Invalid credentials"))
            }
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    fun logout() {
        // No-op in fake auth
    }
}