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
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.lifecycle.asLiveData
import com.example.mitsumetimecard.MainActivity
import com.example.mitsumetimecard.R
import com.example.mitsumetimecard.calendar.CalenderFragment
import com.example.mitsumetimecard.dakoku.Dakoku
import com.example.mitsumetimecard.dakoku.DakokuApplication
import com.example.mitsumetimecard.dakoku.DakokuViewModel
import com.example.mitsumetimecard.setting.LestTimeApplication
import com.example.mitsumetimecard.updatedialog.TimePickerFragment
import com.example.mitsumetimecard.updatedialog.UpdateDialogFragment
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import java.lang.Math.round
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*


class MainFragment : Fragment() {


    var application = DakokuApplication()
    var dakokuViewModel = DakokuViewModel(application.repository)
    private var empname:String = ""
    private var selectedTime:Int =0

    private lateinit var database: DatabaseReference
    private lateinit var model: MainViewModel
    private val repository = dakokuViewModel.repository
    private var data: Int = 0
    private lateinit var uncompletedDakokuList:List<Dakoku>

    private lateinit var shukkinBtn: Button
    private lateinit var taikinBtn: Button

    var myAlertDialog: AlertDialog? = null

    companion object {

        var pickedTaikin:Int = 0
        fun calcurateJitsudou(shukkinTime:Int?, taikinTime:Int?) :Double{
            if (shukkinTime == null || shukkinTime == 0){
                Log.d("calcurate jitsudo","no shukkin record")
            }else if (taikinTime == null || taikinTime == 0) {
                Log.d("calcurate jitsudo","no taikin record")
            }else{
                val startH = (shukkinTime / 100) * 60
                val startS: Int = (shukkinTime % 100)
                val endH: Int = (taikinTime / 100) * 60
                val endS: Int = (taikinTime % 100)

                if (shukkinTime > taikinTime && taikinTime/100 < 7) {
                    val end30H = (taikinTime /100 + 24) *60
                    val start: Int = (startH + startS) //minutes
                    val end: Int = (end30H + endS) //minutes
                    val sa: Double = (end - start) / 60.0
                    val zitsudo: Double = (Math.round(sa * 10.0) / 10.0) //to hour
                    return zitsudo
                }else{
                    val start: Int = (startH + startS) //minutes
                    val end: Int = (endH + endS) //minutes
                    val sa: Double = (end - start) / 60.0
                    val zitsudo: Double = (Math.round(sa * 10.0) / 10.0) //to hour
                    return zitsudo
                }
            }

            return 0.0
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.main_fragment, container, false)
    }

    @SuppressLint("SetTextI18n", "ResourceAsColor", "UseCompatLoadingForColorStateLists")
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        //get date and time
        val textDate = view?.findViewById<TextView>(R.id.textDate)
        val onlyDate: LocalDate = LocalDate.now()
        val dateTime: LocalDateTime = LocalDateTime.now()
        val dtformat: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd E")
        val fdate2: String = dtformat.format(dateTime)
        textDate?.setText("$fdate2")

        CalenderFragment.selectedDate = onlyDate

        //data to insert
        val date = onlyDate.toString()

        //buttons
        shukkinBtn = view?.findViewById<Button>(R.id.shukkinBtn)!!
        taikinBtn = view?.findViewById<Button>(R.id.taikinBtn)!!
        val context = this.requireContext()

        //get username
        model = ViewModelProviders.of(requireActivity()).get(MainViewModel::class.java)
        model.mutableLiveData.observe(requireActivity(),object : Observer,
            androidx.lifecycle.Observer<String> {

            override fun onChanged(o: String?) {
                val selectedName = o!!.toString()
                empname = selectedName
                data = repository.getDataRowCount(date,empname)
                initiallizeButton()

                val dakokuByName = application.repository.getDakokuByName(empname).asLiveData()

                dakokuByName.observe(viewLifecycleOwner, object : Observer,
                    androidx.lifecycle.Observer<List<Dakoku>> {
                    @RequiresApi(Build.VERSION_CODES.O)
                    override fun update(o: Observable?, arg: Any?) {
                        initiallizeButton()
                    }

                    @RequiresApi(Build.VERSION_CODES.O)
                    override fun onChanged(t: List<Dakoku>?) {
                        initiallizeButton()
                    }
                })
            }
            override fun update(o: Observable?, arg: Any?) {
            }

        })

        //firebase database
        database = Firebase.database.reference

        //onClick
        view.setOnClickListener(){
            MainActivity().removeTimer()
            MainActivity().setViewTimer()
            Log.d("MainFragment","view clicked")
        }

        shukkinBtn.setOnClickListener() {
            data = repository.getDataRowCount(date,empname)
            val marumeTime = getMarumeTime()

            if (data == 0) {
                val newdakoku = Dakoku(0, empname, date, marumeTime, 0, 0, 0.0,"")
                Log.v("TAG", "data to insert(Shukkin) : $newdakoku")
                dakokuViewModel.insert(newdakoku)
                dakokuViewModel.insertOriginalShukkin(marumeTime, date, empname)
                Toast.makeText(requireContext(), "出勤しました", Toast.LENGTH_SHORT)
                    .show()
                updateButtonView(newdakoku,shukkinBtn,taikinBtn!!,context)
            } else {
                Log.v("TAG", "data is already here")
                AlertDialog.Builder(this.requireActivity()) // FragmentではActivityを取得して生成
                    .setTitle("すでに出勤しています")
                    .setMessage("出勤時間を更新しますか？")
                    .setPositiveButton("更新する", { dialog, which ->
                        updateDakoku("shukkin")
                    })
                    .setNegativeButton("キャンセル", { dialog, which ->
                        MainActivity().setViewTimer()
                        Log.v("TAG", "shukkin update is canseled")
                    })
                    .show()
            }
        }

        taikinBtn.setOnClickListener() {
            MainActivity().removeTimer()

            uncompletedDakokuList = repository.getDakokuOnlyShukkin(empname)
            if (uncompletedDakokuList.isNullOrEmpty()) {
                data = repository.getDataRowCount(date, empname)
                val marumeTime = getMarumeTime()

                if (data == 0) {
                    val newdakoku =
                        Dakoku(0, empname, date, 0, marumeTime.toInt(), selectedTime, 0.0, "")
                    Log.v("TAG", "data to insert(Taikin) : $newdakoku")
                    dakokuViewModel.insert(newdakoku)
                    dakokuViewModel.insertOriginalTaikin(marumeTime,date,empname)
                    updateButtonView(newdakoku, shukkinBtn!!, taikinBtn, context)
                    showLestAlartDialog(date, empname)

                    Toast.makeText(requireContext(), "出勤時間を追加してください", Toast.LENGTH_SHORT).show()
                    UpdateDialogFragment.newInstance(
                        "${newdakoku.shukkin}",
                        "${newdakoku?.taikin}",
                        "${newdakoku?.lest}",
                        empname,
                        date
                    ).show(requireFragmentManager(), UpdateDialogFragment.TAG)
                }else{
                    updateDakoku("taikin")
                }

            } else {
                val lastShukkinDate = uncompletedDakokuList.last().date
                val today = LocalDate.now().toString()
                if (lastShukkinDate == today){
                    updateDakoku("taikin")
                }else{
                    showTaikinAlartDialog(lastShukkinDate!!, empname)
                }
            }
        }
    }


    @RequiresApi(Build.VERSION_CODES.O)
    private fun getMarumeTime():Int{
        val hour = LocalDateTime.now().hour
        val minute = LocalDateTime.now().minute
        val marume:Int = (round(minute * 1.0 / 6) * 6).toInt()
        val marumeTime:Int
        val checkMarume = (marume % 100)
        if (56 < checkMarume ){
            marumeTime = (hour + 1) * 100
        }else{
            marumeTime = hour * 100 + marume
        }

        return marumeTime
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun updateDakoku(key:String){
        val marumeTime = getMarumeTime()
        val date = LocalDate.now().toString()
        val dakoku:Dakoku? = repository.getDakokuByDateName(date,empname)
        val jitsudo:Double

        when (key){
            "shukkin" -> {
                dakokuViewModel.updateShukkin(marumeTime, date,empname)
                jitsudo = calcurateJitsudou(dakoku?.shukkin, dakoku?.taikin)
                dakokuViewModel.updateJitsudo(jitsudo, date, empname)
                setShukkinTimeText()
            }
            "taikin" -> {
                dakokuViewModel.updateTaikin(marumeTime, date, empname)
                jitsudo = calcurateJitsudou(dakoku?.shukkin, marumeTime)
                dakokuViewModel.updateJitsudo(jitsudo, date, empname)
                setTaikinTimeText()
                showLestAlartDialog(date, empname)

                if (dakoku?.taikin == null || dakoku.taikin == 0) {
                    //退勤がはじめて
                    dakokuViewModel.insertOriginalTaikin(marumeTime,date,empname)
                }
            }
            else -> return
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun initiallizeButton(){
        uncompletedDakokuList = repository.getDakokuOnlyShukkin(empname)
        val today = LocalDate.now()
        val yesterDay = today.minusDays(1)
        val context = this.requireContext()
        val currentData = repository.getDakokuByDateName(today.toString(), empname)

        if(! uncompletedDakokuList.isNullOrEmpty()) {
            val dakokuYesterday = uncompletedDakokuList.last()
            if (dakokuYesterday.date == yesterDay.toString()){
                updateButtonView(dakokuYesterday, shukkinBtn, taikinBtn, context)
            }else{
                updateButtonView(currentData, shukkinBtn, taikinBtn, context)
            }

        } else if (data == 0) {
            taikinBtn.setBackgroundTintList(ContextCompat.getColorStateList(context, R.color.colorAccentLight))
        }else {
            updateButtonView(currentData, shukkinBtn, taikinBtn, context)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun updateButtonView(currentData: Dakoku?, shukkinBtn: Button, taikinBtn: Button, context:Context){
        //shukkin
        if (currentData?.shukkin == 0 || currentData == null) {
            taikinBtn.setBackgroundTintList(ContextCompat.getColorStateList(context, R.color.colorAccentLight))
        }else{
            shukkinBtn.setBackgroundTintList(ContextCompat.getColorStateList(context, R.color.colorAccentLight))
            setShukkinTimeText()
        }
        //taikin
        if (currentData?.taikin == 0 || currentData?.taikin == null) {
            taikinBtn.setBackgroundTintList(ContextCompat.getColorStateList(context, R.color.colorAccent))
        }else{
            taikinBtn.setBackgroundTintList(ContextCompat.getColorStateList(context, R.color.colorAccentLight))
            setTaikinTimeText()
        }
    }

    @SuppressLint("SetTextI18n")
    @RequiresApi(Build.VERSION_CODES.O)
    private fun setShukkinTimeText(){
        val today = LocalDate.now().toString()
        uncompletedDakokuList = repository.getDakokuOnlyShukkin(empname)

        if(! uncompletedDakokuList.isNullOrEmpty()) {
            val lastShukkin = uncompletedDakokuList.last()
            var time = lastShukkin.shukkin.toString()
            if (time.length == 3) {
                time = "0$time"
            }
            time = time.padStart(4, '0')
            val lastShukkinTime = StringBuilder().append(time).insert(2, ":")
            val lastShukkinDate = lastShukkin.date

            if (lastShukkinDate == today){
                shukkinBtn.text = "出勤\n" + "${lastShukkinTime}"
                shukkinBtn . textSize = 20F
            }else{
                shukkinBtn.text = "出勤\n" + "${lastShukkin.date}\n" + "${lastShukkinTime}"
                shukkinBtn . textSize = 20F
            }
        }

        val dakokuToday = repository.getDakokuByDateName(today,empname)
        var time = dakokuToday?.shukkin.toString()
            if (time.length == 3){
                time = "0$time"
            }
        val shukkinToday = StringBuilder().append(time).insert(2, ":")
        if (dakokuToday !== null){
            shukkinBtn.text = "出勤\n" + "${shukkinToday}"
            shukkinBtn . textSize = 20F
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    @SuppressLint("SetTextI18n")
    private fun setTaikinTimeText(){
        val today = LocalDate.now().toString()
        val currentData = repository.getDakokuByDateName(today,empname)
        val time = currentData?.taikin.toString().padStart(4, '0')
        val dakokuTime = StringBuilder().append(time).insert(2, ":")
        taikinBtn.text = "退勤\n" + dakokuTime
        taikinBtn.textSize = 20F
    }

    @SuppressLint("UseCompatLoadingForColorStateLists")
    @RequiresApi(Build.VERSION_CODES.O)
    private fun showTaikinAlartDialog(lastShukkinDate:String, empname:String){
        val dakoku:Dakoku? = repository.getDakokuByDateName(lastShukkinDate,empname)
        val alertDialog = AlertDialog.Builder(requireContext())
        alertDialog.setTitle("$lastShukkinDate の退勤を記録します").setMessage("現在時刻で退勤しますか？")

        alertDialog.setPositiveButton(
            "現在時刻で退勤"
        ){ dialog, which ->
            dakokuViewModel.updateTaikin(getMarumeTime(),lastShukkinDate,empname)
            dakokuViewModel.insertOriginalTaikin(getMarumeTime(),lastShukkinDate,empname)
            val jitsudo = calcurateJitsudou(dakoku?.shukkin , getMarumeTime())
            dakokuViewModel.updateJitsudo(jitsudo,lastShukkinDate,empname)
            updateButtonView(dakoku,shukkinBtn,taikinBtn,this.requireContext())
            showLestAlartDialog(lastShukkinDate,empname)
        }
        alertDialog.setNegativeButton(
            "時間を入力する"
        ) { dialog, which ->
            Toast.makeText(requireContext(), "$lastShukkinDate　の退勤時間を追加してください", Toast.LENGTH_SHORT).show()
            UpdateDialogFragment.newInstance(
                "${dakoku?.shukkin}", "${dakoku?.taikin}", "${dakoku?.lest}", empname, lastShukkinDate
            ).show(requireFragmentManager(), UpdateDialogFragment.TAG)
        }
            //.setIcon(ContextCompat.getDrawable(this.requireContext(),R.drawable.ic_baseline_edit_24))

        val alert = alertDialog.create()
        alert.setCanceledOnTouchOutside(true)
        alert.show()
    }

    @SuppressLint("UseCompatLoadingForColorStateLists")
    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    private fun showLestAlartDialog(date:String, empname:String){
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
            "決定"
        ) { dialog, which ->
            Toast.makeText(requireContext(), "退勤しました", Toast.LENGTH_SHORT)
                .show()
            val taikinBtn: Button? = view?.findViewById(R.id.taikinBtn)
            taikinBtn?.setBackgroundTintList(
                this.resources.getColorStateList(R.color.colorAccentLight)
            );
            hideSystemUI()
            MainActivity().setViewTimer()
        }

        val alert = alertDialog.create()
        alert.setCanceledOnTouchOutside(true)
        alert.show()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun completeDakoku(){
        uncompletedDakokuList = repository.getDakokuOnlyShukkin(empname)
        val yesterDay = LocalDate.now().minusDays(1).toString()
        val today = LocalDate.now().toString()

        if(! uncompletedDakokuList.isNullOrEmpty()) {
            val lastShukkinDakoku = uncompletedDakokuList.last()

            if (lastShukkinDakoku.date != yesterDay && lastShukkinDakoku.date != today) {
                TimePickerFragment.myTimePicker.showTaikinTimePicker(this.requireContext(),lastShukkinDakoku.date!!)
                dakokuViewModel.updateTaikin(pickedTaikin, lastShukkinDakoku.date!!, empname)
                val jitsudo = calcurateJitsudou(lastShukkinDakoku.shukkin, pickedTaikin)
                dakokuViewModel.updateJitsudo(jitsudo, lastShukkinDakoku.date!!, empname)
                hideSystemUI()
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onStart() {
        super.onStart()
        completeDakoku()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onPause() {
        super.onPause()
        Log.d("mainfragment","onPause")
    }

    override fun onResume() {
        super.onResume()
        hideSystemUI()
        Log.d("mainfragment","onResume")
    }

    private fun hideSystemUI() {
        activity?.window?.decorView?.apply {
            systemUiVisibility = ( View.SYSTEM_UI_FLAG_LOW_PROFILE
                    or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_FULLSCREEN
                    or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)
        }
    }
}