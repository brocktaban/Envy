package com.brocktaban.envy.helpers

import android.net.Uri
import com.google.android.gms.tasks.Continuation
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

data class Confession(
        var id: String? = null,
        var title: String? = null,
        var content: String? = null,
        var timestamp: Date? = null,
        var image: String? = null,
        var uid: String? = null,
        var anonymous: Boolean? = null
)

class DataClass {

    companion object {

        suspend fun getConfessionAsArrayList(db: FirebaseFirestore) = suspendCoroutine<ArrayList<Confession>?> {
            db
                    .collection("confessions")
                    .orderBy("timestamp", Query.Direction.DESCENDING)
                    .get()
                    .addOnCompleteListener { task ->
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

        suspend fun createNewConfession(db: FirebaseFirestore, confession: HashMap<String, Any>) = suspendCoroutine<Unit?>{
            val col = db.collection("confessions")
            val id = col.id

            confession["id"] = id

            col.add(confession)
                    .addOnCompleteListener { task ->
                        if (!task.isSuccessful) {
                            it.resume(null)
                            task.exception?.printStackTrace()
                            return@addOnCompleteListener
                        }

                        it.resume(Unit)
                    }

        }

        suspend fun uploadImage(storageReference: StorageReference, byteArray: ByteArray) = suspendCoroutine<String?>{
            storageReference
                    .putBytes(byteArray)
                    .continueWithTask(Continuation<UploadTask.TaskSnapshot, Task<Uri>> { task ->
                        if (!task.isSuccessful) {
                            task.exception?.let {
                                throw it
                            }
                        }
                        return@Continuation storageReference.downloadUrl
                    })
                    .addOnCompleteListener { task ->
                        if (!task.isSuccessful) {
                            it.resume(null)
                            task.exception?.printStackTrace()
                            return@addOnCompleteListener
                        }

                        it.resume(task.result.toString())
                    }
        }
    }
}