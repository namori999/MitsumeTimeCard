package com.example.mitsumetimecard


import android.annotation.SuppressLint
import android.content.Intent
import android.os.Build
import android.os.Bundle
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
import androidx.recyclerview.widget.RecyclerView
import com.example.mitsumetimecard.dakoku.Dakoku
import com.example.mitsumetimecard.dakoku.DakokuApplication
import com.example.mitsumetimecard.dakoku.DakokuViewModel
import com.example.mitsumetimecard.setting.*
import com.example.mitsumetimecard.ui.main.MainFragment
import com.example.mitsumetimecard.ui.main.MainViewModel
import com.example.mitsumetimecard.user.User
import com.example.mitsumetimecard.user.UserAdapter
import com.google.firebase.database.*
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.runBlocking
import java.time.LocalDate
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
        this.application.let { DakokuViewModel.ModelViewModelFactory(application.repository) }
    }

    private lateinit var dateTimeTextView: TextView
    var dakokuList = arrayListOf<Dakoku>()

    @RequiresApi(Build.VERSION_CODES.O)
    @SuppressLint("ClickableViewAccessibility", "ResourceAsColor")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        DakokuApplication.setContext(this)
        RestTimeApplication.setContext(this)
        setContentView(R.layout.activity_stand_by)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        hideSystemUI()

        dateTimeTextView = findViewById(R.id.standByDate)
        setDateTime()

        //firebase
        database = Firebase.database.reference
        val rootRef: DatabaseReference = database.child("EmpNameSpreadSheet")
        val progress = findViewById<ProgressBar>(R.id.progress)
        progress.visibility = VISIBLE

        runBlocking {
            Thread {
                val dakokuRef = database.child("/DakokuSheet")
                dakokuRef.orderByChild("date").limitToLast(400)
                    .addValueEventListener(object : ValueEventListener {
                        override fun onDataChange(dataSnapshot: DataSnapshot) {
                            dakokuList.clear()
                            model.deleteAll()

                            for (h in dataSnapshot.children) {

                                if (h.child("state").getValue() != "deleted") {

                                    val date: String = h.key.toString().substring(0, 10)
                                    val name: String = h.child("name").getValue().toString()

                                    var shukkin: Int = 0
                                    if (h.child("shukkin").getValue() == null) {
                                        shukkin = 0
                                    } else {
                                        shukkin = h.child("shukkin").getValue().toString().toInt()
                                    }

                                    var taikin: Int = 0
                                    if (h.child("taikin").getValue() == null) {
                                        taikin = 0
                                    } else {
                                        taikin = h.child("taikin").getValue().toString().toInt()
                                    }

                                    var rest: Int = 0
                                    if (h.child("rest").getValue() == null) {
                                        rest = 0
                                    } else {
                                        rest = h.child("rest").getValue().toString().toInt()
                                    }

                                    var jitsudo: Double = 0.0
                                    if (h.child("jitsudo").getValue() == null) {
                                        jitsudo = 0.0
                                    } else {
                                        jitsudo =
                                            h.child("jitsudo").getValue().toString().toDouble()
                                    }

                                    val state: String = h.child("state").getValue().toString()

                                    val dakoku = Dakoku(
                                        0,
                                        name,
                                        date,
                                        shukkin,
                                        taikin,
                                        rest,
                                        jitsudo,
                                        state
                                    )
                                    dakokuList.add(dakoku)
                                    model.insertFromFB(dakoku)
                                }
                            }
                            progress.visibility = GONE
                            Log.d("dakoku list", "${dakokuList}")
                        }


                        override fun onCancelled(databaseError: DatabaseError) {
                            // Getting Post failed, log a message
                            Log.w("firebase", "loadPost:onCancelled", databaseError.toException())
                        }
                    })
            }.start()
        }

        Thread {
            //creatuser list
            var valueList = arrayListOf<User>()
            rootRef.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    valueList.clear()

                    for (h in dataSnapshot.children) {
                        if (h.child("Hide").getValue(Boolean::class.java) == true) {
                            Log.v("datasnapshot", "this is hidden data")
                        } else {
                            val value = h.getValue(User::class.java)
                            //Log.i("get dataSnapshot", "{$value}")
                            if (value != null) {
                                valueList.add(value)
                            }
                            //Log.v("datasnapshot","add data {$value}")
                        }
                    }

                    val userArrayList = valueList
                    setUpUserList(userArrayList)
                    progress.visibility = GONE
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    // Getting Post failed, log a message
                    Log.w("firebase", "loadPost:onCancelled", databaseError.toException())
                }
            })
        }.start()

        val settingButton: ImageView = findViewById(R.id.settingBtn)
        settingButton.setOnClickListener() {
            hideSystemUI()
            showPopup(settingButton)
        }

    }


    @RequiresApi(Build.VERSION_CODES.O)
    fun setDateTime() {
        val date1: LocalDateTime = LocalDateTime.now()
        val dtformat2: DateTimeFormatter =
            DateTimeFormatter.ofPattern("yyyy-MM-dd E")
        val fdate2: String = dtformat2.format(date1)

        dateTimeTextView.setText(fdate2)
    }


    fun setUpUserList(userArrayList: MutableList<User>) {
        //prepare employees list
        val recyclerView = findViewById<RecyclerView>(R.id.userList)
        recyclerView?.layoutManager = GridLayoutManager(this, 5, GridLayoutManager.VERTICAL, false)
        recyclerView?.setHasFixedSize(false)

        val adapter = UserAdapter(this@StandByActivity, userArrayList)
        adapter.submitList(userArrayList)
        recyclerView?.adapter = adapter


        adapter.setOnItemClickListener(object : UserAdapter.onItemClickListener {
            @RequiresApi(Build.VERSION_CODES.O)
            override fun onItemClick(position: Int) {
                val empname = adapter.getName(position)
                val model = MainViewModel()
                model.setDakokuByName(dakokuList.filter { dakoku -> dakoku.name == empname })

                Toast.makeText(this@StandByActivity, "${empname}さん お疲れさまです！", Toast.LENGTH_LONG)
                    .show()

                val intent = Intent(this@StandByActivity, MainActivity::class.java)
                intent.putExtra("EMP_NAME", empname)
                startActivity(intent)
            }
        })

    }


    private fun showPopup(v: View) {
        PopupMenu(this, v).apply {
            setOnMenuItemClickListener(object : PopupMenu.OnMenuItemClickListener {
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

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            hideSystemUI()
        } else {
            hideSystemUI()
        }
    }

    override fun onResume() {
        super.onResume()
        Log.d("stand by activity", "resumed")
        hideSystemUI()
    }


    private fun hideSystemUI() {
        val decorView = window.decorView
        decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_FULLSCREEN
                or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)
    }

}
