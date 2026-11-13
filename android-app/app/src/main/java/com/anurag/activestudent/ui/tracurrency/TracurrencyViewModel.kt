package com.anurag.activestudent.ui.tracurrency

import androidx.lifecycle.ViewModel
import com.anurag.activestudent.data.FirebaseRepo
import com.anurag.activestudent.data.LedgerTx
import com.anurag.activestudent.data.TracurrencyEvent
import com.anurag.activestudent.data.UserProfile
import com.anurag.activestudent.data.formatShortDate
import com.anurag.activestudent.data.safeDouble
import com.anurag.activestudent.data.safeString
import com.anurag.activestudent.data.safeTimeMillis
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class TracurrencyUiState(
    val isLoading: Boolean = true,
    val isActionBusy: Boolean = false,
    val isAdmin: Boolean = false,
    val events: List<TracurrencyEvent> = emptyList(),
    val selectedEvent: TracurrencyEvent? = null,
    val credits: List<LedgerTx> = emptyList(),
    val debits: List<LedgerTx> = emptyList(),
    val totalCollected: Double = 0.0,
    val totalSpent: Double = 0.0,
    val liveBalance: Double = 0.0,
    val errorMessage: String? = null,
    val successMessage: String? = null
)

class TracurrencyViewModel : ViewModel() {
    private val db = FirebaseRepo.db

    private val _uiState = MutableStateFlow(TracurrencyUiState())
    val uiState: StateFlow<TracurrencyUiState> = _uiState.asStateFlow()

    private var currentUser: UserProfile? = null
    private var lobbyListener: ListenerRegistration? = null
    private var eventListener: ListenerRegistration? = null
    private var ledgerListener: ListenerRegistration? = null
    private var expenseListener: ListenerRegistration? = null

    private var rawLedgerDocs: List<LedgerTx> = emptyList()
    private var rawExpenseDocs: List<LedgerTx> = emptyList()

    fun bindUser(user: UserProfile) {
        val admin = FirebaseRepo.isTracurrencyAdmin(user.email, user.role)
        currentUser = user
        if (lobbyListener != null) {
            _uiState.update { it.copy(isAdmin = admin) }
            return
        }
        _uiState.update { it.copy(isAdmin = admin, isLoading = true) }

        lobbyListener = db.collection("tracurrency_events")
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snap, err ->
                if (err != null) {
                    _uiState.update {
                        it.copy(isLoading = false, errorMessage = err.message ?: "Failed to load events.")
                    }
                    return@addSnapshotListener
                }
                val list = snap?.documents.orEmpty().map { it.toEvent() }
                _uiState.update { state ->
                    val updated = state.selectedEvent?.let { sel -> list.find { it.id == sel.id } ?: sel }
                    state.copy(isLoading = false, events = list, selectedEvent = updated)
                }
            }
    }

    fun openEvent(event: TracurrencyEvent) {
        stopEventListeners()
        rawLedgerDocs = emptyList()
        rawExpenseDocs = emptyList()
        _uiState.update {
            it.copy(
                selectedEvent = event,
                credits = emptyList(),
                debits = emptyList(),
                totalCollected = 0.0,
                totalSpent = 0.0,
                liveBalance = event.fundsGiven,
                errorMessage = null,
                successMessage = null
            )
        }

        eventListener = db.collection("tracurrency_events").document(event.id)
            .addSnapshotListener { snap, _ ->
                if (snap != null && snap.exists()) {
                    _uiState.update { it.copy(selectedEvent = snap.toEvent()) }
                    recalculate()
                }
            }

        ledgerListener = db.collection("ledger")
            .whereEqualTo("eventId", event.id)
            .addSnapshotListener { snap, err ->
                if (err != null) {
                    _uiState.update { it.copy(errorMessage = err.message) }
                    return@addSnapshotListener
                }
                rawLedgerDocs = snap?.documents.orEmpty().map { it.toLedgerTx("ledger") }
                recalculate()
            }

        expenseListener = db.collection("expenditures")
            .whereEqualTo("eventId", event.id)
            .addSnapshotListener { snap, err ->
                if (err != null) {
                    _uiState.update { it.copy(errorMessage = err.message) }
                    return@addSnapshotListener
                }
                rawExpenseDocs = snap?.documents.orEmpty().map { it.toLedgerTx("expenditures") }
                recalculate()
            }
    }

    fun exitEvent() {
        stopEventListeners()
        rawLedgerDocs = emptyList()
        rawExpenseDocs = emptyList()
        _uiState.update {
            it.copy(
                selectedEvent = null,
                credits = emptyList(),
                debits = emptyList(),
                errorMessage = null,
                successMessage = null
            )
        }
    }

    private fun recalculate() {
        val ev = _uiState.value.selectedEvent ?: return
        val credits = rawLedgerDocs
            .filter { it.type == "credit" }
            .sortedByDescending { it.timestampMillis }
        val debits = (rawExpenseDocs + rawLedgerDocs.filter { it.type == "debit" })
            .sortedByDescending { it.timestampMillis }

        val totalCredits = credits.sumOf { it.amount }
        val totalDebits = debits.sumOf { it.amount }
        val live = ev.fundsGiven + totalCredits - totalDebits

        _uiState.update {
            it.copy(
                credits = credits,
                debits = debits,
                totalCollected = totalCredits,
                totalSpent = totalDebits,
                liveBalance = live
            )
        }
    }

    fun submitTransaction(amountStr: String, purpose: String) {
        val event = _uiState.value.selectedEvent ?: return
        val amount = amountStr.trim().toDoubleOrNull()
        val cleanPurpose = purpose.trim()
        if (amount == null || amount <= 0.0 || cleanPurpose.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Please enter a valid amount and purpose.") }
            return
        }

        val isAdmin = _uiState.value.isAdmin
        val txType = if (isAdmin) "debit" else "credit"
        val targetCollection = if (isAdmin) "expenditures" else "ledger"

        if (!isAdmin) {
            if (event.fixedContribution > 0.0 && amount != event.fixedContribution) {
                _uiState.update {
                    it.copy(errorMessage = "This room requires a fixed contribution of ₹${event.fixedContribution.toInt()}.")
                }
                return
            }
            if (event.minContribution > 0.0 && amount < event.minContribution) {
                _uiState.update {
                    it.copy(errorMessage = "Minimum contribution is ₹${event.minContribution.toInt()}.")
                }
                return
            }
        }

        val now = Timestamp.now()
        val user = currentUser
        _uiState.update { it.copy(isActionBusy = true, errorMessage = null, successMessage = null) }

        db.collection(targetCollection)
            .add(
                mapOf(
                    "eventId" to event.id,
                    "date" to formatShortDate(now.toDate().time),
                    "timestamp" to now,
                    "desc" to cleanPurpose,
                    "amount" to amount,
                    "type" to txType,
                    "status" to "Completed",
                    "userName" to (user?.fullName?.ifBlank { user.email } ?: "Student"),
                    "userEmail" to (user?.email ?: "")
                )
            )
            .addOnSuccessListener {
                _uiState.update {
                    it.copy(
                        isActionBusy = false,
                        successMessage = if (isAdmin) "Expenditure recorded!" else "Contribution added to ledger!"
                    )
                }
            }
            .addOnFailureListener { ex ->
                _uiState.update {
                    it.copy(isActionBusy = false, errorMessage = ex.message ?: "Transaction failed.")
                }
            }
    }

    fun createRoom(title: String, budgetStr: String, minStr: String, fixedStr: String) {
        val cleanTitle = title.trim()
        if (cleanTitle.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Room name is required.") }
            return
        }
        val budget = budgetStr.trim().toDoubleOrNull() ?: 0.0
        val min = minStr.trim().toDoubleOrNull() ?: 0.0
        val fixed = fixedStr.trim().toDoubleOrNull() ?: 0.0

        _uiState.update { it.copy(isActionBusy = true, errorMessage = null) }
        db.collection("tracurrency_events")
            .add(
                mapOf(
                    "fundTitle" to cleanTitle,
                    "fundsGiven" to budget,
                    "minContribution" to min,
                    "fixedContribution" to fixed,
                    "createdAt" to Timestamp.now(),
                    "createdBy" to (currentUser?.email ?: "")
                )
            )
            .addOnSuccessListener {
                _uiState.update {
                    it.copy(isActionBusy = false, successMessage = "Room '$cleanTitle' created!")
                }
            }
            .addOnFailureListener { ex ->
                _uiState.update { it.copy(isActionBusy = false, errorMessage = ex.message) }
            }
    }

    fun deleteRoom(eventId: String) {
        _uiState.update { it.copy(isActionBusy = true, errorMessage = null) }
        db.collection("tracurrency_events").document(eventId).delete()
            .addOnSuccessListener {
                if (_uiState.value.selectedEvent?.id == eventId) exitEvent()
                _uiState.update { it.copy(isActionBusy = false, successMessage = "Room deleted.") }
            }
            .addOnFailureListener { ex ->
                _uiState.update { it.copy(isActionBusy = false, errorMessage = ex.message) }
            }
    }

    fun saveSettings(eventId: String, title: String, minStr: String, fixedStr: String) {
        val cleanTitle = title.trim()
        if (cleanTitle.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Room title cannot be empty.") }
            return
        }
        _uiState.update { it.copy(isActionBusy = true, errorMessage = null) }
        db.collection("tracurrency_events").document(eventId)
            .set(
                mapOf(
                    "fundTitle" to cleanTitle,
                    "minContribution" to (minStr.trim().toDoubleOrNull() ?: 0.0),
                    "fixedContribution" to (fixedStr.trim().toDoubleOrNull() ?: 0.0)
                ),
                SetOptions.merge()
            )
            .addOnSuccessListener {
                _uiState.update { it.copy(isActionBusy = false, successMessage = "Settings updated!") }
            }
            .addOnFailureListener { ex ->
                _uiState.update { it.copy(isActionBusy = false, errorMessage = ex.message) }
            }
    }

    fun deleteTx(tx: LedgerTx) {
        _uiState.update { it.copy(isActionBusy = true, errorMessage = null) }
        db.collection(tx.collection).document(tx.id).delete()
            .addOnSuccessListener {
                _uiState.update { it.copy(isActionBusy = false, successMessage = "Transaction reversed.") }
            }
            .addOnFailureListener { ex ->
                _uiState.update { it.copy(isActionBusy = false, errorMessage = ex.message) }
            }
    }

    fun clearMessages() {
        _uiState.update { it.copy(errorMessage = null, successMessage = null) }
    }

    private fun stopEventListeners() {
        eventListener?.remove()
        ledgerListener?.remove()
        expenseListener?.remove()
        eventListener = null
        ledgerListener = null
        expenseListener = null
    }

    fun clearListeners() {
        lobbyListener?.remove()
        lobbyListener = null
        stopEventListeners()
        currentUser = null
    }

    override fun onCleared() {
        super.onCleared()
        clearListeners()
    }

    private fun DocumentSnapshot.toEvent(): TracurrencyEvent {
        return TracurrencyEvent(
            id = id,
            fundTitle = safeString("fundTitle", "Untitled Event"),
            fundsGiven = safeDouble("fundsGiven"),
            minContribution = safeDouble("minContribution"),
            fixedContribution = safeDouble("fixedContribution"),
            createdBy = safeString("createdBy"),
            createdAtMillis = safeTimeMillis("createdAt")
        )
    }

    private fun DocumentSnapshot.toLedgerTx(col: String): LedgerTx {
        val tsMillis = safeTimeMillis("timestamp")
        return LedgerTx(
            id = id,
            collection = col,
            eventId = safeString("eventId"),
            amount = safeDouble("amount"),
            desc = safeString("desc"),
            type = safeString("type", if (col == "expenditures") "debit" else "credit"),
            dateStr = safeString("date").ifBlank { formatShortDate(tsMillis) },
            userName = safeString("userName", "Anonymous"),
            userEmail = safeString("userEmail"),
            timestampMillis = tsMillis
        )
    }
}

