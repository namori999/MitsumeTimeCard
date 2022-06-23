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
import androidx.lifecycle.asLiveData
import com.example.mitsumetimecard.R
import com.example.mitsumetimecard.dakoku.Dakoku
import com.example.mitsumetimecard.dakoku.DakokuApplication
import com.example.mitsumetimecard.ui.main.MainViewModel
import com.example.mitsumetimecard.updatedialog.UpdateDialogFragment
import com.kizitonwose.calendarview.model.CalendarDay
import com.kizitonwose.calendarview.model.CalendarMonth
import com.kizitonwose.calendarview.model.DayOwner
import com.kizitonwose.calendarview.ui.DayBinder
import com.kizitonwose.calendarview.ui.MonthHeaderFooterBinder
import com.kizitonwose.calendarview.ui.ViewContainer
import com.kizitonwose.calendarview.utils.yearMonth
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
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

    var application = DakokuApplication()
    private lateinit var model: MainViewModel
    private var empName: String = ""

    var data: Dakoku? = null
    val repository = application.repository

    private lateinit var dateTxt :TextView
    private lateinit var shukkinTxt :TextView
    private lateinit var taikinTxt :TextView
    private lateinit var kyukeiTxt :TextView

    companion object {
        var selectedDate: LocalDate? = null
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_calender, container, false)
    }

    @SuppressLint("UseRequireInsteadOfGet", "SetTextI18n")
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        val context = this.requireContext()

        //listener to get selectedDate from other fragment
        setFragmentResultListener("input"){ _, data ->
            val selectedDate = data.getString("toCalender","")
            clearCurrentDakoku()
            setCurrentDakoku(selectedDate)
        }

        //implement textView

        dateTxt = view.findViewById(R.id.dateTxt)
        shukkinTxt = view.findViewById(R.id.shukkinTxt)
        taikinTxt = view.findViewById(R.id.taiknTxt)
        kyukeiTxt = view.findViewById(R.id.kyukeiTxt)

        //get username
        model = ViewModelProviders.of(requireActivity()).get(MainViewModel::class.java)
        model.mutableLiveData.observe(requireActivity(), object : Observer,
            androidx.lifecycle.Observer<String> {

            override fun onChanged(o: String?) {
                val selectedName = o!!.toString()
                empName = selectedName
                val dakokuByName = application.repository.getDakokuByName(empName).asLiveData()

                dakokuByName.observe(viewLifecycleOwner, object : Observer,
                    androidx.lifecycle.Observer<List<Dakoku>> {
                    @RequiresApi(Build.VERSION_CODES.O)
                    override fun update(o: Observable?, arg: Any?) {
                        setCurrentDakoku(selectedDate.toString())
                    }
                    @RequiresApi(Build.VERSION_CODES.O)
                    override fun onChanged(t: List<Dakoku>?) {
                        setCurrentDakoku(selectedDate.toString())
                    }
                })
            }

            override fun update(o: Observable?, arg: Any?) {
            }
        })

        setTodaysDakoku()

        //edit button
        val goEdit: ImageView = view?.findViewById(R.id.editBtn)
        goEdit.setOnClickListener() {
            val shukkinTime = data?.shukkin
            val taikinTime = data?.taikin
            val lestTime = data?.rest
            val name = empName
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
                        val currentSelection = selectedDate
                        selectedDate = day.date
                        calendarView.notifyDateChanged(day.date)
                        if (currentSelection != null) {
                            calendarView.notifyDateChanged(currentSelection)
                        }
                        setCurrentDakoku(selectedDate!!.toString())
                    }
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
                    }else{
                        textView.setTextColor(Color.BLACK)
                        textView.background = null
                    }
                } else {
                    // Hide in and out dates
                    textView.visibility = View.INVISIBLE
                }
            }

        }

        val yearText:TextView = view.findViewById(R.id.yearText)
        val monthText :TextView = view.findViewById(R.id.monthText)
        val monthTitleFormatter = DateTimeFormatter.ofPattern("MM")

        calendarView.monthScrollListener = {
            if (calendarView.maxRowCount == 6) {
                yearText.text = it.yearMonth.year.toString()  + "-"
                monthText.text = monthTitleFormatter.format(it.yearMonth)
            } else {
                val firstDate = it.weekDays.first().first().date
                val lastDate = it.weekDays.last().last().date
                if (firstDate.yearMonth == lastDate.yearMonth) {
                    yearText.text = firstDate.yearMonth.year.toString()  + "-"
                    monthText.text = monthTitleFormatter.format(firstDate)
                } else {
                    monthText.text =
                        "${monthTitleFormatter.format(firstDate)} - ${
                            monthTitleFormatter.format(
                                lastDate
                            )
                        }"
                    if (firstDate.year == lastDate.year) {
                        yearText.text = firstDate.yearMonth.year.toString()  + "-"
                    } else {
                        yearText.text =
                            "${firstDate.yearMonth.year} - ${lastDate.yearMonth.year}"  + "-"
                    }
                }
            }
        }

    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun setTodaysDakoku() {
        val date1: LocalDate = LocalDate.now()
        selectedDate = date1
        setCurrentDakoku(selectedDate!!.toString())
    }

    @RequiresApi(Build.VERSION_CODES.O)
    open fun setCurrentDakoku(day: String) {
        val empname = empName
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

            val kyukei = data?.rest
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

}