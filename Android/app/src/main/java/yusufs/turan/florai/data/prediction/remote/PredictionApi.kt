package yusufs.turan.florai.data.prediction.remote

import okhttp3.MultipartBody
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Part

interface PredictionApi {
    @Multipart
    @POST("predict")
    suspend fun predict(
        @Part image: MultipartBody.Part
    ): PredictResponseDto

    @GET("prediction-history")
    suspend fun getPredictionHistory(): PredictionHistoryResponseDto

    @DELETE("prediction-history/{predictionId}")
    suspend fun deletePredictionHistoryItem(
        @Path("predictionId") predictionId: String
    ): DeleteResponseDto

    @DELETE("prediction-history")
    suspend fun deletePredictionHistory(): DeleteResponseDto
}
