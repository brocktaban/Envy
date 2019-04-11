package com.brocktaban.envy

import android.os.Bundle
import org.jetbrains.anko.intentFor

class MainActivity : _Main() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (mUser == null)
            startActivity(intentFor<Auth>())
        else
            setContentView(R.layout.activity_main)
    }
}
