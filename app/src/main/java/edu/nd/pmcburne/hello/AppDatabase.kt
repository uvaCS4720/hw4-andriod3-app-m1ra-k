package edu.nd.pmcburne.hello

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase

@Entity(tableName = "locations")
data class LocationEntity(
    @PrimaryKey val id: Int,
    val name: String,
    val description: String,
    val latitude: Double,
    val longitude: Double,
    val tags: String
)

@Dao
interface LocationDao {
    @Query("SELECT * FROM locations")
    suspend fun getAll(): List<LocationEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(locations: List<LocationEntity>)
}

@Database(entities = [LocationEntity::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun locationDao(): LocationDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "placemarks_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}