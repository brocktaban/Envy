package com.brocktaban.envy.fragments.auth

import android.os.Bundle
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.activity_auth.*
import android.content.Intent
import com.brocktaban.envy.R
import com.brocktaban.envy._Main

class Auth : _Main() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_auth)

        if (frameLayout != null) {

            if (savedInstanceState != null) return

            supportFragmentManager
                    .beginTransaction()
                    .add(R.id.frameLayout, Login())
                    .commit()
        }
    }

    fun changeFragment(fragment: Fragment) {
        supportFragmentManager
                .beginTransaction()
                .replace(R.id.frameLayout, fragment)
                .addToBackStack(null)
                .commit()
    }

    fun isNullOrEmpty(str: String?): Boolean {
        if (str != null && !str.trim().isEmpty())
            return false
        return true
    }

    fun isEmailValid(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        val fragment = supportFragmentManager.findFragmentById(R.id.frameLayout)
        fragment?.onActivityResult(requestCode, resultCode, data)
    }
}
