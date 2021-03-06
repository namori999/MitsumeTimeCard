package com.example.mitsumetimecard.dakoku

import androidx.room.*
import kotlinx.coroutines.flow.Flow


@Dao
interface DakokuDao {


    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(table: Dakoku)

    @Update
    suspend fun update(table: Dakoku)

    @Query("UPDATE `table` SET date = :date,shukkinTime = :shukkin, taikinTime = :taikin ,lestTime = :lest ,jitsudoTime =:jitsudo,state = :state WHERE id = :id")
    suspend fun updateDakoku(date:String?,shukkin: Int?,taikin:Int?,lest: Int?,jitsudo:Double?,state:String? ,id: Int)


    @Transaction
    suspend fun insertOrUpdateDakoku(dakoku: Dakoku) {
        if (getDateRowCount(dakoku.date!!,dakoku.name!!) > 0) {
            if (dakoku.jitsudo!! > 0.0) {
                updateDakoku(
                    dakoku.date,
                    dakoku.shukkin,
                    dakoku.taikin,
                    dakoku.rest,
                    dakoku.jitsudo,
                    dakoku.state,
                    dakoku.id
                )
            }else{
                updateDakoku(
                    dakoku.date,
                    dakoku.shukkin,
                    dakoku.taikin,
                    dakoku.rest,
                    0.0,
                    dakoku.state,
                    dakoku.id
                )
            }
        } else {
            insert(dakoku)
        }
    }

    @Query("UPDATE `table` SET shukkinTime = :shukkin WHERE (date =:date) AND (name =:name)")
    fun updateShukkin(shukkin: Int?, date: String,name: String)

    @Query("UPDATE `table` SET taikinTime = :taikin WHERE (date =:date) AND (name =:name)")
    fun updateTaikin(taikin: Int?, date: String,name: String)

    @Query("UPDATE `table` SET lestTime = :lest WHERE (date =:date) AND (name =:name)")
    fun updateLest(lest: Int?, date: String,name: String)

    @Query("UPDATE `table` SET jitsudoTime = :jitsudo WHERE (date =:date) AND (name =:name)")
    fun updateJitsudo(jitsudo: Double?, date: String,name: String)

    @Delete
    suspend fun delete(table: Dakoku)

    @Query("DELETE FROM `table`")
    suspend fun deleteAll()


    @Query("SELECT * FROM `table`")
    fun getAll(): Flow<List<Dakoku>>

    @Query("SELECT * FROM `table` order by date")
    fun getList(): List<Dakoku>

    @Query("SELECT COUNT() FROM `table`")
    fun getRowCount(): Int


    @Query("SELECT * FROM `table` where id= :id")
    suspend fun getDakokuById(id: Int) : Dakoku?

    @Query("SELECT * FROM `table` where name= :name")
    fun getDakokuByName(name: String) : Flow<List<Dakoku>>

    @Query("SELECT * FROM `table` where name= :name order by date")
    fun getDakokuListByName(name: String) : List<Dakoku>

    @Query("SELECT * FROM `table` where (date = :date)AND(name =:name)")
    fun getDakokuByDateName(date: String,name: String) : Dakoku?

    @Query("SELECT * FROM `table` where (name =:name)AND(taikinTime = null or taikinTime = 0)")
    fun getDakokuOnlyShukkin(name: String) : List<Dakoku>

    @Query("SELECT COUNT() FROM `table` WHERE (date = :date) AND (name = :name)")
    fun getDateRowCount(date:String,name:String): Int

    @Query("SELECT SUM(jitsudoTime) as totaljitsudo FROM `table` where (name = :name)" )
    fun getTotalJitsudo(name:String):Double

}