package com.example.myapplication.login

import android.content.Context
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.example.myapplication.Role

object AuthManager {

    private val auth: FirebaseAuth
        get() = FirebaseAuth.getInstance()

    val currentUser: FirebaseUser?
        get() = auth.currentUser

    fun isLoggedIn(): Boolean {
        return auth.currentUser != null
    }

    fun signUp(email: String, password: String, name: String, result: (Boolean, String?) -> Unit) {

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
                saveUser(
                    db = FirebaseFirestore.getInstance(),
                    uid = user.uid,
                    name = name,
                    email = email,
                    scpdPoints = 0,
                    totalEvents = 0,
                    eventReg = emptyList(),
                    eventJoined = emptyList()
                ) { pass, error ->
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
        clubId: String? = null,
        clubIdMember: List<String> = emptyList(),
        school: String? = null,
        course: String? = null,
        year: Int? = null,
        result: (Boolean, String?) -> Unit
    ) {
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
                saveUser(
                    db = FirebaseFirestore.getInstance(firebase),
                    uid = newUser.uid,
                    name = name,
                    email = email,
                    clubId = clubId,
                    clubIdMember = clubIdMember,
                    school = school,
                    course = course,
                    year = year,
                    scpdPoints = 0,
                    totalEvents = 0,
                    eventReg = emptyList(),
                    eventJoined = emptyList()
                ) { ok, error ->
                    if (ok) {
                        NFirebase.signOut()   // admin is still logged in
                        result(true, newUser.uid)
                    } else {
                        // If name isn't saved account deleted
                        newUser.delete().addOnCompleteListener {
                            NFirebase.signOut()
                            result(false, error)
                        }
                    }
                }
            }
            .addOnFailureListener { e -> result(false, e.message) }
    }

    fun logIn(role: Role, email: String, password: String, result: (Boolean, String?) -> Unit) {

        auth.signInWithEmailAndPassword(email.trim(), password)
            .addOnSuccessListener { authResult ->

                // Student log in
                if (role != Role.COMMITTEE) {
                    result(true, null)
                    return@addOnSuccessListener
                }

                // committee login
                val authUser = authResult.user
                if (authUser == null) {
                    auth.signOut()
                    result(false, "Login failed")
                    return@addOnSuccessListener
                }
                val uid = authUser.uid
                val db = FirebaseFirestore.getInstance()

                db.collection("users").document(uid)
                    .get()
                    .addOnSuccessListener { user ->
                        val clubId = user.getString("club")
                        if (clubId.isNullOrBlank()) {
                            auth.signOut()
                            result(false, "You are not a committee of any club")
                            return@addOnSuccessListener
                        }

                        db.collection("clubs").document(clubId)
                            .get()
                            .addOnSuccessListener { club ->
                                if (club.getBoolean("isActive") == true) {
                                    result(true, clubId)
                                } else {
                                    auth.signOut()
                                    result(false, "This club is not active")
                                }
                            }
                            .addOnFailureListener { e ->
                                auth.signOut()
                                result(false, e.message)
                            }
                    }
                    .addOnFailureListener { e ->
                        auth.signOut()
                        result(false, e.message)
                    }
            }
            .addOnFailureListener { result(false, "Incorrect Email or Password") }
    }

    fun logOut() {
        auth.signOut()
    }

    fun resetPassword(email: String, result: (Boolean, String?) -> Unit) {
        auth.sendPasswordResetEmail(email.trim())
            .addOnSuccessListener { result(true, null) }
            .addOnFailureListener { e -> result(false, e.message) }
    }

    fun getClubs(result: (List<Pair<String, String>>, String?) -> Unit) {
        FirebaseFirestore.getInstance()
            .collection("clubs")
            .whereEqualTo("isActive", true)
            .get()
            .addOnSuccessListener { snapshot ->
                val clubs = mutableListOf<Pair<String, String>>()
                for (doc in snapshot.documents) {
                    val clubName = doc.getString("name")
                    if (clubName != null) {
                        clubs.add(Pair(doc.id, clubName))
                    }
                }
                clubs.sortBy { club -> club.second }
                result(clubs, null)
            }
            .addOnFailureListener { e -> result(emptyList(), e.message) }
    }

    private fun saveUser(
        db: FirebaseFirestore,
        uid: String,
        name: String,
        email: String,
        clubId: String? = null,
        clubIdMember: List<String> = emptyList(),
        school: String? = null,
        course: String? = null,
        year: Int? = null,
        scpdPoints: Int,
        totalEvents: Int,
        eventReg: List<String>,
        eventJoined: List<String>,
        onDone: (Boolean, String?) -> Unit
    ) {
        val data = HashMap<String, Any>()
        data["name"] = name.trim()
        data["email"] = email.trim()
        if (!clubId.isNullOrBlank()) {
            data["club"] = clubId
        }
        data["clubIdMember"] = clubIdMember
        if (!school.isNullOrBlank()) {
            data["school"] = school       // school code, e.g. MCOMP
        }
        if (!course.isNullOrBlank()) {   // course id, e.g. MCOMP002
            data["course"] = course.trim()
        }
        if (year != null) {
            data["year"] = year
        }
        data["scpd_points"] = scpdPoints
        data["total_events"] = totalEvents
        data["event_reg"] = eventReg
        data["event_joined"] = eventJoined

        db.collection("users").document(uid)
            .set(data)
            .addOnSuccessListener { onDone(true, null) }
            .addOnFailureListener { e -> onDone(false, e.message) }
    }
}
