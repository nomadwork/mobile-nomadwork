package com.example.nomadwork.Activity

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Point
import android.location.Location
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.*
import android.widget.*
import androidx.core.app.ActivityCompat
import androidx.viewpager.widget.ViewPager
import butterknife.BindView
import butterknife.ButterKnife
import butterknife.OnClick
import com.example.nomadwork.API.Models.BaseResponse
import com.example.nomadwork.API.NomadWorkAPIService
import com.example.nomadwork.API.Session
import com.example.nomadwork.Adapters.WSDetailsImagesAdapter
import com.example.nomadwork.Adapters.WSListAdapter
import com.example.nomadwork.Fragments.BannerFragment
import com.example.nomadwork.Fragments.LoginFragment
import com.example.nomadwork.Fragments.NewWorkStationFragment
import com.example.nomadwork.Fragments.UserMenu
import com.example.nomadwork.Helpers.*
import com.example.nomadwork.R
import com.example.nomadwork.models.WorkStation
import com.example.nomadwork.models.WorkStationDetails
import com.google.android.gms.location.*
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.bottomsheet.BottomSheetBehavior
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.bottom_sheet.*
import retrofit2.HttpException
import kotlin.collections.ArrayList
import com.google.android.material.floatingactionbutton.FloatingActionButton as FloatingActionButton1

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private val disposable = AndroidDisposable()

    private var mFusedLocationProviderClient: FusedLocationProviderClient? = null
    private val INTERVAL: Long = 5 * 1000
    private lateinit var mLocationRequest: LocationRequest
    private val distanceToNewMarkers = 30 //meters
    private var centerMapLocation: Location? = null
    @BindView(R.id.ws_filter_layout) lateinit var wsFilter: View
    @BindView(R.id.ws_list_view) lateinit var wsList: ListView
    @BindView(R.id.simpleSearchView) lateinit var searchEdit: SearchView
    @BindView(R.id.button_ws_register) lateinit var buttonWSRegister: FloatingActionButton1
    @BindView(R.id.menuButtom) lateinit var menuButton: Button

    private lateinit var sheetBehavior: BottomSheetBehavior<LinearLayout>

    private lateinit var viewpager: ViewPager

    private var workStationLatSelected: Double? = null
    private var workStationLongSelected: Double? = null


    companion object {
        const val TAG = "MapsActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)
        ButterKnife.bind(this)
        initLocation()
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        sheetBehavior = BottomSheetBehavior.from<LinearLayout>(bottom_sheet)

        sheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN

        searchWS()

        buttomWSRegister()

        bottomSheetCallback()

        bottomSheetClickListener()

        Handler().postDelayed({
            showBanner()
        }, 5000)

    }

    override fun onResume() {
        super.onResume()
        if (checkPermissionForLocation(this)) {
            startLocationUpdates()
        }
    }

    override fun onPause() {
        super.onPause()
        stopLocationUpdates()
        PreferencesManager.deletePreference(this, Constants.USER_PHOTO)
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        return if (sheetBehavior.state == BottomSheetBehavior.STATE_EXPANDED && event!!.keyCode != 8) { //KeyCode == 8 is BackSpace on keyboard.
            expandCloseSheet()
            false
        } else if (supportFragmentManager.findFragmentByTag("NewWorkStation") != null) {
            val fragment = supportFragmentManager.findFragmentByTag("NewWorkStation")
            supportFragmentManager.beginTransaction().remove(fragment!!).commit()
            false
        } else if (supportFragmentManager.findFragmentByTag("UserMenu") != null) {
            closeUserMenuFromActivity()
            false
        } else if (wsFilter.visibility == View.VISIBLE){
            wsFilter.visibility = View.GONE
            false
        } else {
            false
        }
        //return super.onKeyDown(keyCode, event)
    }

    @SuppressLint("RestrictedApi")
    @OnClick(R.id.menuButtom)
    fun openMenu(){
        if (sheetBehavior.state == BottomSheetBehavior.STATE_EXPANDED) {
            expandCloseSheet()
        }
        val fragment = UserMenu()

        buttonWSRegister.visibility = View.GONE
        menuButton.visibility = View.GONE

        val t = supportFragmentManager.beginTransaction()
        t.setCustomAnimations(
            R.anim.enter_from_left,
            R.anim.exit_to_right
        )
        t.replace(R.id.fragments_holder_maps, fragment, "UserMenu")
        t.commit()
    }

    fun logout() {
        WorkStationHelper.clearListWS()
        Session.get(null)?.logout(this)
        PreferencesManager.deletePreference(this, Constants.USER_PHOTO)
        val i = Intent(this, LoginActivity::class.java)
        i.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
        this.startActivity(i)
        this.finish()
    }

    fun searchWS(){
        searchEdit.setOnQueryTextListener(object : SearchView.OnQueryTextListener{
            override fun onQueryTextChange(newText: String): Boolean {
                //if (searchEdit.query.toString() == "") toggleFilterWS(wsFilter)
                return false
            }

            override fun onQueryTextSubmit(query: String): Boolean {
                if (searchEdit.query.toString() != "") {
                    getWSNames()
                }
                return false
            }
        })
    }

    fun buttomWSRegister(){
        buttonWSRegister.setOnClickListener {
            val fragment = NewWorkStationFragment()

            val t = supportFragmentManager.beginTransaction()
            t.setCustomAnimations(
                R.anim.enter_from_right,
                R.anim.exit_to_left
            )
            t.replace(R.id.fragments_holder_maps, fragment, "NewWorkStation")
            t.commit()
        }
    }

    @SuppressLint("RestrictedApi")
    fun closeUserMenuFromActivity(){
        val fragment = supportFragmentManager.findFragmentByTag("UserMenu")
        val t = supportFragmentManager.beginTransaction()
        t.setCustomAnimations(
            R.anim.enter_from_right,
            R.anim.exit_to_left
        )
        t.remove(fragment!!).commit()

        buttonWSRegister.visibility = View.VISIBLE
        menuButton.visibility = View.VISIBLE
    }

    private fun expandCloseSheet() {

        if (sheetBehavior.state == BottomSheetBehavior.STATE_EXPANDED) {
            sheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
            return
        }

        if (sheetBehavior.state != BottomSheetBehavior.STATE_HIDDEN) {
            bottom_sheet.visibility = View.VISIBLE
            return
        } else {
            sheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
            return
        }
    }

    private fun bottomSheetCallback() {
        /**
         * bottom sheet state change listener
         * we are changing button text when sheet changed state
         * */
        sheetBehavior.setBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
            @SuppressLint("SwitchIntDef")
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                when (newState) {
                    BottomSheetBehavior.STATE_HIDDEN -> {
                    }
                    BottomSheetBehavior.STATE_EXPANDED -> {
                    }
                    BottomSheetBehavior.STATE_COLLAPSED -> {
                    }
                    BottomSheetBehavior.STATE_DRAGGING -> {
                    }
                    BottomSheetBehavior.STATE_SETTLING -> {
                    }
                }
            }

            override fun onSlide(bottomSheet: View, slideOffset: Float) {
            }
        })
    }

    private fun bottomSheetClickListener() {
        bottom_sheet.setOnTouchListener { p0, event ->
            if (event!!.action == MotionEvent.ACTION_UP) {
                sheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
            }
            true
        }
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */

    @SuppressLint("MissingPermission")
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        mMap.isMyLocationEnabled = true

        LocationHelper.getUserLocation()?.run {
            val myLocation = LatLng(
                LocationHelper.getUserLocation()!!.latitude,
                LocationHelper.getUserLocation()!!.longitude
            )
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(myLocation, 16.0f))

            getMarkersByLocation(
                LocationHelper.getUserLocation()!!.latitude,
                LocationHelper.getUserLocation()!!.longitude
            ) {
                addMarkersByLocation()
            }
        }

        mMap.setOnMarkerClickListener(GoogleMap.OnMarkerClickListener {
            if (WorkStationHelper.checkInitialized()) {
                val mark =
                    WorkStationHelper.getWSByPosition(it.position.latitude, it.position.longitude)

                val api = NomadWorkAPIService.api()
                val r = api.workStatinDetails.workStationDetails(mark!!.workStationId)

                val subscription = r.observeOn(AndroidSchedulers.mainThread())
                    .subscribeOn(Schedulers.io())
                    .subscribe({ result ->
                        // Invalid access_token. Force logout and login
                        val s = Session.get(null)
                        if (result.code == 401 && s != null) {
                            logout()
                        }
                        showWSDetails(result.result)
                        workStationLatSelected = mark.workStationLocation.wsLat
                        workStationLongSelected = mark.workStationLocation.wsLong
                    }, { error ->
                        if (error is HttpException) {
                            Toast.makeText(this, error.message!!, Toast.LENGTH_SHORT).show()
                            Log.e(LoginFragment.TAG, error.message!!)
                        } else {
                            Toast.makeText(
                                this,
                                getString(R.string.login_error_contact_admin),
                                Toast.LENGTH_SHORT
                            ).show()
                            Log.e(LoginFragment.TAG, error.message!!)
                        }
                    })
                disposable.add(subscription)

                return@OnMarkerClickListener true
            }
            return@OnMarkerClickListener false
        })

        mMap.setOnMapClickListener {

            if(supportFragmentManager.findFragmentByTag("NewWorkStation") != null){
                //val fragment = supportFragmentManager.findFragmentByTag("NewWorkStation")
                //supportFragmentManager.beginTransaction().remove(fragment!!).commit()
                return@setOnMapClickListener
            }

            if (sheetBehavior.state != BottomSheetBehavior.STATE_HIDDEN) {
                expandCloseSheet()
            }


            if (supportFragmentManager.findFragmentByTag("UserMenu") != null) {
                closeUserMenuFromActivity()
                return@setOnMapClickListener
            }
        }

        mMap.setOnCameraMoveStartedListener {

            if(WorkStationHelper.checkInitialized()){
                getMarkersByLocation(
                    mMap.projection.fromScreenLocation(Point()).latitude,
                    mMap.projection.fromScreenLocation(Point()).longitude
                ) {
                    addMarkersByLocation()
                }
                Log.i(TAG, "New Markers")
            }

            /*if(LocationHelper.distanceCurrentLocationAndNewLocation(LocationHelper.getUserLocation()!!.latitude,
                    LocationHelper.getUserLocation()!!.longitude, mMap.projection.fromScreenLocation(Point()).latitude,
                    mMap.projection.fromScreenLocation(Point()).longitude) > distanceToNewMarkers) {

                if(WorkStationHelper.checkInitialized()) {
                    getMarkersByLocation(
                        mMap.projection.fromScreenLocation(Point()).latitude,
                        mMap.projection.fromScreenLocation(Point()).longitude
                    ) {
                        addMarkersByLocation()
                    }
                    Log.i(TAG, "New Markers")
                }
            } else {
                getMarkersByLocation(LocationHelper.getUserLocation()!!.latitude, LocationHelper.getUserLocation()!!.longitude) {
                    addMarkersByLocation()
                }
                Log.i(TAG, "New Markers")
            }*/
        }
    }

    private fun initLocation() {

        if (checkPermissionForLocation(this)) {
            // Create the location request to start receiving updates
            mLocationRequest = LocationRequest()
            mLocationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
            mLocationRequest.interval = 10000
            mLocationRequest.fastestInterval = INTERVAL
            // Create LocationSettingsRequest object using location request
            val builder = LocationSettingsRequest.Builder()
            builder.addLocationRequest(mLocationRequest)
            val locationSettingsRequest = builder.build()

            val settingsClient = LocationServices.getSettingsClient(this)
            settingsClient.checkLocationSettings(locationSettingsRequest)
            mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)

        }
    }

    private fun startLocationUpdates() {
        // new Google API SDK v11 uses getFusedLocationProviderClient(this)
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        mFusedLocationProviderClient?.requestLocationUpdates(
            mLocationRequest,
            mLocationCallback,
            Looper.myLooper()
        )
    }

    private val mLocationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            // do work here
            locationResult.lastLocation
            onLocationChanged(locationResult.lastLocation)

            Log.i(
                TAG,
                locationResult.lastLocation.latitude.toString() + "," + locationResult.lastLocation.longitude.toString()
            )

        }
    }

    fun onLocationChanged(location: Location) {

        when (LocationHelper.getUserLocation()) {
            null -> run{
                LocationHelper.updateLocation(location)
                val myLocation = LatLng(LocationHelper.getUserLocation()!!.latitude, LocationHelper.getUserLocation()!!.longitude)
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(myLocation, 15.5f))

                getMarkersByLocation(LocationHelper.getUserLocation()!!.latitude, LocationHelper.getUserLocation()!!.longitude) {
                    addMarkersByLocation()
                }
            }
            else -> run{
                if(LocationHelper.distanceCurrentLocationAndNewLocation(LocationHelper.getUserLocation()!!.latitude,
                    LocationHelper.getUserLocation()!!.longitude, location.latitude, location.longitude) > distanceToNewMarkers) {

                    LocationHelper.updateLocation(location)

                    getMarkersByLocation(LocationHelper.getUserLocation()!!.latitude, LocationHelper.getUserLocation()!!.longitude) {
                        addMarkersByLocation()
                    }
                    Log.i(TAG, "New Markers")
                }
            }
        }

        System.out.println(mMap.projection.fromScreenLocation(Point()))
    }

    private fun stopLocationUpdates() {
        mFusedLocationProviderClient?.removeLocationUpdates(mLocationCallback)
    }

    private fun checkPermissionForLocation(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            if (context.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                true
            } else {
                // Show the permission request
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    PackageManager.PERMISSION_GRANTED
                )
                false
            }
        } else {
            true
        }
    }

    private fun getMarkerDetails() {

    }

    private fun getMarkersByLocation(lat: Double, long: Double, callback: () -> Unit) {

        val api = NomadWorkAPIService.api()

        val latLong = "$lat,$long"


        val r = api.workStationsList.workStationsList(latLong)

        val subscription = r.observeOn(AndroidSchedulers.mainThread())
            .subscribeOn(Schedulers.io())
            .subscribe({ result ->
                // Invalid access_token. Force logout and login
                val s = Session.get(null)
                if (result.code == 401 && s != null) {
                    logout()
                }
                mMap.clear()
                WorkStationHelper.initListWS(result.result)
                callback()
            }, { error ->
                if (error is HttpException) {
                    Toast.makeText(this, error.message!!, Toast.LENGTH_SHORT).show()
                    Log.e(LoginFragment.TAG, error.message!!)
                } else {
                    Toast.makeText(this, getString(R.string.login_error_contact_admin), Toast.LENGTH_SHORT).show()
                    Log.e(LoginFragment.TAG, error.message!!)
                }
            })
        disposable.add(subscription)
    }

    private fun addMarkersByLocation(){
        if(WorkStationHelper.checkInitialized()){
            for(wsStation in WorkStationHelper.getAllws()){
                val position = LatLng(wsStation.workStationLocation.wsLat, wsStation.workStationLocation.wsLong)
                mMap.addMarker(MarkerOptions().position(position).title(wsStation.workStationName).icon(
                        BitmapDescriptorFactory.fromResource(R.drawable.ws_pin))).showInfoWindow()
            }
        }
    }

    fun showWSDetails(workStationDetails: WorkStationDetails){

        wsDetailsName.text = workStationDetails.workStationName

        wsDetailsSchedule.text = (workStationDetails.workStationScheduleOpen + " | " + workStationDetails.workStationScheduleClose)

        wsDetailsAddress.text = when (workStationDetails.workStationAddress){
            null -> (resources.getString(R.string.address_ws_null))
            else -> workStationDetails.workStationAddress
        }

        wsDetailsWifiDescription.text = workStationDetails.workStationWifi.descriptionName
        wsDetailsEnergyDescription.text = workStationDetails.workStationNoise.descriptionName
        wsDetailsSoundDescription.text = workStationDetails.workStationPlug.descriptionName
        wsDetailsEmailDescription.text = workStationDetails.workStationEmail
        wsDetailsPhoneDescription.text = workStationDetails.workStationPhone

        viewpager = findViewById(R.id.viewpager)

        val adapter = WSDetailsImagesAdapter(this, workStationDetails.workStationPhotos)
        viewpager.adapter = adapter

        tab_layout.setupWithViewPager(viewpager, true)

        expandCloseSheet()
    }

    @OnClick(R.id.wsDetailsAddress)
    fun openGoogleMaps(){
        Log.i("LOG", "$workStationLatSelected, $workStationLongSelected")

        val gmmIntentUri = Uri.parse("http://maps.google.com/maps?q=loc:$workStationLatSelected,$workStationLongSelected")
        val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)

        mapIntent.setPackage("com.google.android.apps.maps")
        startActivity(mapIntent)
    }

    private fun toggleFilterWS(layout: View) {
        val isOpen = layout.visibility == View.VISIBLE

        if (isOpen) {
            layout.animate().alpha(0.0f).withEndAction {
                layout.visibility = View.GONE
            }.start()
        } else {
            layout.alpha = 0.0f
            layout.visibility = View.VISIBLE

            layout.animate().alpha(1.0f)
        }
    }

    private fun buildListWS(list: List<WorkStation>) {

        val list = ArrayList(list)

        val adapter = WSListAdapter(applicationContext!!, list)
        wsList.adapter = adapter

        wsList.setOnItemClickListener { parent, _, position, _ ->
            val mark = list[position]

            val wsLocation = LatLng(list[position].workStationLocation.wsLat, list[position].workStationLocation.wsLong)
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(wsLocation, 18.5f))

            val api = NomadWorkAPIService.api()
            val r = api.workStatinDetails.workStationDetails(mark.workStationId)

            val subscription = r.observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({ result ->
                    // Invalid access_token. Force logout and login
                    val s = Session.get(null)
                    if (result.code == 401 && s != null) {
                        logout()
                    }
                    //TODO: refazer marcacao. Nao esta funcionando
                    getMarkersByLocation(wsLocation.latitude, wsLocation.longitude){
                        addMarkersByLocation()
                    }
                    showWSDetails(result.result)
                    toggleFilterWS(wsFilter)
                    searchEdit.setQuery("", false)
                }, { error ->
                    if (error is HttpException) {
                        Toast.makeText(this, error.message!!, Toast.LENGTH_SHORT).show()
                        Log.e(LoginFragment.TAG, error.message!!)
                    } else {
                        Toast.makeText(
                            this,
                            getString(R.string.login_error_contact_admin),
                            Toast.LENGTH_SHORT
                        ).show()
                        Log.e(LoginFragment.TAG, error.message!!)
                    }
                })
            disposable.add(subscription)

        }
    }

    fun getWSNames(){
        val api = NomadWorkAPIService.api()

        val r = api.workStationSeacrhList.workStationSearchList(searchEdit.query.toString())

        val subscription = r.observeOn(AndroidSchedulers.mainThread())
            .subscribeOn(Schedulers.io())
            .subscribe({ result ->
                // Invalid access_token. Force logout and login
                val s = Session.get(null)
                if (result.code == 401 && s != null) {
                    logout()
                }
                if(result.code == 404){
                    Toast.makeText(this, result.message, Toast.LENGTH_SHORT).show()
                }
                if(result.code == 200) {
                    buildListWS(result.result)
                    toggleFilterWS(wsFilter)
                }
            }, { error ->
                if (error is HttpException) {
                    Toast.makeText(this, getString(R.string.ws_not_found), Toast.LENGTH_LONG).show()
                    Log.e(LoginFragment.TAG, error.message!!)
                } else {
                    Toast.makeText(this, getString(R.string.login_error_contact_admin), Toast.LENGTH_SHORT).show()
                    Log.e(LoginFragment.TAG, error.message!!)
                }
            })
        disposable.add(subscription)
    }

    private fun showBanner(){
        if(WorkStationHelper.checkInitialized()) {
            val ws = WorkStationHelper.getAllws()[0]

            getWSDetails(ws.workStationId) {
                val fragment = BannerFragment()
                fragment.ws = it.result
                val t = supportFragmentManager.beginTransaction()
                t.setCustomAnimations(
                    R.anim.enter_from_left,
                    R.anim.exit_to_right
                )
                t.replace(R.id.fragments_holder_maps, fragment, "Banner")
                t.commit()

                if (supportFragmentManager.findFragmentByTag("UserMenu") != null) {
                    menuButton.visibility = View.VISIBLE
                    false
                }
            }
        }
    }

    private fun getWSDetails(wsID: Int, callback: (result: BaseResponse<WorkStationDetails>) -> Unit){
        val api = NomadWorkAPIService.api()
        val r = api.workStatinDetails.workStationDetails(wsID)

        val subscription = r.observeOn(AndroidSchedulers.mainThread())
            .subscribeOn(Schedulers.io())
            .subscribe({ result ->
                // Invalid access_token. Force logout and login
                val s = Session.get(null)
                if (result.code == 401 && s != null) {
                    logout()
                }
                callback(result)
            }, { error ->
                if (error is HttpException) {
                    Toast.makeText(this, error.message!!, Toast.LENGTH_SHORT).show()
                    Log.e(LoginFragment.TAG, error.message!!)
                } else {
                    Toast.makeText(
                        this,
                        getString(R.string.login_error_contact_admin),
                        Toast.LENGTH_SHORT
                    ).show()
                    Log.e(LoginFragment.TAG, error.message!!)
                }
            })
        disposable.add(subscription)
    }

    fun moveCamByFragment(wsDetails: WorkStationDetails){
        val location = LatLng(
            WorkStationHelper.getWSById(wsDetails.workStationId)!!.workStationLocation.wsLat,
            WorkStationHelper.getWSById(wsDetails.workStationId)!!.workStationLocation.wsLong
        )
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 16.0f))
    }
}
