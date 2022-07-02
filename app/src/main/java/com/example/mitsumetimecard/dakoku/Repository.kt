package com.example.mitsumetimecard.dakoku

import android.util.Log
import androidx.annotation.WorkerThread
import com.example.mitsumetimecard.MainActivity
import com.example.mitsumetimecard.ui.main.MainFragment
import kotlinx.coroutines.flow.Flow

class Repository(private val dakokuDao: DakokuDao) {

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
    suspend fun updateJitsudo(date:String,name: String) :Double{
        val shukkin = getDakokuByDateName(date, name)?.shukkin
        val taikin = getDakokuByDateName(date,name)?.taikin
        val jitsudo = calcurateJitsudou(shukkin,taikin)
        dakokuDao.updateJitsudo(jitsudo,date,name)

        return jitsudo
    }

    fun calcurateJitsudou(shukkinTime:Int?, taikinTime:Int?) :Double{
        if (shukkinTime == null || shukkinTime == 0){
            Log.d("calcurate jitsudo","no shukkin record")
        }else if (taikinTime == null || taikinTime == 0) {
            Log.d("calcurate jitsudo","no taikin record")
        }else{
            val startH = (shukkinTime / 100) * 60
            val startS: Int = (shukkinTime % 100)
            val endH: Int = (taikinTime / 100) * 60
            val endS: Int = (taikinTime % 100)

            if (shukkinTime > taikinTime && taikinTime/100 < 7) {
                val end30H = (taikinTime /100 + 24) *60
                val start: Int = (startH + startS) //minutes
                val end: Int = (end30H + endS) //minutes
                val sa: Double = (end - start) / 60.0
                val jitsudo: Double = (Math.round(sa * 10.0) / 10.0) //to hour
                return jitsudo
            }else{
                val start: Int = (startH + startS) //minutes
                val end: Int = (endH + endS) //minutes
                val sa: Double = (end - start) / 60.0
                val jitsudo: Double = (Math.round(sa * 10.0) / 10.0) //to hour
                return jitsudo
            }
        }

        return 0.0
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

    fun getDakokuOnlyShukkin(name:String):List<Dakoku>{
        return dakokuDao.getDakokuOnlyShukkin(name)
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