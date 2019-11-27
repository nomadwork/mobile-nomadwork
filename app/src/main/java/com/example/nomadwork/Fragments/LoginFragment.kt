package com.example.nomadwork.Fragments


import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.util.Base64
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.view.isVisible
import butterknife.BindView
import butterknife.ButterKnife
import butterknife.OnClick
import com.example.nomadwork.API.NomadWorkAPIService
import com.example.nomadwork.API.Session
import com.example.nomadwork.API.request.LoginRequest
import com.example.nomadwork.Activity.LoginActivity
import com.example.nomadwork.Activity.LoginStep
import com.example.nomadwork.Activity.MapsActivity
import com.example.nomadwork.Helpers.AndroidDisposable
import com.example.nomadwork.Helpers.Constants
import com.example.nomadwork.Helpers.PreferencesManager
import com.example.nomadwork.Helpers.SoftInput

import com.example.nomadwork.R
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_login.*
import retrofit2.HttpException

class LoginFragment : Fragment() {
    companion object {
        val disposable = AndroidDisposable()
        const val TAG = "LoginFragment"
    }

    @BindView(R.id.wrong_login_message) lateinit var messagesTextView: TextView
    @BindView(R.id.user_editText) lateinit var userEditText: EditText
    @BindView(R.id.password_editText) lateinit var passwordEditText: EditText
    @BindView(R.id.forget_password) lateinit var forgetPasswordText: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val v = inflater.inflate(R.layout.fragment_login, container, false)
        ButterKnife.bind(this, v)

        PreferencesManager.getPreferenceString((activity as LoginActivity).applicationContext, Constants.USER_EMAIL)?.let {
            userEditText.setText(it.replace("\"", ""))
        }

        forgetPasswordText.text = getString(R.string.forget_password, getString(R.string.forget_password_click))

        return v
    }

    @OnClick(R.id.enter_button)
    fun nextStepLogin(){
        if(checkPermissionForLocation(activity as LoginActivity)){
            if(userEditText.isVisible && isEmailValid(userEditText.text.toString()) && checkPermissionForLocation((activity as LoginActivity))) {
                //TODO: Retirar codigo abaixo checkUserEmail(userEditText.text.toString())
                //val i = Intent((activity as LoginActivity).applicationContext, MapsActivity::class.java)
                //startActivity(i)
                //(activity as LoginActivity).finish()
                checkUserEmail(userEditText.text.toString())
                return
            }
        } else {
            Toast.makeText((activity as LoginActivity), "Ative as permissoes", Toast.LENGTH_SHORT).show()
        }

        if(passwordEditText.isVisible){//TODO: Retirar codigo abaixo checkUserEmail(userEditText.text.toString())
            //val i = Intent((activity as LoginActivity).applicationContext, MapsActivity::class.java)
            //startActivity(i)
            //(activity as LoginActivity).finish()
            login(userEditText.text.toString(), passwordEditText.text.toString())
        }
    }

    @OnClick(R.id.new_user)
    fun registerNewUser(){
        hideKeyboard()
        //(activity as LoginActivity).doReplaceFragment(LoginStep.NEWUSER)
    }

    @OnClick(R.id.back_login_button)
    fun backLogin(){
        backLoginStep()
    }

    private fun changeLoginStep(){
        user_editText_relative.visibility = View.GONE
        new_user.visibility = View.GONE
        user_editText.visibility = View.GONE

        enter_button.text = getString(R.string.enter)
        forgetPasswordText.visibility= View.VISIBLE
        password_editText_relative.visibility = View.VISIBLE
        back_login_button.visibility = View.VISIBLE
    }

    private fun backLoginStep(){
        enter_button.text = getString(R.string.next)
        password_editText_relative.visibility = View.GONE
        back_login_button.visibility = View.GONE

        user_editText_relative.visibility = View.VISIBLE
        user_editText.visibility = View.VISIBLE
        forgetPasswordText.visibility= View.INVISIBLE
        new_user.visibility = View.VISIBLE

    }

    private fun hideKeyboard() {
        SoftInput.hide(userEditText, (activity as LoginActivity).applicationContext)
        SoftInput.hide(passwordEditText, (activity as LoginActivity).applicationContext)
    }

    private fun isEmailValid(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    private fun checkUserEmail(email: String) {
        val api = NomadWorkAPIService.api()


        val r = api.verifyUserEmail.verifyEmail(email)
        val subscription = r.observeOn(AndroidSchedulers.mainThread())
            .subscribeOn(Schedulers.io())
            .subscribe({ result ->
                if (result.code() == 200) {
                    changeLoginStep()
                }
            }, { error ->
                if (error is HttpException) {
                    if (error.code() == 404) {
                        showMessage(getString(R.string.wrong_username_or_password))
                    } else {
                        showMessage(error.message!!)
                        Log.e(TAG, error.message)
                    }
                } else {
                    showMessage(getString(R.string.login_error_contact_admin))
                    Log.e(TAG, error.message)
                }
            })
        disposable.add(subscription)
    }

    private fun login(email: String,password: String){
        val api = NomadWorkAPIService.api()

        val passwordEncond: String = password //TODO:Base64.encodeToString(password.toByteArray(), Base64.DEFAULT)

        val r = api.userLogin.login(LoginRequest(email, passwordEncond))

        val subscription = r.observeOn(AndroidSchedulers.mainThread())
            .subscribeOn(Schedulers.io())
            .subscribe({ result ->
                if (result.code == 200){
                    Session(result.result.token.getToken).save((activity as LoginActivity).applicationContext)
                    PreferencesManager.initUserPreferences(result.result.user)
                    PreferencesManager.setPreference((activity as LoginActivity).applicationContext, Constants.USER_EMAIL, email)
                    val i = Intent((activity as LoginActivity).applicationContext, MapsActivity::class.java)
                    startActivity(i)
                    (activity as LoginActivity).finish()
                }
            },{ error ->
                if (error is HttpException) {
                    if (error.code() == 404) {
                        showMessage(getString(R.string.wrong_username_or_password))
                    } else {
                        showMessage(error.message!!)
                        Log.e(TAG, error.message)
                    }
                } else {
                    showMessage(getString(R.string.login_error_contact_admin))
                    Log.e(TAG, error.message)
                }
            })
        disposable.add(subscription)
    }



    private fun showMessage(message: String) {
        messagesTextView.text = message

        if (messagesTextView.visibility != View.VISIBLE) {
            messagesTextView.alpha = 0.0f
            messagesTextView.visibility = View.VISIBLE
            messagesTextView.animate().alpha(1.0f)

            Handler().postDelayed({
                messagesTextView.animate()
                    .alpha(0.0f)
                    .withEndAction {
                        messagesTextView.visibility = View.INVISIBLE
                    }
            }, 3000)
        }
    }

    private fun checkPermissionForLocation(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            if (context.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                true
            } else {
                // Show the permission request
                ActivityCompat.requestPermissions(
                    (activity as LoginActivity),
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