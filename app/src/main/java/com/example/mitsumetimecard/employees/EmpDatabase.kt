package com.example.mitsumetimecard.employees

import android.annotation.SuppressLint
import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


@Database(entities = arrayOf(EmpName::class), version = 0 ,  exportSchema = false)
abstract class EmpDatabase : RoomDatabase() {

    abstract fun empDao(): EmpDao


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

                    populateDatabase(database.empDao())
                }
            }

        }


        suspend fun populateDatabase(dao: EmpDao) {
            // Start the app with a clean database every time.
            // Not needed if you only populate on creation.

            val item = EmpName(0, "name", "","ffffff")
            dao.insert(item)

        }
    }

    companion object {
        @Volatile
        var INSTANCE: EmpDatabase? = null





        @SuppressLint("RestrictedApi")
        fun getDatabase(
            context: Context,
            scope: CoroutineScope
        ): EmpDatabase {
            return INSTANCE ?: synchronized(this) {
                // Create database here

                val instance = Room.databaseBuilder(
                    context,
                    EmpDatabase::class.java,
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

