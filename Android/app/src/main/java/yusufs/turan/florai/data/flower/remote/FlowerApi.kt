package yusufs.turan.florai.data.flower.remote

import retrofit2.http.GET

interface FlowerApi {
    @GET("flowers")
    suspend fun getFlowers(): List<FlowerDto>
}
