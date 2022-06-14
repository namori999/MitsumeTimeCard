package com.example.mitsumetimecard.setting

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import androidx.room.*
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch


@Entity (tableName = "restTimeTable")
data class RestTime(
    @PrimaryKey
    var restime :Int
)


@Dao
interface RestTimeDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(restTime: RestTime)

    @Delete
    fun delete(restTime: RestTime)


    @Query("DELETE FROM `restTimeTable`")
    suspend fun deleteAll()

    @Insert
    fun insertAll(users: List<RestTime>)

    @Query("SELECT * FROM `restTimeTable` ORDER BY restime")
    fun getList(): List<RestTime>

    @Query("SELECT * FROM `restTimeTable` ORDER BY restime")
    fun getMutableList(): MutableList<Int>


}


@Database(entities = arrayOf(RestTime::class), version = 2,  exportSchema = false)
abstract class RestTimeDatabase : RoomDatabase() {

    abstract fun RestTimeDao(): RestTimeDao

    private class databaseCallback(
        private val scope: CoroutineScope
    ) : RoomDatabase.Callback() {

        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            // If you want to keep the data through app restarts,
            // comment out the following line.
            INSTANCE?.let { database ->
                scope.launch(Dispatchers.IO) {
                    populateDatabase(database.RestTimeDao())
                }
            }
        }

        suspend fun populateDatabase(dao: RestTimeDao) {
            dao.deleteAll()
            val lestTimeList = mutableListOf(
                RestTime(12), RestTime(30),RestTime(42),RestTime(60)
            )
            dao.insertAll(lestTimeList)
        }
    }

    companion object {
        @Volatile
        var INSTANCE: RestTimeDatabase? = null

        @SuppressLint("RestrictedApi")
        fun getDatabase(
            context: Context,
            scope: CoroutineScope
        ): RestTimeDatabase {
            return INSTANCE ?: synchronized(this) {
                // Create database here

                val instance = Room.databaseBuilder(
                    context,
                    RestTimeDatabase::class.java,
                    "lestTimeTable"
                )
                    .addCallback(databaseCallback(scope))
                    .fallbackToDestructiveMigration()
                    .allowMainThreadQueries()
                    .build()

                INSTANCE = instance
                instance
            }
        }
    }

}


class RestTimeApplication: Application()  {

    companion object {
        private lateinit var context: Context
        fun setContext(con: Context) {
            context =con
        }
    }

    val applicationScope = CoroutineScope(SupervisorJob())
    val database = RestTimeDatabase.getDatabase(context, applicationScope)
    val lestTimeDao = database.RestTimeDao()
}
