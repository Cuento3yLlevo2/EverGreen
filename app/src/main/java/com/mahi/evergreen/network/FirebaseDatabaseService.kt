package com.mahi.evergreen.network

import android.util.Log
import com.google.firebase.database.GenericTypeIndicator
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.mahi.evergreen.model.User

const val USERS_REFERENCE = "/users"

class FirebaseDatabaseService {
    val database = Firebase.database("https://evergreen-app-bdbc2-default-rtdb.europe-west1.firebasedatabase.app")

    init {
        database.setPersistenceEnabled(true)
    }

    fun getUsers(callback: com.mahi.evergreen.network.Callback<List<User>>){
        database.getReference(USERS_REFERENCE)
                .get()
                .addOnSuccessListener { result ->
                    for (child in result.children) {
                        Log.d("Data reading success", "${child.key} => ${child.value}")
                        val t = object : GenericTypeIndicator<List<User>>() {}
                        val list = result.getValue(t)
                        callback.onSuccess(list)
                        break
                    }
                }
                .addOnFailureListener { exception ->
                    Log.w("Data reading failure", "Error getting documents.", exception)
                }
    }

}