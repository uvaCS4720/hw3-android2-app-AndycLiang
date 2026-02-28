package edu.nd.pmcburne.hwapp.one

import com.google.gson.annotations.SerializedName
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path

data class ScoreboardResponse(
    @SerializedName("updated_at")
    val updatedAt: String?,
    @SerializedName("games")
    val games: List<Game> = emptyList(),
)

data class Game(
    @SerializedName("game")
    val game: ApiGame,
)

data class ApiGame(
    @SerializedName("gameID")
    val gameId: String,
    @SerializedName("gameState")
    val gameState: String?,
    @SerializedName("startDate")
    val startDate: String?,
    @SerializedName("startTime")
    val startTime: String?,
    @SerializedName("startTimeEpoch")
    val startTimeEpoch: Long?,
    @SerializedName("currentPeriod")
    val currentPeriod: String?,
    @SerializedName("contestClock")
    val contestClock: String?,
    @SerializedName("finalMessage")
    val finalMessage: String?,
    @SerializedName("title")
    val title: String?,
    @SerializedName("url")
    val url: String?,
    @SerializedName("home")
    val home: ApiTeam,
    @SerializedName("away")
    val away: ApiTeam,
)

data class ApiTeam(
    @SerializedName("score")
    val score: String?,
    @SerializedName("winner")
    val winner: Boolean?,
    @SerializedName("rank")
    val rank: String?,
    @SerializedName("names")
    val names: ApiTeamNames?,
)

data class ApiTeamNames(
    @SerializedName("short")
    val short: String?,
    @SerializedName("full")
    val full: String?,
    @SerializedName("char6")
    val char6: String?,
    @SerializedName("seo")
    val seo: String?,
)

interface NcaaApiService {
    @GET("scoreboard/{sport}/{path}")
    suspend fun getScoreboard(
        @Path("sport") sport: String,
        @Path("path", encoded = true) path: String,
    ): ScoreboardResponse
}

object NetworkModule {
    val api_base = "https://ncaa-api.henrygd.me/"

    fun createService(): NcaaApiService {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        }

        val client = OkHttpClient.Builder()
            .addInterceptor(logging)
            .build()

        return Retrofit.Builder()
            .baseUrl(api_base)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(NcaaApiService::class.java)
    }
}

