package yusufs.turan.florai.data.user.remote

import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PUT

interface UserApi {
    @GET("users/me")
    suspend fun getCurrentUserProfile(): UserProfileDto

    @PUT("users/me")
    suspend fun updateCurrentUserProfile(
        @Body request: UpdateUserProfileRequestDto
    ): UserProfileDto

    @DELETE("users/me")
    suspend fun deleteCurrentUserData(): DeleteUserDataResponseDto
}
