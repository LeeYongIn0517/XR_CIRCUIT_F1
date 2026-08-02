package app.yongin.xr_circuit.data.network

import android.content.Context
import app.yongin.xr_circuit.data.remote.jolpica.JolpicaApiService
import app.yongin.xr_circuit.data.remote.openf1.OpenF1ApiService
import app.yongin.xr_circuit.data.remote.openmeteo.OpenMeteoApiService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import java.io.File
import java.util.concurrent.TimeUnit
import javax.inject.Named
import javax.inject.Singleton
import kotlinx.serialization.json.Json
import okhttp3.Cache
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    private const val CACHE_SIZE_BYTES = 10L * 1024L * 1024L

    @Provides
    @Singleton
    fun provideJson(): Json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        explicitNulls = false
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(
        @ApplicationContext context: Context,
    ): OkHttpClient {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        }
        val cacheDir = File(context.cacheDir, "http_cache")
        return OkHttpClient.Builder()
            .cache(Cache(cacheDir, CACHE_SIZE_BYTES))
            .addInterceptor(logging)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    @Provides
    @Singleton
    @Named("OpenF1")
    fun provideOpenF1Retrofit(
        client: OkHttpClient,
        json: Json,
    ): Retrofit = retrofit(ApiBases.OPEN_F1, client, json)

    @Provides
    @Singleton
    @Named("Jolpica")
    fun provideJolpicaRetrofit(
        client: OkHttpClient,
        json: Json,
    ): Retrofit = retrofit(ApiBases.JOLPICA, client, json)

    @Provides
    @Singleton
    @Named("OpenMeteo")
    fun provideOpenMeteoRetrofit(
        client: OkHttpClient,
        json: Json,
    ): Retrofit = retrofit(ApiBases.OPEN_METEO, client, json)

    @Provides
    @Singleton
    fun provideOpenF1ApiService(
        @Named("OpenF1") retrofit: Retrofit,
    ): OpenF1ApiService = retrofit.create(OpenF1ApiService::class.java)

    @Provides
    @Singleton
    fun provideJolpicaApiService(
        @Named("Jolpica") retrofit: Retrofit,
    ): JolpicaApiService = retrofit.create(JolpicaApiService::class.java)

    @Provides
    @Singleton
    fun provideOpenMeteoApiService(
        @Named("OpenMeteo") retrofit: Retrofit,
    ): OpenMeteoApiService = retrofit.create(OpenMeteoApiService::class.java)

    private fun retrofit(
        baseUrl: String,
        client: OkHttpClient,
        json: Json,
    ): Retrofit {
        val contentType = "application/json".toMediaType()
        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(client)
            .addConverterFactory(json.asConverterFactory(contentType))
            .build()
    }
}
