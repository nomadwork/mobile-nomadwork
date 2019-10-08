package com.example.nomadwork.Fragments

import android.os.Bundle
import android.view.*
import android.widget.TextView
import butterknife.BindView
import butterknife.ButterKnife
import com.example.nomadwork.R


class WorkStationDetailsFragment : BottomSheetWSDetails() {

    @BindView (R.id.wsDetailsName) lateinit var wsName: TextView
    @BindView (R.id.wsDetailsTest1) lateinit var wsTest1: TextView
    @BindView (R.id.wsDetailsTest3) lateinit var wsTest3: TextView

    companion object{
        const val TAG = "WorkStationDetails"
    }

    var name: String? = null

    var pDownX=0
    var pDownY=0
    var pUpX=0
    var pUpY=0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val v = inflater.inflate(R.layout.fragment_work_station_details, container, false)
        ButterKnife.bind(this, v!!)

        wsName.text = name

        /*v.setOnTouchListener(View.OnTouchListener { next, motionEvent ->
            when (motionEvent.action){
                MotionEvent.ACTION_DOWN -> {
                    this.onDestroy()
                }
                MotionEvent.ACTION_UP -> {
                    wsTest1.visibility = View.VISIBLE
                    wsTest3.visibility = View.VISIBLE
                }
            }
            return@OnTouchListener true
        })*/



        v.setOnClickListener {
            System.out.println(it)
            wsTest1.visibility = View.VISIBLE
            wsTest3.visibility = View.VISIBLE
        }

        v.setOnTouchListener { v, event ->
            val action = event.action
            when(action){

                MotionEvent.ACTION_DOWN -> {
                    pDownX= event.x.toInt()
                    pDownY= event.y.toInt()
                }


                MotionEvent.ACTION_MOVE -> { }

                MotionEvent.ACTION_UP -> {
                    pUpX= event.x.toInt()
                    pUpY= event.y.toInt()
                }

                MotionEvent.ACTION_CANCEL -> {

                }

                else ->{

                }
            }
            true
        }


        return v
    }

}
