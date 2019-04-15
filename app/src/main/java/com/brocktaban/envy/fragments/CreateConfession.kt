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
import com.google.android.libraries.places.internal.i
import android.R.attr.data
import android.app.Activity.RESULT_CANCELED
import android.widget.EditText
import com.google.android.libraries.places.widget.AutocompleteActivity
import com.google.android.material.textfield.TextInputEditText
import org.jetbrains.anko.design.snackbar
import org.jetbrains.anko.info


class CreateConfession : Fragment(), AnkoLogger {

    private val AUTOCOMPLETE_REQUEST_CODE = 2

    private lateinit var mActivity: MainActivity

    private lateinit var mImage: ImageView
    private lateinit var mMain: ScrollView
    private lateinit var mEtPlace: TextInputEditText

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val v = inflater.inflate(R.layout.fragment_create_confession, container, false)

        if (!Places.isInitialized()) {
            Places.initialize(context!!, context!!.getString(R.string.google_places_api))
        }
        val fields = Arrays.asList(Place.Field.ID, Place.Field.NAME)

        mActivity = activity as MainActivity

        mImage = v.image
        mMain = v.main
        mEtPlace = v.etPlace

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

        return v
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            val result = CropImage.getActivityResult(data)
            if (resultCode == RESULT_OK) {
                val resultUri = result.uri
                mImage.setPadding(0,0,0,0)
                mImage.setImageURI(resultUri)
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                wtf(result.error)
            }
        }

        if (requestCode == AUTOCOMPLETE_REQUEST_CODE) {
            when (resultCode) {
                RESULT_OK -> {
                    val place = Autocomplete.getPlaceFromIntent(data)
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
