package com.example.mitsumetimecard.employees

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "empname")
data class EmpName (
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo var id: Int = 0,
    @ColumnInfo(name = "name") var name:String?,
    @ColumnInfo(name = "appaerance") var appearance:String?,
    @ColumnInfo(name = "color") var color:String?,

    ){

    // Null default values create a no-argument default constructor, which is needed
    // for deserialization from a DataSnapshot.

}