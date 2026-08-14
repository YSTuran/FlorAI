package yusufs.turan.florai.core.network

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import yusufs.turan.florai.BuildConfig
import yusufs.turan.florai.data.prediction.PredictionRepository
import yusufs.turan.florai.data.prediction.remote.PredictionApi
import java.util.concurrent.TimeUnit

object NetworkModule {
    private val tokenProvider: AuthTokenProvider by lazy {
        FirebaseAuthTokenProvider()
    }

    private val okHttpClient: OkHttpClient by lazy {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) {
                HttpLoggingInterceptor.Level.BASIC
            } else {
                HttpLoggingInterceptor.Level.NONE
            }
        }

        OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(45, TimeUnit.SECONDS)
            .writeTimeout(45, TimeUnit.SECONDS)
            .addInterceptor(AuthInterceptor(tokenProvider))
            .addInterceptor(loggingInterceptor)
            .build()
    }

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BuildConfig.API_BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val predictionRepository: PredictionRepository by lazy {
        PredictionRepository(
            api = retrofit.create(PredictionApi::class.java)
        )
    }
}
