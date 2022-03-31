 package com.example.mitsumetimecard


import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.*
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room
import com.example.mitsumetimecard.dakoku.Dakoku
import com.example.mitsumetimecard.dakoku.DakokuApplication
import com.example.mitsumetimecard.dakoku.DakokuViewModel
import com.example.mitsumetimecard.employees.User
import com.example.mitsumetimecard.setting.*
import com.example.mitsumetimecard.ui.main.MainViewModel
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter


 /**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
class StandByActivity : AppCompatActivity() {

    private lateinit var database: DatabaseReference

    val application = DakokuApplication()
    private val model: DakokuViewModel by viewModels {
        this?.application?.let { DakokuViewModel.ModelViewModelFactory(application.repository) }
    }



    @RequiresApi(Build.VERSION_CODES.O)
    @SuppressLint("ClickableViewAccessibility", "ResourceAsColor")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        DakokuApplication.setContext(this)
        LestTimeApplication.setContext(this)
        setContentView(R.layout.activity_stand_by)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        //set date
        val date1: LocalDateTime = LocalDateTime.now()
        val dtformat2: DateTimeFormatter =
            DateTimeFormatter.ofPattern("yyyy-MM-dd E")
        val fdate2: String = dtformat2.format(date1)
        Log.v("today :","$fdate2")

        findViewById<TextView>(R.id.standByDate).setText("$fdate2")


        //firebase
        database = Firebase.database.reference
        val rootRef: DatabaseReference = database.child("EmpNameSpreadSheet")

        val progress= findViewById<ProgressBar>(R.id.progress)

        //creatuser list
        var valueList = arrayListOf<User>()

        rootRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                progress.visibility = VISIBLE
                valueList.clear()

                for (h in dataSnapshot.children) {

                    if (h.child("Hide").getValue(Boolean::class.java) == true) {
                        Log.v("datasnapshot","this is hidden data")
                    }
                    else{
                        val value = h.getValue(User::class.java)
                        //Log.i("get dataSnapshot", "{$value}")
                        if (value != null) {
                            valueList.add(value)
                        }
                        //Log.v("datasnapshot","add data {$value}")
                    }
                }

                val list = valueList
                startSelectNameMode(list)
                progress.visibility = GONE

            }
                override fun onCancelled(databaseError: DatabaseError) {
                    // Getting Post failed, log a message
                    Log.w("firebase", "loadPost:onCancelled", databaseError.toException())
                }
            })


        val dakokuRef = database.child("/DakokuSheet")
        var dakokuList = arrayListOf<Dakoku>()
        dakokuRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                progress.visibility = VISIBLE
                dakokuList.clear()
                model.deleteAll()

                for (h in dataSnapshot.children) {

                    val date :String = h.key.toString().substring(0,10)
                    val name :String = h.child("name").getValue().toString()

                    var shukkin:Int =0
                    if (h.child("shukkin").getValue().toString() == null){
                        shukkin = 0
                    } else {
                        shukkin = h.child("shukkin").getValue().toString().toInt()
                    }

                    var taikin:Int =0
                    if (h.child("taikin").getValue().toString() == null){
                        taikin = 0
                    } else {
                        taikin = h.child("taikin").getValue().toString().toInt()
                    }

                    var lest:Int =0
                    if (h.child("lest").getValue().toString() == null){
                        lest = 0
                    } else {
                        lest = h.child("lest").getValue().toString().toInt()
                    }

                    var jitsudo:Double =0.0
                    if (h.child("jitsudo").getValue().toString() == null){
                        jitsudo = 0.0
                    } else {
                        jitsudo = h.child("jitsudo").getValue().toString().toDouble()
                    }

                    val state :String = h.child("state").getValue().toString()

                    val dakoku = Dakoku(
                        0,
                        name,
                        date,
                        shukkin,
                        taikin,
                        lest,
                        jitsudo,
                        state
                    )

                    if (dakoku != null) {
                        dakokuList.add(dakoku)
                        model.insertFromFB(dakoku)
                    }
                        //Log.v("datasnapshot","add data {$value}")

                }

                val list = dakokuList
                Log.d("dakoku list","${list}")
                progress.visibility = GONE

            }
            override fun onCancelled(databaseError: DatabaseError) {
                // Getting Post failed, log a message
                Log.w("firebase", "loadPost:onCancelled", databaseError.toException())
            }
        })

        val settingButton: ImageView = findViewById(R.id.settingBtn)
        settingButton.setOnClickListener(){
            showPopup(settingButton)
        }

    }


    fun startSelectNameMode(list:MutableList<User>){
        //prepare employees list
        val recyclerView = findViewById<RecyclerView>(R.id.userList)
        recyclerView?.layoutManager = GridLayoutManager(this, 5, GridLayoutManager.VERTICAL, false)
        recyclerView?.setHasFixedSize(false)

        val adapter = UserAdapter(this@StandByActivity, list)
        adapter?.submitList(list)
        recyclerView?.adapter = adapter


        adapter?.setOnItemClickListener(object : UserAdapter.onItemClickListener {
            override fun onItemClick(position: Int) {
                val empname = adapter.getName(position)
                Toast.makeText(this@StandByActivity,"${empname}さん お疲れさまです！", Toast.LENGTH_LONG)
                    .show()

                Log.v("name at position","${empname}")

                val intent = Intent(this@StandByActivity, MainActivity ::class.java)

                intent.putExtra("EMP_NAME",empname)
                startActivity(intent)
                this@StandByActivity.finish()

            }
        })

    }


    private fun showPopup(v: View) {
        PopupMenu(this, v).apply {
            setOnMenuItemClickListener(object: PopupMenu.OnMenuItemClickListener {
                override fun onMenuItemClick(item: MenuItem?): Boolean {

                    return when (item?.itemId) {
                        R.id.SettingFragment -> {
                            val currentFragment = SettingFragment.getInstance();
                            if (currentFragment != null) {
                                getSupportFragmentManager()
                                    .beginTransaction()
                                    .add(R.id.stand_by_activity, currentFragment, "TAG")
                                    .addToBackStack(null)
                                    .commit()
                            }
                            true
                        }
                        else -> false
                    }
                }

            })
            inflate(R.menu.setting_nav_menu)
            show()
        }
    }

    override fun onStop(){
        super.onStop()
        Log.d("stand by activity","stopped")
    }

    override fun onResume(){
        super.onResume()
        Log.d("stand by activity","resumed")
    }

}
