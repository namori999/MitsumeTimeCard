package com.example.mitsumetimecard.employees

import androidx.room.*


@Dao
interface EmpDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(empname:EmpName)

    @Update
    suspend fun update(empname: EmpName)



    //@Query("UPDATE `empname` SET name = :newname WHERE name=:name")
    //fun updateName(newname: String,name:String)



    @Delete
    suspend fun delete(empname: EmpName)
}