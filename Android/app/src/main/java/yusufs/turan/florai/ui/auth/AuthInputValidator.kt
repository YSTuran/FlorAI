package yusufs.turan.florai.ui.auth

object AuthInputValidator {
    fun validateLogin(email: String, password: String): String? {
        if (email.isBlank() || password.isBlank()) {
            return "E-posta ve sifre zorunlu."
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
            return "E-posta ve sifre zorunlu."
        }

        validateEmail(email)?.let { return it }

        if (displayName.isBlank()) {
            return "Kullanici ismi zorunlu."
        }

        if (displayName.length < 2) {
            return "Kullanici ismi en az 2 karakter olmali."
        }

        if (password.length < 6) {
            return "Sifre en az 6 karakter olmali."
        }

        if (password != confirmPassword) {
            return "Sifreler eslesmiyor."
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
            return "Gecerli bir e-posta adresi gir."
        }

        return null
    }
}
