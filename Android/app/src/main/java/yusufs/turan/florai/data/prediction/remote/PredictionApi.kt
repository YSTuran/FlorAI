package yusufs.turan.florai.data.prediction.remote

import okhttp3.MultipartBody
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Part
import retrofit2.http.Query

interface PredictionApi {
    @Multipart
    @POST("predict")
    suspend fun predict(
        @Part image: MultipartBody.Part
    ): PredictResponseDto

    @GET("prediction-history")
    suspend fun getPredictionHistory(
        @Query("limit") limit: Int,
        @Query("cursor") cursor: String?
    ): PredictionHistoryResponseDto

    @GET("prediction-history/{predictionId}")
    suspend fun getPredictionHistoryItem(
        @Path("predictionId") predictionId: String
    ): PredictionHistoryItemDto

    @DELETE("prediction-history/{predictionId}")
    suspend fun deletePredictionHistoryItem(
        @Path("predictionId") predictionId: String
    ): DeleteResponseDto

    @DELETE("prediction-history")
    suspend fun deletePredictionHistory(): DeleteResponseDto
}
