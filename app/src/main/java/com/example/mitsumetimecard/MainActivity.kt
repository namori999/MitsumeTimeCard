package com.example.mitsumetimecard


import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback
import com.example.mitsumetimecard.dakoku.Dakoku
import com.example.mitsumetimecard.dakoku.DakokuApplication
import com.example.mitsumetimecard.dakoku.DakokuViewModel
import com.example.mitsumetimecard.ui.main.MainViewModel
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import java.util.*


class MainActivity : AppCompatActivity() {

    private var mAdapter: ViewsSliderAdapter? = null
    private lateinit var layouts: IntArray
    lateinit var viewPager :ViewPager2

    private lateinit var model: MainViewModel

    val application = DakokuApplication()
    private val viewModel: DakokuViewModel by viewModels {
        this?.application?.let { DakokuViewModel.ModelViewModelFactory(application.repository) }!!
    }

    private lateinit var database: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)

        //firebase
        database = Firebase.database.reference

        //show username
        model = ViewModelProviders.of(this@MainActivity).get(MainViewModel::class.java)
        val userNameTxt = findViewById<TextView>(R.id.dakokushaTxt)

        model.mutableLiveData.observe(this,object : Observer,
            androidx.lifecycle.Observer<String> {

            override fun onChanged(o: String?) {
                val selectedName = o!!.toString()
                userNameTxt?.setText("$selectedName" + "さん")
            }
            override fun update(o: Observable?, arg: Any?) {
            }
        })

        //back button
        val backBtn: ImageView? = findViewById(R.id.back)
        backBtn?.setOnClickListener (){
            Log.v("onclick","back button tapped")
            startActivity(Intent(this@MainActivity, StandByActivity ::class.java))
            this@MainActivity.finish()
        }

        //set username
        val name :String = intent.getStringExtra("EMP_NAME").toString()
        val b = Bundle()
        b.putString("COMMON_NAME_KEY", name)
        model!!.setCurrentName(name)

        // removing toolbar elevation

        val tabLayout = findViewById<TabLayout>(R.id.tab_layout)

        val viewPager = findViewById<ViewPager2>(R.id.view_pager)
        viewPager.setAdapter(ViewPagerFragmentAdapter(this))

        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            val titles = arrayOf("打刻モード", "カレンダー","月別の記録")
            val currentPage = titles.get(position)
            tab.text = "${currentPage}"
        }.attach()

        init()

    }

    override fun onPause() {
        super.onPause()
        Log.d("mainActivity","Paused")

        val list:List<Dakoku> = viewModel.dakokuList
        Log.d("dakokuList","${list}")
    }


    override fun onDestroy() {
        super.onDestroy()
        Log.d("mainActivity","Destoroyed")
    }

    override fun onRestart() {
        super.onRestart()

        Log.d("mainActivity","onRestart")
    }


private fun init() {
        layouts = intArrayOf(
            R.layout.main_fragment,
            R.layout.fragment_calender,
            R.layout.kintai_table_layout
        )
        mAdapter = ViewsSliderAdapter()

        val viewPager = findViewById<ViewPager2>(R.id.view_pager)
        viewPager.setAdapter(ViewPagerFragmentAdapter(this))
        viewPager.registerOnPageChangeCallback(pageChangeCallback)
    }

    private fun getItem(i: Int): Int {
        return viewPager.getCurrentItem() + i
    }

    var pageChangeCallback: OnPageChangeCallback = object : OnPageChangeCallback() {
        override fun onPageSelected(position: Int) {
            super.onPageSelected(position)

            // changing the next button text 'NEXT' / 'GOT IT'
            if (position == layouts.size - 1) {
                // last page. make button text to GOT IT

            } else {
                // still pages are left
            }
        }
    }

    inner class ViewsSliderAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            val view: View = LayoutInflater.from(parent.context)
                .inflate(viewType, parent, false)
            return SliderViewHolder(view)
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

        }

        override fun getItemViewType(position: Int): Int {
            return layouts.get(position)
        }

        override fun getItemCount(): Int {
            return layouts.size
        }

        inner class SliderViewHolder(view: View?) : RecyclerView.ViewHolder(view!!) {
            var title: TextView? = null
            var year: TextView? = null
            var genre: TextView? = null
        }
    }

}
