package com.example.mitsumetimecard.employees


import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import com.example.mitsumetimecard.R


class EmpAdapter (val context: Context, val UserList: ArrayList<EmpName>) : BaseAdapter() {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view: View = LayoutInflater.from(context).inflate(R.layout.enprow, null)
        val Name = view.findViewById<TextView>(R.id.name)

        val user = UserList[position]

        Name.text = user.name

        return view
    }

    override fun getItem(position: Int): Any {
        return UserList[position]
    }

    override fun getItemId(position: Int): Long {
        return 0
    }

    fun getName(position: Int): String? {
        val user = UserList[position]

        val name = user.name


        return name

    }

    override fun getCount(): Int {
        return UserList.size
    }
}