package com.example.myapplication.login

import android.content.Context
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore

object AuthManager {

    private val auth: FirebaseAuth
        get() = FirebaseAuth.getInstance()

    //
    val currentUser: FirebaseUser?
        get() = auth.currentUser

    fun isLoggedIn(): Boolean = auth.currentUser != null

    // Create account (test)
    fun signUp(email: String, password: String, name: String, result: (Boolean, String?) -> Unit) {
        // Stop BEFORE the account is created if there is no name
        if (name.isBlank()) {
            result(false, "Name is required")
            return
        }

        auth.createUserWithEmailAndPassword(email.trim(), password)
            .addOnSuccessListener { authResult ->
                val user = authResult.user
                if (user == null) {
                    result(false, "Unknown error")
                    return@addOnSuccessListener
                }
                saveUser(FirebaseFirestore.getInstance(), user.uid, name, email) { pass, error ->
                    if (pass) {
                        user.sendEmailVerification()
                        result(true, user.uid)
                    } else {
                        // Delete the account if any issue uploading to firebase occur
                        user.delete()
                        result(false, error)
                    }
                }
            }
            .addOnFailureListener { e -> result(false, e.message) }
    }

    // Create another account when logged in (admin)
    fun createUser(
        context: Context,
        email: String,
        password: String,
        name: String,
        result: (Boolean, String?) -> Unit
    ) {
        // Stop BEFORE the account is created if there is no name
        if (name.isBlank()) {
            result(false, "Name is required")
            return
        }

        val firebase = try {
            FirebaseApp.getInstance("NFirebase")
        } catch (e: IllegalStateException) {
            FirebaseApp.initializeApp(context, FirebaseApp.getInstance().options, "NFirebase")
        }
        val NFirebase = FirebaseAuth.getInstance(firebase)

        NFirebase.createUserWithEmailAndPassword(email.trim(), password)
            .addOnSuccessListener { authResult ->
                val newUser = authResult.user
                if (newUser == null) {
                    result(false, "Unknown error")
                    return@addOnSuccessListener
                }
                // Saved through the secondary connection, while it is logged in as the new user
                saveUser(FirebaseFirestore.getInstance(firebase), newUser.uid, name, email) { ok, error ->
                    if (ok) {
                        NFirebase.signOut()   // you stay logged in
                        result(true, newUser.uid)
                    } else {
                        // Name could not be saved: remove the account so the email isn't left registered
                        newUser.delete().addOnCompleteListener {
                            NFirebase.signOut()
                            result(false, error)
                        }
                    }
                }
            }
            .addOnFailureListener { e -> result(false, e.message) }
    }

    fun logIn(email: String, password: String, result: (Boolean, String?) -> Unit) {
        auth.signInWithEmailAndPassword(email.trim(), password)
            .addOnSuccessListener { result(true, null) }
            .addOnFailureListener { e -> result(false, e.message) }
    }

    fun logOut() {
        auth.signOut()
    }

    fun resetPassword(email: String, result: (Boolean, String?) -> Unit) {
        auth.sendPasswordResetEmail(email.trim())
            .addOnSuccessListener { result(true, null) }
            .addOnFailureListener { e -> result(false, e.message) }
    }

    private fun saveUser(
        db: FirebaseFirestore,
        uid: String,
        name: String,
        email: String,
        onDone: (Boolean, String?) -> Unit
    ) {
        val data = linkedMapOf(
            "name" to name.trim(),
            "email" to email.trim()
        )

        db.collection("users").document(uid)
            .set(data)
            .addOnSuccessListener { onDone(true, null) }
            .addOnFailureListener { e -> onDone(false, e.message) }
    }
}
