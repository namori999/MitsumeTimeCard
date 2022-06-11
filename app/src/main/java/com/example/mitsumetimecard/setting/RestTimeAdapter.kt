package com.example.mitsumetimecard.setting

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.example.mitsumetimecard.R


class RestTimeAdapter(val context: Context, var list: List<RestTime>) :  RecyclerView.Adapter<RestTimeAdapter.RecyclerViewHolder>() {

    private lateinit var listener: RestTimeAdapter.onItemClickListener

    //インターフェースの作成
    interface onItemClickListener {
        fun onItemClick(position: Int) {
        }
    }
    fun setOnItemClickListener(listener: onItemClickListener) {
        this.listener = listener
    }

    fun submitList(myDataSet: List<RestTime>?): List<RestTime>? {
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
    ): RestTimeAdapter.RecyclerViewHolder {
        val layoutInflater = LayoutInflater.from(context)
        val mView = layoutInflater.inflate(R.layout.userrow, parent, false)
        return RecyclerViewHolder(mView)
    }

    @SuppressLint("ResourceAsColor")
    override fun onBindViewHolder(holder: RestTimeAdapter.RecyclerViewHolder, @SuppressLint("RecyclerView") position: Int) {
        val lestTimeList = list[position]
        holder.let { it.nameView.text = lestTimeList.restime.toString() + "分" }

        holder.itemView.setOnClickListener() {
            listener.onItemClick(position)
        }

        holder.nameLayout.setOnClickListener() {

            //Toast.makeText(context, "${list[position]}}", Toast.LENGTH_LONG).show()

            PopupMenu(context, holder.itemView).apply {
                setOnMenuItemClickListener(object : PopupMenu.OnMenuItemClickListener {
                    val application = RestTimeApplication()

                    @SuppressLint("ResourceAsColor")
                    override fun onMenuItemClick(item: MenuItem): Boolean {
                        return when (item.itemId) {
                            R.id.delete -> {
                                val lestTimeList: List<RestTime> = application.database.RestTimeDao().getList()
                                application.database.RestTimeDao().delete(lestTimeList[position])
                                submitList(null)
                                submitList(application.database.RestTimeDao().getList())
                                notifyDataSetChanged()

                                true
                            }
                            else -> {
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

    override fun getItemCount(): Int {
        return list.size
    }



}



