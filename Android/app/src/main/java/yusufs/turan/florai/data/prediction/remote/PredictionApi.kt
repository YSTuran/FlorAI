package yusufs.turan.florai.data.prediction.remote

import okhttp3.MultipartBody
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface PredictionApi {
    @Multipart
    @POST("predict")
    suspend fun predict(
        @Part image: MultipartBody.Part
    ): PredictResponseDto
}
