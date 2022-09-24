package com.example.myplaces.activities

import android.Manifest
import android.app.Activity
import android.app.DatePickerDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
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
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import de.hdodenhof.circleimageview.CircleImageView
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

@Suppress("DEPRECATION")
class AddLocationActivity : BaseActivity() {

    private var mySelectedImageFileUri: Uri? = null
    private lateinit var myUserName: String
    private var myLocationImageURL: String = ""
    private var mLatitude: Double = 0.0
    private var mLongitude: Double = 0.0

    private var cal = Calendar.getInstance()
    private lateinit var dateSetListener: DatePickerDialog.OnDateSetListener

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

        if (!Places.isInitialized()) {
            Places.initialize(this, resources.getString(R.string.google_maps_api_key))
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

        val locationName: AppCompatEditText = findViewById(R.id.et_location_name)
        locationName.setOnClickListener {
            try {
                    val fields = listOf(
                        Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG, Place.Field.ADDRESS
                    )
                    val intent =
                        Autocomplete.IntentBuilder(AutocompleteActivityMode.FULLSCREEN, fields)
                            .build(this)
                    startActivityForResult(intent, Constants.PLACE_AUTOCOMPLETE_REQUEST_CODE)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        dateSetListener = DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
            cal.set(Calendar.YEAR, year)
            cal.set(Calendar.MONTH, month)
            cal.set(Calendar.DAY_OF_MONTH, dayOfMonth)
            updateLocationDate()
        }

        val locationDate: AppCompatEditText = findViewById(R.id.et_location_date)
        locationDate.setOnClickListener {
            DatePickerDialog(this,
                R.style.DialogTheme,
                dateSetListener,
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH)).show()
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        val locationImage: CircleImageView = findViewById(R.id.iv_location_image)
        val location: AppCompatEditText = findViewById(R.id.et_location_name)

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
        else if (resultCode == Activity.RESULT_OK &&
            requestCode == Constants.PLACE_AUTOCOMPLETE_REQUEST_CODE
        ) {
            val place: Place = Autocomplete.getPlaceFromIntent(data!!)
            location.setText(place.address)
            mLatitude = place.latLng!!.latitude
            mLongitude = place.latLng!!.longitude
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
        val locationDate: AppCompatEditText = findViewById(R.id.et_location_date)
        val locationCreator = getCurrentUserId()

        val location = Location(
            id = UUID.randomUUID().toString(),
            name = locationName.text.toString(),
            description = locationDescription.text.toString(),
            date = locationDate.text.toString(),
            image = myLocationImageURL,
            createdBy = locationCreator,
            latitude = mLatitude,
            longitude = mLongitude
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

    private fun updateLocationDate() {
        val myFormat = "dd.MM.yyyy"
        val sdf = SimpleDateFormat(myFormat, Locale.getDefault())
        val locationDate: AppCompatEditText = findViewById(R.id.et_location_date)
        locationDate.setText(sdf.format(cal.time).toString())
    }
}