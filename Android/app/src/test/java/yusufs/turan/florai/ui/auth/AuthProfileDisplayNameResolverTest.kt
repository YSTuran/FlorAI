package yusufs.turan.florai.ui.auth

import org.junit.Assert.assertEquals
import org.junit.Test
import yusufs.turan.florai.data.auth.AuthUser

class AuthProfileDisplayNameResolverTest {
    @Test
    fun resolveUsesExistingDisplayNameFirst() {
        val user = AuthUser(
            uid = "uid-1",
            email = "mail@example.com",
            displayName = "Yusuf Turan",
            emailVerified = true
        )

        val result = AuthProfileDisplayNameResolver.resolve(user)

        assertEquals("Yusuf Turan", result)
    }

    @Test
    fun resolveBuildsNameFromEmailWhenDisplayNameIsMissing() {
        val user = AuthUser(
            uid = "uid-1",
            email = "yusuf.turan@example.com",
            displayName = null,
            emailVerified = true
        )

        val result = AuthProfileDisplayNameResolver.resolve(user)

        assertEquals("yusuf turan", result)
    }

    @Test
    fun resolveFallsBackWhenNoReadableNameExists() {
        val user = AuthUser(
            uid = "uid-1",
            email = null,
            displayName = " ",
            emailVerified = true
        )

        val result = AuthProfileDisplayNameResolver.resolve(user)

        assertEquals("FlorAI kullanıcısı", result)
    }
}
