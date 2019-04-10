package com.brocktaban.envy

import android.os.Bundle
import kotlinx.android.synthetic.main.activity_auth.*


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
}
