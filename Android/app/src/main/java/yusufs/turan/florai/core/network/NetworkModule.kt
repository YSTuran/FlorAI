package yusufs.turan.florai.core.network

import com.google.firebase.auth.FirebaseAuth
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import yusufs.turan.florai.BuildConfig
import yusufs.turan.florai.data.auth.FirebaseAuthRepository
import yusufs.turan.florai.data.flower.FlowerRepository
import yusufs.turan.florai.data.flower.remote.FlowerApi
import yusufs.turan.florai.data.prediction.PredictionRepository
import yusufs.turan.florai.data.prediction.remote.PredictionApi
import yusufs.turan.florai.data.user.UserRepository
import yusufs.turan.florai.data.user.remote.UserApi
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth {
        return FirebaseAuth.getInstance()
    }

    @Provides
    @Singleton
    fun provideFirebaseAuthRepository(firebaseAuth: FirebaseAuth): FirebaseAuthRepository {
        return FirebaseAuthRepository(firebaseAuth = firebaseAuth)
    }

    @Provides
    @Singleton
    fun provideAuthTokenProvider(
        authRepository: FirebaseAuthRepository
    ): AuthTokenProvider {
        return FirebaseAuthTokenProvider(authRepository = authRepository)
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(tokenProvider: AuthTokenProvider): OkHttpClient {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) {
                HttpLoggingInterceptor.Level.BASIC
            } else {
                HttpLoggingInterceptor.Level.NONE
            }
        }

        return OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(45, TimeUnit.SECONDS)
            .writeTimeout(45, TimeUnit.SECONDS)
            .addInterceptor(AuthInterceptor(tokenProvider))
            .addInterceptor(loggingInterceptor)
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BuildConfig.API_BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    fun providePredictionApi(retrofit: Retrofit): PredictionApi {
        return retrofit.create(PredictionApi::class.java)
    }

    @Provides
    @Singleton
    fun provideUserApi(retrofit: Retrofit): UserApi {
        return retrofit.create(UserApi::class.java)
    }

    @Provides
    @Singleton
    fun provideFlowerApi(retrofit: Retrofit): FlowerApi {
        return retrofit.create(FlowerApi::class.java)
    }

    @Provides
    @Singleton
    fun providePredictionRepository(api: PredictionApi): PredictionRepository {
        return PredictionRepository(api = api)
    }

    @Provides
    @Singleton
    fun provideUserRepository(api: UserApi): UserRepository {
        return UserRepository(api = api)
    }

    @Provides
    @Singleton
    fun provideFlowerRepository(api: FlowerApi): FlowerRepository {
        return FlowerRepository(api = api)
    }
}
