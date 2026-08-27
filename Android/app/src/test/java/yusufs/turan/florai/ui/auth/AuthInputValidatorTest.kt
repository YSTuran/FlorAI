package yusufs.turan.florai.ui.auth

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class AuthInputValidatorTest {
    @Test
    fun validateLoginReturnsNullForValidInput() {
        val result = AuthInputValidator.validateLogin(
            email = "user@example.com",
            password = "123456"
        )

        assertNull(result)
    }

    @Test
    fun validateLoginRequiresEmailAndPassword() {
        val result = AuthInputValidator.validateLogin(
            email = "",
            password = ""
        )

        assertEquals("E-posta ve şifre zorunlu.", result)
    }

    @Test
    fun validateRegisterRejectsShortDisplayName() {
        val result = AuthInputValidator.validateRegister(
            displayName = "A",
            email = "user@example.com",
            password = "123456",
            confirmPassword = "123456"
        )

        assertEquals("Kullanıcı ismi en az 2 karakter olmalı.", result)
    }

    @Test
    fun validateRegisterRejectsPasswordMismatch() {
        val result = AuthInputValidator.validateRegister(
            displayName = "Yusuf",
            email = "user@example.com",
            password = "123456",
            confirmPassword = "654321"
        )

        assertEquals("Şifreler eşleşmiyor.", result)
    }

    @Test
    fun validatePasswordResetRequiresValidEmail() {
        val result = AuthInputValidator.validatePasswordResetEmail("user")

        assertEquals("Geçerli bir e-posta adresi gir.", result)
    }
}
