package com.example.mitsumetimecard.updatedialog

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.Dialog
import android.app.TimePickerDialog
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.widget.DatePicker
import android.widget.EditText
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.room.Update
import com.example.mitsumetimecard.MainActivity
import com.example.mitsumetimecard.R
import com.example.mitsumetimecard.ui.main.MainFragment
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

class TimePickerFragment : DialogFragment() {
    @RequiresApi(Build.VERSION_CODES.O)
    object myTimePicker {

        fun showTimePicker (eText : EditText) {
            var hourOfDay:Int
            var minutes:Int

            var ldt = eText.text.toString()
            if (ldt.length == 3) {
                ldt = "0$ldt"
            }

            if (ldt == "0" || ldt =="") {
                val dateTIme = LocalDateTime.now()
                hourOfDay = dateTIme.hour
                minutes = dateTIme.minute
            }else{
                hourOfDay = ldt.substring(0,2).toIntOrNull()!!
                minutes = ldt.substring(2,4).toIntOrNull()!!
            }

            // ドラム式TimePicker表示
            val picker = TimePickerDialog(
                eText.rootView.context,
                AlertDialog.THEME_HOLO_LIGHT,
                // ダイアログでOKをクリックされたときの処理 時刻入力フィールドへ値を設定
                { _, getHour, getMinutes ->
                    val time = String.format("%02d%02d", getHour, getMinutes)
                    if (time.substring(0,2) =="00"){
                        eText.setText(String.format("%02d%02d", 24, getMinutes))
                    } else {
                        eText.setText(time)
                    }
                },
                // TimePickerが初期表示する時刻
                hourOfDay,
                minutes,
                true
            )
            picker.show()

        }

        fun showTaikinTimePicker(context:Context, lastShukkinDate:String){
            val dateTIme = LocalDateTime.now()
            var hourOfDay = dateTIme.hour
            var minutes = dateTIme.minute

            // ドラム式TimePicker表示
            val picker = TimePickerDialog(
                context,
                AlertDialog.THEME_HOLO_LIGHT,

                { _, getHour, getMinutes ->
                    val time = String.format("%02d%02d", getHour, getMinutes)
                    if (time.substring(0,2) =="00"){
                        MainFragment.pickedTaikin = String.format("%02d%02d", 24, getMinutes).toIntOrNull()!!
                    } else {
                        MainFragment.pickedTaikin = time.toIntOrNull()!!
                    }
                },
                // TimePickerが初期表示する時刻
                hourOfDay,
                minutes,
                true
            )
            picker.setTitle("$lastShukkinDate の退勤")
            picker.show()

            Toast.makeText(
                context, "${lastShukkinDate} の退勤をお忘れのようです。\n" +
                        "退勤した時間を選んでください。", Toast.LENGTH_LONG
            ).show()
        }

    }
}