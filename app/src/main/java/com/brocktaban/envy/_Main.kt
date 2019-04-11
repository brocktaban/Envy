package com.brocktaban.envy

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore

open class _Main: AppCompatActivity() {

    protected lateinit var mAuth: FirebaseAuth
    protected lateinit var mFirestore: FirebaseFirestore
    protected var mUser: FirebaseUser? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mAuth = FirebaseAuth.getInstance()
        mFirestore = FirebaseFirestore.getInstance()
        mUser = mAuth.currentUser
    }
}