package com.example.nomadwork.Adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import com.example.nomadwork.R
import com.example.nomadwork.models.WorkStation

class WSListAdapter (context: Context, items: List<WorkStation>): ArrayAdapter<WorkStation>(context,0, items){
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var v = convertView

        if (v == null) {
            v = LayoutInflater.from(context).inflate(R.layout.maps_ws_list_search, parent, false)
        }

        val textView = v!!.findViewById<TextView>(R.id.wsListTextView)
        textView.text = getItem(position)!!.workStationName

        return v
    }
}