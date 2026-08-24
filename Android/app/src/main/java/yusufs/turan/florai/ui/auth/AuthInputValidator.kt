package yusufs.turan.florai.ui.auth

object AuthInputValidator {
    fun validateLogin(email: String, password: String): String? {
        if (email.isBlank() || password.isBlank()) {
            return "E-posta ve şifre zorunlu."
        }

        return validateEmail(email)
    }

    fun validateRegister(
        displayName: String,
        email: String,
        password: String,
        confirmPassword: String
    ): String? {
        if (email.isBlank() || password.isBlank()) {
            return "E-posta ve şifre zorunlu."
        }

        validateEmail(email)?.let { return it }

        if (displayName.isBlank()) {
            return "Kullanıcı ismi zorunlu."
        }

        if (displayName.length < 2) {
            return "Kullanıcı ismi en az 2 karakter olmalı."
        }

        if (password.length < 6) {
            return "Şifre en az 6 karakter olmalı."
        }

        if (password != confirmPassword) {
            return "Şifreler eşleşmiyor."
        }

        return null
    }

    fun validatePasswordResetEmail(email: String): String? {
        return validateEmail(email)
    }

    private fun validateEmail(email: String): String? {
        if (email.isBlank()) {
            return "E-posta zorunlu."
        }

        if (!email.contains("@") || !email.contains(".")) {
            return "Geçerli bir e-posta adresi gir."
        }

        return null
    }
}
