package com.example.myplaces.activities

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.text.TextUtils
import android.util.Log
import android.view.WindowInsets
import android.view.WindowManager
import android.webkit.MimeTypeMap
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.example.myplaces.R
import com.example.myplaces.database.DatabaseRepository
import com.example.myplaces.models.User
import com.example.myplaces.utils.Constants
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import de.hdodenhof.circleimageview.CircleImageView
import java.io.IOException

@Suppress("DEPRECATION")
class ProfileActivity : BaseActivity() {

    companion object {
        private const val READ_STORAGE_PERMISSION_CODE = 1
        private const val PICK_IMAGE_REQUEST_CODE = 2
    }

    private var mySelectedImageFileUri : Uri? = null
    private var myProfileImageURL: String = ""
    private lateinit var myUserDetails: User
    private lateinit var galleryImageResultLauncher: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

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

        DatabaseRepository().loadUserData(this)

        val profileImage : CircleImageView = findViewById(R.id.iv_user_image)
        profileImage.setOnClickListener {
            if(ContextCompat.checkSelfPermission
                    (this, Manifest.permission.READ_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED) {
                showImageChooser()
            } else {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                    READ_STORAGE_PERMISSION_CODE
                )
            }
        }

        val updateButton : Button = findViewById(R.id.btn_update)
        updateButton.setOnClickListener {
            if(mySelectedImageFileUri != null) {
                uploadUserImage()
            } else {
                showProgressDialog(resources.getString(R.string.please_wait))
                updateUserProfileData()
            }
        }
    }

    private fun setupActionBar() {
        val profileToolbar: Toolbar = findViewById(R.id.toolbar_my_profile_activity)
        setSupportActionBar(profileToolbar)

        val actionBar = supportActionBar
        if(actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.ic_white_arrow_back_24)
            actionBar.title = resources.getString(R.string.my_profile_title)
        }

        profileToolbar.setNavigationOnClickListener { onBackPressed() }
    }

    fun setUserData(user: User) {
        myUserDetails = user
        val userImage: CircleImageView = findViewById(R.id.iv_user_image)
        val username: TextView = findViewById(R.id.et_name)
        val email: TextView = findViewById(R.id.et_email)

        Glide
            .with(this@ProfileActivity)
            .load(user.image)
            .centerCrop()
            .placeholder(R.drawable.ic_user_place_holder)
            .into(userImage);

        username.text = user.name
        email.text = user.email

    }

    private fun showImageChooser() {
        val galleryIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(galleryIntent, PICK_IMAGE_REQUEST_CODE)
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        val userImage: CircleImageView = findViewById(R.id.iv_user_image)

        if(resultCode == Activity.RESULT_OK
            && requestCode == PICK_IMAGE_REQUEST_CODE
            && data!!.data != null) {
            mySelectedImageFileUri = data.data

            try {
                Glide
                    .with(this@ProfileActivity)
                    .load(mySelectedImageFileUri)
                    .centerCrop()
                    .placeholder(R.drawable.ic_user_place_holder)
                    .into(userImage);
            } catch(e: IOException) {
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
        if(requestCode == READ_STORAGE_PERMISSION_CODE) {
            if(grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                showImageChooser()
            }
        } else {
            Toast.makeText(
                this,
                "Oops, you just denied permission for storage. You can also allow it from settings.", Toast.LENGTH_LONG)
                .show()
        }
    }

    private fun getFileExtension(uri: Uri?) : String? {
        return MimeTypeMap.getSingleton().getExtensionFromMimeType(contentResolver.getType(uri!!))
    }

    private fun uploadUserImage() {
        showProgressDialog(resources.getString(R.string.please_wait))

        if (mySelectedImageFileUri != null) {
            val storageRef: StorageReference = FirebaseStorage.getInstance().reference
                .child(
                    "USER_IMAGE" + System.currentTimeMillis()
                            + "." + getFileExtension(mySelectedImageFileUri)
                )
            storageRef.putFile(mySelectedImageFileUri!!).addOnSuccessListener { taskSnapshot ->
                Log.i(
                    "Firebase Image URL",
                    taskSnapshot.metadata!!.reference!!.downloadUrl.toString()
                )
                taskSnapshot.metadata!!.reference!!.downloadUrl.addOnSuccessListener { uri ->
                    Log.i("Downloadable Image URL", uri.toString())
                    myProfileImageURL = uri.toString()

                    updateUserProfileData()

                }.addOnFailureListener { exception ->
                    Toast.makeText(this,
                        exception.message,
                        Toast.LENGTH_LONG).show()
                    hideProgressDialog()
                }
            }
        }
    }

    private fun updateUserProfileData() {
        val username: TextView = findViewById(R.id.et_name)

        val userHashMap = HashMap<String, Any>()


        if (myProfileImageURL.isNotEmpty() && myProfileImageURL != myUserDetails.image) {
            userHashMap[Constants.IMAGE] = myProfileImageURL
        }
        if (username.text.toString() != myUserDetails.name) {
            userHashMap[Constants.NAME] = username.text.toString()
        }
        DatabaseRepository().updateUserProfileData(this, userHashMap)
    }

    fun profileUpdateSuccess() {
        hideProgressDialog()
        setResult(Activity.RESULT_OK)
        finish()
    }

}