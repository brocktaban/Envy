package com.brocktaban.envy.fragments

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.brocktaban.envy.MainActivity
import com.brocktaban.envy.R
import com.theartofdev.edmodo.cropper.CropImage
import com.theartofdev.edmodo.cropper.CropImageView
import kotlinx.android.synthetic.main.fragment_create_confession.view.*
import android.app.Activity.RESULT_OK
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.wtf
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import java.util.*
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode
import com.google.android.libraries.places.widget.Autocomplete
import android.app.Activity.RESULT_CANCELED
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import androidx.appcompat.app.AlertDialog
import com.brocktaban.envy.helpers.DataClass
import com.google.android.libraries.places.widget.AutocompleteActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import org.jetbrains.anko.design.snackbar
import org.jetbrains.anko.info
import java.io.ByteArrayOutputStream
import kotlin.collections.HashMap
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class CreateConfession : Fragment(), AnkoLogger {

    private val AUTOCOMPLETE_REQUEST_CODE = 2

    private lateinit var mActivity: MainActivity
    private lateinit var db: FirebaseFirestore
    private lateinit var mAuth: FirebaseAuth
    private lateinit var storage: FirebaseStorage

    private lateinit var mView: View

    private var imageSelected = false
    private var mPlace: Place? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        mView = inflater.inflate(R.layout.fragment_create_confession, container, false)

        if (!Places.isInitialized()) {
            Places.initialize(context!!, context!!.getString(R.string.google_places_api))
        }
        val fields = Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG)

        mActivity = activity as MainActivity
        db = FirebaseFirestore.getInstance()
        storage = FirebaseStorage.getInstance()
        mAuth = FirebaseAuth.getInstance()

        mView.image.setOnClickListener {
            context?.let { c ->
                CropImage.activity()
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .setAspectRatio(1, 1)
                        .start(c, this)
            }
        }

        mView.selectPlaceButton.setOnClickListener {
            val intent = Autocomplete.IntentBuilder(
                    AutocompleteActivityMode.FULLSCREEN, fields)
                    .build(context!!)
            startActivityForResult(intent, AUTOCOMPLETE_REQUEST_CODE)
        }

        mView.confession.counterMaxLength = 500

        return mView
    }

    suspend fun create(): Boolean {

        val title = mView.etTitle.text.toString()
        val content = mView.etConfession.text.toString()

        mView.title.isErrorEnabled = false
        mView.place.isErrorEnabled = false
        mView.confession.isErrorEnabled = false

        if (mActivity.isNullOrEmpty(title)) {
            mView.title.isErrorEnabled = true
            mView.title.error = "Subject can't be empty"
            return false
        }

        if (mPlace == null) {
            mView.place.isErrorEnabled = true
            mView.place.error = "Place can't be empty"
            return false
        }

        if (mActivity.isNullOrEmpty(content)) {
            mView.confession.isErrorEnabled = true
            mView.confession.error = "Confession can't be empty"
            return false
        }

        if (!imageSelected) {
            mView.main.snackbar("Choose an image", "Choose") {
                mView.image.performClick()
            }
            return false
        }

        if (showAlert() == null) {
            return false
        }

        startLoading()

        val confessionMap: HashMap<String, Any> = HashMap()
        val uid = mAuth.currentUser?.uid!!

        confessionMap["title"] = title
        confessionMap["content"] = content
        confessionMap["timestamp"] = Date()
        confessionMap["uid"] = uid
        confessionMap["place"] = mPlace!!
        confessionMap["anonymous"] = mView.checkbox.isChecked

        val ref = storage.reference.child("confessions/$uid.png")
        mView.image.isDrawingCacheEnabled = true
        mView.image.buildDrawingCache()
        val bitmap = (mView.image.drawable as BitmapDrawable).bitmap
        val baos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos)
        val data = baos.toByteArray()

        val url = DataClass.uploadImage(ref, data)

        confessionMap["image"] = url.toString()

        val success = DataClass.createNewConfession(db, confessionMap) != null

        endLoading()

        return success

    }

    private suspend fun showAlert() = suspendCoroutine<Unit?> {
        AlertDialog.Builder(context!!)
                .setTitle("Publish")
                .setMessage("Do you really want to post this confession?")
                .setPositiveButton("yes") { _, _ -> it.resume(Unit) }
                .setNegativeButton("No") { _, _ -> it.resume(null) }
                .show()
    }

    private fun startLoading() {
        mView.progressBar.visibility = View.VISIBLE
        mView.image.isEnabled = false
        mView.title.isEnabled = false
        mView.confession.isEnabled = false
        mView.selectPlaceButton.isEnabled = false
        mView.checkbox.isEnabled = false
    }

    private fun endLoading() {
        mView.progressBar.visibility = View.GONE
        mView.image.isEnabled = true
        mView.title.isEnabled = true
        mView.confession.isEnabled = true
        mView.selectPlaceButton.isEnabled = true
        mView.checkbox.isEnabled = true
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            val result = CropImage.getActivityResult(data)
            wtf(result)
            if (resultCode == RESULT_OK) {
                wtf("result ok")
                val resultUri = result.uri
                mView.image.setPadding(0, 0, 0, 0)
                mView.image.setImageURI(resultUri)
                imageSelected = true
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                wtf("result error")
                wtf(result.error)
            }
        }

        if (requestCode == AUTOCOMPLETE_REQUEST_CODE) {
            when (resultCode) {
                RESULT_OK -> {
                    val place = Autocomplete.getPlaceFromIntent(data)
                    mPlace = place
                    mView.etPlace.setText(place.name)
                    info("Place: " + place.name + ", " + place.id)
                }
                AutocompleteActivity.RESULT_ERROR -> {
                    val status = Autocomplete.getStatusFromIntent(data)
                    mView.main.snackbar(status.statusMessage.toString())
                    wtf(status.statusMessage)
                }
                RESULT_CANCELED -> {
                    mView.main.snackbar("Cancelled")
                }
            }
        }
    }
}
