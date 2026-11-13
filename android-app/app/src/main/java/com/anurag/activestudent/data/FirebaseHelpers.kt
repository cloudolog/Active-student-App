package com.anurag.activestudent.data

import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object FirebaseRepo {
    val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
    val db: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }

    const val GOOGLE_WEB_CLIENT_ID = "601731908205-5vgeiikrrn7ku8q6jtou9pulrn9naiq3.apps.googleusercontent.com"

    const val VOTING_ADMIN_EMAIL = "voting@anurag.edu.in"
    const val TRACURRENCY_ADMIN_EMAIL = "tracurrency@anurag.edu.in"
    const val CANTEEN_ADMIN_EMAIL = "canteen@anurag.edu.in"
    const val SUPER_ADMIN_EMAIL = "cloudolog@gmail.com"

    fun isSuperAdmin(email: String?): Boolean {
        val e = email?.trim() ?: return false
        return e.equals(SUPER_ADMIN_EMAIL, ignoreCase = true)
    }

    fun isVotingAdmin(email: String?, role: String? = null): Boolean {
        val r = role?.trim()?.lowercase(Locale.ROOT)
        if (r == "voting_admin" || r == "super_admin" || r == "admin") return true
        val e = email?.trim() ?: return false
        return e.equals(VOTING_ADMIN_EMAIL, ignoreCase = true) || isSuperAdmin(e)
    }

    fun isTracurrencyAdmin(email: String?, role: String? = null): Boolean {
        val r = role?.trim()?.lowercase(Locale.ROOT)
        if (r == "tracurrency_admin" || r == "super_admin" || r == "admin") return true
        val e = email?.trim() ?: return false
        return e.equals(TRACURRENCY_ADMIN_EMAIL, ignoreCase = true) || isSuperAdmin(e)
    }

    fun isCanteenAdmin(email: String?, role: String? = null): Boolean {
        val r = role?.trim()?.lowercase(Locale.ROOT)
        if (r == "canteen_admin" || r == "super_admin" || r == "admin") return true
        val e = email?.trim() ?: return false
        return e.equals(CANTEEN_ADMIN_EMAIL, ignoreCase = true) || isSuperAdmin(e)
    }

    fun isAnyAdmin(email: String?, role: String? = null): Boolean =
        isSuperAdmin(email) || isVotingAdmin(email, role) || isTracurrencyAdmin(email, role) || isCanteenAdmin(email, role)
}

fun DocumentSnapshot.safeString(field: String, default: String = ""): String {
    val raw = get(field) ?: return default
    return raw.toString()
}

fun DocumentSnapshot.safeDouble(field: String, default: Double = 0.0): Double {
    val raw = get(field) ?: return default
    return when (raw) {
        is Number -> raw.toDouble()
        is String -> raw.toDoubleOrNull() ?: default
        else -> default
    }
}

fun DocumentSnapshot.safeInt(field: String, default: Int = 0): Int {
    val raw = get(field) ?: return default
    return when (raw) {
        is Number -> raw.toInt()
        is String -> raw.toIntOrNull() ?: raw.toDoubleOrNull()?.toInt() ?: default
        else -> default
    }
}

fun DocumentSnapshot.safeTimeMillis(field: String, default: Long = 0L): Long {
    val raw = get(field) ?: return default
    return when (raw) {
        is Timestamp -> raw.toDate().time
        is Date -> raw.time
        is Number -> raw.toLong()
        is Map<*, *> -> {
            val secs = (raw["seconds"] as? Number)?.toLong() ?: return default
            secs * 1000L
        }
        else -> default
    }
}

private val ANURAG_EMAIL_REGEX = Regex("^(\\d{2})eg105([a-z])(\\d{2})@anurag\\.edu\\.in$")

fun parseAnuragEmail(email: String): AnuragEmailMeta {
    val match = ANURAG_EMAIL_REGEX.matchEntire(email.trim().lowercase(Locale.ROOT))
        ?: return AnuragEmailMeta()
    val batch = match.groupValues[1]
    val section = match.groupValues[2].uppercase(Locale.ROOT)
    val shortRoll = match.groupValues[3]
    val year = when (batch.toIntOrNull()) {
        26 -> "1st Year"
        25 -> "2nd Year"
        24 -> "3rd Year"
        23 -> "4th Year"
        22 -> "4th Year"
        else -> "Alumni"
    }
    return AnuragEmailMeta(
        batch = batch,
        section = section,
        yearOfStudy = year,
        shortRoll = shortRoll
    )
}

fun formatRupees(amount: Double, showDecimals: Boolean = false): String {
    val locale = Locale.Builder().setLanguage("en").setRegion("IN").build()
    val format = NumberFormat.getNumberInstance(locale).apply {
        maximumFractionDigits = if (showDecimals) 2 else 0
        minimumFractionDigits = if (showDecimals) 2 else 0
    }
    return "₹${format.format(amount)}"
}

fun formatShortDate(millis: Long): String {
    if (millis <= 0L) return "Recent"
    val sdf = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
    return sdf.format(Date(millis))
}

fun formatDateTime(millis: Long): String {
    if (millis <= 0L) return "Just now"
    val sdf = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault())
    return sdf.format(Date(millis))
}

fun mapFirebaseAuthError(ex: Exception): String {
    val msg = ex.message.orEmpty()
    return when {
        msg.contains("INVALID_LOGIN_CREDENTIALS", ignoreCase = true) ||
            msg.contains("INVALID_CREDENTIAL", ignoreCase = true) ->
            "Invalid email or password."
        msg.contains("USER_NOT_FOUND", ignoreCase = true) ||
            msg.contains("no user record", ignoreCase = true) ->
            "No account found with this email."
        msg.contains("WRONG_PASSWORD", ignoreCase = true) ->
            "Incorrect password."
        msg.contains("EMAIL_EXISTS", ignoreCase = true) ||
            msg.contains("already in use", ignoreCase = true) ->
            "This email is already registered."
        msg.contains("WEAK_PASSWORD", ignoreCase = true) ||
            msg.contains("at least 6 characters", ignoreCase = true) ->
            "Password must be at least 6 characters."
        msg.contains("TOO_MANY_REQUESTS", ignoreCase = true) ->
            "Too many attempts. Please wait a moment and try again."
        msg.contains("network", ignoreCase = true) ->
            "Network error. Please check your internet connection."
        else -> msg.ifBlank { "Authentication failed. Please try again." }
    }
}

