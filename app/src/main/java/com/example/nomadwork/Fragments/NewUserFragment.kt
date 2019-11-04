package com.example.nomadwork.Fragments

import android.content.Intent
import android.graphics.MaskFilter
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.os.Handler
import android.text.style.MaskFilterSpan
import android.util.Base64
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.core.graphics.drawable.toDrawable
import androidx.core.graphics.toColor
import androidx.core.graphics.toColorLong
import butterknife.BindView
import butterknife.ButterKnife
import butterknife.OnClick
import com.example.nomadwork.API.NomadWorkAPIService
import com.example.nomadwork.API.Session
import com.example.nomadwork.API.request.EmailRequest
import com.example.nomadwork.API.request.LoginRequest
import com.example.nomadwork.API.request.RegisterRequest
import com.example.nomadwork.Activity.LoginActivity
import com.example.nomadwork.Activity.MapsActivity
import com.example.nomadwork.Helpers.AndroidDisposable
import com.example.nomadwork.Helpers.PreferencesManager

import com.example.nomadwork.R
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import retrofit2.HttpException


class NewUserFragment : Fragment() {
    companion object {
        val disposable = AndroidDisposable()
        const val TAG = "NewUserFragment"
    }

    @BindView(R.id.wrong_login_message_user) lateinit var messagesTextView: TextView
    @BindView(R.id.new_user_name_editText) lateinit var newUserEditText: EditText
    @BindView(R.id.new_user_lastName_editText) lateinit var newUserLastNameEditText: EditText
    @BindView(R.id.new_user_email_editText) lateinit var newUserEmailEditText: EditText
    @BindView(R.id.new_password_editText) lateinit var newUserPassword: EditText
    @BindView(R.id.confirm_new_password_editText) lateinit var newUserConfirmPassword: EditText

    var allFormulareIsValid = false
    var nameValid = false
    var lastNameValid = false
    var emailIsValid = false
    var passwordIsvalid = false
    var passwordConfirmIsValid = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment

        val v = inflater.inflate(R.layout.fragment_new_user, container, false)
        ButterKnife.bind(this, v)

        newUserEditText.setOnFocusChangeListener { _, hasFocus ->
            changeEditTextNames(newUserEditText, newUserEditText.compoundDrawables.first(), hasFocus)
        }

        newUserLastNameEditText.setOnFocusChangeListener { _, hasFocus ->
            changeEditTextNames(newUserLastNameEditText, newUserLastNameEditText.compoundDrawables.first(), hasFocus)
        }

        newUserEmailEditText.setOnFocusChangeListener { _, hasFocus ->
            if(!hasFocus){
                if(newUserEmailEditText.text.isNotEmpty() && !isEmailValid(newUserEmailEditText.text.toString())){

                } else {

                }
            }
        }

        newUserPassword.setOnFocusChangeListener { _, hasFocus ->
            changeVisualPasswordsText(newUserPassword, newUserPassword.compoundDrawables.first(), hasFocus)
        }

        newUserConfirmPassword.setOnFocusChangeListener { _, hasFocus ->
            changeVisualPasswordsText(newUserConfirmPassword, newUserConfirmPassword.compoundDrawables.first(), hasFocus)
        }


        return v
    }

    @OnClick (R.id.new_user_enter_button)
    fun newUser(){
        registerNewUser(newUserEmailEditText.text.toString(), newUserPassword.text.toString())
    }

    @OnClick (R.id.new_user_back)
    fun fragmentBack(){
        (activity as LoginActivity).onBackPressed()
    }

    private fun registerNewUser(email: String, password: String){

        val api = NomadWorkAPIService.api()

        val passwordEncond: String = Base64.encodeToString(password.toByteArray(), Base64.DEFAULT)

        val r = api.newUser.registerUser(RegisterRequest(email, passwordEncond))

        val subscription = r.observeOn(AndroidSchedulers.mainThread())
            .subscribeOn(Schedulers.io())
            .subscribe({ result ->
                if (result.code == 200){
                    Session(result.result.token.getToken).save((activity as LoginActivity).applicationContext)
                    PreferencesManager.initUserPreferences(result.result.user)
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
                        Log.e(LoginFragment.TAG, error.message)
                    }
                } else {
                    showMessage(getString(R.string.login_error_contact_admin))
                    Log.e(LoginFragment.TAG, error.message)
                }
            })
        LoginFragment.disposable.add(subscription)
    }

    private fun isEmailValid(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    private fun validateNameOrLastName(string: String): Boolean{
        System.out.println("Letters Only: "+ string.matches("[a-zA-Z ]+".toRegex()).toString())
        return string.matches("[a-zA-Z ]+".toRegex())
    }

    private fun changeEditTextNames(currentEditText: EditText, drawable: Drawable, boolean: Boolean){
        if(!boolean){
            if(currentEditText.text.toString().isNotEmpty() && !validateNameOrLastName(currentEditText.text.toString())){
                editTextWrongVisual(currentEditText, drawable)
            } else {
                editTextDefaultVisual(currentEditText, drawable)
            }
        }
    }

    private fun changeVisualPasswordsText(currentEditText: EditText, drawable: Drawable, boolean: Boolean) {
        if(!boolean) {
            when (currentEditText) {
                newUserPassword -> if(currentEditText.text.toString().length < 8) {
                    editTextWrongVisual(currentEditText, drawable)
                } else {
                    editTextDefaultVisual(currentEditText, drawable)
                }
                newUserConfirmPassword -> if(currentEditText.text.toString() != newUserPassword.text.toString() ) {
                    editTextWrongVisual(currentEditText, drawable)
                } else {
                    editTextDefaultVisual(currentEditText, drawable)
                }
            }
        }
    }

    private fun editTextWrongVisual(currentEditText: EditText, drawable: Drawable) {
        currentEditText.setCompoundDrawablesWithIntrinsicBounds(drawable,
            null, resources.getDrawable(android.R.drawable.ic_dialog_alert), null)
    }

    private fun editTextDefaultVisual(currentEditText: EditText, drawable: Drawable) {
        currentEditText.setCompoundDrawablesWithIntrinsicBounds(drawable,
            null, null, null)
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
}
