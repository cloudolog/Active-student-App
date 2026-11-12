package com.anurag.activestudent.data

data class UserProfile(
    val uid: String = "",
    val fullName: String = "Student",
    val email: String = "",
    val rollNo: String = "",
    val role: String = "student",
    val yearOfStudy: String = "",
    val section: String = "",
    val batch: String = ""
)

data class ElectionRoom(
    val id: String = "",
    val title: String = "Untitled",
    val status: String = "not_started", // not_started, active, ended
    val roomType: String = "public",    // public, private
    val secretKey: String = "",
    val noticeMessage: String = "",
    val startTime: String = "",
    val endTime: String = "",
    val createdAtMillis: Long = 0L
)

data class Candidate(
    val id: String = "",
    val electionId: String = "",
    val name: String = "",
    val position: String = "",
    val motto: String = "",
    val img: String = "",
    val voteCount: Int = 0
)

data class UserVote(
    val electionId: String = "",
    val candidateId: String = "",
    val candidateName: String = "",
    val votedAtMillis: Long = 0L
)

data class TracurrencyEvent(
    val id: String = "",
    val fundTitle: String = "",
    val fundsGiven: Double = 0.0,
    val minContribution: Double = 0.0,
    val fixedContribution: Double = 0.0,
    val createdBy: String = "",
    val createdAtMillis: Long = 0L
)

data class LedgerTx(
    val id: String = "",
    val collection: String = "ledger", // ledger or expenditures
    val eventId: String = "",
    val amount: Double = 0.0,
    val desc: String = "",
    val type: String = "credit", // credit or debit
    val dateStr: String = "",
    val userName: String = "",
    val userEmail: String = "",
    val timestampMillis: Long = 0L
)

data class CanteenItem(
    val id: String = "",
    val name: String = "",
    val price: Double = 0.0,
    val stock: Int = 20,
    val category: String = "Veg", // Veg, Non-Veg, Essentials
    val desc: String = "",
    val img: String = ""
)

data class CartItem(
    val id: String = "",
    val name: String = "",
    val price: Double = 0.0,
    val qty: Int = 1,
    val maxStock: Int = 20
)

data class CanteenOrder(
    val docId: String = "",
    val orderId: String = "",
    val userId: String = "",
    val userName: String = "",
    val userEmail: String = "",
    val items: List<CartItem> = emptyList(),
    val totalAmount: Double = 0.0,
    val status: String = "pending", // pending, ready, picked_up
    val timestampMillis: Long = 0L,
    val updatedAtMillis: Long = 0L,
    val barcodeUrl: String = ""
)

data class AnuragEmailMeta(
    val batch: String = "",
    val section: String = "",
    val yearOfStudy: String = "Unknown",
    val shortRoll: String = ""
)

