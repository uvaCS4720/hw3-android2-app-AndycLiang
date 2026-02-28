package edu.nd.pmcburne.hwapp.one

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import kotlinx.coroutines.flow.Flow

@Entity(
    tableName = "games",
    primaryKeys = ["sport", "gameId"],
)
data class GameEntity(
    val sport: String,
    val dateIso: String,
    val gameId: String,
    val homeTeamName: String,
    val awayTeamName: String,
    val homeScore: Int?,
    val awayScore: Int?,
    val gameState: String,
    val startTime: String?,
    val startTimeEpochSeconds: Long?,
    val currentPeriod: String?,
    val contestClock: String?,
    val homeWinner: Boolean,
    val awayWinner: Boolean,
    val updatedAt: String?,
)

@Dao
interface GamesDao {
    @Query(
        """
        SELECT * FROM games
        WHERE sport = :sport AND dateIso = :dateIso
        ORDER BY (startTimeEpochSeconds IS NULL) ASC, startTimeEpochSeconds ASC, gameId ASC
        """,
    )
    fun getGames(
        sport: String,
        dateIso: String,
    ): Flow<List<GameEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(games: List<GameEntity>)

    @Query("DELETE FROM games WHERE sport = :sport AND dateIso = :dateIso")
    suspend fun deleteByDateSport(
        sport: String,
        dateIso: String,
    )
}

@Database(
    entities = [GameEntity::class],
    version = 1,
    exportSchema = false,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun gamesDao(): GamesDao
}

object DatabaseModule {
    val database_name = "scores.db"

    fun create(context: Context): AppDatabase =
        Room.databaseBuilder(context.applicationContext, AppDatabase::class.java, database_name)
            .fallbackToDestructiveMigration()
            .build()
}

