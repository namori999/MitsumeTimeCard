package com.example.mitsumetimecard.kintaitable


import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.core.widget.TextViewCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResultListener
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.lifecycle.asLiveData
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mitsumetimecard.JitudoViewModel
import com.example.mitsumetimecard.R
import com.example.mitsumetimecard.calendar.CalenderFragment
import com.example.mitsumetimecard.dakoku.Dakoku
import com.example.mitsumetimecard.dakoku.DakokuApplication
import com.example.mitsumetimecard.dakoku.DakokuViewModel
import com.example.mitsumetimecard.ui.main.MainViewModel
import com.example.mitsumetimecard.updatedialog.UpdateDialogFragment
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.util.*
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import com.example.mitsumetimecard.kintaitable.TableAdapter as TableAdapter1


class KintaiTableFragment() : Fragment(){


    private lateinit var nameViewModel: MainViewModel
    val application = DakokuApplication()

    lateinit var dakokuViewModel : DakokuViewModel
    private lateinit var viewModel: MainViewModel
    private lateinit var jitudoModel : JitudoViewModel
    private lateinit var currentMonthText: TextView

    private var userName:String = ""

    private var selectedMonth:String = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.kintai_table_layout, container, false)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        // RecyclerView の設定
        val recyclerView = view?.findViewById<RecyclerView>(R.id.recycleview)
        val adapter = this.activity?.let { TableAdapter1(it) }
        recyclerView?.adapter =adapter
        recyclerView?.layoutManager = LinearLayoutManager(this.requireContext())
        recyclerView?.setHasFixedSize(true)

        //show userName
        viewModel = ViewModelProviders.of(requireActivity()).get(MainViewModel::class.java)
        val userNameTxt = view?.findViewById<TextView>(R.id.dakokushaTxt)
        currentMonthText = view.findViewById(R.id.currentMonthText)

        selectedMonth = CalenderFragment.selectedDate.toString().substring(0,7)
        updateList(selectedMonth)

        viewModel.mutableLiveData.observe(viewLifecycleOwner, object : Observer,
            androidx.lifecycle.Observer<String> {

            @RequiresApi(Build.VERSION_CODES.O)
            override fun onChanged(o: String?) {
                //name
                val selectedName = o!!.toString()
                userNameTxt?.setText("$selectedName" + "さん")
                userName = selectedName


                val dakokuByName = application.repository.getDakokuByName(userName).asLiveData()

                dakokuByName.observe(viewLifecycleOwner, object : Observer,
                    androidx.lifecycle.Observer<List<Dakoku>> {

                    @RequiresApi(Build.VERSION_CODES.O)
                    override fun update(o: Observable?, arg: Any?) {
                        updateList(selectedMonth)
                    }

                    @RequiresApi(Build.VERSION_CODES.O)
                    override fun onChanged(t: List<Dakoku>?) {
                       updateList(selectedMonth)
                    }
                })

            }

            override fun update(o: Observable?, arg: Any?) {

            }

        })

        var name = userNameTxt?.text.toString()
        name = name.replace(("さん").toRegex(), "")


        setFragmentResultListener("input"){ _, data ->
            val selectedDate = data.getString("when","")
            Log.d("callbackListner","selectedDate = $selectedDate")
            selectedMonth = selectedDate.substring(0,7)
            //updateList(selectedMonth)
            currentMonthText.setText("${selectedMonth}" )
        }

        val factory = activity?.application?.let {
            DakokuViewModel.ModelViewModelFactory(
                application.repository
            )
        }
        dakokuViewModel = factory?.let { ViewModelProvider(this, it).get(DakokuViewModel::class.java) }!!

        var cal = Calendar.getInstance()
        val df: DateFormat = SimpleDateFormat("yyyy-MM-dd")

        val nextMonthButton :View = view.findViewById(R.id.rightArrow)
        nextMonthButton.setOnClickListener(){
            cal.add(CalenderFragment.selectedDate!!.monthValue, 1)
            println("after: ${df.format(cal.time)}")
            val selectedMonth = "${df.format(cal.time)}".substring(0,7)
            Log.v("selectedMonth","${selectedMonth}")

            updateList(selectedMonth)
        }

        val previousMonthButton :View = view.findViewById(R.id.leftArrow)
        previousMonthButton.setOnClickListener(){
            cal.add(CalenderFragment.selectedDate!!.monthValue, -1)
            println("after: ${df.format(cal.time)}")
            val selectedMonth = "${df.format(cal.time)}".substring(0,7)
            Log.v("selectedMonth","${selectedMonth}")

            updateList(selectedMonth)
        }

    }

    @SuppressLint("NotifyDataSetChanged")
    private fun updateList(selectedMonth: String){
        val adapter = this.activity?.let { TableAdapter1(it) }

        val list:List<Dakoku> = application.repository.getDakokuListByName(userName)
        val filterdList = list.filter { it.date!!.startsWith("${selectedMonth}")}
        notifyDatasetChanged(adapter,filterdList)

        val nullcheckList = filterdList.filterNotNull()
        val totalJitudo:Double = Math.round(nullcheckList.sumOf {it.jitsudo!!} * 100.0) / 100.0

        view?.findViewById<TextView>(R.id.sumTxt)?.setText(totalJitudo.toString() + " h")
        currentMonthText.setText("${selectedMonth}" )
    }

    private fun notifyDatasetChanged(adapter:com.example.mitsumetimecard.kintaitable.TableAdapter?,list:List<Dakoku>?){
        adapter?.submitList(null)

        val recyclerView = view?.findViewById<RecyclerView>(R.id.recycleview)
        val adapter = this.activity?.let { TableAdapter1(it) }
        recyclerView?.adapter =adapter

        adapter?.submitList(list)
        adapter?.notifyDataSetChanged()

        if (adapter != null) {
            setListener(adapter)
        }

    }

    private fun setListener(adapter: com.example.mitsumetimecard.kintaitable.TableAdapter) {
        adapter?.setOnItemClickListener(object : TableAdapter1.onItemClickListener {
            override fun onItemClick(position: Int) {

                val dakoku = adapter.getItem(position)
                Log.v("dakoku at position", "${dakoku}")
                val shukkinTime = dakoku?.shukkin.toString()
                val taikinTime = dakoku?.taikin.toString()
                val lestTime = dakoku?.lest.toString()
                val name = dakoku?.name
                val date = dakoku?.date

                UpdateDialogFragment.newInstance(
                    "${shukkinTime}", "${taikinTime}", "${lestTime}", "${name}", "${date}"
                ).show(fragmentManager!!, UpdateDialogFragment.TAG)

            }
        })
    }


    @Throws(InterruptedException::class)
    fun <T> getValue(liveData: LiveData<T>): T? {
        val objects = arrayOfNulls<Any>(1)
        val latch = CountDownLatch(1)

        val observer = object : Observer,
            androidx.lifecycle.Observer<Any>  {

            override fun update(o: Observable?, arg: Any?) {
            }

            override fun onChanged(t: Any?) {
                objects[0] = t
                latch.countDown()
                liveData.removeObserver(this as androidx.lifecycle.Observer<in T>)
            }
        }
        liveData.observeForever(observer as androidx.lifecycle.Observer<in T>)
        latch.await(2, TimeUnit.SECONDS)
        return objects[0] as T?
    }

    override fun onResume() {
        super.onResume()
        selectedMonth = CalenderFragment.selectedDate.toString().substring(0,7)
        updateList(selectedMonth)
    }

}

fun Any.toInt(): Any {
    return  Int
}

