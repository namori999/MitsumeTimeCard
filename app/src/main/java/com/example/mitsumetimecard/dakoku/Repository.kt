package com.example.mitsumetimecard.dakoku

import androidx.annotation.WorkerThread
import kotlinx.coroutines.flow.Flow

class Repository(private val dakokuDao: DakokuDao) {

    // Room executes all queries on a separate thread.
    // Observed Flow will notify the observer when the data has changed.

    val allWords: Flow<List<Dakoku>> = dakokuDao.getAll()
    val dakokuList: List<Dakoku> = dakokuDao.getList()


    // By default Room runs suspend queries off the main thread, therefore, we don't need to
    // implement anything else to ensure we're not doing long running database work
    // off the main thread.
    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun insert(dakoku: Dakoku) {
        dakokuDao.insert(dakoku)
    }

    suspend fun insertOrUpdate(dakoku: Dakoku){
        dakokuDao.insertOrUpdateDakoku(dakoku)
    }

    fun editDakoku(date: String,shukkin: Int?,taikin:Int?,lest:Int?,jitsudo:Double?,state:String?,id:Int){
        dakokuDao.editDakoku(date,shukkin,taikin,lest,jitsudo,state,id)
    }


    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun updateShukiin(shukkin:Int?,date:String,name: String){
        dakokuDao.updateShukkin(shukkin,date,name)
    }

    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun updateTaikin(taikin:Int?,date:String,name: String){
        dakokuDao.updateTaikin(taikin,date,name)
    }

    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun updateLest(golest:Int?,date:String,name: String){
        dakokuDao.updateLest(golest,date,name)
    }

    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun updateJitsudo(jitsudo: Double?,date:String,name: String){
        dakokuDao.updateJitsudo(jitsudo,date,name)
    }


    suspend fun delete(dakoku: Dakoku){
        dakokuDao.delete(dakoku)
    }

    suspend fun update(dakoku: Dakoku){
        dakokuDao.update(dakoku)
    }

    suspend fun deleteAll() {
        dakokuDao.deleteAll()
    }


    fun getDakokuByDateName(date:String,name:String): Dakoku?{
        return dakokuDao.getDakokuByDateName(date,name)
    }

    fun getDakokuByName(name: String):Flow<List<Dakoku>>{
        return dakokuDao.getDakokuByName(name)
    }

    fun getDakokuListByName(name: String):List<Dakoku>{
        return  dakokuDao.getDakokuListByName(name)
    }


    fun getDataRowCount(date:String,name:String): Int {
        return dakokuDao.getDateRowCount(date,name)
    }
    fun getDataCount():Int {
        return dakokuDao.getRowCount()
    }

    fun getTotalJitsudo(name: String):Double{
        return dakokuDao.getTotalJitsudo(name)
    }

}