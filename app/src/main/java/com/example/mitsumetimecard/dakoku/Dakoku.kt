package com.example.mitsumetimecard.dakoku

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "table")
data class Dakoku(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo var id: Int = 0,
    @ColumnInfo(name = "name") var name:String?,
    @ColumnInfo(name = "date") var date:String?,
    @ColumnInfo(name = "shukkinTime") var shukkin: Int?,
    @ColumnInfo(name = "taikinTime") var taikin:Int?,
    @ColumnInfo(name = "lestTime") var lest: Int?,
    @ColumnInfo(name = "jitsudoTime") var jitsudo:Double?,
    @ColumnInfo(name = "state") var state:String?,

){
    fun makeString(): Contents {
        var shukkinFB :Int =0
        var taikinFB :Int =0
        var lestFB :Int =0
        var jitsudoFB :Double =0.0

        if (shukkin == null) {
            shukkinFB = 0
        }else {
            shukkinFB = shukkin as Int
        }

        if (taikin == null) {
            taikinFB = 0
        }else {
            taikinFB = taikin as Int
        }

        if (lest == null) {
            lestFB = 0
        }else {
            lestFB = lest as Int
        }

        if (jitsudo == null) {
            jitsudoFB = 0.0
        }else {
            jitsudoFB = jitsudo as Double
        }

        return Contents(
            name.toString(),
            shukkinFB,
            taikinFB,
            lestFB,
            jitsudoFB,
            state.toString()
            )
    }
}


data class Contents(
    @ColumnInfo(name = "name") var name:String,
    @ColumnInfo(name = "shukkinTime") var shukkin: Int,
    @ColumnInfo(name = "taikinTime") var taikin:Int,
    @ColumnInfo(name = "lestTime") var lest: Int,
    @ColumnInfo(name = "jitsudoTime") var jitsudo:Double,
    @ColumnInfo(name = "state") var state:String,
)