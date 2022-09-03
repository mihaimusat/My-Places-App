package com.example.myplaces.activities

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.WindowInsets
import android.view.WindowManager
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.widget.AppCompatEditText
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.example.myplaces.R
import com.example.myplaces.database.DatabaseRepository
import com.example.myplaces.models.Location
import com.example.myplaces.utils.Constants
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import de.hdodenhof.circleimageview.CircleImageView
import java.io.IOException

@Suppress("DEPRECATION")
class AddLocationActivity : BaseActivity() {

    private var mySelectedImageFileUri: Uri? = null
    private lateinit var myUserName: String
    private var myLocationImageURL: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_location)

        @Suppress("DEPRECATION")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.insetsController?.hide(WindowInsets.Type.statusBars())
        } else {
            window.setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
            )
        }

        setupActionBar()

        if (intent.hasExtra(Constants.NAME)) {
            myUserName = intent.getStringExtra(Constants.NAME)!!
        }

        val locationImage: CircleImageView = findViewById(R.id.iv_location_image)
        locationImage.setOnClickListener {
            if (ContextCompat.checkSelfPermission
                    (this, Manifest.permission.READ_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED
            ) {
                Constants.showImageChooser(this)
            } else {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                    Constants.READ_STORAGE_PERMISSION_CODE
                )
            }
        }

        val createButton: Button = findViewById(R.id.btn_create)
        createButton.setOnClickListener {
            if (mySelectedImageFileUri != null) {
                uploadLocationImage()
            } else {
                showProgressDialog(resources.getString(R.string.please_wait))
                addLocation()
            }
        }
    }

    private fun setupActionBar() {
        val addLocationToolbar: Toolbar = findViewById(R.id.toolbar_add_location_activity)
        setSupportActionBar(addLocationToolbar)

        val actionBar = supportActionBar
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.ic_white_arrow_back_24)
            actionBar.title = resources.getString(R.string.add_location_title)
        }

        addLocationToolbar.setNavigationOnClickListener { onBackPressed() }
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        val locationImage: CircleImageView = findViewById(R.id.iv_location_image)

        if (resultCode == Activity.RESULT_OK
            && requestCode == Constants.PICK_IMAGE_REQUEST_CODE
            && data!!.data != null
        ) {
            mySelectedImageFileUri = data.data

            try {
                Glide
                    .with(this)
                    .load(mySelectedImageFileUri)
                    .centerCrop()
                    .placeholder(R.drawable.ic_location_place_holder)
                    .into(locationImage)
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == Constants.READ_STORAGE_PERMISSION_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Constants.showImageChooser(this)
            }
        } else {
            Toast.makeText(
                this,
                "Oops, you just denied permission for storage. You can also allow it from settings.",
                Toast.LENGTH_LONG
            )
                .show()
        }
    }

    fun locationCreatedSuccessfully() {
        hideProgressDialog()
        setResult(Activity.RESULT_OK)
        finish()
    }

    private fun addLocation() {

        val locationName: AppCompatEditText = findViewById(R.id.et_location_name)
        val locationDescription: AppCompatEditText = findViewById(R.id.et_location_description)
        val locationCreator = getCurrentUserId()

        val location = Location(
            name = locationName.text.toString(),
            description = locationDescription.text.toString(),
            image = myLocationImageURL,
            createdBy = locationCreator
        )

        DatabaseRepository().addLocation(this, location)
    }

    private fun uploadLocationImage() {
        showProgressDialog(resources.getString(R.string.please_wait))

        val storageRef: StorageReference = FirebaseStorage.getInstance().reference
            .child(
                "LOCATION_IMAGE" + System.currentTimeMillis()
                        + "." + Constants.getFileExtension(this, mySelectedImageFileUri)
            )
        storageRef.putFile(mySelectedImageFileUri!!).addOnSuccessListener { taskSnapshot ->
            Log.i(
                "Location Image URL",
                taskSnapshot.metadata!!.reference!!.downloadUrl.toString()
            )
            taskSnapshot.metadata!!.reference!!.downloadUrl.addOnSuccessListener { uri ->
                Log.i("Downloadable Image URL", uri.toString())
                myLocationImageURL = uri.toString()

                addLocation()

            }.addOnFailureListener { exception ->
                Toast.makeText(
                    this,
                    exception.message,
                    Toast.LENGTH_LONG
                ).show()
                hideProgressDialog()
            }
        }
    }
}