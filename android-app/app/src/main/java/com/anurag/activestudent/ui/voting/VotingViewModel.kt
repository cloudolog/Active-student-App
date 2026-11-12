package com.anurag.activestudent.ui.voting

import androidx.lifecycle.ViewModel
import com.anurag.activestudent.data.Candidate
import com.anurag.activestudent.data.ElectionRoom
import com.anurag.activestudent.data.FirebaseRepo
import com.anurag.activestudent.data.UserProfile
import com.anurag.activestudent.data.UserVote
import com.anurag.activestudent.data.safeInt
import com.anurag.activestudent.data.safeString
import com.anurag.activestudent.data.safeTimeMillis
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class VotingUiState(
    val isLoading: Boolean = true,
    val isActionBusy: Boolean = false,
    val isAdmin: Boolean = false,
    val selectedTab: Int = 0, // 0 = Active Polls, 1 = Results (for students); Admin sees All Rooms
    val allRooms: List<ElectionRoom> = emptyList(),
    val selectedRoom: ElectionRoom? = null,
    val candidates: List<Candidate> = emptyList(),
    val userVote: UserVote? = null,
    val errorMessage: String? = null,
    val successMessage: String? = null
)

class VotingViewModel : ViewModel() {
    private val db = FirebaseRepo.db

    private val _uiState = MutableStateFlow(VotingUiState())
    val uiState: StateFlow<VotingUiState> = _uiState.asStateFlow()

    private var roomsListener: ListenerRegistration? = null
    private var roomDocListener: ListenerRegistration? = null
    private var candidatesListener: ListenerRegistration? = null
    private var userVoteListener: ListenerRegistration? = null
    private var currentUid: String? = null

    fun bindUser(user: UserProfile) {
        val admin = FirebaseRepo.isVotingAdmin(user.email)
        if (currentUid == user.uid && roomsListener != null) {
            _uiState.update { it.copy(isAdmin = admin) }
            return
        }
        clearListeners()
        currentUid = user.uid
        _uiState.update { it.copy(isAdmin = admin, isLoading = true) }

        roomsListener = db.collection("elections")
            .addSnapshotListener { snap, err ->
                if (err != null) {
                    _uiState.update {
                        it.copy(isLoading = false, errorMessage = err.message ?: "Failed to load elections.")
                    }
                    return@addSnapshotListener
                }
                val rooms = snap?.documents.orEmpty()
                    .map { it.toElectionRoom() }
                    .sortedByDescending { it.createdAtMillis }
                _uiState.update { state ->
                    val updatedSelected = state.selectedRoom?.let { sel ->
                        rooms.find { it.id == sel.id } ?: sel
                    }
                    state.copy(isLoading = false, allRooms = rooms, selectedRoom = updatedSelected)
                }
            }
    }

    fun setTab(tab: Int) {
        _uiState.update { it.copy(selectedTab = tab, errorMessage = null, successMessage = null) }
    }

    fun openRoom(room: ElectionRoom) {
        stopRoomListeners()
        _uiState.update {
            it.copy(
                selectedRoom = room,
                candidates = emptyList(),
                userVote = null,
                errorMessage = null,
                successMessage = null
            )
        }

        roomDocListener = db.collection("elections").document(room.id)
            .addSnapshotListener { snap, _ ->
                if (snap != null && snap.exists()) {
                    _uiState.update { it.copy(selectedRoom = snap.toElectionRoom()) }
                }
            }

        candidatesListener = db.collection("candidates")
            .whereEqualTo("electionId", room.id)
            .addSnapshotListener { snap, err ->
                if (err != null) {
                    _uiState.update { it.copy(errorMessage = err.message) }
                    return@addSnapshotListener
                }
                val list = snap?.documents.orEmpty().map { doc ->
                    Candidate(
                        id = doc.id,
                        electionId = doc.safeString("electionId"),
                        name = doc.safeString("name"),
                        position = doc.safeString("position"),
                        motto = doc.safeString("motto"),
                        img = doc.safeString("img"),
                        voteCount = doc.safeInt("voteCount")
                    )
                }.sortedByDescending { it.voteCount }
                _uiState.update { it.copy(candidates = list) }
            }

        val uid = currentUid
        if (!uid.isNullOrBlank()) {
            userVoteListener = db.collection("users").document(uid)
                .collection("votes").document(room.id)
                .addSnapshotListener { snap, _ ->
                    if (snap != null && snap.exists()) {
                        _uiState.update {
                            it.copy(
                                userVote = UserVote(
                                    electionId = room.id,
                                    candidateId = snap.safeString("candidateId"),
                                    candidateName = snap.safeString("candidateName"),
                                    votedAtMillis = snap.safeTimeMillis("votedAt")
                                )
                            )
                        }
                    } else {
                        _uiState.update { it.copy(userVote = null) }
                    }
                }
        }
    }

    fun exitRoom() {
        stopRoomListeners()
        _uiState.update {
            it.copy(
                selectedRoom = null,
                candidates = emptyList(),
                userVote = null,
                errorMessage = null,
                successMessage = null
            )
        }
    }

    fun castVote(candidate: Candidate, electionId: String) {
        val uid = currentUid ?: return
        _uiState.update { it.copy(isActionBusy = true, errorMessage = null, successMessage = null) }

        val voteRef = db.collection("users").document(uid).collection("votes").document(electionId)
        val candidateRef = db.collection("candidates").document(candidate.id)

        db.runTransaction { tx ->
            val existingVote = tx.get(voteRef)
            if (existingVote.exists()) {
                throw IllegalStateException("You have already voted in this election!")
            }
            val candSnap = tx.get(candidateRef)
            if (!candSnap.exists()) {
                throw IllegalStateException("Candidate no longer exists.")
            }
            val currentVotes = candSnap.safeInt("voteCount")
            tx.update(candidateRef, "voteCount", currentVotes + 1)
            tx.set(
                voteRef,
                mapOf(
                    "votedAt" to Timestamp.now(),
                    "candidateId" to candidate.id,
                    "candidateName" to candidate.name
                )
            )
        }.addOnSuccessListener {
            _uiState.update {
                it.copy(
                    isActionBusy = false,
                    successMessage = "Vote recorded for ${candidate.name}!"
                )
            }
        }.addOnFailureListener { ex ->
            _uiState.update {
                it.copy(
                    isActionBusy = false,
                    errorMessage = ex.message ?: "Failed to submit vote."
                )
            }
        }
    }

    // --- ADMIN ACTIONS ---

    fun createElectionRoom(title: String, isPrivate: Boolean, secretKey: String) {
        val cleanTitle = title.trim()
        val cleanKey = secretKey.trim()
        if (cleanTitle.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Please enter an election title.") }
            return
        }
        if (isPrivate && cleanKey.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Secret Access Code is required for private rooms.") }
            return
        }

        _uiState.update { it.copy(isActionBusy = true, errorMessage = null) }
        db.collection("elections")
            .add(
                mapOf(
                    "electionTitle" to cleanTitle,
                    "status" to "not_started",
                    "createdAt" to Timestamp.now(),
                    "noticeMessage" to "Voting starts soon.",
                    "startTime" to "",
                    "endTime" to "",
                    "roomType" to if (isPrivate) "private" else "public",
                    "secretKey" to if (isPrivate) cleanKey else ""
                )
            )
            .addOnSuccessListener {
                _uiState.update {
                    it.copy(isActionBusy = false, successMessage = "Room '$cleanTitle' created!")
                }
            }
            .addOnFailureListener { ex ->
                _uiState.update {
                    it.copy(isActionBusy = false, errorMessage = ex.message ?: "Failed to create room.")
                }
            }
    }

    fun deleteElectionRoom(roomId: String) {
        _uiState.update { it.copy(isActionBusy = true, errorMessage = null) }
        db.collection("candidates").whereEqualTo("electionId", roomId).get()
            .addOnSuccessListener { candSnap ->
                val batch = db.batch()
                candSnap.documents.forEach { batch.delete(it.reference) }
                batch.delete(db.collection("elections").document(roomId))
                batch.commit()
                    .addOnSuccessListener {
                        if (_uiState.value.selectedRoom?.id == roomId) {
                            exitRoom()
                        }
                        _uiState.update {
                            it.copy(isActionBusy = false, successMessage = "Election room deleted.")
                        }
                    }
                    .addOnFailureListener { ex ->
                        _uiState.update { it.copy(isActionBusy = false, errorMessage = ex.message) }
                    }
            }
            .addOnFailureListener { ex ->
                _uiState.update { it.copy(isActionBusy = false, errorMessage = ex.message) }
            }
    }

    fun updateElectionStatus(roomId: String, newStatus: String) {
        _uiState.update { it.copy(isActionBusy = true, errorMessage = null) }
        db.collection("elections").document(roomId)
            .set(mapOf("status" to newStatus), SetOptions.merge())
            .addOnSuccessListener {
                _uiState.update {
                    it.copy(
                        isActionBusy = false,
                        successMessage = "Election status updated to ${newStatus.uppercase()}."
                    )
                }
            }
            .addOnFailureListener { ex ->
                _uiState.update { it.copy(isActionBusy = false, errorMessage = ex.message) }
            }
    }

    fun updateNotice(roomId: String, notice: String) {
        _uiState.update { it.copy(isActionBusy = true, errorMessage = null) }
        db.collection("elections").document(roomId)
            .set(mapOf("noticeMessage" to notice.trim()), SetOptions.merge())
            .addOnSuccessListener {
                _uiState.update { it.copy(isActionBusy = false, successMessage = "Notice updated!") }
            }
            .addOnFailureListener { ex ->
                _uiState.update { it.copy(isActionBusy = false, errorMessage = ex.message) }
            }
    }

    fun updateRoomSettings(
        roomId: String,
        title: String,
        startTime: String,
        endTime: String,
        secretKey: String
    ) {
        val cleanTitle = title.trim()
        if (cleanTitle.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Election title cannot be empty.") }
            return
        }
        val cleanSecret = secretKey.trim()
        _uiState.update { it.copy(isActionBusy = true, errorMessage = null) }
        db.collection("elections").document(roomId)
            .set(
                mapOf(
                    "electionTitle" to cleanTitle,
                    "startTime" to startTime.trim(),
                    "endTime" to endTime.trim(),
                    "secretKey" to cleanSecret,
                    "roomType" to if (cleanSecret.isNotBlank()) "private" else "public"
                ),
                SetOptions.merge()
            )
            .addOnSuccessListener {
                _uiState.update { it.copy(isActionBusy = false, successMessage = "Settings saved!") }
            }
            .addOnFailureListener { ex ->
                _uiState.update { it.copy(isActionBusy = false, errorMessage = ex.message) }
            }
    }

    fun saveCandidate(
        electionId: String,
        editingCandidateId: String?,
        name: String,
        position: String,
        motto: String,
        img: String
    ) {
        if (name.isBlank() || position.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Candidate name and position are required.") }
            return
        }
        _uiState.update { it.copy(isActionBusy = true, errorMessage = null) }
        if (!editingCandidateId.isNullOrBlank()) {
            db.collection("candidates").document(editingCandidateId)
                .set(
                    mapOf(
                        "name" to name.trim(),
                        "position" to position.trim(),
                        "motto" to motto.trim(),
                        "img" to img.trim()
                    ),
                    SetOptions.merge()
                )
                .addOnSuccessListener {
                    _uiState.update { it.copy(isActionBusy = false, successMessage = "Candidate updated!") }
                }
                .addOnFailureListener { ex ->
                    _uiState.update { it.copy(isActionBusy = false, errorMessage = ex.message) }
                }
        } else {
            db.collection("candidates")
                .add(
                    mapOf(
                        "electionId" to electionId,
                        "name" to name.trim(),
                        "position" to position.trim(),
                        "motto" to motto.trim(),
                        "img" to img.trim(),
                        "voteCount" to 0
                    )
                )
                .addOnSuccessListener {
                    _uiState.update { it.copy(isActionBusy = false, successMessage = "Candidate added!") }
                }
                .addOnFailureListener { ex ->
                    _uiState.update { it.copy(isActionBusy = false, errorMessage = ex.message) }
                }
        }
    }

    fun deleteCandidate(candidateId: String) {
        _uiState.update { it.copy(isActionBusy = true, errorMessage = null) }
        db.collection("candidates").document(candidateId).delete()
            .addOnSuccessListener {
                _uiState.update { it.copy(isActionBusy = false, successMessage = "Candidate removed.") }
            }
            .addOnFailureListener { ex ->
                _uiState.update { it.copy(isActionBusy = false, errorMessage = ex.message) }
            }
    }

    fun clearMessages() {
        _uiState.update { it.copy(errorMessage = null, successMessage = null) }
    }

    private fun stopRoomListeners() {
        roomDocListener?.remove()
        candidatesListener?.remove()
        userVoteListener?.remove()
        roomDocListener = null
        candidatesListener = null
        userVoteListener = null
    }

    fun clearListeners() {
        roomsListener?.remove()
        roomsListener = null
        stopRoomListeners()
        currentUid = null
    }

    override fun onCleared() {
        super.onCleared()
        clearListeners()
    }

    private fun DocumentSnapshot.toElectionRoom(): ElectionRoom {
        return ElectionRoom(
            id = id,
            title = safeString("electionTitle").ifBlank { safeString("name", "Untitled") },
            status = safeString("status", "not_started"),
            roomType = safeString("roomType", "public"),
            secretKey = safeString("secretKey"),
            noticeMessage = safeString("noticeMessage"),
            startTime = safeString("startTime"),
            endTime = safeString("endTime"),
            createdAtMillis = safeTimeMillis("createdAt")
        )
    }
}

