package com.example.mitsumetimecard.updatedialog

import android.app.AlertDialog
import android.content.DialogInterface
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.*
import android.widget.*
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProviders
import com.example.mitsumetimecard.JitudoViewModel
import com.example.mitsumetimecard.R
import com.example.mitsumetimecard.dakoku.Dakoku
import com.example.mitsumetimecard.dakoku.DakokuApplication
import com.example.mitsumetimecard.dakoku.DakokuViewModel
import com.google.android.material.snackbar.Snackbar


class UpdateDialogFragment : DialogFragment() {

    var application = DakokuApplication()
    var dakokuViewModel = DakokuViewModel(application.repository)
    private lateinit var jitudoModel : JitudoViewModel

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

        lateinit var editD: EditText

    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.update_dialog, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupView(view)

        val progress: ProgressBar? = view.findViewById(R.id.progress)

        val date = arguments?.getString(KEY_DATE)
        editD = view.findViewById<EditText>(R.id.des_date)
        editD.setOnClickListener(){
            val datePicker = DatePicker(this.requireContext())
            datePicker.calendarViewShown = false
            DatePickerFragment().show(requireFragmentManager(), "timePicker")
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


        view.findViewById<LinearLayout>(R.id.btnPositive).setOnClickListener {

            val editS =  view.findViewById<EditText>(R.id.des_shukkin).text.padStart(4, '0').toString()
            val sH = editS.substring(0,2).toIntOrNull()
            val sM = editS.substring(2,4).toIntOrNull()

            val editT = view.findViewById<EditText>(R.id.des_taikin).text.padStart(4, '0').toString()
            val tH = editT.substring(0,2).toIntOrNull()
            val tM = editT.substring(2,4).toIntOrNull()

            val editL = view.findViewById<EditText>(R.id.des_kyukei).text.toString()

            val name = arguments?.getString(KEY_NAME).toString()
            val date = editD.text.toString()


            val dakoku = application.repository.getDakokuByDateName(date, name)
            val id:Int
            if (dakoku?.id == null){
                id = 0
            }else {
                id = dakoku.id
            }
                if (editS == "" && editT == ""){
                    Toast.makeText(this.requireActivity(),"出勤/退勤 時間を追加してください", Toast.LENGTH_SHORT).show()
                }else if (editS == "") {
                    Toast.makeText(this.requireActivity(),"出勤時間を追加してください", Toast.LENGTH_SHORT).show()
                }else if(editT == "") {
                    Toast.makeText(this.requireActivity(),"退勤時間を追加してください", Toast.LENGTH_SHORT).show()
                }else if (sH != null && sH > 24) {
                    Toast.makeText(this.requireActivity(), "出勤時間は 24時までを入力してください", Toast.LENGTH_SHORT).show()
                } else if (sM != null && sM > 59) {
                    Toast.makeText(this.requireActivity(), "出勤時間は 59分までを入力してください", Toast.LENGTH_SHORT).show()
                } else if (tH != null && tH > 24) {
                    Toast.makeText(this.requireActivity(), "退勤時間は 24時までを入力してください", Toast.LENGTH_SHORT).show()
                } else if (tM != null && tM > 59) {
                    Toast.makeText(this.requireActivity(), "退勤時間は 59分までを入力してください", Toast.LENGTH_SHORT).show()
                } else {

                    val jitsudo = editS.toIntOrNull()
                        ?.let { it1 -> editT.toIntOrNull()
                            ?.let { it2 -> calcurateJitsudo(it1, it2) } }
                    if (jitsudo != null && jitsudo < 0.0) {
                        Toast.makeText(this.requireActivity(), "実働時間がマイナスになります", Toast.LENGTH_SHORT).show()
                    }else {
                        val data = Dakoku(
                            id,
                            name,
                            date,
                            editS.toIntOrNull(),
                            editT.toIntOrNull(),
                            editL.toIntOrNull()!!,
                            jitsudo,
                            ""
                        )

                        dakokuViewModel.insertOrUpdate(data)

                        Log.v("TAG", "data to insert : $data")

                        val totalJitsudo: Double =
                            application.repository.getTotalJitsudo(name.toString())
                        jitudoModel = ViewModelProviders.of(this).get(JitudoViewModel::class.java)
                        jitudoModel.setJitsudo(totalJitsudo)

                        selectedDate = date.toString()

                        //返したい情報をセット
                        val dataTo = Bundle()
                        dataTo.putString("when", selectedDate)
                        dataTo.putString("toCalender", selectedDate)
                        parentFragmentManager.setFragmentResult("input", dataTo)

                        dismiss()
                    }

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

        val editD = view.findViewById<EditText>(R.id.des_date)
        editD.setText("${ arguments?.getString(KEY_DATE)}")

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

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        Log.d("updateDialog","onDismiss")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d("updateDialog","onDestroy")
    }

}

private operator fun Any.compareTo(i: Int): Int {
    return i
}
