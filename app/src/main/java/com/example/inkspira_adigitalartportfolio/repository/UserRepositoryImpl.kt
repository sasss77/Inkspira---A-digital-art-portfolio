package com.example.inkspira_adigitalartportfolio.repository

import com.example.inkspira_adigitalartportfolio.model.UserModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.FirebaseDatabase

class UserRepositoryImpl : UserRepository {



    val auth : FirebaseAuth = FirebaseAuth.getInstance()
    val database : FirebaseDatabase = FirebaseDatabase.getInstance()


    override fun login(
        email: String,
        password: String,
        callBack: (Boolean, String) -> Unit
    ) {
        TODO("Not yet implemented")
    }

    override fun register(
        email: String,
        password: String,
        callback: (Boolean, String, String) -> Unit
    ) {
        TODO("Not yet implemented")
    }

    override fun forgetPassword(
        email: String,
        callback: (Boolean, String) -> Unit
    ) {
        TODO("Not yet implemented")
    }

    override fun getCurrentUser(): FirebaseUser? {
        TODO("Not yet implemented")
    }

    override fun addUserToDatabase(
        userID: String,
        model: UserModel,
        callback: (Boolean, String) -> Unit
    ) {
        TODO("Not yet implemented")
    }

    override fun logout(callback: (Boolean, String) -> Unit) {
        TODO("Not yet implemented")
    }

    override fun getUserByID(
        userID: String,
        callback: (UserModel?, Boolean, String) -> Unit
    ) {
        TODO("Not yet implemented")
    }

    override fun updateProfile(
        userID: String,
        userData: MutableMap<String, Any?>,
        callback: (Boolean, String) -> Unit
    ) {
        TODO("Not yet implemented")
    }
}