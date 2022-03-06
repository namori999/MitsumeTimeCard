package com.example.mitsumetimecard.dakoku

import android.annotation.SuppressLint
import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


@Database(entities = arrayOf(Dakoku::class), version = 5 ,  exportSchema = false)
abstract class DakokuDatabase : RoomDatabase() {

    abstract fun dakokuDao(): DakokuDao


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

                    populateDatabase(database.dakokuDao())
                }
            }

        }


        suspend fun populateDatabase(dao: DakokuDao) {
            // Start the app with a clean database every time.
            // Not needed if you only populate on creation.

            dao.deleteAll()
            val shukkinTime: Int = 1000
            val taikinTime: Int = 2000

            val startH: Int = (shukkinTime / 100) * 60
            val startS: Int = (shukkinTime % 100)
            val start: Int = (startH + startS) //minutes

            val endH: Int = (taikinTime / 100) * 60
            val endS: Int = (taikinTime % 100)
            val end: Int = (endH + endS) //minutes
            val sa: Double = (end - start) / 60.0

            val zitsudo: Double = (Math.round(sa * 10.0) / 10.0) //to hour


            val item = Dakoku(0, "name", "date",
                shukkinTime, taikinTime, 0, zitsudo, "")
            dao.insert(item)

        }
    }

    companion object {
        @Volatile
        var INSTANCE: DakokuDatabase? = null





        @SuppressLint("RestrictedApi")
        fun getDatabase(
            context: Context,
            scope: CoroutineScope
        ): DakokuDatabase {
          return INSTANCE ?: synchronized(this) {
                // Create database here

              val instance = Room.databaseBuilder(
                    context,
                    DakokuDatabase::class.java,
                    "table"
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

