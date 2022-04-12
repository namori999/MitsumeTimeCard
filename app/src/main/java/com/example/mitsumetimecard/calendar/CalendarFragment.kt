package com.example.mitsumetimecard.calendar

import android.annotation.SuppressLint
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import android.widget.Toast.LENGTH_SHORT
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.core.os.HandlerCompat.postDelayed
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResultListener
import androidx.lifecycle.ViewModelProviders
import com.example.mitsumetimecard.R
import com.example.mitsumetimecard.dakoku.Dakoku
import com.example.mitsumetimecard.dakoku.DakokuApplication
import com.example.mitsumetimecard.kintaitable.UpdateDialogFragment
import com.example.mitsumetimecard.ui.main.MainViewModel
import com.kizitonwose.calendarview.model.CalendarDay
import com.kizitonwose.calendarview.model.CalendarMonth
import com.kizitonwose.calendarview.model.DayOwner
import com.kizitonwose.calendarview.ui.DayBinder
import com.kizitonwose.calendarview.ui.MonthHeaderFooterBinder
import com.kizitonwose.calendarview.ui.ViewContainer
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.util.*


// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [CalenderFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
@Suppress("UNREACHABLE_CODE")
open class CalenderFragment : Fragment() {
    private var param1: String? = null
    private var param2: String? = null

    private var selectedDate: LocalDate? = null

    var application = DakokuApplication()
    private lateinit var model: MainViewModel
    private var userName: String = ""
    private lateinit var empname: String

    var data: Dakoku? = null
    val repository = application.repository

    private lateinit var dateTxt :TextView
    private lateinit var shukkinTxt :TextView
    private lateinit var taikinTxt :TextView
    private lateinit var kyukeiTxt :TextView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_calender, container, false)
    }

    @SuppressLint("UseRequireInsteadOfGet")
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        val context = this.requireContext()

        setFragmentResultListener("input"){ _, data ->
            val selectedDate = data.getString("toCalender","")
            clearCurrentDakoku()
            setCurrentDakoku(selectedDate)
        }

        dateTxt = view?.findViewById<TextView>(R.id.dateTxt)
        shukkinTxt = view?.findViewById<TextView>(R.id.shukkinTxt)
        taikinTxt = view?.findViewById<TextView>(R.id.taiknTxt)
        kyukeiTxt = view?.findViewById<TextView>(R.id.kyukeiTxt)

        //get username
        model = ViewModelProviders.of(requireActivity()).get(MainViewModel::class.java)
        model.mutableLiveData.observe(requireActivity(), object : Observer,
            androidx.lifecycle.Observer<String> {

            override fun onChanged(o: String?) {
                val selectedName = o!!.toString()
                userName = selectedName
            }

            override fun update(o: Observable?, arg: Any?) {
            }

        })

        //get dakoku today
        val date1: LocalDate = LocalDate.now()
        selectedDate = date1

        data = application.repository.getDakokuByDateName(date1.toString(), userName)
        setCurrentDakoku(selectedDate!!.toString())
        Log.v("dakoku today", "${data}")

        //go update fragment
        val goEdit: ImageView = view?.findViewById(R.id.editBtn)
        goEdit.setOnClickListener() {
            val shukkinTime = data?.shukkin
            val taikinTime = data?.taikin
            val lestTime = data?.lest
            val name = userName
            val date = selectedDate

            if (date == null){
                Toast.makeText(this.requireContext(),"日付を選択してください",LENGTH_SHORT)
            } else {
                UpdateDialogFragment.newInstance(
                    "${shukkinTime}", "${taikinTime}", "${lestTime}", "${name}", "${date}"
                ).show(fragmentManager!!, UpdateDialogFragment.TAG)
            }
        }

        //calender view
        val calendarView: com.kizitonwose.calendarview.CalendarView =
            view?.findViewById(R.id.calendarView)

        val currentMonth = YearMonth.now()
        val firstMonth = currentMonth.minusMonths(10)
        val lastMonth = currentMonth.plusMonths(10)
        val daysOfWeek = arrayOf(
            DayOfWeek.SUNDAY,
            DayOfWeek.MONDAY,
            DayOfWeek.TUESDAY,
            DayOfWeek.WEDNESDAY,
            DayOfWeek.THURSDAY,
            DayOfWeek.FRIDAY,
            DayOfWeek.SATURDAY
        )
        calendarView.setup(firstMonth, lastMonth, daysOfWeek.first())
        calendarView.scrollToMonth(currentMonth)

        @SuppressLint("ResourceAsColor")
        class DayViewContainer(view: View) : ViewContainer(view) {
            val textView = view.findViewById<TextView>(R.id.calendarDayText)
            lateinit var day: CalendarDay

            init {
                view.setOnClickListener {
                    if (day.owner == DayOwner.THIS_MONTH) {
                        // Keep a reference to any previous selection
                        // in case we overwrite it and need to reload it.
                        val currentSelection = selectedDate
                        if (currentSelection == day.date) {
                            // If the user clicks the same date, clear selection.
                            selectedDate = null
                            calendarView.notifyDateChanged(currentSelection)

                        } else {
                            selectedDate = day.date

                            Log.v("selected dakoku", "$data")

                            calendarView.notifyDateChanged(day.date)
                            if (currentSelection != null) {
                                calendarView.notifyDateChanged(currentSelection)
                            }

                            setCurrentDakoku(selectedDate!!.toString())

                            calendarView.scrollToDay(day)
                        }
                    }

                    Log.v("calender tapped", "{$day}")
                }
            }
        }

        calendarView.dayBinder = object : DayBinder<DayViewContainer> {
            // Called only when a new container is needed.
            override fun create(view: View) = DayViewContainer(view)

            @SuppressLint("ResourceAsColor")
            @RequiresApi(Build.VERSION_CODES.O)
            override fun bind(container: DayViewContainer, day: CalendarDay) {
                container.day = day
                val textView = container.textView
                textView.text = day.date.dayOfMonth.toString()

                if (day.owner == DayOwner.THIS_MONTH) {
                    // Show the month dates. Remember that views are recycled!
                    textView.visibility = View.VISIBLE
                    if (day.date == selectedDate) {
                        textView.setTextColor(Color.WHITE)
                        textView.setBackgroundResource(R.drawable.maru)
                        textView.setBackgroundTintList(
                            ContextCompat.getColorStateList(
                                context, R.color.colorAccent
                            )
                        )
                    }
                } else {
                    // Hide in and out dates
                    textView.visibility = View.INVISIBLE
                }
            }

        }

        class MonthViewContainer(view: View) : ViewContainer(view) {
            val textView = view.findViewById<TextView>(R.id.headerTextView)
            val legendLayout = R.layout.calendar_day_legend
            val txt1 = view.findViewById<TextView>(R.id.legendText1)
            val txt2 = view.findViewById<TextView>(R.id.legendText2)
            val txt3 = view.findViewById<TextView>(R.id.legendText3)
            val txt4 = view.findViewById<TextView>(R.id.legendText4)
            val txt5 = view.findViewById<TextView>(R.id.legendText5)
            val txt6 = view.findViewById<TextView>(R.id.legendText6)
            val txt7 = view.findViewById<TextView>(R.id.legendText7)
        }

        calendarView.monthHeaderBinder = object : MonthHeaderFooterBinder<MonthViewContainer> {
            override fun create(view: View) = MonthViewContainer(view)

            @SuppressLint("SetTextI18n")
            override fun bind(container: MonthViewContainer, month: CalendarMonth) {
                container.textView.text =
                    "${month.yearMonth.month.name.toLowerCase().capitalize()} ${month.year}"

                // Setup each header day １text if we have not done that already.
                container.txt1.text = "日"
                container.txt2.text = "月"
                container.txt3.text = "火"
                container.txt4.text = "水"
                container.txt5.text = "木"
                container.txt6.text = "金"
                container.txt7.text = "土"
            }

        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    open fun setCurrentDakoku(day: String) {
        val empname = userName
        data = repository.getDakokuByDateName(selectedDate.toString(), empname)

        val dateTxt = view?.findViewById<TextView>(R.id.dateTxt)
        val shukkinTxt = view?.findViewById<TextView>(R.id.shukkinTxt)
        val taikinTxt = view?.findViewById<TextView>(R.id.taiknTxt)
        val kyukeiTxt = view?.findViewById<TextView>(R.id.kyukeiTxt)


        if (data == null) {
            dateTxt?.setText("${day}")

            shukkinTxt?.setText("出勤 :　")
            taikinTxt?.setText("退勤 :　")
            kyukeiTxt?.setText("休憩 :　")

        } else {
            dateTxt?.setText("${day}")

            val shukkin = data?.shukkin
            if (shukkin == 0) {
                shukkinTxt?.setText("出勤 :　" + "")
            } else if (shukkin.toString() == "null"){
                shukkinTxt?.setText("出勤 : " + "")
            } else {
                val time = shukkin.toString().padStart(4, '0')
                val shukintime = StringBuilder().append(time).insert(2, ":")
                shukkinTxt?.setText("出勤 :　" + "$shukintime")
            }

            val taikin = data?.taikin
            if (taikin == 0) {
                taikinTxt?.setText("退勤 :　" + "")
            } else if (taikin.toString() == "null"){
                taikinTxt?.setText("退勤 : " + "")
            } else {
                val time = taikin.toString().padStart(4, '0')
                val taikintime = StringBuilder().append(time).insert(2, ":")
                taikinTxt?.setText("退勤 :　" + "$taikintime")
            }

            val kyukei = data?.lest
            if (kyukei == 0) {
                kyukeiTxt?.setText("休憩 :　")
            } else {
                kyukeiTxt?.setText("休憩 :　" + "$kyukei" + "分")
            }

        }
    }

    fun clearCurrentDakoku(){
        view?.findViewById<TextView>(R.id.shukkinTxt)?.text =""
        view?.findViewById<TextView>(R.id.taiknTxt)?.text =""
        view?.findViewById<TextView>(R.id.kyukeiTxt)?.text =""
    }

    companion object {
        fun newInstance(param1: String, param2: String) =
            CalenderFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }

    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onPause() {
        super.onPause()
        Log.v("calenderFragment", "paused")

    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onResume() {
        super.onResume()

        Log.v("calenderFragment", "resumed")

    }


}