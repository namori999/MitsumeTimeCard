package com.example.mitsumetimecard

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class JitudoViewModel: ViewModel() {

    val dataSet = MutableLiveData<Double>()
    var total :Double = 0.0

    fun getJitsudoTime(shukkin: Int,taikin: Int):Double{

        val startH: Int = (shukkin / 100) * 60
        val startS: Int = (shukkin % 100)
        val start: Int = (startH + startS) //minutes

        val endH: Int = (taikin / 100) * 60
        val endS: Int = (taikin % 100)
        val end: Int = (endH + endS) //minutes
        val sa: Double = (end - start) / 60.0

        val jitsudo: Double = (Math.round(sa * 10.0) / 10.0) //to hour
        total = total + jitsudo

        return jitsudo
    }


    fun setJitsudo(totalJitsudo:Double){
            dataSet.setValue(totalJitsudo)
    }

}