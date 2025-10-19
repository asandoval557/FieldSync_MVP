package com.example.fieldsync.data

import com.example.fieldsync.data.model.LoggedInUser
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.tasks.await

class LoginDataSource {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    // Real Firebase sign in
    suspend fun login(username: String, password: String): Result<LoggedInUser> {
        return try {
            val result = auth.signInWithEmailAndPassword(username, password).await()
            val user = result.user ?: return Result.Error(Exception("Authentication failed"))

            val loggedInUser = LoggedInUser(
                userId = user.uid,
                displayName = user.email ?: "FieldSync User"
            )
            Result.Success(loggedInUser)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    fun logout() {
        auth.signOut()
    }
}
