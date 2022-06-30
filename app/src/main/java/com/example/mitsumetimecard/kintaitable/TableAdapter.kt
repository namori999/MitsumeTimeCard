package com.example.mitsumetimecard.kintaitable



import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.example.mitsumetimecard.JitudoViewModel
import com.example.mitsumetimecard.R
import com.example.mitsumetimecard.dakoku.Dakoku


class TableAdapter(context: Context) : RecyclerView.Adapter<TableAdapter.RecyclerViewHolder>() {

    private val mContext: Context? = context
    var myDataSet: List<Dakoku>? = null

    private lateinit var listener: TableAdapter.onItemClickListener
    private var jitudoTimes = mutableListOf<Int>()
    private var totalTime = 0.0

    @SuppressLint("NotifyDataSetChanged")
    fun submitList(myDataSet: List<Dakoku>?): List<Dakoku>? {
        this.myDataSet = null
        this.myDataSet = myDataSet
        notifyDataSetChanged()
        return myDataSet
    }

    interface onItemClickListener {
        fun onItemClick(position: Int)
    }

    fun setOnItemClickListener(listener: onItemClickListener) {
        this.listener = listener
    }

    inner class RecyclerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        var dateView: TextView
        var shukkinView: TextView
        var taikinView: TextView
        var restView: TextView
        var jitudouView: TextView
        var cardView: CardView

        init {
            dateView = itemView.findViewById(R.id.DateTxt)
            shukkinView = itemView.findViewById(R.id.startTimeTxt)
            taikinView = itemView.findViewById(R.id.endTimeTxt)
            restView = itemView.findViewById(R.id.restTimeTxt)
            jitudouView = itemView.findViewById(R.id.jitudouTxt)
            cardView = itemView.findViewById(R.id.card_view)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerViewHolder {
        val layoutInflater = LayoutInflater.from(mContext)
        val mView = layoutInflater.inflate(R.layout.kintai_row, parent, false)
        return RecyclerViewHolder(mView)
    }

    @SuppressLint("ObjectAnimatorBinding", "SetTextI18n")
    override fun onBindViewHolder(holder: RecyclerViewHolder, position: Int) {
        val current = myDataSet?.get(position)
        val shukkinTime = current?.shukkin
        val taikinTime = current?.taikin
        val lestTime = current?.rest
        val jitsudoTime = current?.jitsudo

        holder.let {
            it.dateView.text = current?.date
            if (shukkinTime == 0){
                it.shukkinView.text=""
            }else{
                val time =current?.shukkin.toString().padStart(4, '0')
                it.shukkinView.text = StringBuilder().append(time).insert(2,":")
            }
            if (taikinTime == 0){
                it.taikinView.text = ""
            }else{
                val time =current?.taikin.toString().padStart(4, '0')
                it.taikinView.text = StringBuilder().append(time).insert(2,":")
            }
            if (lestTime == 0){
                it.restView.text = ""
            }else{
                it.restView.text = current?.rest.toString() + "分"
            }
        }

        //start Edit Modal
        holder.itemView.setOnClickListener(){
            listener.onItemClick(position)
        }

        if (shukkinTime ==null){
            holder.let { it.shukkinView.text = "" }
            holder.let { it.jitudouView.text = ""}
        }else if(taikinTime == 0) {
            holder.let { it.taikinView.text = "" }
            holder.let { it.jitudouView.text = "" }
        }else if(lestTime == null) {
            holder.let { it.restView.text = "" }
            holder.let { it.jitudouView.text = "" }
        }

        val jitsudo: Double? = jitsudoTime
        if (jitsudo != null) {
            if (jitsudo <= 0.0) {
                holder.let { it.jitudouView.text = "＊" }
            } else {
                holder.let{it.jitudouView.text = jitsudo.toString() + " h" }
            }
        }

        if (jitsudo == null){
            jitudoTimes.add((0.0).toInt())
        }else{
            jitudoTimes.add((jitsudo * 10).toInt())
        }

        totalTime = (jitudoTimes.sum()) / 10.0
        val model = JitudoViewModel()
        model.setJitsudo(totalTime)
    }


    override fun getItemCount(): Int {
        return if (myDataSet == null) 0 else myDataSet?.size!!
    }

    fun getItem(position: Int): Dakoku? {
        return myDataSet?.get(position)
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getItemViewType(position: Int): Int {
        return position
    }
}