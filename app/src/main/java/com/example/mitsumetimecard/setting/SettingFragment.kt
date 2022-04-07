package com.example.mitsumetimecard.setting

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room
import com.example.mitsumetimecard.R
import com.example.mitsumetimecard.StandByActivity
import com.example.mitsumetimecard.dakoku.DakokuApplication


// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class SettingFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_setting, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val textDate: TextView? = view.findViewById(R.id.standByDate)
        textDate?.visibility = GONE

        val backButton: ImageView = view?.findViewById(R.id.back)!!
        backButton.setOnClickListener() {
            if (fragmentManager?.getBackStackEntryCount()!! > 0) {
                getFragmentManager()?.popBackStack();
            } else {
                val entry = activity?.supportFragmentManager?.getBackStackEntryAt(0)
                entry?.id?.let { _entry ->
                    activity?.supportFragmentManager?.popBackStack(
                        _entry,
                        FragmentManager.POP_BACK_STACK_INCLUSIVE
                    )
                }
            }
        }

        val recyclerView = view.findViewById<RecyclerView>(R.id.lestTimeList)
        recyclerView?.layoutManager =
            GridLayoutManager(context, 5, GridLayoutManager.VERTICAL, false)
        recyclerView?.setHasFixedSize(false)

        val application = LestTimeApplication()
        val list: List<lestTime> = application.lestTimeDao.getList()

        val adapter = LestTimeAdapter(this.requireContext(), list)
        adapter?.submitList(list)
        recyclerView?.adapter = adapter


        val tView = view.findViewById<TextView>(R.id.tView)
        val sBar = view.findViewById(R.id.seekBar1) as SeekBar?
        tView!!.text = sBar!!.progress.toString() + "分"

        sBar!!.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
            var pval = 0

            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                pval = progress
                tView!!.text = pval.toString() + "分"
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {
                //write custom code to on start progress
            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {

            }
        })

        val addButton :TextView = view.findViewById(R.id.addLestButton)
        addButton.setOnClickListener(){
            application.database.lestTimeDao().insert(lestTime(sBar!!.progress))
            recyclerView.adapter = adapter

            adapter.submitList(null)
            adapter.submitList(application.database.lestTimeDao().getList())

            adapter.notifyDataSetChanged()
        }




    }


    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            SettingFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }

        fun getInstance(): SettingFragment? {
            val fragment = SettingFragment()
            return fragment
        }

    }


}

private fun Any.toInt(): Int.Companion {
    return Int
}
