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
import android.widget.ImageView
import android.widget.ScrollView
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
import android.widget.CheckBox
import android.widget.ProgressBar
import com.brocktaban.envy.helpers.DataClass
import com.google.android.libraries.places.widget.AutocompleteActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import org.jetbrains.anko.design.indefiniteSnackbar
import org.jetbrains.anko.design.snackbar
import org.jetbrains.anko.info
import java.io.ByteArrayOutputStream
import kotlin.collections.HashMap


class CreateConfession : Fragment(), AnkoLogger {

    private val AUTOCOMPLETE_REQUEST_CODE = 2

    private lateinit var mActivity: MainActivity
    private lateinit var db: FirebaseFirestore
    private lateinit var mAuth: FirebaseAuth
    private lateinit var storage: FirebaseStorage

    private lateinit var mImage: ImageView
    private lateinit var mMain: ScrollView
    private lateinit var mEtTitle: TextInputEditText
    private lateinit var mEtPlace: TextInputEditText
    private lateinit var mEtConfession: TextInputEditText
    private lateinit var mCheckBox: CheckBox
    private lateinit var mProgressBar: ProgressBar
    private lateinit var mSelectPlace: MaterialButton

    private lateinit var mLTitle: TextInputLayout
    private lateinit var mLPlace: TextInputLayout
    private lateinit var mLConfession: TextInputLayout

    private var imageSelected = false
    private var mPlace: Place? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val v = inflater.inflate(R.layout.fragment_create_confession, container, false)

        if (!Places.isInitialized()) {
            Places.initialize(context!!, context!!.getString(R.string.google_places_api))
        }
        val fields = Arrays.asList(Place.Field.ID, Place.Field.NAME)

        mActivity = activity as MainActivity
        db = FirebaseFirestore.getInstance()
        storage = FirebaseStorage.getInstance()
        mAuth = FirebaseAuth.getInstance()

        mImage = v.image
        mMain = v.main
        mEtTitle = v.etTitle
        mEtPlace = v.etPlace
        mEtConfession = v.etConfession
        mCheckBox = v.checkbox
        mProgressBar = v.progressBar
        mSelectPlace = v.selectPlaceButton

        mLTitle = v.title
        mLPlace = v.place
        mLConfession = v.confession

//        mActivity.canCreateConfession = true

        mImage.setOnClickListener {
            context?.let { c ->
                CropImage.activity()
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .setAspectRatio(1,1)
                        .start(c, this)
            }
        }

        v.selectPlaceButton.setOnClickListener {
            val intent = Autocomplete.IntentBuilder(
                    AutocompleteActivityMode.FULLSCREEN, fields)
                    .build(context!!)
            startActivityForResult(intent, AUTOCOMPLETE_REQUEST_CODE)
        }

        mLConfession.counterMaxLength = 500

        return v
    }

    suspend fun create(): Boolean {

        val title = mEtTitle.text.toString()
        val content = mEtConfession.text.toString()

        mLTitle.isErrorEnabled = false
        mLPlace.isErrorEnabled = false
        mLConfession.isErrorEnabled = false

        if (mActivity.isNullOrEmpty(title)) {
            mLTitle.isErrorEnabled = true
            mLTitle.error = "Subject can't be empty"
            return false
        }

        if (mPlace == null) {
            mLPlace.isErrorEnabled = true
            mLPlace.error = "Place can't be empty"
            return false
        }

        if (mActivity.isNullOrEmpty(content)) {
            mLConfession.isErrorEnabled = true
            mLConfession.error = "Confession can't be empty"
            return false
        }

        if (!imageSelected) {
            mMain.snackbar("Choose an image", "Choose") {
                mImage.performClick()
            }
            return false
        }

        startLoading()

        val confessionMap: HashMap<String, Any> = HashMap()
        val uid = mAuth.currentUser?.uid!!

        confessionMap["title"] = title
        confessionMap["content"] = content
        confessionMap["timestamp"] = Date()
        confessionMap["uid"] = uid
        confessionMap["anonymous"] = mCheckBox.isChecked

        val ref = storage.reference.child("confessions/$uid.png")
        mImage.isDrawingCacheEnabled = true
        mImage.buildDrawingCache()
        val bitmap = (mImage.drawable as BitmapDrawable).bitmap
        val baos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos)
        val data = baos.toByteArray()

        val url = DataClass.uploadImage(ref, data)

        confessionMap["image"] = url.toString()

        val success = DataClass.createNewConfession(db, confessionMap) != null

        endLoading()

        return success

    }

    fun startLoading() {
        mProgressBar.visibility = View.VISIBLE
        mImage.isEnabled = false
        mLTitle.isEnabled = false
        mLConfession.isEnabled = false
        mLConfession.isEnabled = false
        mSelectPlace.isEnabled = false
        mCheckBox.isEnabled = false
    }

    fun endLoading() {
        mProgressBar.visibility = View.GONE
        mImage.isEnabled = true
        mLTitle.isEnabled = true
        mLConfession.isEnabled = true
        mLConfession.isEnabled = true
        mSelectPlace.isEnabled = true
        mCheckBox.isEnabled = true
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            val result = CropImage.getActivityResult(data)
            if (resultCode == RESULT_OK) {
                val resultUri = result.uri
                mImage.setPadding(0,0,0,0)
                mImage.setImageURI(resultUri)
                imageSelected = true
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                wtf(result.error)
            }
        }

        if (requestCode == AUTOCOMPLETE_REQUEST_CODE) {
            when (resultCode) {
                RESULT_OK -> {
                    val place = Autocomplete.getPlaceFromIntent(data)
                    mPlace = place
                    mEtPlace.setText(place.name)
                    info("Place: " + place.name + ", " + place.id)
                }
                AutocompleteActivity.RESULT_ERROR -> {
                    val status = Autocomplete.getStatusFromIntent(data)
                    mMain.snackbar(status.statusMessage.toString())
                    wtf(status.statusMessage)
                }
                RESULT_CANCELED -> {
                    mMain.snackbar("Cancelled")
                }
            }
        }
    }
}
