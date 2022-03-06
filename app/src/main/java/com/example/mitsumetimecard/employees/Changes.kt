package com.example.mitsumetimecard.employees

import android.util.Log
import androidx.lifecycle.ViewModel
import com.example.mitsumetimecard.dakoku.Dakoku
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase


class ChangesModel : ViewModel(){

    val changes = changesOperator.changes
    val fordebug = ArrayList<Dakoku?>()

    val database = Firebase.database.reference


    fun setChange(change:Dakoku){
        changesOperator.addChanges(change)
        fordebug.add(change)
        applyData(change)

    }

    fun showAllChanges(){
        Log.d("all changes","${fordebug}")
    }

    fun applyData(change:Dakoku){
        //database.child("DakokuSheet").child(change.id.toString()).setValue(changes)
        Log.d("single changes","applyed")
    }

    fun clearStock(){
        changesOperator.clearStock()

    }
}

object changesOperator {
    val changes = mutableListOf<Dakoku>()

    fun addChanges(newitem: Dakoku): MutableList<Dakoku> {
        changes.add(newitem)
        Log.d("changes stock" ,"${changes}")
        return changes
    }

    fun clearStock(){
        changes.clear()
         Log.d("changes stock" ,"${changes}")
    }
}

