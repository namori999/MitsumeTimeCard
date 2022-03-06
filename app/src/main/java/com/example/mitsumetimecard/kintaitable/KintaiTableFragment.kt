package com.example.mitsumetimecard.kintaitable


import android.annotation.SuppressLint
import android.icu.text.MessageFormat.format
import android.os.Build
import android.os.Bundle
import android.provider.ContactsContract
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.lifecycle.asLiveData
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mitsumetimecard.JitudoViewModel
import com.example.mitsumetimecard.R
import com.example.mitsumetimecard.dakoku.Dakoku
import com.example.mitsumetimecard.dakoku.DakokuApplication
import com.example.mitsumetimecard.dakoku.DakokuViewModel
import com.example.mitsumetimecard.ui.main.MainViewModel
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import java.time.ZonedDateTime
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

    private var userName:String = ""

    lateinit var storage: FirebaseStorage

    private var currentMonth:Int = 0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {


        return inflater.inflate(R.layout.kintai_table_layout, container, false)

    }



    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val storage = Firebase.storage

        // RecyclerView の設定
        val recyclerView = view?.findViewById<RecyclerView>(R.id.recycleview)
        val adapter = this.activity?.let { TableAdapter1(it) }
        recyclerView?.adapter =adapter
        recyclerView?.layoutManager = LinearLayoutManager(this.requireContext())
        recyclerView?.setHasFixedSize(true)


        //show userName
        viewModel = ViewModelProviders.of(requireActivity()).get(MainViewModel::class.java)
        val userNameTxt = view?.findViewById<TextView>(R.id.dakokushaTxt)
        val currentMonthText :TextView = view.findViewById(R.id.currentMonthText)

        viewModel.mutableLiveData.observe(viewLifecycleOwner, object : Observer,
            androidx.lifecycle.Observer<String> {

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

                    }

                    @RequiresApi(Build.VERSION_CODES.O)
                    override fun onChanged(t: List<Dakoku>?) {
                        val thisMonth = LocalDate.now().toString().substring(0,7)
                        currentMonthText.setText("${thisMonth}" )
                        val filterdList: List<Dakoku>? = t?.filter { it.date!!.startsWith("${thisMonth}")}
                        adapter?.submitList(filterdList)

                        val listToCalculate = filterdList?.filterNot{ it.shukkin == null}?.filterNot { it.taikin == null }
                        val totalJitudo :Double? = listToCalculate?.sumOf { it.jitsudo!! }
                        view.findViewById<TextView>(R.id.sumTxt).setText(totalJitudo.toString() + " h")
                    }
                })
            }


            override fun update(o: Observable?, arg: Any?) {
                adapter?.notifyDataSetChanged()
            }

        })

        var name = userNameTxt?.text.toString()
        name = name.replace(("さん").toRegex(), "")



        adapter?.setOnItemClickListener(object : TableAdapter1.onItemClickListener {
            override fun onItemClick(position: Int) {
                /*
                Toast.makeText(requireContext(),"${position}がタップされました", Toast.LENGTH_LONG)
                    .show()
                 */
                val dakoku = adapter.getItem(position)
                Log.v("dakoku at position","${dakoku}")
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


        val factory = activity?.application?.let {
            DakokuViewModel.ModelViewModelFactory(
                application.repository
            )
        }
        dakokuViewModel = factory?.let { ViewModelProvider(this, it).get(DakokuViewModel::class.java) }!!


        /*
        val totalJitsudo:Double = application.repository.getTotalJitsudo(userName)
        jitudoModel = ViewModelProviders.of(this).get(JitudoViewModel::class.java)
        jitudoModel.setJitsudo(totalJitsudo)
        jitudoModel.dataSet.observe(viewLifecycleOwner, object : Observer,
            androidx.lifecycle.Observer<Double> {

            override fun update(o: Observable?, arg: Any?) {
                val totalJitudo = o!!.toString()
                Log.d("model / totaljitsudo","$totalJitudo")
                view.findViewById<TextView>(R.id.sumTxt).setText(totalJitudo.toString() + " h")
            }

            override fun onChanged(t: Double?) {
                Log.d("model / totaljitsudo","$t")
                view.findViewById<TextView>(R.id.sumTxt).setText(t!!.toString() + " h")
            }

        })
        */

        var cal = Calendar.getInstance()
        val df: DateFormat = SimpleDateFormat("yyyy-MM-dd")
        val after = df.format(cal.time)


        val nextMonthButton :LinearLayout = view.findViewById(R.id.nextMonth)
        nextMonthButton.setOnClickListener(){
            cal.add(Calendar.MONTH, 1)
            println("after: ${df.format(cal.time)}")
            val selectedMonth = "${df.format(cal.time)}".substring(0,7)
            Log.v("selectedMonth","${selectedMonth}")

            updateList(selectedMonth)
            currentMonthText.setText("${selectedMonth}" )
        }

        val previousMonthButton :LinearLayout = view.findViewById(R.id.previousMonth)
        previousMonthButton.setOnClickListener(){
            cal.add(Calendar.MONTH, -1)
            println("after: ${df.format(cal.time)}")
            val selectedMonth = "${df.format(cal.time)}".substring(0,7)
            Log.v("selectedMonth","${selectedMonth}")

            updateList(selectedMonth)
            currentMonthText.setText("${selectedMonth}" )
        }

    }

    @SuppressLint("NotifyDataSetChanged")
    private fun updateList(selectedMonth: String){
        val recyclerView = view?.findViewById<RecyclerView>(R.id.recycleview)
        val adapter = this.activity?.let { TableAdapter1(it) }
        recyclerView?.adapter =adapter

        adapter?.clear()
        val list:List<Dakoku> = application.repository.getDakokuListByName(userName)
        val filterdList = list.filter { it.date!!.startsWith("${selectedMonth}")}
        adapter?.submitList(filterdList)

        println(list.filter { it.date!!.startsWith("${selectedMonth}")})
        adapter?.notifyDataSetChanged()

        val totalJitudo:Double? = filterdList?.sumOf { it.jitsudo!! }
        view?.findViewById<TextView>(R.id.sumTxt)?.setText(totalJitudo.toString() + " h")

    }


    @Throws(InterruptedException::class)
    fun <T> getValue(liveData: LiveData<T>): T? {
        val objects = arrayOfNulls<Any>(1)
        val latch = CountDownLatch(1)

        val observer = object : Observer,
            androidx.lifecycle.Observer<Any>  {

            override fun update(o: Observable?, arg: Any?) {
                TODO("Not yet implemented")
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


    override fun onPause() {
        super.onPause()
        val adapter = this.activity?.let { TableAdapter1(it) }
        adapter?.notifyDataSetChanged()
    }

    override fun onResume() {
        super.onResume()

        val adapter = this.activity?.let { TableAdapter1(it) }
        adapter?.notifyDataSetChanged()
        val totalJitsudo:Double = application.repository.getTotalJitsudo(userName)

        jitudoModel = ViewModelProviders.of(this).get(JitudoViewModel::class.java)
        jitudoModel.setJitsudo(totalJitsudo)
    }

}

private fun Any.toInt(): Any {
    return  Int
}

