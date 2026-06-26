package live.hails.hailsdotgo.api

import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ApiClient {
    private const val BASE_URL = "https://pogo.hails.live/"

    private var authToken: String? = null

    fun setToken(token: String?) { authToken = token }
    fun hasToken(): Boolean = authToken != null

    private val authInterceptor = Interceptor { chain ->
        val req = chain.request().newBuilder().apply {
            authToken?.let { header("Authorization", "Bearer $it") }
        }.build()
        chain.proceed(req)
    }

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val http = OkHttpClient.Builder()
        .addInterceptor(authInterceptor)
        .addInterceptor(loggingInterceptor)
        .build()

    val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(http)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val auth: AuthService   by lazy { retrofit.create(AuthService::class.java) }
    val iv: IVService       by lazy { retrofit.create(IVService::class.java) }
    val raid: RaidService   by lazy { retrofit.create(RaidService::class.java) }
    val events: EventService by lazy { retrofit.create(EventService::class.java) }
}
