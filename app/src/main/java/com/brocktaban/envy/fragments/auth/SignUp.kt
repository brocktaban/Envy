package com.brocktaban.envy.fragments.auth


import android.os.Bundle
import android.util.Log.wtf
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.brocktaban.envy.MainActivity
import com.brocktaban.envy.R
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.fragment_sign_up.view.*
import org.jetbrains.anko.design.longSnackbar
import org.jetbrains.anko.intentFor

class SignUp : Fragment() {

    private lateinit var mActivity: Auth
    private lateinit var mAuth: FirebaseAuth

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        val v = inflater.inflate(R.layout.fragment_sign_up, container, false)

        mActivity = activity as Auth
        mAuth = FirebaseAuth.getInstance()


        v.btnRegister.setOnClickListener { signUp(v) }

        return v
    }

    private fun signUp(v: View) {
        val displayName = v.etName.text.toString()
        val email = v.etEmail.text.toString()
        val password = v.etPassword.text.toString()

        val nameLayout = v.ilName
        val emailLayout = v.ilEmail
        val passwordLayout = v.ilPassword

        nameLayout.isErrorEnabled = false
        emailLayout.isErrorEnabled = false
        passwordLayout.isErrorEnabled = false

        if (mActivity.isNullOrEmpty(displayName)) {
            nameLayout.isErrorEnabled = true
            nameLayout.error = "Name can't be empty"
            return
        }

        if (displayName.length <= 3) {
            nameLayout.isErrorEnabled = true
            nameLayout.error = "Name is too short"
            return
        }

        if (mActivity.isNullOrEmpty(email)) {
            emailLayout.isErrorEnabled = true
            emailLayout.error = "Email can't be empty"
            return
        }

        if (mActivity.isNullOrEmpty(password)) {
            passwordLayout.isErrorEnabled = true
            passwordLayout.error = "Password can't be empty"
            return
        }

        if (!mActivity.isEmailValid(email)) {
            emailLayout.isErrorEnabled = true
            emailLayout.error = "Email is not valid"
            return
        }

        if (password.length <= 6) {
            passwordLayout.isErrorEnabled = true
            passwordLayout.error = "Password can't be less than 6 characters"
            return
        }

        if (!v.checkbox.isChecked) {
            v.main.longSnackbar("Please accept the terms and conditions", "Accept") {
                v.checkbox.isChecked = true
            }
            return
        }

        v.btnRegister.isEnabled = false
        v.progressBar.visibility = View.VISIBLE

        mAuth
                .createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->

                    v.btnRegister.isEnabled = true
                    v.progressBar.visibility = View.GONE

                    if (!task.isSuccessful) {
                        wtf("Could not sign in", task.exception)
                        v.main.longSnackbar(task.exception?.localizedMessage.toString())
                        return@addOnCompleteListener
                    }

                    startActivity(context?.intentFor<MainActivity>())
                }
    }
}
