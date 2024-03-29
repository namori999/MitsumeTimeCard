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

    fun insertOriginalShukkin(shukkin: Int, date: String, name: String) = viewModelScope.launch{
        database.child("DakokuSheet").child("/${date}-${name}-original").setValue(Original(shukkin,0,"original"))
    }

    fun insertOriginalTaikin(taikin: Int, date: String, name: String) = viewModelScope.launch{
        database.child("DakokuSheet").child("/${date}-${name}-original").child("taikin").setValue(taikin)
        database.child("DakokuSheet").child("/${date}-${name}-original").child("state").setValue("original")
    }

    fun insertOrUpdate(dakoku: Dakoku) = viewModelScope.launch {
        val jitsudo = repository.calcurateJitsudou(dakoku.shukkin!!,dakoku.taikin!!)
        val dakokuWidhJitsudo = (Dakoku(0,dakoku.name,dakoku.date,dakoku.shukkin,dakoku.taikin,dakoku.rest,jitsudo,dakoku.state))
        repository.insertOrUpdate(dakokuWidhJitsudo)
        database.child("DakokuSheet").child("${dakoku.date}-${dakoku.name}").setValue(dakokuWidhJitsudo.makeString())
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

    fun updateJitsudo(date:String,name:String) = viewModelScope.launch {
        val jitsudo = repository.updateJitsudo(date,name)
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


    class ModelViewModelFactory(private val repository: Repository) : ViewModelProvider.NewInstanceFactory() {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(DakokuViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return DakokuViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }

}
