package com.example.myplaces.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.example.myplaces.R
import com.example.myplaces.models.Location
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

@Suppress("DEPRECATION")
class MapActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var myLocationDetails: Location

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map)

        if (intent.hasExtra(MainActivity.EXTRA_PLACE_DETAILS)){
            myLocationDetails = intent.getParcelableExtra(MainActivity.EXTRA_PLACE_DETAILS)!!
        }

        setupActionBar(myLocationDetails.name)
    }

    private fun setupActionBar(title: String) {
        val mapToolbar : Toolbar = findViewById(R.id.toolbar_map)
        setSupportActionBar(mapToolbar)

        val actionBar = supportActionBar
        if(actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.ic_white_arrow_back_24)
            actionBar.title = title
        }
        mapToolbar.setNavigationOnClickListener { onBackPressed() }

        val supportMapFragment: SupportMapFragment =
            supportFragmentManager.findFragmentById(R.id.map_fragment) as SupportMapFragment
        supportMapFragment.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        val position = LatLng(myLocationDetails.latitude, myLocationDetails.longitude)
        googleMap.addMarker(MarkerOptions().position(position).title(myLocationDetails.name))
        val newLatLngZoom = CameraUpdateFactory.newLatLngZoom(position,15f)
        googleMap.animateCamera(newLatLngZoom)
    }
}