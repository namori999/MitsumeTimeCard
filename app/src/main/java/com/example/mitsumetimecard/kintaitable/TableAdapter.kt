package com.example.mitsumetimecard.kintaitable



import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.example.mitsumetimecard.JitudoViewModel
import com.example.mitsumetimecard.R
import com.example.mitsumetimecard.dakoku.Dakoku


class TableAdapter(context: Context) : RecyclerView.Adapter<TableAdapter.RecyclerViewHolder>() {

    private val mContext: Context? = context
    var myDataSet: List<Dakoku>? = null

    private lateinit var listener: onItemClickListener
    private var jitudoTimes = mutableListOf<Int>()
    private var totalTime = 0.0

    @SuppressLint("NotifyDataSetChanged")
    fun submitList(myDataSet: List<Dakoku>?): List<Dakoku>? {
        this.myDataSet = null
        this.myDataSet = myDataSet
        notifyDataSetChanged()
        return myDataSet

    }

    @SuppressLint("NotifyDataSetChanged")
    fun notify(newDataset: List<Dakoku>?){
        this.myDataSet = newDataset
        notifyDataSetChanged()
    }

    @SuppressLint("NotifyDataSetChanged")
    fun clear() {
        val size: Int = itemCount
        myDataSet?.toMutableList()?.clear()
        notifyItemRangeRemoved(0, size)
        notifyDataSetChanged()
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
        var desView:LinearLayout
        var desShukkin: TextView
        var desTaikin: TextView
        var desKyukei: TextView
        var editButton: ImageView
        var cardView: CardView

        init {
            dateView = itemView.findViewById(R.id.DateTxt)
            shukkinView = itemView.findViewById(R.id.startTimeTxt)
            taikinView = itemView.findViewById(R.id.endTimeTxt)
            restView = itemView.findViewById(R.id.restTimeTxt)
            jitudouView = itemView.findViewById(R.id.jitudouTxt)
            desView = itemView.findViewById(R.id.description)
            desShukkin = itemView.findViewById(R.id.des_shukkin)
            desTaikin = itemView.findViewById(R.id.des_taikin)
            desKyukei = itemView.findViewById(R.id.des_kyukei)
            editButton = itemView.findViewById(R.id.editBtn)
            cardView = itemView.findViewById(R.id.card_view)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerViewHolder {
        val layoutInflater = LayoutInflater.from(mContext)
        val mView = layoutInflater.inflate(R.layout.kintai_row, parent, false)
        return RecyclerViewHolder(mView)
    }

    @SuppressLint("ObjectAnimatorBinding")
    override fun onBindViewHolder(holder: RecyclerViewHolder, position: Int) {
        val current = myDataSet?.get(position)
        val shukkinTime = current?.shukkin
        val taikinTime = current?.taikin
        val lestTime = current?.lest
        val jitsudoTime = current?.jitsudo

        holder?.let {
            it.dateView.text = current?.date

            if (shukkinTime == 0){
                it.shukkinView.text=""
            }else{
                it.shukkinView.text = StringBuilder().append(current?.shukkin).insert(2,":")
            }

            if (taikinTime == 0){
                it.taikinView.text = ""
            }else{
                it.taikinView.text = StringBuilder().append(current?.taikin).insert(2,":")
            }

            if (lestTime == 0){
                it.restView.text = ""
            }else{
                it.restView.text = current?.lest.toString() + "分"
            }


        }

        //start Edit Modal
        holder.itemView.setOnClickListener(){
            this.listener = listener
            listener.onItemClick(position)
        }

        /*
        // expand list onClick
        holder.itemView.setOnClickListener(){


            val increaseValue = 200


            if (holder.desView.visibility == View.VISIBLE){

                val closeAnim =
                    ValueAnimator.ofInt(holder.cardView.measuredHeight, holder.cardView.measuredHeight - increaseValue)
                closeAnim.duration = 300L
                closeAnim.addUpdateListener { model = ViewModelProviders.of(this@MainActivity).get(MainViewModel::class.java)
                    val animatedValue = closeAnim.animatedValue as Int
                    val layoutParams = holder.cardView.layoutParams
                    layoutParams.height = animatedValue
                    holder.cardView.layoutParams = layoutParams
                }
                closeAnim.start()
                holder.desView.visibility = View.GONE

            }else {
                holder?.let {
                    it.desShukkin.text =
                        "出勤：　" + it.shukkinView.text
                    it.desTaikin.text =
                        "退勤：　" + it.taikinView.text

                    it.desKyukei.text = "休憩：　" + StringBuilder().append(current?.lest) + "分"


                }
                //holder.cardView.startAnimation(inAnimation)
                //holder.cardView.setPadding(30)
                val anim =
                    ValueAnimator.ofInt(holder.cardView.measuredHeight, holder.cardView.measuredHeight + increaseValue)
                anim.duration = 500L
                anim.addUpdateListener {
                    val animatedValue = anim.animatedValue as Int
                    val layoutParams = holder.cardView.layoutParams
                    layoutParams.height = animatedValue
                    holder.cardView.layoutParams = layoutParams
                }
                anim.start()

                holder.desView.visibility = View.VISIBLE
            }
        }
        */


        if (shukkinTime ==null){

            holder?.let { it.shukkinView.text = "" }
            holder?.let { it.jitudouView.text = ""}

        }else if(taikinTime == null) {

            holder?.let { it.taikinView.text = "" }
            holder?.let { it.jitudouView.text = "" }

        }else if(lestTime == null) {

            holder?.let { it.restView.text = "" }
            holder?.let { it.jitudouView.text = "" }


        } else{


            val zitsudo: Double? = jitsudoTime

            Log.d("debug","zitudo = $zitsudo" )
            holder?.let{it.jitudouView.text = zitsudo.toString() + " h" }

            if (zitsudo == null){
                jitudoTimes.add((0.0).toInt())
            }else{
                jitudoTimes.add((zitsudo * 10).toInt())
            }


            totalTime = (jitudoTimes.sum()) / 10.0
            Log.d("tag","total Int: $totalTime")
            val model = JitudoViewModel()
            model.setJitsudo(totalTime)


        }

        holder.editButton?.setOnClickListener() {
            listener.onItemClick(position)
        }


    }



    override fun getItemCount(): Int {
        return if (myDataSet == null) 0 else myDataSet?.size!!
    }

    fun getItem(position: Int): Dakoku? {
        return myDataSet?.get(position)
    }

    fun getList(): List<Dakoku>? {
        return myDataSet
    }




}