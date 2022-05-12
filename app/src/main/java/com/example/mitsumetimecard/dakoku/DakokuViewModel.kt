package com.example.mitsumetimecard.dakoku

import androidx.lifecycle.*
import androidx.room.Database
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch


open class DakokuViewModel(val repository: Repository) : ViewModel() {

    val database = Firebase.database.reference

    fun insertFromFB (dakoku: Dakoku) = viewModelScope.launch {
        repository.insert(dakoku)
    }

    fun insert(dakoku: Dakoku) = viewModelScope.launch {
        repository.insert(dakoku)
        database.child("DakokuSheet").child("${dakoku.date}-${dakoku.name}").setValue(dakoku.makeString())
    }

    fun insertOrUpdate(dakoku: Dakoku) = viewModelScope.launch {
        repository.insertOrUpdate(dakoku)
        database.child("DakokuSheet").child("${dakoku.date}-${dakoku.name}").setValue(dakoku.makeString())
    }


    fun updateShukkin(shukkin:Int,date:String,name:String) = viewModelScope.launch {
        repository.updateShukiin(shukkin,date,name)
        database.child("DakokuSheet").child("/${date}-${name}").child("shukkin").setValue(shukkin)
    }

    fun updateTaikin(taikin:Int,date:String,name:String) = viewModelScope.launch {
        repository.updateTaikin(taikin,date,name)
        database.child("DakokuSheet").child("/${date}-${name}").child("taikin").setValue(taikin)

    }

    fun updateLest(golest:Int,date:String,name: String) = viewModelScope.launch{
        repository.updateLest(golest,date,name)
        database.child("DakokuSheet").child("/${date}-${name}").child("lest").setValue(golest)

    }

    fun updateJitsudo(jitsudo:Double,date:String,name:String) = viewModelScope.launch {
        repository.updateJitsudo(jitsudo,date,name)
        database.child("DakokuSheet").child("/${date}-${name}").child("jitsudo").setValue(jitsudo)

    }


    fun delete(dakoku: Dakoku) = viewModelScope.launch {
        repository.delete(dakoku)
        database.child("DakokuSheet").child("/${dakoku.date}-${dakoku.name}").child("state").setValue("deleted")

    }

    fun update(dakoku: Dakoku) = viewModelScope.launch {
        repository.update(dakoku)
        database.child("DakokuSheet").child("/${dakoku.date}-${dakoku.name}").setValue(dakoku.makeString())

    }

    fun deleteAll() = viewModelScope.launch {
        repository.deleteAll()
    }


    class ModelViewModelFactory(private val repository: Repository) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(DakokuViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return DakokuViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }

}
