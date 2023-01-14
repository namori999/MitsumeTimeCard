package com.example.mitsumetimecard.ui.main

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.mitsumetimecard.dakoku.Dakoku

class MainViewModel : ViewModel() {

    val mutableLiveData = MutableLiveData<String>()
    fun setCurrentName(name:String) {
        mutableLiveData.value = name
        Log.d("setCurrentName",name);
    }

    fun getSelectedName():String{
        return mutableLiveData.value.toString()
    }

    val dakokuLiveData = MutableLiveData<List<Dakoku>>()
    fun setDakokuByName(dakokuList: List<Dakoku>){
        dakokuLiveData.value = dakokuList;
        Log.d("setDakokuByName",dakokuList.toString());
    }

    fun getDakokuByName(): List<Dakoku>? {
        return dakokuLiveData.value
    }
}
