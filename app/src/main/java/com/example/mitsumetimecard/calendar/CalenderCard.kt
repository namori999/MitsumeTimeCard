package com.example.mitsumetimecard.calendar

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.cardview.widget.CardView
import com.example.mitsumetimecard.R

@RequiresApi(Build.VERSION_CODES.LOLLIPOP)
@SuppressLint("ResourceAsColor", "RestrictedApi")
class CalenderCard(context: Context) : CardView(context) {

        init {
            setCardBackgroundColor(R.color.colorAccent)
            radius = 20F
            elevation = 8f
        }


}

