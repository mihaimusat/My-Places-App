package com.example.myplaces.utils

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.provider.MediaStore
import android.webkit.MimeTypeMap

object Constants {

    const val USERS : String = "Users"
    const val IMAGE: String = "image"
    const val NAME: String = "name"
    const val FCM_TOKEN_UPDATED = "fcmTokenUpdated"

    const val LOCATIONS: String = "Locations"
    const val CREATED_BY : String = "createdBy"
    const val DESCRIPTION: String = "description"
    const val LOCATION_ID: String = "id"

    const val READ_STORAGE_PERMISSION_CODE = 1
    const val PICK_IMAGE_REQUEST_CODE = 2
    const val PLACE_AUTOCOMPLETE_REQUEST_CODE = 3

    fun showImageChooser(activity: Activity) {
        val galleryIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        activity.startActivityForResult(galleryIntent, PICK_IMAGE_REQUEST_CODE)
    }

    fun getFileExtension(activity: Activity, uri: Uri?) : String? {
        return MimeTypeMap.getSingleton().getExtensionFromMimeType(activity.contentResolver.getType(uri!!))
    }
}