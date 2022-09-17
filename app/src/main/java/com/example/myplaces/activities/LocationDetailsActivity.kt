package com.example.myplaces.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.Toolbar
import com.bumptech.glide.Glide
import com.example.myplaces.R
import com.example.myplaces.database.DatabaseRepository
import com.example.myplaces.models.Location
import com.example.myplaces.utils.Constants

@Suppress("DEPRECATION")
class LocationDetailsActivity : BaseActivity() {

    private lateinit var myLocationDetails: Location

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_location_details)

        var locationId = ""
        if (intent.hasExtra(Constants.LOCATION_ID)) {
            locationId = intent.getStringExtra(Constants.LOCATION_ID)!!
        }

        showProgressDialog(resources.getString(R.string.please_wait))
        DatabaseRepository().getLocationDetails(this, locationId)


        val viewMapButton: Button = findViewById(R.id.btn_view_on_map)
        viewMapButton.setOnClickListener {
            val intent = Intent(this, MapActivity::class.java)
            intent.putExtra(MainActivity.EXTRA_PLACE_DETAILS, myLocationDetails)
            startActivity(intent)
        }
    }

    private fun setupActionBar(title: String) {
        val locationDetailToolbar: Toolbar = findViewById(R.id.toolbar_location_detail)
        setSupportActionBar(locationDetailToolbar)

        val actionBar = supportActionBar
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.ic_white_arrow_back_24)
            actionBar.title = title
        }

        locationDetailToolbar.setNavigationOnClickListener { onBackPressed() }

    }

    fun loadLocationDetails(location: Location) {
        myLocationDetails = location
        val locationImage: AppCompatImageView = findViewById(R.id.iv_location_detail_image)
        val locationDescription: TextView = findViewById(R.id.tv_location_detail_description)

        hideProgressDialog()

        setupActionBar(location.name)

        Glide
            .with(this)
            .load(location.image)
            .centerCrop()
            .placeholder(R.drawable.detail_screen_image_placeholder)
            .into(locationImage)

        locationDescription.text = location.description
    }
}