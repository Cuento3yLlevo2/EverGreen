package com.mahi.evergreen.network

import android.util.Log
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.mahi.evergreen.model.User

const val USERS_REFERENCE = "/users"

class FirebaseDatabaseService {
    val database = Firebase.database("https://evergreen-app-bdbc2-default-rtdb.europe-west1.firebasedatabase.app")

    init {
        // database.setPersistenceEnabled(true)
    }

    fun getDatabaseUsersReference(): DatabaseReference {
        database.getReference(USERS_REFERENCE)
        return database.getReference(USERS_REFERENCE)
    }

    fun getUsersExcludingCurrent(firebaseUser: FirebaseUser?, callback: Callback<List<User>>){
        database.getReference(USERS_REFERENCE)
                .get()
                .addOnSuccessListener { result ->
                    var userList = ArrayList<User>()
                    for (child in result.children) {
                        Log.d("Data reading success", "${child.key} => ${child.value}")
                        if (firebaseUser != null) {
                            if (child.key?.equals(firebaseUser.uid) == false) {
                                child.getValue(User::class.java)?.let { userList.add(it) }
                            }
                        }
                    }
                    callback.onSuccess(userList)
                }
                .addOnFailureListener { exception ->
                    Log.w("Data reading failure", "Error getting documents.", exception)
                }
    }

    fun getUsersQuery(keyword: String, firebaseUser: FirebaseUser?, callback: Callback<List<User>>) {
        database.getReference(USERS_REFERENCE)
                .orderByChild("search")
                .startAt(keyword)
                .endAt(keyword + "\uf8ff")
                .get()
                .addOnSuccessListener { result ->
                    var userList = ArrayList<User>()
                    for (child in result.children) {
                        Log.d("Data reading success", "${child.key} => ${child.value}")
                        if (firebaseUser != null) {
                            if (child.key?.equals(firebaseUser.uid) == false) {
                                child.getValue(User::class.java)?.let { userList.add(it) }
                            }
                        }
                    }
                    callback.onSuccess(userList)
                }
                .addOnFailureListener { exception ->
                    Log.w("Data reading failure", "Error getting documents.", exception)
                }
    }


}