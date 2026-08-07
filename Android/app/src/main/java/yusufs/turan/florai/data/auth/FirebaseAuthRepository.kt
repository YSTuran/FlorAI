package yusufs.turan.florai.data.auth

import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.auth.FirebaseUser
import com.google.android.gms.tasks.Tasks
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
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
                if (task.isSuccessful) {
                    onResult(Result.success(Unit))
                } else {
                    onResult(Result.failure(task.exception ?: IllegalStateException()))
                }
            }
    }

    fun register(
        email: String,
        password: String,
        onResult: (Result<Unit>) -> Unit
    ) {
        firebaseAuth.createUserWithEmailAndPassword(email.trim(), password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    onResult(Result.success(Unit))
                } else {
                    onResult(Result.failure(task.exception ?: IllegalStateException()))
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

    fun getReadableMessage(error: Throwable): String {
        return when (error) {
            is FirebaseAuthInvalidUserException -> "Bu e-posta ile kayitli kullanici bulunamadi."
            is FirebaseAuthInvalidCredentialsException -> "E-posta veya sifre hatali."
            is FirebaseAuthUserCollisionException -> "Bu e-posta adresi zaten kullaniliyor."
            is FirebaseAuthWeakPasswordException -> "Sifre en az 6 karakter olmali."
            is FirebaseNetworkException -> "Ag baglantisi kontrol edilmeli."
            else -> error.localizedMessage ?: "Islem tamamlanamadi."
        }
    }
}

private fun FirebaseUser.toAuthUser(): AuthUser {
    return AuthUser(
        uid = uid,
        email = email
    )
}
