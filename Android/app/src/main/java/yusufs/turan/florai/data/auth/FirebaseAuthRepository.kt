package yusufs.turan.florai.data.auth

import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthRecentLoginRequiredException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.android.gms.tasks.Tasks
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit

class FirebaseAuthRepository(
    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()
) {
    val currentUser: AuthUser?
        get() = firebaseAuth.currentUser?.toAuthUser()

    fun observeAuthState(): Flow<AuthUser?> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { auth ->
            trySend(auth.currentUser?.toAuthUser())
        }
        firebaseAuth.addAuthStateListener(listener)
        awaitClose { firebaseAuth.removeAuthStateListener(listener) }
    }

    fun signIn(
        email: String,
        password: String,
        onResult: (Result<Unit>) -> Unit
    ) {
        firebaseAuth.signInWithEmailAndPassword(email.trim(), password)
            .addOnCompleteListener { task ->
                if (!task.isSuccessful) {
                    onResult(Result.failure(task.exception ?: IllegalStateException()))
                    return@addOnCompleteListener
                }

                val user = task.result?.user ?: firebaseAuth.currentUser
                if (user == null) {
                    onResult(Result.success(Unit))
                    return@addOnCompleteListener
                }

                user.reload()
                    .addOnCompleteListener { reloadTask ->
                        if (!reloadTask.isSuccessful) {
                            onResult(
                                Result.failure(
                                    reloadTask.exception ?: IllegalStateException()
                                )
                            )
                            return@addOnCompleteListener
                        }

                        user.getIdToken(true)
                            .addOnCompleteListener { tokenTask ->
                                if (tokenTask.isSuccessful) {
                                    onResult(Result.success(Unit))
                                } else {
                                    onResult(
                                        Result.failure(
                                            tokenTask.exception ?: IllegalStateException()
                                        )
                                    )
                                }
                            }
                    }
            }
    }

    fun register(
        displayName: String,
        email: String,
        password: String,
        onResult: (Result<Unit>) -> Unit
    ) {
        firebaseAuth.createUserWithEmailAndPassword(email.trim(), password)
            .addOnCompleteListener { task ->
                if (!task.isSuccessful) {
                    onResult(Result.failure(task.exception ?: IllegalStateException()))
                    return@addOnCompleteListener
                }

                val user = task.result?.user ?: firebaseAuth.currentUser
                if (user == null) {
                    onResult(Result.success(Unit))
                    return@addOnCompleteListener
                }

                val profileUpdates = UserProfileChangeRequest.Builder()
                    .setDisplayName(displayName.trim())
                    .build()

                user.updateProfile(profileUpdates)
                    .addOnCompleteListener { profileTask ->
                        if (!profileTask.isSuccessful) {
                            onResult(
                                Result.failure(
                                    profileTask.exception ?: IllegalStateException()
                                )
                            )
                            return@addOnCompleteListener
                        }

                        user.sendEmailVerification()
                            .addOnCompleteListener { verificationTask ->
                                if (verificationTask.isSuccessful) {
                                    onResult(Result.success(Unit))
                                } else {
                                    onResult(
                                        Result.failure(
                                            verificationTask.exception
                                                ?: IllegalStateException()
                                        )
                                    )
                                }
                            }
                    }
            }
    }

    fun sendEmailVerification(
        onResult: (Result<Unit>) -> Unit
    ) {
        val user = firebaseAuth.currentUser
        if (user == null) {
            onResult(Result.failure(IllegalStateException("Oturum açık değil.")))
            return
        }

        user.sendEmailVerification()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    onResult(Result.success(Unit))
                } else {
                    onResult(Result.failure(task.exception ?: IllegalStateException()))
                }
            }
    }

    fun sendPasswordResetEmail(
        email: String,
        onResult: (Result<Unit>) -> Unit
    ) {
        firebaseAuth.sendPasswordResetEmail(email.trim())
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    onResult(Result.success(Unit))
                } else {
                    onResult(Result.failure(task.exception ?: IllegalStateException()))
                }
            }
    }

    fun refreshCurrentUser(
        onResult: (Result<Unit>) -> Unit
    ) {
        val user = firebaseAuth.currentUser
        if (user == null) {
            onResult(Result.failure(IllegalStateException("Oturum açık değil.")))
            return
        }

        user.reload()
            .addOnCompleteListener { reloadTask ->
                if (!reloadTask.isSuccessful) {
                    onResult(
                        Result.failure(
                            reloadTask.exception ?: IllegalStateException()
                        )
                    )
                    return@addOnCompleteListener
                }

                user.getIdToken(true)
                    .addOnCompleteListener { tokenTask ->
                        if (tokenTask.isSuccessful) {
                            onResult(Result.success(Unit))
                        } else {
                            onResult(
                                Result.failure(
                                    tokenTask.exception ?: IllegalStateException()
                                )
                            )
                        }
                    }
            }
    }

    fun signOut() {
        firebaseAuth.signOut()
    }

    fun getIdToken(): String? {
        val user = firebaseAuth.currentUser ?: return null
        return runCatching {
            Tasks.await(user.getIdToken(false), 10, TimeUnit.SECONDS).token
        }.getOrNull()
    }

    suspend fun updateDisplayName(displayName: String): Result<Unit> {
        return withContext(Dispatchers.IO) {
            runCatching {
                val user = firebaseAuth.currentUser
                    ?: throw IllegalStateException("Oturum açık değil.")
                val profileUpdates = UserProfileChangeRequest.Builder()
                    .setDisplayName(displayName.trim())
                    .build()

                Tasks.await(user.updateProfile(profileUpdates), 10, TimeUnit.SECONDS)
                Tasks.await(user.getIdToken(true), 10, TimeUnit.SECONDS)
                Unit
            }
        }
    }

    suspend fun reauthenticateWithPassword(password: String): Result<Unit> {
        return withContext(Dispatchers.IO) {
            runCatching {
                val user = firebaseAuth.currentUser
                    ?: throw IllegalStateException("Oturum açık değil.")
                val email = user.email
                    ?: throw IllegalStateException("E-posta bilgisi bulunamadı.")
                val credential = EmailAuthProvider.getCredential(email, password)

                Tasks.await(user.reauthenticate(credential), 10, TimeUnit.SECONDS)
                Tasks.await(user.getIdToken(true), 10, TimeUnit.SECONDS)
                Unit
            }
        }
    }

    suspend fun deleteCurrentUser(): Result<Unit> {
        return withContext(Dispatchers.IO) {
            runCatching {
                val user = firebaseAuth.currentUser
                    ?: throw IllegalStateException("Oturum açık değil.")
                Tasks.await(user.delete(), 10, TimeUnit.SECONDS)
                Unit
            }
        }
    }

    fun getReadableMessage(error: Throwable): String {
        return when (error) {
            is FirebaseAuthInvalidUserException -> "Bu e-posta ile kayıtlı kullanıcı bulunamadı."
            is FirebaseAuthInvalidCredentialsException -> "E-posta veya şifre hatalı."
            is FirebaseAuthRecentLoginRequiredException -> "Bu işlem için şifrenle tekrar doğrulama gerekiyor."
            is FirebaseAuthUserCollisionException -> "Bu e-posta adresi zaten kullanılıyor."
            is FirebaseAuthWeakPasswordException -> "Şifre en az 6 karakter olmalı."
            is FirebaseNetworkException -> "Ağ bağlantısı kontrol edilmeli."
            is IllegalStateException -> error.localizedMessage ?: "Oturum bilgisi bulunamadı."
            else -> error.localizedMessage ?: "İşlem tamamlanamadı."
        }
    }
}

private fun FirebaseUser.toAuthUser(): AuthUser {
    return AuthUser(
        uid = uid,
        email = email,
        displayName = displayName,
        emailVerified = isEmailVerified
    )
}
