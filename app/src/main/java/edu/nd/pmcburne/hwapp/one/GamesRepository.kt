package edu.nd.pmcburne.hwapp.one

import androidx.room.withTransaction
import java.time.LocalDate
import kotlinx.coroutines.flow.Flow

class ScoresRepository(
    private val api: NcaaApiService,
    private val db: AppDatabase,
) {
    private val dao = db.gamesDao()

    fun getGames(
        sport: Sport,
        date: LocalDate,
    ): Flow<List<GameEntity>> = dao.getGames(sport = sport.name, dateIso = date.toString())

    suspend fun refresh(
        sport: Sport,
        date: LocalDate,
    ) {
        val response = api.getScoreboard(
            sport = sport.apiSportPath,
            path = sport.apiScoreboardPath(date),
        )

        val entities = response.games.map { wrapper ->
            wrapper.game.toEntity(
                sport = sport,
                date = date,
                updatedAt = response.updatedAt,
            )
        }

        db.withTransaction {
            dao.deleteByDateSport(sport = sport.name, dateIso = date.toString())
            dao.upsertAll(entities)
        }
    }
}

private fun ApiGame.toEntity(
    sport: Sport,
    date: LocalDate,
    updatedAt: String?,
): GameEntity {
    val homeName = home.names?.short ?: home.names?.full ?: "Home"
    val awayName = away.names?.short ?: away.names?.full ?: "Away"
    return GameEntity(
        sport = sport.name,
        dateIso = date.toString(),
        gameId = gameId,
        homeTeamName = homeName,
        awayTeamName = awayName,
        homeScore = home.score?.toIntOrNull(),
        awayScore = away.score?.toIntOrNull(),
        gameState = gameState ?: "unknown",
        startTime = startTime,
        startTimeEpochSeconds = startTimeEpoch,
        currentPeriod = currentPeriod,
        contestClock = contestClock,
        homeWinner = home.winner == true,
        awayWinner = away.winner == true,
        updatedAt = updatedAt,
    )
}

