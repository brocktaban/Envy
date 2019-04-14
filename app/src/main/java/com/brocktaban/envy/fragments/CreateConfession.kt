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
class CreateConfession : Fragment(), AnkoLogger {

    private val REQUEST_PLACE_PICKER = 2

    private lateinit var mActivity: MainActivity

    private lateinit var mImage: ImageView
    private lateinit var mMain: ScrollView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val v = inflater.inflate(R.layout.fragment_create_confession, container, false)

        Places.initialize(context!!, context!!.getString(R.string.google_places_api))
        val placesClient = Places.createClient(context!!)

        mActivity = activity as MainActivity

        mImage = v.image
        mMain = v.main

//        mActivity.canCreateConfession = true

        mImage.setOnClickListener {
            context?.let { c ->
                CropImage.activity()
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .setAspectRatio(1,1)
                        .start(c, this)
            }
        }

        return v
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
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
    }
}
