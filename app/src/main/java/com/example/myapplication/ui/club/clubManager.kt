package com.example.myapplication.ui.clubs

import androidx.lifecycle.ViewModel
import com.example.myapplication.events.Event
import com.example.myapplication.events.EventStatus
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class ClubsViewModel : ViewModel() {

    private val db = FirebaseFirestore.getInstance()
    //UI States
    //All clubs list (for the list screen)
    private val _clubs = MutableStateFlow<List<Club>>(emptyList())
    val clubs: StateFlow<List<Club>> = _clubs

    //Loading indicator state
    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading

    //Error message state
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    //Selected single club (for the details screen)
    private val _selectedClub = MutableStateFlow<Club?>(null)
    val selectedClub: StateFlow<Club?> = _selectedClub

    //Approved events belonging to the selected club
    private val _events = MutableStateFlow<List<Event>>(emptyList())
    val events: StateFlow<List<Event>> = _events

    //Firestore Listeners (for real-time updates)
    private var clubsListener: ListenerRegistration? = null
    private var clubListener: ListenerRegistration? = null
    private var eventsListener: ListenerRegistration? = null

    //Automatically fetch all clubs in real-time when the ViewModel is created
    init {
        clubsListener = db.collection("clubs")
            .orderBy("name")
            .addSnapshotListener { snapshot, e ->
                _isLoading.value = false
                if (e != null) {
                    _error.value = e.message
                    return@addSnapshotListener
                }
                _error.value = null
                _clubs.value = snapshot?.toObjects(Club::class.java) ?: emptyList()
            }
    }
    //Load a specific club's details and its approved events when opening the details page
    fun loadClub(clubId: String) {
        //Clear previous listeners and data before loading a new club
        clubListener?.remove()
        eventsListener?.remove()
        _selectedClub.value = null
        _events.value = emptyList()
        //Fetch single club details
        clubListener = db.collection("clubs").document(clubId)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    _error.value = e.message
                    return@addSnapshotListener
                }
                _selectedClub.value = snapshot?.toObject(Club::class.java)
            }
        //Fetch approved events for this club (ignoring pending, rejected, or canceled ones)
        eventsListener = db.collection("events")
            .whereEqualTo("clubId", clubId)
            .whereEqualTo("status", EventStatus.APPROVED.name)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    _error.value = e.message
                    return@addSnapshotListener
                }
                _events.value = snapshot?.documents
                    ?.mapNotNull { doc -> doc.toObject(Event::class.java)?.copy(id = doc.id) }
                    ?.sortedBy { it.startTime }
                    ?: emptyList()
            }
    }
    //Clean up active Firestore listeners when the ViewModel is destroyed to prevent memory leaks
    override fun onCleared() {
        clubsListener?.remove()
        clubListener?.remove()
        eventsListener?.remove()
        super.onCleared()
    }
}