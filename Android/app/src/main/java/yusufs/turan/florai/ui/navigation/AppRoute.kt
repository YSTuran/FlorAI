package yusufs.turan.florai.ui.navigation

import android.net.Uri

sealed class AppRoute(val route: String) {
    data object Home : AppRoute("home")
    data object Prediction : AppRoute("prediction")
    data object History : AppRoute("history")
    data object Settings : AppRoute("settings")
    data object Profile : AppRoute("profile")

    data object HistoryDetail : AppRoute("history/{$PREDICTION_ID_ARG}") {
        fun createRoute(predictionId: String): String {
            return "history/${Uri.encode(predictionId)}"
        }
    }

    companion object {
        const val PREDICTION_ID_ARG = "predictionId"
    }
}
