package com.brocktaban.envy.helpers

import com.google.firebase.firestore.FirebaseFirestore
import org.jetbrains.anko.AnkoLogger
import java.util.*
import kotlin.collections.ArrayList
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

data class Confession(
        var id: String? = null,
        var title: String? = null,
        var content: String? = null,
        var timestamp: Date? = null,
        var image: String? = null,
        var uid: String? = null
)

class DataClass {

    companion object : AnkoLogger {
        suspend fun getConfessionAsArrayList(db: FirebaseFirestore) = suspendCoroutine<ArrayList<Confession>?> {
            db.collection("confessions").get().addOnCompleteListener { task ->
                if (!task.isSuccessful) {
                    it.resume(null)
                    task.exception?.printStackTrace()
                    return@addOnCompleteListener
                }

                if (task.result == null) {
                    it.resume(null)
                    return@addOnCompleteListener
                }

                if (task.result?.isEmpty!!) {
                    it.resume(null)
                    return@addOnCompleteListener
                }

                val arr = ArrayList<Confession>()

                for (x in task.result!!.documents)
                    arr.add(x.toObject(Confession::class.java)!!)

                it.resume(arr)
            }
        }
    }
}