package com.anurag.activestudent.ui.hub

import androidx.lifecycle.ViewModel
import com.anurag.activestudent.data.FirebaseRepo
import com.anurag.activestudent.data.safeDouble
import com.anurag.activestudent.data.safeString
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class HubUiState(
    val activeElectionsCount: Int = 0,
    val votingConnected: Boolean = false,
    val ledgerBalance: Double = 45250.0,
    val ledgerConnected: Boolean = false,
    val menuItemsCount: Int = 0,
    val pendingOrdersCount: Int = 0
)

class HubViewModel : ViewModel() {
    private val db = FirebaseRepo.db

    private val _uiState = MutableStateFlow(HubUiState())
    val uiState: StateFlow<HubUiState> = _uiState.asStateFlow()

    private val listeners = mutableListOf<ListenerRegistration>()
    private var startedForUid: String? = null

    fun ensureListening(uid: String) {
        if (startedForUid == uid) return
        stopListening()
        startedForUid = uid

        // 1. Live Voting Status
        listeners += db.collection("elections")
            .whereEqualTo("status", "active")
            .addSnapshotListener { snap, err ->
                if (err != null || snap == null) return@addSnapshotListener
                _uiState.update {
                    it.copy(
                        activeElectionsCount = snap.size(),
                        votingConnected = true
                    )
                }
            }

        // 2. Live Tracurrency Balance (matches index.html base 45250 + ledger credits - debits)
        listeners += db.collection("ledger")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .limit(50)
            .addSnapshotListener { snap, err ->
                if (err != null || snap == null) return@addSnapshotListener
                var balance = 45250.0
                for (doc in snap.documents) {
                    val type = doc.safeString("type")
                    val amount = doc.safeDouble("amount")
                    if (type == "debit") balance -= amount
                    if (type == "credit") balance += amount
                }
                _uiState.update {
                    it.copy(
                        ledgerBalance = balance,
                        ledgerConnected = true
                    )
                }
            }

        // 3. Canteen Menu Count
        listeners += db.collection("canteen_menu")
            .addSnapshotListener { snap, err ->
                if (err != null || snap == null) return@addSnapshotListener
                _uiState.update { it.copy(menuItemsCount = snap.size()) }
            }

        // 4. User's Active Canteen Orders
        listeners += db.collection("canteen_orders")
            .whereEqualTo("userId", uid)
            .addSnapshotListener { snap, err ->
                if (err != null || snap == null) return@addSnapshotListener
                val activeCount = snap.documents.count { doc ->
                    val status = doc.safeString("status")
                    status == "pending" || status == "ready"
                }
                _uiState.update { it.copy(pendingOrdersCount = activeCount) }
            }
    }

    fun stopListening() {
        listeners.forEach { it.remove() }
        listeners.clear()
        startedForUid = null
    }

    override fun onCleared() {
        super.onCleared()
        stopListening()
    }
}

