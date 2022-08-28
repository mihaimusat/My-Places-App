package com.example.myplaces.activities

import android.app.Activity
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.WindowInsets
import android.view.WindowManager
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.core.view.children
import androidx.drawerlayout.widget.DrawerLayout
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.signature.ObjectKey
import com.example.myplaces.R
import com.example.myplaces.database.DatabaseRepository
import com.example.myplaces.models.User
import com.example.myplaces.utils.Constants
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import de.hdodenhof.circleimageview.CircleImageView

@Suppress("DEPRECATION")
class MainActivity : BaseActivity(), NavigationView.OnNavigationItemSelectedListener {

    companion object {
        const val MY_PROFILE_REQUEST_CODE : Int = 11
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

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

        val navigationView : NavigationView = findViewById(R.id.nav_view)
        navigationView.setNavigationItemSelectedListener(this)

        DatabaseRepository().loadUserData(this)
    }

    private fun setupActionBar() {
        val mainToolbar: Toolbar = findViewById(R.id.toolbar_main_activity)

        mainToolbar.setNavigationIcon(R.drawable.ic_action_navigation_menu)
        mainToolbar.setNavigationOnClickListener {
            toggleDrawer()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(resultCode == Activity.RESULT_OK && requestCode == MY_PROFILE_REQUEST_CODE) {
            DatabaseRepository().loadUserData(this)
        } else {
            Log.e("Cancelled","Cancelled")
        }
    }

    private fun toggleDrawer() {
        val drawerLayout : DrawerLayout = findViewById(R.id.drawer_layout)
        if(drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            drawerLayout.openDrawer(GravityCompat.START)
        }
    }

    override fun onBackPressed() {
        val drawerLayout : DrawerLayout = findViewById(R.id.drawer_layout)
        if(drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            doubleBackToExit()
        }
    }

    fun updateNavigationUserDetails(user: User) {
        val headerView : LinearLayout = findViewById(R.id.nav_header_main)
        val profileImage = headerView.children.find {
            it is CircleImageView
        } as? CircleImageView ?: return
        val profileUsername : TextView = findViewById(R.id.tv_username)

        Glide
            .with(this)
            .load(user.image)
            .centerCrop()
            .placeholder(R.drawable.ic_user_place_holder)
            .into(profileImage);

        profileUsername.text = user.name

    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        val drawerLayout : DrawerLayout = findViewById(R.id.drawer_layout)
        when (item.itemId) {
            R.id.nav_my_profile -> {
                startActivityForResult(Intent(this, ProfileActivity::class.java), MY_PROFILE_REQUEST_CODE)
            }
            R.id.nav_sign_out -> {
                FirebaseAuth.getInstance().signOut()
                val intent = Intent(this, IntroActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
                finish()
            }
        }
        drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }
}