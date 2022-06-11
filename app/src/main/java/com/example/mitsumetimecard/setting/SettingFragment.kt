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
import com.example.mitsumetimecard.R

class SettingFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_setting, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val backButton: ImageView = view.findViewById(R.id.backButton)
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

        val recyclerView = view.findViewById<RecyclerView>(R.id.restTimeList)
        recyclerView?.layoutManager =
            GridLayoutManager(context, 5, GridLayoutManager.VERTICAL, false)
        recyclerView?.setHasFixedSize(false)

        val application = RestTimeApplication()
        val list: List<RestTime> = application.lestTimeDao.getList()

        val adapter = RestTimeAdapter(this.requireContext(), list)
        adapter?.submitList(list)
        recyclerView?.adapter = adapter


        val selecedTimeTextView = view.findViewById<TextView>(R.id.selectedTimeText)
        val sBar = view.findViewById(R.id.seekBar) as SeekBar?
        selecedTimeTextView!!.text = sBar!!.progress.toString() + "分"

        sBar!!.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
            var pval = 0
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                pval = progress
                selecedTimeTextView!!.text = pval.toString() + "分"
            }
            override fun onStartTrackingTouch(seekBar: SeekBar) {
            }
            override fun onStopTrackingTouch(seekBar: SeekBar) {
            }
        })

        val addButton :TextView = view.findViewById(R.id.addLRestButton)
        addButton.setOnClickListener(){
            application.database.RestTimeDao().insert(RestTime(sBar!!.progress))
            recyclerView.adapter = adapter

            adapter.submitList(null)
            adapter.submitList(application.database.RestTimeDao().getList())

            adapter.notifyDataSetChanged()
        }
    }

    companion object {
        fun getInstance(): SettingFragment? {
            val fragment = SettingFragment()
            return fragment
        }
    }

    private fun Any.toInt(): Int.Companion {
        return Int
    }

}
