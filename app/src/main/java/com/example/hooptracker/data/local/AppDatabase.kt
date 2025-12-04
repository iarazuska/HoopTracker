package com.example.hooptracker.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.hooptracker.data.local.dao.MatchDao
import com.example.hooptracker.data.local.dao.PlayerDao
import com.example.hooptracker.data.local.dao.PlayerStatDao
import com.example.hooptracker.data.local.dao.TeamDao
import com.example.hooptracker.data.local.dao.TournamentDao
import com.example.hooptracker.data.local.dao.UserDao
import com.example.hooptracker.data.local.entity.Match
import com.example.hooptracker.data.local.entity.Player
import com.example.hooptracker.data.local.entity.PlayerStat
import com.example.hooptracker.data.local.entity.Team
import com.example.hooptracker.data.local.entity.Tournament
import com.example.hooptracker.data.local.entity.User
import com.example.hooptracker.domain.UserRole
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Base de datos principal de la app que reúne todas las entidades y DAOs,
 * aplica convertidores y crea un usuario administrador por defecto.
 */

@Database(
    entities = [
        User::class,
        Team::class,
        Player::class,
        Match::class,
        PlayerStat::class,
        Tournament::class
    ],
    version = 5,
    exportSchema = true
)
@TypeConverters(UserRoleConverters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun userDao(): UserDao
    abstract fun teamDao(): TeamDao
    abstract fun playerDao(): PlayerDao
    abstract fun matchDao(): MatchDao
    abstract fun playerStatDao(): PlayerStatDao
    abstract fun tournamentDao(): TournamentDao

    companion object {

        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {

                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "hooptracker.db"
                )
                    .addCallback(object : RoomDatabase.Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)

                            CoroutineScope(Dispatchers.IO).launch {
                                val database = INSTANCE ?: return@launch
                                val userDao = database.userDao()

                                val existing = userDao.findByEmail("admin@admin.com")
                                if (existing == null) {
                                    userDao.insert(
                                        User(
                                            id = 0,
                                            name = "admin",
                                            email = "admin@admin.com",
                                            password = "admin123",
                                            role = UserRole.TOURNAMENT_ADMIN,
                                            isLoggedIn = false
                                        )
                                    )
                                }
                            }
                        }
                    })
                    .fallbackToDestructiveMigration()
                    .build()

                INSTANCE = instance
                instance
            }
        }
    }
}
