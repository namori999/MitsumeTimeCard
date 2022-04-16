package com.example.mitsumetimecard.user

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.example.mitsumetimecard.R


class UserAdapter(val context: Context, var UserList: MutableList<User>) :  RecyclerView.Adapter<UserAdapter.RecyclerViewHolder>() {
    // リスナー格納変数
    private lateinit var listener: onItemClickListener

    fun submitList(myDataSet: List<User>?): List<User>? {

        this.UserList = myDataSet as ArrayList<User>
        notifyDataSetChanged()
        return myDataSet

    }

    //インターフェースの作成
    interface onItemClickListener {
        fun onItemClick(position: Int)
    }
    fun setOnItemClickListener(listener: onItemClickListener) {
        this.listener = listener
    }

    inner class RecyclerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        var nameView: TextView
        var nameLayout : CardView
        init {
            nameView = itemView.findViewById(R.id.name)
            nameLayout = itemView.findViewById(R.id.nameLayout)
        }
    }


    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): RecyclerViewHolder {
        val layoutInflater = LayoutInflater.from(context)
        val mView = layoutInflater.inflate(R.layout.userrow, parent, false)

        return RecyclerViewHolder(mView)

    }

    override fun onBindViewHolder(holder: RecyclerViewHolder, position: Int) {
        val current = UserList?.get(position)

        val username = current?.Name
        holder?.let { it.nameView.text = username }


        holder.itemView?.setOnClickListener() {
            listener.onItemClick(position)
        }
    }



    override fun getItemId(position: Int): Long {
        return 0
    }

    fun getName(position: Int): String? {
        val user = UserList[position]

        val name = user.Name
        return name

    }


    override fun getItemCount(): Int {
        return UserList.size
    }




}