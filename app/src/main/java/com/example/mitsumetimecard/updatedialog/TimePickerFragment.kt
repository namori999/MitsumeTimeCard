package com.example.mitsumetimecard.updatedialog

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.Dialog
import android.app.TimePickerDialog
import android.os.Build
import android.os.Bundle
import android.widget.DatePicker
import android.widget.EditText
import androidx.annotation.RequiresApi
import androidx.fragment.app.DialogFragment
import androidx.room.Update
import com.example.mitsumetimecard.MainActivity
import com.example.mitsumetimecard.R
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

class TimePickerFragment : DialogFragment() {
    @RequiresApi(Build.VERSION_CODES.O)
    object myTimePicker {

        fun showTimePicker (eText : EditText) {
            var hourOfDay:Int
            var minutes:Int

            val ldt = eText.text.toString()
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
                { _, getHour, getMinutes
                    -> eText.setText(String.format("%02d%02d",
                    getHour,
                    getMinutes))
                },
                // TimePickerが初期表示する時刻
                hourOfDay,
                minutes,
                true
            )
            picker.show()

        }


    }

}