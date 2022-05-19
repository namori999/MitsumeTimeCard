package com.example.mitsumetimecard.updatedialog

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.Dialog
import android.os.Bundle
import android.widget.DatePicker
import android.widget.EditText
import androidx.fragment.app.DialogFragment
import androidx.room.Update
import com.example.mitsumetimecard.MainActivity
import com.example.mitsumetimecard.R
import java.util.*

class DatePickerFragment : DialogFragment(), DatePickerDialog.OnDateSetListener {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val c = Calendar.getInstance()
        val year = c.get(Calendar.YEAR)
        val month = c.get(Calendar.MONTH)
        val day = c.get(Calendar.DAY_OF_MONTH)

        var dp = DatePickerDialog(
            requireContext(),
            AlertDialog.THEME_HOLO_LIGHT,
            this,
            year,
            month,
            day
        )

        dp.datePicker.calendarViewShown = false
        return dp
    }

    override fun onDateSet(view: DatePicker?, year: Int, month: Int, dayOfMonth: Int) {
        UpdateDialogFragment.editD.setText(String.format("%d-%02d-%02d", year, month+1, dayOfMonth))
    }

}