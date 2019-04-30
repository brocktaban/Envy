package com.brocktaban.envy.helpers

import android.net.Uri
import com.google.android.gms.tasks.Continuation
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask
import kotlinx.android.synthetic.main.fragment_confession_info.*
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.design.snackbar
import org.jetbrains.anko.wtf
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

data class User(
        var id: String? = null,
        var displayName: String? = null,
        var photoURL: String? = null,
        var timeCreated: Date? = null
)

class DataClass {

    companion object: AnkoLogger {

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
            val doc = db.collection("confessions").document()
            val id = doc.id

            confession["id"] = id

            doc.set(confession)
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

        suspend fun getConfessionById(db: FirebaseFirestore, id: String) = suspendCoroutine<Confession?> {
            db.collection("confessions").document(id).get().addOnCompleteListener { task ->
                if (!task.isSuccessful) {
                    wtf("Could not get confession: $id", task.exception)
                    return@addOnCompleteListener
                }

                if (task.result == null) {
                    wtf("Result is null: $id")
                    it.resume(null)
                    return@addOnCompleteListener
                }

                val confession = task.result!!.toObject(Confession::class.java)
                it.resume(confession)
            }
        }

        suspend fun getUserById(db: FirebaseFirestore, uid: String) = suspendCoroutine<User?>{
            db.collection("users").document(uid).get().addOnCompleteListener { task ->
                if (!task.isSuccessful) {
                    wtf("Could not get user: $uid", task.exception)
                    it.resume(null)
                    return@addOnCompleteListener
                }

                if (task.result == null) {
                    wtf("Result is null: $uid")
                    it.resume(null)
                    return@addOnCompleteListener
                }

                if (!task.result!!.exists()) {
                    wtf("There's no user: $uid")
                    it.resume(null)
                    return@addOnCompleteListener
                }

                val user = task.result!!.toObject(User::class.java)
                it.resume(user)
            }
        }
    }
}