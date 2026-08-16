package com.itbenevides.genesys21.data.repository

import com.itbenevides.genesys21.domain.repository.AuthRepository
import com.itbenevides.genesys21.domain.model.UserRole
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.GoogleAuthProvider
import dev.gitlive.firebase.auth.auth
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow

class AndroidAuthRepository : AuthRepository {
    private val auth = Firebase.auth

    override val authState: Flow<String?> = flow {
        emit(auth.currentUser?.uid)
    }

    override val userRole: Flow<UserRole?> = MutableStateFlow(null)

    override suspend fun signIn(
        email: String,
        password: String,
    ): Result<String?> {
        return try {
            auth.signInWithEmailAndPassword(email, password)
            Result.success(getCurrentUserToken())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun signIn(
        idToken: String,
        accessToken: String?,
        provider: String,
    ): Result<String?> {
        return try {
            if (provider == "google") {
                val credential = GoogleAuthProvider.credential(idToken, accessToken)
                auth.signInWithCredential(credential)
                Result.success(getCurrentUserToken())
            } else {
                Result.failure(Exception("Provedor não suportado: \$provider"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun signUp(
        email: String,
        password: String,
    ): Result<String?> {
        return try {
            auth.createUserWithEmailAndPassword(email, password)
            Result.success(getCurrentUserToken())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getCurrentUserToken(): String? {
        return auth.currentUser?.getIdToken(false)
    }

    override suspend fun getCurrentUserId(): String? {
        return auth.currentUser?.uid
    }

    override suspend fun getCurrentUserEmail(): String? {
        return auth.currentUser?.email
    }

    override suspend fun getCurrentUserName(): String? {
        return auth.currentUser?.displayName
    }

    override fun initializeOneTap() {
        // Implementar via Credential Manager se necessário
    }

    override suspend fun signOut() {
        auth.signOut()
    }

    override suspend fun deleteUser(): Result<Unit> {
        return try {
            auth.currentUser?.delete()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
