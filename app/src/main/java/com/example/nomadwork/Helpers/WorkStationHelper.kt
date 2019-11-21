package com.example.nomadwork.Helpers

import com.example.nomadwork.models.WorkStation

object WorkStationHelper {

    private var listWorkStation: List<WorkStation>? = null

    fun initListWS(listWS: List<WorkStation>){
        listWorkStation = listWS
    }

    fun clearListWS(){
        listWorkStation = null
    }

    fun checkInitialized(): Boolean {
        return listWorkStation != null
    }

    fun getWSById(workStationId: Int): WorkStation? {
        return listWorkStation?.firstOrNull {it.workStationId == workStationId}
    }

    fun getWSByPosition(lat: Double, long: Double): WorkStation? {
        return listWorkStation?.firstOrNull { it.workStationLocation.wsLat == lat && it.workStationLocation.wsLong == long }
    }

    fun getAllws(): List<WorkStation> {
        return listWorkStation!!
    }
}