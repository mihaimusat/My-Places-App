package com.example.myplaces.database

import android.app.Activity
import android.util.Log
import android.widget.Toast
import com.example.myplaces.activities.LoginActivity
import com.example.myplaces.activities.MainActivity
import com.example.myplaces.activities.ProfileActivity
import com.example.myplaces.activities.SignUpActivity
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

    fun loadUserData(activity: Activity) {
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
                        activity.updateNavigationUserDetails(loggedInUser)
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


}