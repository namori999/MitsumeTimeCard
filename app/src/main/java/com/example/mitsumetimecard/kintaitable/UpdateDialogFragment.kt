package com.example.mitsumetimecard.kintaitable

import android.app.AlertDialog
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.*
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProviders
import com.example.mitsumetimecard.JitudoViewModel
import com.example.mitsumetimecard.R
import com.example.mitsumetimecard.calendar.CalenderFragment
import com.example.mitsumetimecard.dakoku.Dakoku
import com.example.mitsumetimecard.dakoku.DakokuApplication
import com.example.mitsumetimecard.dakoku.DakokuViewModel
import com.example.mitsumetimecard.employees.ChangesModel
import com.google.android.material.snackbar.Snackbar


class UpdateDialogFragment : DialogFragment() {

    var application = DakokuApplication()
    var dakokuViewModel = DakokuViewModel(application.repository)
    private lateinit var jitudoModel : JitudoViewModel
    private lateinit var changesModel: ChangesModel


    companion object {

        const val TAG = "SimpleDialog"
        private const val KEY_SHUKKIN = "KEY_SHUKKIN"
        private const val KEY_TAIKIN = "KEY_TAIKIN"
        private const val KEY_LEST = "KEY_LEST"
        private const val KEY_NAME = "KEY_NAME"
        private const val KEY_DATE = "KEY_DATE"

        fun newInstance(shukkin: String, taikin: String, lest: String, name: String, date: String): UpdateDialogFragment {
            val args = Bundle()
            args.putString(KEY_SHUKKIN, shukkin)
            args.putString(KEY_TAIKIN, taikin)
            args.putString(KEY_LEST, lest)
            args.putString(KEY_NAME, name)
            args.putString(KEY_DATE, date)
            val fragment = UpdateDialogFragment()
            fragment.arguments = args
            return fragment
        }

        var selectedDate:String =""
        fun getDateToChange():String{
            return selectedDate
        }


    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.update_dialog, container, false)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupView(view)

        val progress: ProgressBar? = view.findViewById(R.id.progress)

        val date = arguments?.getString(KEY_DATE)
        val editD = view.findViewById<EditText>(R.id.des_date)
        editD.setOnClickListener(){
            val calendarView = CalendarView(this.requireActivity())
            calendarView.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)

            val layout = view.findViewById<LinearLayout>(R.id.frameLayout)
            layout.addView(calendarView)
        }

        val name = arguments?.getString(KEY_NAME)
        val dakoku = application.repository.getDakokuByDateName(
            date.toString(),
            name.toString()
        )


        val deleteImg = view.findViewById<ImageView>(R.id.closeImg)
        deleteImg.setOnClickListener {
            progress?.visibility = View.VISIBLE
            view.let {
                dismiss()
            }
        }

        changesModel = ChangesModel()

        view.findViewById<LinearLayout>(R.id.btnPositive).setOnClickListener {

            val editS = view.findViewById<EditText>(R.id.des_shukkin).text.toString()
            val editT = view.findViewById<EditText>(R.id.des_taikin).text.toString()
            val editL = view.findViewById<EditText>(R.id.des_kyukei).text.toString()

            val name = arguments?.getString(KEY_NAME)
            val date = arguments?.getString(KEY_DATE)


            val dakoku = application.repository.getDakokuByDateName(
                date.toString(),
                name.toString()
            )
            val id:Int
            if (dakoku?.id == null){
                id = 0
            }else{
                id =dakoku.id
            }

                if (editS == "" && editT == ""){
                    Toast.makeText(this.requireActivity(),"出勤/退勤 時間を追加してください",Toast.LENGTH_SHORT).show()
                }else if (editS == "") {
                    Toast.makeText(this.requireActivity(),"出勤時間を追加してください",Toast.LENGTH_SHORT).show()
                }else if(editT == "") {
                    Toast.makeText(this.requireActivity(),"退勤時間を追加してください",Toast.LENGTH_SHORT).show()
                }else {
                    val jitsudo = calcurateJitsudo(editS.toInt(),editT.toInt())

                    val data = Dakoku(
                        id,
                        name,
                        date.toString(),
                        editS.toInt(),
                        editT.toInt(),
                        editL.toInt(),
                        jitsudo,
                    ""
                    )

                    dakokuViewModel.insertOrUpdate(data)
                    changesModel.setChange( Dakoku(
                        id,
                        name,
                        date.toString(),
                        editS.toInt(),
                        editT.toInt(),
                        editL.toInt(),
                        jitsudo,
                        "Insert or Update"
                    )
                    )
                    Log.v("TAG", "data to insert : $data")

                    val totalJitsudo:Double = application.repository.getTotalJitsudo(name.toString())
                    jitudoModel = ViewModelProviders.of(this).get(JitudoViewModel::class.java)
                    jitudoModel.setJitsudo(totalJitsudo)

                    selectedDate = date.toString()

                    dismiss()
                }

        }

        view.findViewById<LinearLayout>(R.id.btnNegative).setOnClickListener {
            AlertDialog.Builder(requireActivity()) // FragmentではActivityを取得して生成
                .setTitle("")
                .setMessage("この打刻を削除しますか？")
                .setPositiveButton(
                    "削除する"
                ) { dialog, which ->

                    if (dakoku != null) {
                        reallyDeleted(dakoku)
                    }
                    Handler().postDelayed(Runnable {
                        progress?.visibility = View.GONE
                    }, 1000)
                    Snackbar.make(view, "削除されました", Snackbar.LENGTH_LONG)
                        .show()

                    dismiss()
                }.setNegativeButton("キャンセル", { dialog, which ->
                    Log.v("TAG", "delete is canseled")
                    progress?.visibility = View.GONE
                })
                .show()
        }
    }

    private fun calcurateJitsudo(shukkinTime:Int,taikinTime:Int):Double {

        val startH: Int = (shukkinTime / 100) * 60
        val startS: Int = (shukkinTime % 100)
        val start: Int = (startH + startS) //minutes

        val endH: Int = (taikinTime / 100) * 60
        val endS: Int = (taikinTime % 100)
        val end: Int = (endH + endS) //minutes
        val sa: Double = (end - start) / 60.0

        val zitsudo: Double = (Math.round(sa * 10.0) / 10.0) //to hour

        return zitsudo

    }

    private fun reallyDeleted(dakoku: Dakoku){
        dakokuViewModel.delete(dakoku)
        //adapter.notifyItemRemoved(position)

    }

    private fun setupView(view: View) {

        val editS = view.findViewById<EditText>(R.id.des_shukkin)
        val shukkin = arguments?.getString(KEY_SHUKKIN)
        if (shukkin == "null") {
            editS.setText("")
        }else{
            editS.setText(shukkin)
        }

        val editT = view.findViewById<EditText>(R.id.des_taikin)
        val taikin = arguments?.getString(KEY_TAIKIN)
        if (taikin == "null") {
            editT.setText("")
        }else{
            editT.setText(taikin)
        }

        val editL = view.findViewById<EditText>(R.id.des_kyukei)
        val lest = arguments?.getString(KEY_LEST)
        if (lest == "null") {
            editL.setText("0")
        }else{
            editL.setText(lest)
        }

        view.findViewById<EditText>(R.id.des_date).setText("${ arguments?.getString(KEY_DATE)}")

    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT
        )
    }

    override fun onResume() {
        super.onResume()
        val window: Window? = dialog!!.window
        window!!.setLayout(1000, 600)
        window.setGravity(Gravity.CENTER)
    }

    override fun onDestroy() {
        super.onDestroy()

    }

}
