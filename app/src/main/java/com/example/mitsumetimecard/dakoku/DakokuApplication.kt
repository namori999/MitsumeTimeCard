package com.example.mitsumetimecard.dakoku

import android.app.Application
import android.content.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob


class DakokuApplication: Application()  {



    companion object {
        private lateinit var context: Context
        fun setContext(con: Context) {
            context =con
        }

    }

    val applicationScope = CoroutineScope(SupervisorJob())
    val database by lazy { DakokuDatabase.getDatabase(context, applicationScope) }
    val repository by lazy { Repository(database.dakokuDao()) }

}