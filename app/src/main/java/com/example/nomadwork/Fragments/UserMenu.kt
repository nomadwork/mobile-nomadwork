package com.example.nomadwork.Fragments

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat.checkSelfPermission
import butterknife.BindView
import butterknife.ButterKnife
import butterknife.OnClick
import com.example.nomadwork.Activity.MapsActivity
import com.example.nomadwork.Helpers.PreferencesManager

import com.example.nomadwork.R
class UserMenu : Fragment() {

    @BindView (R.id.user_menu_photo) lateinit var userPhoto: ImageView
    @BindView (R.id.user_menu_name) lateinit var userName: TextView
    @BindView (R.id.user_menu_email) lateinit var userEmail: TextView
    @BindView (R.id.user_menu_gender) lateinit var userGender: TextView
    @BindView (R.id.user_menu_age) lateinit var userAge: TextView

    private val mascID = 0
    private val femID = 1

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment

        val v = inflater.inflate(R.layout.fragment_user_menu, container, false)
        ButterKnife.bind(this, v)

        gertUserInfo()

        v.bringToFront()

        userPhoto.setOnClickListener {
            //check runtime Manifest.permission
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                if (checkSelfPermission((activity as MapsActivity).applicationContext, Manifest.permission.READ_EXTERNAL_STORAGE) ==
                    PackageManager.PERMISSION_DENIED){
                    //permission denied
                    val permissions = arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
                    //show popup to request runtime permission
                    requestPermissions(permissions, PERMISSION_CODE)
                }
                else{
                    //permission already granted
                    pickImageFromGallery()
                }
            }
            else{
                //system OS is < Marshmallow
                pickImageFromGallery()
            }
        }

        return v
    }

    private fun gertUserInfo(){
        if(PreferencesManager.checkInitialized()){
            userName.text = PreferencesManager.user.getName
            userEmail.text = PreferencesManager.user.getEmail
            userGender.text = when (PreferencesManager.user.getGender) {
                mascID.toString() -> getString(R.string.new_user_male)
                femID.toString() -> getString(R.string.new_user_female)
                else -> getString(R.string.new_user_not_inform)
            }
            userAge.text = PreferencesManager.user.getAge
        }
    }

    @OnClick(R.id.user_menu_logout)
    fun logoutFromMenu(){
        (activity as MapsActivity).logout()
    }

    @SuppressLint("RestrictedApi")
    @OnClick(R.id.back_menu_button)
    fun finishMenuFragment(){
        this.closeMenuFragment()

        (activity as MapsActivity).run {
            buttonWSRegister.visibility = View.VISIBLE
            menuButton.visibility = View.VISIBLE
        }
    }

    private fun closeMenuFragment(){
        val t = (activity as MapsActivity).supportFragmentManager.beginTransaction()
        t.setCustomAnimations(
            R.anim.enter_from_right,
            R.anim.exit_to_left
        )
        t.remove(this).commit()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when(requestCode){
            PERMISSION_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] ==
                    PackageManager.PERMISSION_GRANTED){
                    //permission from popup granted
                    pickImageFromGallery()
                }
                else{
                    //permission from popup denied
                    //Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    //handle result of picked image
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK && requestCode == IMAGE_PICK_CODE){
            if(data!!.data != null){
                PreferencesManager.setUserPhotoPreferences(data.data!!)
                userPhoto.setImageURI(PreferencesManager.userPhoto)
            }
        }
    }

    private fun pickImageFromGallery() {
        //Intent to pick image
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, IMAGE_PICK_CODE)
    }

    companion object {
        //image pick code
        private val IMAGE_PICK_CODE = 1000
        //Permission code
        private val PERMISSION_CODE = 1001
    }
}
