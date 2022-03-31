package com.example.mitsumetimecard.setting

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import androidx.annotation.WorkerThread
import androidx.room.*
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.mitsumetimecard.StandByActivity
import com.example.mitsumetimecard.dakoku.Dakoku
import com.example.mitsumetimecard.dakoku.DakokuDao
import com.example.mitsumetimecard.dakoku.Repository
import com.example.mitsumetimecard.employees.EmpName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch


@Entity (tableName = "lestTimeTable")
data class lestTime(
    @PrimaryKey
    var lestTime :Int
)


@Dao
interface LestTimeDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(lestTime: lestTime)

    @Update
    suspend fun update(lestTime: lestTime)

    @Delete
    fun delete(lestTime: lestTime)

    @Query("DELETE FROM `lestTimeTable`")
    suspend fun deleteAll()

    @Insert
    fun insertAll(users: List<lestTime>)

    @Query("SELECT * FROM `lestTimeTable`")
    fun getList(): List<lestTime>


}


@Database(entities = arrayOf(lestTime::class), version = 1 ,  exportSchema = false)
abstract class LestTimeDatabase : RoomDatabase() {

    abstract fun lestTimeDao(): LestTimeDao


    private class databaseCallback(
        private val scope: CoroutineScope
    ) : RoomDatabase.Callback() {
        /**
         * Override the onCreate method to populate the database.
         */
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            // If you want to keep the data through app restarts,
            // comment out the following line.
            INSTANCE?.let { database ->
                scope.launch(Dispatchers.IO) {
                    populateDatabase(database.lestTimeDao())
                }
            }

        }

        suspend fun populateDatabase(dao: LestTimeDao) {
            // Start the app with a clean database every time.
            // Not needed if you only populate on creation.

            dao.deleteAll()
            val lestTimeList = mutableListOf(
                lestTime(0),lestTime(12), lestTime(30),lestTime(42),lestTime(60)
            )
            dao.insertAll(lestTimeList)

        }
    }

    companion object {
        @Volatile
        var INSTANCE: LestTimeDatabase? = null

        @SuppressLint("RestrictedApi")
        fun getDatabase(
            context: Context,
            scope: CoroutineScope
        ): LestTimeDatabase {
            return INSTANCE ?: synchronized(this) {
                // Create database here

                val instance = Room.databaseBuilder(
                    context,
                    LestTimeDatabase::class.java,
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


class LestTimeApplication: Application()  {

    companion object {
        private lateinit var context: Context
        fun setContext(con: Context) {
            context =con
        }
    }

    val applicationScope = CoroutineScope(SupervisorJob())
    val database by lazy { LestTimeDatabase.getDatabase(context, applicationScope) }
    val lestTimeDao = database.lestTimeDao()
}
