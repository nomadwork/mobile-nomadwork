package com.example.nomadwork.Activity

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import butterknife.BindView
import butterknife.ButterKnife
import butterknife.OnClick
import com.example.nomadwork.BuildConfig
import com.example.nomadwork.Fragments.LoginFragment
import com.example.nomadwork.Fragments.NewUserFragment
import com.example.nomadwork.R
import kotlinx.android.synthetic.main.fragment_login.*
import java.util.*

enum class LoginStep {
    SINGIN, NEWUSER
}

class LoginActivity : AppCompatActivity() {
    companion object {
        const val TAG = "LoginActivity"
    }

    private var currentLoginView = LoginStep.SINGIN
    private var _loginFragment: LoginFragment? = null
    private var _newUserFragment: NewUserFragment? = null


    private val loginFragment: LoginFragment
        get() {
            if(_loginFragment == null){
                _loginFragment = LoginFragment()
            }
            return _loginFragment!!
    }

    private val newUserFragment: NewUserFragment
        get() {
            if(_newUserFragment == null){
                _newUserFragment = NewUserFragment()
            }
            return _newUserFragment!!
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        ButterKnife.bind(this)

        doReplaceFragment(currentLoginView)

        supportFragmentManager.addOnBackStackChangedListener { invalidateOptionsMenu() }

        checkPermissions()
    }

    override fun onResume() {
        super.onResume()
    }

    override fun onPause() {
        super.onPause()
    }

    override fun onBackPressed() {
        when (currentLoginView) {
            LoginStep.NEWUSER -> doReplaceFragment(LoginStep.SINGIN)
            LoginStep.SINGIN -> {
                if(back_login_button.isVisible){
                    loginFragment.backLogin()
                } else {
                    super.onBackPressed()
                }
            }
        }
    }



    fun doReplaceFragment(loginStep: LoginStep) {
        val fragment = when (loginStep) {
            LoginStep.SINGIN -> loginFragment
            LoginStep.NEWUSER -> newUserFragment
        }

        currentLoginView = loginStep

        val transaction = supportFragmentManager.beginTransaction()
        if (fragment != null) {
            transaction.replace(R.id.fragments_holder_login, fragment)
        }
        transaction.commit()
    }

    /**
     * Verifica as permissões do APP e caso não tenha solicita a permissão.
     */
    private fun checkPermissions() {

        val writePermission = (ContextCompat.checkSelfPermission(
            applicationContext,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED)

        val locationPermission = (ContextCompat.checkSelfPermission(
            applicationContext,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED)

        val cameraPermission = (ContextCompat.checkSelfPermission(
            applicationContext,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED)


        val appPermissions = ArrayList<String>()
        appPermissions.add(Manifest.permission.BLUETOOTH_ADMIN)
        appPermissions.add(Manifest.permission.ACCESS_FINE_LOCATION)
        appPermissions.add(Manifest.permission.ACCESS_COARSE_LOCATION)
        appPermissions.add(Manifest.permission.CAMERA)
        appPermissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)

        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M) {
            appPermissions.add(Manifest.permission.REQUEST_INSTALL_PACKAGES)
        }

        val permissions = arrayOfNulls<String>(appPermissions.size)
        appPermissions.toArray(permissions)

        if (!writePermission || !locationPermission || !cameraPermission) {
            ActivityCompat.requestPermissions(this, permissions, PackageManager.PERMISSION_GRANTED)
        }
    }

    fun checkPermissionForLocation(context: Context): Boolean {
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
}
