package yusufs.turan.florai.ui.auth

import yusufs.turan.florai.data.auth.AuthUser

object AuthProfileDisplayNameResolver {
    fun resolve(user: AuthUser): String {
        val candidates = listOfNotNull(
            user.displayName,
            user.email?.substringBefore("@")?.replace(Regex("[._-]+"), " ")
        )

        return candidates
            .map { it.trim() }
            .firstOrNull { it.length >= 2 }
            ?.take(40)
            ?: "FlorAI kullanicisi"
    }
}
