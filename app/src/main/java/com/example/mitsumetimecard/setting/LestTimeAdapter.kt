package com.example.mitsumetimecard.setting

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room
import com.example.mitsumetimecard.R
import com.example.mitsumetimecard.StandByActivity
import com.example.mitsumetimecard.dakoku.Dakoku
import java.util.Collections.list


class LestTimeAdapter(val context: Context, var list: List<lestTime>) :  RecyclerView.Adapter<LestTimeAdapter.RecyclerViewHolder>() {

    // リスナー格納変数
    private lateinit var listener: LestTimeAdapter.onItemClickListener

    //インターフェースの作成
    interface onItemClickListener {
        fun onItemClick(position: Int) {
        }
    }
    fun setOnItemClickListener(listener: onItemClickListener) {
        this.listener = listener
    }

    fun submitList(myDataSet: List<lestTime>?): List<lestTime>? {

        if (myDataSet != null) {
            this.list = myDataSet
        }
        notifyDataSetChanged()
        return myDataSet

    }

    inner class RecyclerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        var nameView: TextView
        var nameLayout: CardView

        init {
            nameView = itemView.findViewById(R.id.name)
            nameLayout = itemView.findViewById(R.id.nameLayout)
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): LestTimeAdapter.RecyclerViewHolder {
        val layoutInflater = LayoutInflater.from(context)
        val mView = layoutInflater.inflate(R.layout.userrow, parent, false)
        return RecyclerViewHolder(mView)
    }

    @SuppressLint("ResourceAsColor")
    override fun onBindViewHolder(holder: LestTimeAdapter.RecyclerViewHolder, @SuppressLint("RecyclerView") position: Int) {
        val lestTimeList = list[position]
        holder?.let { it.nameView.text = lestTimeList.lestTime.toString() + "分" }


        holder.itemView?.setOnClickListener() {
            listener.onItemClick(position)
        }


        holder.nameLayout?.setOnClickListener() {
            Toast.makeText(context, "${list[position]}}", Toast.LENGTH_LONG).show()
            //holder.nameLayout.setCardBackgroundColor(R.color.colorAccentLight)

            PopupMenu(context, holder.itemView).apply {
                setOnMenuItemClickListener(object : PopupMenu.OnMenuItemClickListener {
                    val application = LestTimeApplication()

                    @SuppressLint("ResourceAsColor")
                    override fun onMenuItemClick(item: MenuItem): Boolean {
                        return when (item?.itemId) {
                            R.id.delete -> {
                                val lestTimeList: List<lestTime> = application.database.lestTimeDao().getList()
                                application.database.lestTimeDao().delete(lestTimeList[position])
                                list = emptyList()
                                submitList(lestTimeList)

                                notifyItemRemoved(position)

                                true
                            }
                            else -> {
                                holder.itemView.setBackgroundColor(R.color.white)
                                false
                            }
                        }
                    }

                })
                inflate(R.menu.delete_menu)
                show()
            }


        }
    }

    fun getItem(position: Int):lestTime {
        return list[position]
    }

    override fun getItemCount(): Int {
        return list.size
    }



}



