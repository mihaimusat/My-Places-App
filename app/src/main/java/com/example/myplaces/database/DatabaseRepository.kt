package com.example.myplaces.database

import android.app.Activity
import android.util.Log
import android.widget.Toast
import com.example.myplaces.activities.*
import com.example.myplaces.models.Location
import com.example.myplaces.models.User
import com.example.myplaces.utils.Constants
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions

class DatabaseRepository {

    private val myDatabaseRepo = FirebaseFirestore.getInstance()

    fun registerUser(activity: SignUpActivity, userInfo: User) {
        myDatabaseRepo.collection(Constants.USERS)
            .document(getCurrentUserId())
            .set(userInfo, SetOptions.merge())
            .addOnSuccessListener {
                activity.userRegisteredSuccess()
            }
            .addOnFailureListener { e ->
                Log.e("error", "Register error")
            }
    }

    fun loadUserData(activity: Activity, readLocationsList: Boolean = false) {
        myDatabaseRepo.collection(Constants.USERS)
            .document(getCurrentUserId())
            .get()
            .addOnSuccessListener { document ->
                val loggedInUser = document.toObject(User::class.java)!!

                when(activity) {
                    is LoginActivity -> {
                        activity.userLoginSuccess(loggedInUser)
                    }
                    is MainActivity -> {
                        activity.updateNavigationUserDetails(loggedInUser, readLocationsList)
                    }
                    is ProfileActivity -> {
                        activity.setUserData(loggedInUser)
                    }
                }

            }
            .addOnFailureListener { e ->
                    when(activity) {
                        is LoginActivity -> {
                            activity.hideProgressDialog()
                        }
                        is MainActivity -> {
                            activity.hideProgressDialog()
                        }
                    }
                    Log.e("error", "Login error")
            }
    }

    fun getCurrentUserId() : String {
        var currentUser = FirebaseAuth.getInstance().currentUser
        var currentUserId = ""
        if(currentUser != null) {
            currentUserId = currentUser.uid
        }
        return currentUserId
    }

    fun updateUserProfileData(activity: ProfileActivity, userHashMap: HashMap<String,Any>) {
        myDatabaseRepo.collection(Constants.USERS)
            .document(getCurrentUserId())
            .update(userHashMap)
            .addOnSuccessListener {
                Log.i(activity.javaClass.simpleName,"Profile data updated successfully!")
                Toast.makeText(activity, "Profile updated successfully!", Toast.LENGTH_SHORT).show()
                activity.profileUpdateSuccess()
            }.addOnFailureListener { e ->
                activity.hideProgressDialog()
                Toast.makeText(activity, "Error when updating the profile!", Toast.LENGTH_SHORT).show()
            }
    }

    fun addLocation(activity: AddLocationActivity, location: Location) {
        myDatabaseRepo.collection(Constants.LOCATIONS)
            .document()
            .set(location, SetOptions.merge())
            .addOnSuccessListener {
                Log.i(activity.javaClass.simpleName,"Location was successfully added!")
                Toast.makeText(activity, "Location added successfully!", Toast.LENGTH_SHORT).show()
                activity.locationCreatedSuccessfully()
            }
            .addOnFailureListener { e ->
                activity.hideProgressDialog()
                Toast.makeText(activity, "Error when adding the location!", Toast.LENGTH_SHORT).show()
            }
    }

    fun getLocationsList(activity: MainActivity) {
        myDatabaseRepo.collection(Constants.LOCATIONS)
            .whereEqualTo(Constants.CREATED_BY, getCurrentUserId())
            .get()
            .addOnSuccessListener {
                    document ->
                Log.i(activity.javaClass.simpleName, document.documents.toString())
                val locationList: ArrayList<Location> = ArrayList()
                for (i in document.documents) {
                    val location = i.toObject(Location::class.java)!!
                    location.id = i.id
                    locationList.add(location)
                }
                activity.populateLocationsList(locationList)
            }.addOnFailureListener { e ->
                activity.hideProgressDialog()
                Log.e(activity.javaClass.simpleName, "Error while creating locations list", e)
            }
    }

    fun getLocationDetails(activity: LocationDetailsActivity, locationId: String) {
        myDatabaseRepo.collection(Constants.LOCATIONS)
            .document(locationId)
            .get()
            .addOnSuccessListener {
                    document -> Log.i(activity.javaClass.simpleName, document.toString())
                activity.loadLocationDetails(document.toObject(Location::class.java)!!)
            }.addOnFailureListener { e ->
                activity.hideProgressDialog()
                Log.e(activity.javaClass.simpleName, "Error while creating locations list", e)
            }
    }

}