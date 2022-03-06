package com.example.mitsumetimecard.ui.main

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import com.example.mitsumetimecard.R
import com.example.mitsumetimecard.dakoku.Dakoku
import com.example.mitsumetimecard.dakoku.DakokuApplication
import com.example.mitsumetimecard.dakoku.DakokuViewModel
import com.example.mitsumetimecard.employees.ChangesModel
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import java.lang.Math.round
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.*


class MainFragment : Fragment() {


    var application = DakokuApplication()
    var dakokuViewModel = DakokuViewModel(application.repository)

    private var userName:String = ""

    private var selectedTime:Int =0

    private lateinit var database: DatabaseReference

    private lateinit var model: MainViewModel
    private lateinit var changesModel: ChangesModel


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.main_fragment, container, false)

    }

    @SuppressLint("SetTextI18n", "ResourceAsColor", "UseCompatLoadingForColorStateLists")
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        //get username
        model = ViewModelProviders.of(requireActivity()).get(MainViewModel::class.java)
        model.mutableLiveData.observe(requireActivity(),object : Observer,
            androidx.lifecycle.Observer<String> {

            override fun onChanged(o: String?) {
                val selectedName = o!!.toString()
                userName = selectedName
            }
            override fun update(o: Observable?, arg: Any?) {
            }

        })



        //get date and time
        val textDate = view?.findViewById<TextView>(R.id.textDate)

        val onlyDate: LocalDate = LocalDate.now()

        val date1: LocalDateTime = LocalDateTime.now()
        val dtformat2: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd E")
        val fdate2: String = dtformat2.format(date1)
        textDate?.setText("$fdate2")



        //data to insert

        val date = onlyDate.toString()

        val noSeconds: DateTimeFormatter = DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT)
            .withLocale(Locale.JAPAN)
        var time = LocalTime.now().format(noSeconds)
        time = time.replace((":").toRegex(), "")

        val marume = (round(time.toInt() * 1.0 / 6) * 6)
        var marumeTime:Int

        val checkMarume = (marume % 100)
        if (56 < checkMarume ){
            marumeTime = (((marume / 100 ) + 1)* 100).toInt()
        }else{
            marumeTime = marume.toInt()
        }




        //firebase database
        database = Firebase.database.reference

        //view model to stock changes
        changesModel = ChangesModel()

        //check dakoku already exist or not
        val repository = dakokuViewModel.repository
        var data: Int

        val empname = userName

        //buttons
        val shukkinBtn = view?.findViewById<Button>(R.id.shukkinBtn)
        val taikinBtn = view?.findViewById<Button>(R.id.taikinBtn)

        //setup button color
        data = repository.getDataRowCount(date,empname)
        if (data == 0) {
            Log.d("data check","first dakoku today")
        }else{
            val currentData = repository.getDakokuByDateName(date,empname)

            if (currentData?.shukkin == null) {
                taikinBtn?.setBackgroundTintList(
                    this.getResources().getColorStateList(R.color.colorAccentLight)
                );
            }
            if (currentData?.taikin == null){
                shukkinBtn?.setBackgroundTintList(
                    this.getResources().getColorStateList(R.color.colorAccentLight)
                );
            }

        }


        //onClick
        shukkinBtn?.setOnClickListener() {
            data = repository.getDataRowCount(date,empname)

            if (data == 0) {

                val newdakoku = Dakoku(0, empname, date, marumeTime, null, null, null,"")
                Log.v("TAG", "data to insert(Shukkin) : $newdakoku")
                dakokuViewModel.insert(newdakoku)
                //changesModel.setChange(Dakoku(0, empname, date, marumeTime, null, null, null,"insert"))

                Toast.makeText(requireContext(), "出勤しました", Toast.LENGTH_SHORT)
                    .show()

                shukkinBtn?.setBackgroundTintList(this.getResources().getColorStateList(R.color.colorAccentLight));

            } else {
                Log.v("TAG", "data is already here")

                AlertDialog.Builder(this.requireActivity()) // FragmentではActivityを取得して生成
                    .setTitle("すでに出勤しています")
                    .setMessage("出勤時間を更新しますか？")
                    .setPositiveButton("更新する", { dialog, which ->
                        dakokuViewModel.updateShukkin(marumeTime,date,empname)
                        val dakoku:Dakoku? = repository.getDakokuByDateName(date,empname)
                        val jitsudo = calcurateJitsudou(dakoku)
                        dakokuViewModel.updateJitsudo(jitsudo,date,empname)

                        /*
                        changesModel.setChange(
                            Dakoku(0, empname, date, marumeTime, null, null, null,"update")
                        )
                        */

                    })
                    .setNegativeButton("キャンセル", { dialog, which ->
                        Log.v("TAG", "shukkin update is canseled")
                    })
                    .show()

            }

        }

        taikinBtn?.setOnClickListener() {
            Log.d("tag","selectedTime : $selectedTime")
            data = repository.getDataRowCount(date,empname)

            if (data == 0) {

                val newdakoku = Dakoku(0, empname, date, null, marumeTime.toInt(),  selectedTime, 0.0,"")
                Log.v("TAG", "data to insert(Taikin) : $newdakoku")
                dakokuViewModel.insert(newdakoku)
                //database.child("DakokuSheet").setValue(newdakoku)

                changesModel.setChange(newdakoku)
                changesModel.clearStock()

                showAlertDialog(date,empname)

                Toast.makeText(
                    requireContext(),
                    "出勤が記録されていません。今月の記録 から追加してください。",
                    Toast.LENGTH_SHORT
                )
                    .show()


            } else {

                dakokuViewModel.updateTaikin(marumeTime.toInt(),date,empname)
                val dakoku:Dakoku? = repository.getDakokuByDateName(date,empname)
                calcurateJitsudou(dakoku)
                val jitsudo = calcurateJitsudou(dakoku)
                dakokuViewModel.updateJitsudo(jitsudo,date,empname)


                showAlertDialog(date,empname)

            }

        }


    }



    private fun calcurateJitsudou(dakoku: Dakoku?) :Double{
        val shukkinTime = dakoku?.shukkin
        val taikinTime = dakoku?.taikin

        if (shukkinTime == null){
            Log.d("calcurate jitsudo","no shukkin record")

        }else if (taikinTime == null) {
            Log.d("calcurate jitsudo","no taikin record")
        } else {

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

        return 0.0

    }


    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    private fun showAlertDialog(date:String, empname:String){
        val alertDialog = AlertDialog.Builder(requireContext())
        alertDialog.setTitle("休憩時間を選んでください")

        val items = arrayOf("休憩なし", "12分", "30分", "42分", "60分")
        val checkedItem = 0

        alertDialog.setSingleChoiceItems(
            items, checkedItem
        ) { dialog, which ->
            when (which) {
                0 -> dakokuViewModel.updateLest(0,date,empname)
                1 -> dakokuViewModel.updateLest(12,date,empname)
                2 -> dakokuViewModel.updateLest(30,date,empname)
                3 -> dakokuViewModel.updateLest(42,date,empname)
                4 -> dakokuViewModel.updateLest(60,date,empname)
            }
        }

        alertDialog.setPositiveButton(
            "退勤"
        ) { dialog, which ->
            Toast.makeText(requireContext(), "退勤しました", Toast.LENGTH_SHORT)
                .show()

            val taikinBtn: Button? = view?.findViewById(R.id.taikinBtn)
            taikinBtn?.setBackgroundTintList(
                this.getResources().getColorStateList(R.color.colorAccentLight)
            );
        }


        val alert = alertDialog.create()
        alert.setCanceledOnTouchOutside(true)
        alert.show()

    }
}