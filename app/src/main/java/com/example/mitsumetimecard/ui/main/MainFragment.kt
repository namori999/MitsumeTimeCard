package com.example.mitsumetimecard.ui.main

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import com.example.mitsumetimecard.R
import com.example.mitsumetimecard.dakoku.Dakoku
import com.example.mitsumetimecard.dakoku.DakokuApplication
import com.example.mitsumetimecard.dakoku.DakokuViewModel
import com.example.mitsumetimecard.employees.ChangesModel
import com.example.mitsumetimecard.setting.LestTimeApplication
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

    private var empname:String = ""

    private var selectedTime:Int =0

    private lateinit var database: DatabaseReference

    private lateinit var model: MainViewModel
    private lateinit var changesModel: ChangesModel

    private val repository = dakokuViewModel.repository
    private var data: Int = 0


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

        //buttons
        val shukkinBtn = view?.findViewById<Button>(R.id.shukkinBtn)
        val taikinBtn = view?.findViewById<Button>(R.id.taikinBtn)

        val context = this.requireContext()

        //get username
        model = ViewModelProviders.of(requireActivity()).get(MainViewModel::class.java)
        model.mutableLiveData.observe(requireActivity(),object : Observer,
            androidx.lifecycle.Observer<String> {

            override fun onChanged(o: String?) {
                val selectedName = o!!.toString()
                empname = selectedName
                data = repository.getDataRowCount(date,empname)

                //setup button color
                if (data == 0) {
                    taikinBtn?.setBackgroundTintList(ContextCompat.getColorStateList(context,R.color.colorAccentLight));
                    Log.d("data check","first dakoku today")
                }else{
                    val currentData = repository.getDakokuByDateName(date,empname)
                    setButtonColor(currentData,shukkinBtn!!,taikinBtn!!,context)
                }
            }

            override fun update(o: Observable?, arg: Any?) {
            }

        })

        //firebase database
        database = Firebase.database.reference

        //view model to stock changes
        changesModel = ChangesModel()



        //onClick
        shukkinBtn?.setOnClickListener() {
            data = repository.getDataRowCount(date,empname)

            if (data == 0) {

                val newdakoku = Dakoku(0, empname, date, marumeTime, null, 0, 0.0,"")
                Log.v("TAG", "data to insert(Shukkin) : $newdakoku")
                dakokuViewModel.insert(newdakoku)

                Toast.makeText(requireContext(), "出勤しました", Toast.LENGTH_SHORT)
                    .show()

                setShukkinTimeText(newdakoku,shukkinBtn)
                setButtonColor(newdakoku,shukkinBtn!!,taikinBtn!!,context)


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
                        setShukkinTimeText(dakoku,shukkinBtn)
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
                setTaikinTimeText(newdakoku,taikinBtn)
                setButtonColor(newdakoku,shukkinBtn!!,taikinBtn,context)
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
                setTaikinTimeText(dakoku,taikinBtn)
                setButtonColor(dakoku,shukkinBtn!!,taikinBtn,context)
                showAlertDialog(date,empname)

            }

        }


    }

    private fun setShukkinTimeText(currentData:Dakoku?,button: Button){
        if (currentData?.shukkin == null){
            return
        }else {
            val time = currentData?.shukkin.toString().padStart(4, '0')
            val dakokuTime = StringBuilder().append(time).insert(2, ":")
            button?.text = "出勤\n" + dakokuTime
            button?.textSize = 20F
        }
    }

    private fun setTaikinTimeText(currentData:Dakoku?,button: Button){
        if (currentData?.taikin == null){
            return
        }else{
            val time = currentData?.taikin.toString().padStart(4, '0')
            val dakokuTime = StringBuilder().append(time).insert(2, ":")
            button?.text = "退勤\n" + dakokuTime
            button?.textSize = 20F
        }
    }


    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    private fun setButtonColor(currentData: Dakoku?, shukkinBtn: Button, taikinBtn: Button, context:Context){
        if (currentData?.shukkin == 0) {
            taikinBtn?.setBackgroundTintList(
                ContextCompat.getColorStateList(
                    context, R.color.colorAccentLight
                )
            )
        }else{
            shukkinBtn?.setBackgroundTintList(
                ContextCompat.getColorStateList(
                    context,
                    R.color.colorAccentLight
                )
            )
            setShukkinTimeText(currentData,shukkinBtn)
        }

        if (currentData?.taikin == 0) {
            shukkinBtn?.setBackgroundTintList(
                ContextCompat.getColorStateList(
                    context,
                    R.color.colorAccentLight
                )
            )
        }else{
            taikinBtn?.setBackgroundTintList(
                ContextCompat.getColorStateList(
                    context,
                    R.color.colorAccentLight
                )
            )
            setTaikinTimeText(currentData,taikinBtn)
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

        val application = LestTimeApplication()
        val list: MutableList<Int> = application.lestTimeDao.getMutableList()
        val arrayList:ArrayList<Int> = ArrayList(list)
        val stringArray:ArrayList<String> = arrayList.map { it.toString() }.toTypedArray().toCollection(ArrayList())
        stringArray.add(0,"休憩なし")

        val array: Array<CharSequence> = stringArray.toArray(arrayOfNulls(0))

        alertDialog.setSingleChoiceItems(array,0, { _, which ->
            when (which) {
                0 -> dakokuViewModel.updateLest(0,date,empname)
                else -> dakokuViewModel.updateLest("${array[which]}".toInt(),date,empname)
            }
        })

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