package com.mahi.evergreen.viewmodel


import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseUser

import com.mahi.evergreen.network.DatabaseService


class ProfileViewModel : ViewModel() {
    val firestoreService = DatabaseService()
    private var firebaseUser: FirebaseUser? = null

}