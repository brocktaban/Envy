package com.brocktaban.envy.fragments


import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import com.brocktaban.envy.R
import com.brocktaban.envy.helpers.Confession
import com.brocktaban.envy.helpers.DataClass
import com.bumptech.glide.Glide
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.fragment_confession_info.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.design.snackbar
import org.jetbrains.anko.wtf

class ConfessionInfo(private val id: String) : Fragment(), AnkoLogger {

    private lateinit var db: FirebaseFirestore

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val v = inflater.inflate(R.layout.fragment_confession_info, container, false)

        db = FirebaseFirestore.getInstance()

        GlobalScope.launch(Dispatchers.Main) {
            getConfession()
        }


        return v
    }

    private suspend fun getConfession() {
        wtf(id)
        val confession = DataClass.getConfessionById(db, id) ?: return

        Glide.with(main).load(confession.image).into(image)

        title.text = confession.title
        text.text = confession.content

        if (!confession.anonymous!!) {
            val user = DataClass.getUserById(db, confession.uid!!) ?: return
            author.text = "by ${user.displayName}"
        } else {
            author.text = "by anonymous"
        }
    }


}
