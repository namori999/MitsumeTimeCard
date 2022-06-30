package com.example.mitsumetimecard.ui.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class MainViewModel : ViewModel() {

    val mutableLiveData = MutableLiveData<String>()
    fun setCurrentName(name:String) {
        mutableLiveData.setValue(name)
    }

    fun getSelectedName():String{
        return mutableLiveData.value.toString()
    }
}
