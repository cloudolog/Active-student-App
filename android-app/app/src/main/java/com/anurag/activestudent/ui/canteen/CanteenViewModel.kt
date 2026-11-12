package com.anurag.activestudent.ui.canteen

import androidx.lifecycle.ViewModel
import com.anurag.activestudent.data.CanteenItem
import com.anurag.activestudent.data.CanteenOrder
import com.anurag.activestudent.data.CartItem
import com.anurag.activestudent.data.FirebaseRepo
import com.anurag.activestudent.data.UserProfile
import com.anurag.activestudent.data.safeDouble
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
import java.util.Locale

sealed class ScanResultState {
    data object Idle : ScanResultState()
    data object Checking : ScanResultState()
    data class Invalid(val orderId: String) : ScanResultState()
    data class AlreadyClaimed(val orderId: String, val claimedAtMillis: Long) : ScanResultState()
    data class Approved(val order: CanteenOrder) : ScanResultState()
    data class Error(val message: String) : ScanResultState()
}

data class CanteenUiState(
    val isLoading: Boolean = true,
    val isActionBusy: Boolean = false,
    val isAdmin: Boolean = false,
    val menuItems: List<CanteenItem> = emptyList(),
    val cart: List<CartItem> = emptyList(),
    val myOrders: List<CanteenOrder> = emptyList(),
    val adminActiveOrders: List<CanteenOrder> = emptyList(),
    val adminCompletedOrders: List<CanteenOrder> = emptyList(),
    val activeReceipt: CanteenOrder? = null,
    val claimedPopupOrderId: String? = null,
    val scanResult: ScanResultState = ScanResultState.Idle,
    val errorMessage: String? = null,
    val successMessage: String? = null
)

class CanteenViewModel : ViewModel() {
    private val db = FirebaseRepo.db

    private val _uiState = MutableStateFlow(CanteenUiState())
    val uiState: StateFlow<CanteenUiState> = _uiState.asStateFlow()

    private var currentUser: UserProfile? = null
    private var menuListener: ListenerRegistration? = null
    private var myOrdersListener: ListenerRegistration? = null
    private var adminQueueListener: ListenerRegistration? = null
    private var adminStatsListener: ListenerRegistration? = null
    private var previousOrderStatuses = mutableMapOf<String, String>()
    private var initialMyOrdersLoaded = false

    fun bindUser(user: UserProfile) {
        val admin = FirebaseRepo.isCanteenAdmin(user.email)
        val sameUser = currentUser?.uid == user.uid && menuListener != null
        currentUser = user
        if (sameUser) {
            _uiState.update { it.copy(isAdmin = admin) }
            return
        }
        clearListeners()
        currentUser = user
        _uiState.update { it.copy(isAdmin = admin, isLoading = true) }

        menuListener = db.collection("canteen_menu")
            .addSnapshotListener { snap, err ->
                if (err != null) {
                    _uiState.update { it.copy(isLoading = false, errorMessage = err.message) }
                    return@addSnapshotListener
                }
                val items = snap?.documents.orEmpty().map { doc ->
                    CanteenItem(
                        id = doc.id,
                        name = doc.safeString("name", "Item"),
                        price = doc.safeDouble("price"),
                        stock = if (doc.contains("stock")) doc.safeInt("stock", 20) else 20,
                        category = doc.safeString("category", "Veg").ifBlank { "Veg" },
                        desc = doc.safeString("desc"),
                        img = doc.safeString("img")
                    )
                }.sortedBy { it.name.lowercase(Locale.ROOT) }
                _uiState.update { it.copy(isLoading = false, menuItems = items) }
            }

        if (admin) {
            adminQueueListener = db.collection("canteen_orders")
                .whereIn("status", listOf("pending", "ready"))
                .addSnapshotListener { snap, _ ->
                    val list = snap?.documents.orEmpty()
                        .map { it.toOrder() }
                        .sortedWith(
                            compareBy<CanteenOrder> { if (it.status == "pending") 0 else 1 }
                                .thenByDescending { it.timestampMillis }
                        )
                    _uiState.update { it.copy(adminActiveOrders = list) }
                }

            adminStatsListener = db.collection("canteen_orders")
                .whereEqualTo("status", "picked_up")
                .addSnapshotListener { snap, _ ->
                    val list = snap?.documents.orEmpty().map { it.toOrder() }
                    _uiState.update { it.copy(adminCompletedOrders = list) }
                }
        } else {
            loadCart(user.uid)
            initialMyOrdersLoaded = false
            previousOrderStatuses.clear()
            myOrdersListener = db.collection("canteen_orders")
                .whereEqualTo("userId", user.uid)
                .addSnapshotListener { snap, err ->
                    if (err != null) return@addSnapshotListener
                    val orders = snap?.documents.orEmpty()
                        .map { it.toOrder() }
                        .sortedByDescending { it.timestampMillis }

                    if (initialMyOrdersLoaded) {
                        orders.forEach { o ->
                            val prev = previousOrderStatuses[o.docId]
                            if (prev != null && prev != "picked_up" && o.status == "picked_up") {
                                _uiState.update { it.copy(claimedPopupOrderId = o.orderId) }
                            }
                        }
                    }
                    initialMyOrdersLoaded = true
                    previousOrderStatuses.clear()
                    orders.forEach { previousOrderStatuses[it.docId] = it.status }

                    _uiState.update { it.copy(myOrders = orders) }
                }
        }
    }

    private fun loadCart(uid: String) {
        db.collection("carts").document(uid).get()
            .addOnSuccessListener { doc ->
                if (doc.exists()) {
                    val raw = doc.get("items") as? List<*> ?: emptyList<Any>()
                    val parsed = raw.mapNotNull { entry ->
                        val map = entry as? Map<*, *> ?: return@mapNotNull null
                        CartItem(
                            id = map["id"]?.toString().orEmpty(),
                            name = map["name"]?.toString().orEmpty(),
                            price = (map["price"] as? Number)?.toDouble() ?: 0.0,
                            qty = (map["qty"] as? Number)?.toInt() ?: 1,
                            maxStock = (map["maxStock"] as? Number)?.toInt() ?: 20
                        )
                    }.filter { it.id.isNotBlank() && it.qty > 0 }
                    _uiState.update { it.copy(cart = parsed) }
                }
            }
    }

    private fun saveCart(cart: List<CartItem>) {
        val uid = currentUser?.uid ?: return
        val serialized = cart.map {
            mapOf(
                "id" to it.id,
                "name" to it.name,
                "price" to it.price,
                "qty" to it.qty,
                "maxStock" to it.maxStock
            )
        }
        db.collection("carts").document(uid)
            .set(mapOf("items" to serialized, "updatedAt" to Timestamp.now()))
    }

    fun addToCart(item: CanteenItem) {
        val current = _uiState.value.cart.toMutableList()
        val idx = current.indexOfFirst { it.id == item.id }
        val existingQty = if (idx >= 0) current[idx].qty else 0
        if (existingQty >= item.stock) {
            _uiState.update { it.copy(errorMessage = "Sorry, only ${item.stock} ${item.name} available.") }
            return
        }
        if (idx >= 0) {
            current[idx] = current[idx].copy(qty = existingQty + 1, maxStock = item.stock, price = item.price)
        } else {
            current.add(CartItem(item.id, item.name, item.price, 1, item.stock))
        }
        _uiState.update { it.copy(cart = current, errorMessage = null) }
        saveCart(current)
    }

    fun updateCartQty(itemId: String, delta: Int) {
        val current = _uiState.value.cart.toMutableList()
        val idx = current.indexOfFirst { it.id == itemId }
        if (idx < 0) return
        val item = current[idx]
        val liveStock = _uiState.value.menuItems.find { it.id == itemId }?.stock ?: item.maxStock
        val newQty = item.qty + delta
        if (delta > 0 && newQty > liveStock) {
            _uiState.update { it.copy(errorMessage = "Only $liveStock ${item.name} available.") }
            return
        }
        if (newQty <= 0) {
            current.removeAt(idx)
        } else {
            current[idx] = item.copy(qty = newQty, maxStock = liveStock)
        }
        _uiState.update { it.copy(cart = current, errorMessage = null) }
        saveCart(current)
    }

    fun placeOrder(onSuccessCloseBill: () -> Unit) {
        val user = currentUser ?: return
        val cartSnapshot = _uiState.value.cart
        if (cartSnapshot.isEmpty()) return

        _uiState.update { it.copy(isActionBusy = true, errorMessage = null) }
        val suffix = (System.currentTimeMillis() % 1_000_000L).toString().padStart(6, '0')
        val rand = (100..999).random()
        val orderId = "ORD-$suffix-$rand"
        val totalWithGst = cartSnapshot.sumOf { it.price * it.qty } * 1.05
        val barcodeUrl = "https://bwipjs-api.metafloor.com/?bcid=qrcode&text=$orderId&scale=5"
        val now = Timestamp.now()

        val serializedItems = cartSnapshot.map {
            mapOf(
                "id" to it.id,
                "name" to it.name,
                "price" to it.price,
                "qty" to it.qty,
                "maxStock" to it.maxStock
            )
        }

        db.runTransaction { tx ->
            val stockUpdates = mutableListOf<Pair<com.google.firebase.firestore.DocumentReference, Int>>()
            for (cItem in cartSnapshot) {
                val menuRef = db.collection("canteen_menu").document(cItem.id)
                val snap = tx.get(menuRef)
                if (!snap.exists()) throw IllegalStateException("Item ${cItem.name} not found.")
                val serverStock = if (snap.contains("stock")) snap.safeInt("stock", 20) else 20
                if (serverStock < cItem.qty) {
                    throw IllegalStateException("Not enough stock for ${cItem.name} ($serverStock left).")
                }
                stockUpdates.add(menuRef to (serverStock - cItem.qty))
            }

            for ((ref, newStock) in stockUpdates) {
                tx.update(ref, "stock", newStock)
            }

            val orderRef = db.collection("canteen_orders").document()
            tx.set(
                orderRef,
                mapOf(
                    "orderId" to orderId,
                    "userId" to user.uid,
                    "userName" to user.fullName,
                    "userEmail" to user.email,
                    "items" to serializedItems,
                    "totalAmount" to totalWithGst,
                    "status" to "pending",
                    "timestamp" to now,
                    "barcodeUrl" to barcodeUrl
                )
            )
            orderRef.id
        }.addOnSuccessListener { newDocId ->
            val receiptOrder = CanteenOrder(
                docId = newDocId,
                orderId = orderId,
                userId = user.uid,
                userName = user.fullName,
                userEmail = user.email,
                items = cartSnapshot,
                totalAmount = totalWithGst,
                status = "pending",
                timestampMillis = now.toDate().time,
                barcodeUrl = barcodeUrl
            )
            _uiState.update {
                it.copy(
                    isActionBusy = false,
                    cart = emptyList(),
                    activeReceipt = receiptOrder
                )
            }
            saveCart(emptyList())
            onSuccessCloseBill()
        }.addOnFailureListener { ex ->
            _uiState.update {
                it.copy(isActionBusy = false, errorMessage = ex.message ?: "Order failed.")
            }
        }
    }

    fun showReceipt(order: CanteenOrder) {
        _uiState.update { it.copy(activeReceipt = order) }
    }

    fun closeReceipt() {
        _uiState.update { it.copy(activeReceipt = null) }
    }

    fun dismissClaimedPopup() {
        _uiState.update { it.copy(claimedPopupOrderId = null) }
    }

    // --- ADMIN ACTIONS ---

    fun updateOrderStatus(docId: String, newStatus: String) {
        _uiState.update { it.copy(isActionBusy = true, errorMessage = null) }
        db.collection("canteen_orders").document(docId)
            .update(
                mapOf(
                    "status" to newStatus,
                    "updatedAt" to Timestamp.now()
                )
            )
            .addOnSuccessListener {
                _uiState.update {
                    it.copy(isActionBusy = false, successMessage = "Order marked as ${newStatus.uppercase()}.")
                }
            }
            .addOnFailureListener { ex ->
                _uiState.update { it.copy(isActionBusy = false, errorMessage = ex.message) }
            }
    }

    fun verifyAndClaimOrder(rawOrderId: String) {
        val code = rawOrderId.trim().uppercase(Locale.ROOT)
        if (code.isBlank()) return
        _uiState.update { it.copy(scanResult = ScanResultState.Checking) }

        db.collection("canteen_orders")
            .whereEqualTo("orderId", code)
            .limit(1)
            .get()
            .addOnSuccessListener { snap ->
                if (snap.isEmpty) {
                    _uiState.update { it.copy(scanResult = ScanResultState.Invalid(code)) }
                    return@addOnSuccessListener
                }
                val doc = snap.documents[0]
                val order = doc.toOrder()
                if (order.status == "picked_up") {
                    _uiState.update {
                        it.copy(scanResult = ScanResultState.AlreadyClaimed(code, order.updatedAtMillis))
                    }
                    return@addOnSuccessListener
                }

                db.collection("canteen_orders").document(doc.id)
                    .update(
                        mapOf(
                            "status" to "picked_up",
                            "updatedAt" to Timestamp.now()
                        )
                    )
                    .addOnSuccessListener {
                        _uiState.update {
                            it.copy(scanResult = ScanResultState.Approved(order.copy(status = "picked_up")))
                        }
                    }
                    .addOnFailureListener { ex ->
                        _uiState.update {
                            it.copy(scanResult = ScanResultState.Error(ex.message ?: "Update failed"))
                        }
                    }
            }
            .addOnFailureListener { ex ->
                _uiState.update {
                    it.copy(scanResult = ScanResultState.Error(ex.message ?: "Lookup failed"))
                }
            }
    }

    fun saveMenuItem(
        editingId: String?,
        name: String,
        priceStr: String,
        stockStr: String,
        category: String,
        desc: String,
        img: String
    ) {
        if (name.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Item name is required.") }
            return
        }
        val price = priceStr.trim().toDoubleOrNull() ?: 0.0
        val stock = stockStr.trim().toIntOrNull() ?: 0

        val data = mapOf(
            "name" to name.trim(),
            "price" to price,
            "stock" to stock,
            "category" to category.ifBlank { "Veg" },
            "desc" to desc.trim(),
            "img" to img.trim()
        )
        _uiState.update { it.copy(isActionBusy = true, errorMessage = null) }
        val task = if (!editingId.isNullOrBlank()) {
            db.collection("canteen_menu").document(editingId).set(data, SetOptions.merge())
        } else {
            db.collection("canteen_menu").add(data)
        }
        task.addOnSuccessListener {
            _uiState.update {
                it.copy(
                    isActionBusy = false,
                    successMessage = if (editingId == null) "Item added to menu!" else "Item updated!"
                )
            }
        }.addOnFailureListener { ex ->
            _uiState.update { it.copy(isActionBusy = false, errorMessage = ex.message) }
        }
    }

    fun deleteMenuItem(itemId: String) {
        _uiState.update { it.copy(isActionBusy = true, errorMessage = null) }
        db.collection("canteen_menu").document(itemId).delete()
            .addOnSuccessListener {
                _uiState.update { it.copy(isActionBusy = false, successMessage = "Item deleted.") }
            }
            .addOnFailureListener { ex ->
                _uiState.update { it.copy(isActionBusy = false, errorMessage = ex.message) }
            }
    }

    fun clearMessages() {
        _uiState.update { it.copy(errorMessage = null, successMessage = null) }
    }

    fun clearListeners() {
        menuListener?.remove()
        myOrdersListener?.remove()
        adminQueueListener?.remove()
        adminStatsListener?.remove()
        menuListener = null
        myOrdersListener = null
        adminQueueListener = null
        adminStatsListener = null
        currentUser = null
    }

    override fun onCleared() {
        super.onCleared()
        clearListeners()
    }

    private fun DocumentSnapshot.toOrder(): CanteenOrder {
        val rawItems = get("items") as? List<*> ?: emptyList<Any>()
        val parsedItems = rawItems.mapNotNull { entry ->
            val map = entry as? Map<*, *> ?: return@mapNotNull null
            CartItem(
                id = map["id"]?.toString().orEmpty(),
                name = map["name"]?.toString().orEmpty(),
                price = (map["price"] as? Number)?.toDouble() ?: 0.0,
                qty = (map["qty"] as? Number)?.toInt() ?: 1,
                maxStock = (map["maxStock"] as? Number)?.toInt() ?: 20
            )
        }
        return CanteenOrder(
            docId = id,
            orderId = safeString("orderId", id),
            userId = safeString("userId"),
            userName = safeString("userName", "Student"),
            userEmail = safeString("userEmail"),
            items = parsedItems,
            totalAmount = safeDouble("totalAmount"),
            status = safeString("status", "pending"),
            timestampMillis = safeTimeMillis("timestamp"),
            updatedAtMillis = safeTimeMillis("updatedAt"),
            barcodeUrl = safeString("barcodeUrl")
        )
    }
}

