package com.example.nomadwork.Fragments

import android.content.Intent
import android.os.Bundle
import android.util.Base64
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import butterknife.BindView
import butterknife.ButterKnife
import butterknife.OnClick
import com.example.nomadwork.API.NomadWorkAPIService
import com.example.nomadwork.API.request.RegisterRequest
import com.example.nomadwork.API.request.WSSuggestRequest
import com.example.nomadwork.Activity.LoginActivity
import com.example.nomadwork.Activity.MapsActivity
import com.example.nomadwork.Helpers.LocationHelper
import com.example.nomadwork.R
import com.example.nomadwork.models.Noise
import com.example.nomadwork.models.Plug
import com.example.nomadwork.models.Schedule
import com.example.nomadwork.models.Wifi
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_new_work_station.*
import retrofit2.HttpException

class NewWorkStationFragment : Fragment() {

    @BindView (R.id.new_ws_name_editText) lateinit var wsNameRegister: EditText
    @BindView (R.id.new_ws_email_editText) lateinit var wsEmailRegister: EditText
    @BindView (R.id.new_ws_phone_editText) lateinit var wsPhoneRegister: EditText
    @BindView (R.id.new_ws_open_hour_editText) lateinit var wsOpenHour: EditText
    @BindView (R.id.new_ws_close_hour_editText) lateinit var wsCloseHour: EditText
    @BindView (R.id.wifi_seekBar) lateinit var wsWifi: SeekBar
    @BindView (R.id.sound_seekBar) lateinit var wsNoise: SeekBar
    @BindView (R.id.energy_seekBar) lateinit var wsEnergy: SeekBar

    var wifiRate = 0
    var noiseRate = 0
    var energyRate = 0


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment

        val v = inflater.inflate(R.layout.fragment_new_work_station, container, false)
        ButterKnife.bind(this, v)

        seekBarsListener()

        return v
    }

    @OnClick (R.id.new_ws_back)
    fun finishFragment(){
        this.closeFragment()
    }

    @OnClick (R.id.new_ws_enter_button)
    fun registerNewWS(){

        val api = NomadWorkAPIService.api()

        val wsName = wsNameRegister.text.toString()
        val wsPhone = wsPhoneRegister.text.toString()
        val wsEmail = wsEmailRegister.text.toString()
        val wsLat = LocationHelper.getUserLocation()!!.latitude
        val wsLong = LocationHelper.getUserLocation()!!.longitude
        val schedule = Schedule(wsOpenHour.text.toString(), wsCloseHour.text.toString())
        val wsWifi = Wifi(wifiRate, "")
        val wsNoise = Noise(noiseRate, "")
        val wsPlug = Plug(energyRate, "")


        val r = api.workStationSuggest.workStationSuggest(WSSuggestRequest(wsName, wsPhone, wsEmail, wsLat, wsLong, schedule, wsWifi, wsNoise, wsPlug))

        val subscription = r.observeOn(AndroidSchedulers.mainThread())
            .subscribeOn(Schedulers.io())
            .subscribe({ result ->
                if (result.message != ""){
                    Toast.makeText(activity!!.applicationContext, result.message, Toast.LENGTH_SHORT).show()
                    closeFragment()
                }
            },{ error ->
                if (error is HttpException) {
                    if (error.code() == 404) {
                        //showMessage(getString(R.string.wrong_username_or_password))
                    } else {
                        //showMessage(error.message!!)
                        Log.e(LoginFragment.TAG, error.message)
                    }
                } else {
                    //showMessage(getString(R.string.login_error_contact_admin))
                    Log.e(LoginFragment.TAG, error.message)
                }
            })
        LoginFragment.disposable.add(subscription)
    }

    private fun seekBarsListener(){
        wsWifi.setOnSeekBarChangeListener( object : SeekBar.OnSeekBarChangeListener {

            override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {
                return
            }

            override fun onStartTrackingTouch(p0: SeekBar?) {
                return
            }

            override fun onStopTrackingTouch(p0: SeekBar?) {
                wifiRate = p0!!.progress
                val wifiSeekBar = p0.progress
                Toast.makeText(activity!!.applicationContext, wifiSeekBar.toString(), Toast.LENGTH_SHORT).show()
            }
        })

        wsNoise.setOnSeekBarChangeListener( object : SeekBar.OnSeekBarChangeListener {

            override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {
                return
            }

            override fun onStartTrackingTouch(p0: SeekBar?) {
                return
            }

            override fun onStopTrackingTouch(p0: SeekBar?) {
                noiseRate = p0!!.progress
                val noiseSeekBar = p0.progress
                Toast.makeText(activity!!.applicationContext, noiseSeekBar.toString(), Toast.LENGTH_SHORT).show()
            }
        })

        wsEnergy.setOnSeekBarChangeListener( object : SeekBar.OnSeekBarChangeListener {

            override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {
                return
            }

            override fun onStartTrackingTouch(p0: SeekBar?) {
                return
            }

            override fun onStopTrackingTouch(p0: SeekBar?) {
                energyRate = p0!!.progress
                val energySeekBar = p0.progress
                Toast.makeText(activity!!.applicationContext, energySeekBar.toString(), Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun closeFragment(){
        activity!!.supportFragmentManager.beginTransaction().remove(this).commit()
    }

    private fun validateName(string: String): Boolean{
        System.out.println("Letters Only: "+ string.matches("[a-zA-Z ]+".toRegex()).toString())
        return string.matches("[a-zA-Z ]+".toRegex())
    }
}
