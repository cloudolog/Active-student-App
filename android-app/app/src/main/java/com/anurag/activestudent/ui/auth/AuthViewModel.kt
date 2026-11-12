package com.anurag.activestudent.ui.auth

import androidx.lifecycle.ViewModel
import com.anurag.activestudent.data.FirebaseRepo
import com.anurag.activestudent.data.UserProfile
import com.anurag.activestudent.data.mapFirebaseAuthError
import com.anurag.activestudent.data.parseAnuragEmail
import com.anurag.activestudent.data.safeString
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.util.Locale

data class AuthUiState(
    val isInitializing: Boolean = true,
    val isLoading: Boolean = false,
    val user: UserProfile? = null,
    val errorMessage: String? = null,
    val infoMessage: String? = null
)

class AuthViewModel : ViewModel() {
    private val auth = FirebaseRepo.auth
    private val db = FirebaseRepo.db

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    private val authListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
        val fbUser = firebaseAuth.currentUser
        if (fbUser == null) {
            _uiState.update {
                it.copy(isInitializing = false, isLoading = false, user = null)
            }
        } else {
            val email = fbUser.email.orEmpty()
            val fallbackName = fbUser.displayName?.takeIf { it.isNotBlank() }
                ?: email.substringBefore("@").ifBlank { "Student" }
            val meta = parseAnuragEmail(email)
            val role = when {
                FirebaseRepo.isVotingAdmin(email) -> "voting_admin"
                FirebaseRepo.isTracurrencyAdmin(email) -> "tracurrency_admin"
                FirebaseRepo.isCanteenAdmin(email) -> "canteen_admin"
                else -> "student"
            }

            // Immediately set basic profile so navigation never blocks on Firestore
            _uiState.update {
                it.copy(
                    isInitializing = false,
                    isLoading = false,
                    user = UserProfile(
                        uid = fbUser.uid,
                        fullName = fallbackName,
                        email = email,
                        role = role,
                        yearOfStudy = meta.yearOfStudy,
                        section = meta.section,
                        batch = meta.batch
                    )
                )
            }

            // Enrich from Firestore users/{uid}
            db.collection("users").document(fbUser.uid).get()
                .addOnSuccessListener { doc ->
                    if (doc.exists()) {
                        val dbName = doc.safeString("fullName")
                            .ifBlank { doc.safeString("name") }
                            .ifBlank { fallbackName }
                        val rollNo = doc.safeString("rollNo")
                            .ifBlank { doc.safeString("collegeId") }
                        _uiState.update { state ->
                            val cur = state.user ?: return@update state
                            if (cur.uid != fbUser.uid) return@update state
                            state.copy(
                                user = cur.copy(
                                    fullName = dbName,
                                    rollNo = rollNo,
                                    yearOfStudy = doc.safeString("yearOfStudy").ifBlank { cur.yearOfStudy },
                                    section = doc.safeString("section").ifBlank { cur.section },
                                    batch = doc.safeString("batch").ifBlank { cur.batch }
                                )
                            )
                        }
                    }
                }
        }
    }

    init {
        auth.addAuthStateListener(authListener)
    }

    override fun onCleared() {
        super.onCleared()
        auth.removeAuthStateListener(authListener)
    }

    fun clearMessages() {
        _uiState.update { it.copy(errorMessage = null, infoMessage = null) }
    }

    fun signIn(rawEmail: String, password: String) {
        val email = rawEmail.trim().lowercase(Locale.ROOT)
        if (email.isBlank() || password.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Please enter both email and password.") }
            return
        }
        if (!email.endsWith("@anurag.edu.in")) {
            _uiState.update {
                it.copy(errorMessage = "Access restricted: Only @anurag.edu.in emails are accepted.")
            }
            return
        }

        _uiState.update { it.copy(isLoading = true, errorMessage = null, infoMessage = null) }
        auth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener { result ->
                val user = result.user
                if (user != null) {
                    db.collection("users").document(user.uid)
                        .set(
                            mapOf(
                                "email" to email,
                                "lastLogin" to Timestamp.now()
                            ),
                            SetOptions.merge()
                        )
                }
                _uiState.update { it.copy(isLoading = false) }
            }
            .addOnFailureListener { ex ->
                _uiState.update {
                    it.copy(isLoading = false, errorMessage = mapFirebaseAuthError(ex))
                }
            }
    }

    fun register(
        rawName: String,
        rawRollNo: String,
        rawEmail: String,
        password: String,
        confirmPassword: String
    ) {
        val fullName = rawName.trim()
        val rollNo = rawRollNo.trim().uppercase(Locale.ROOT)
        val email = rawEmail.trim().lowercase(Locale.ROOT)

        when {
            fullName.length < 2 -> {
                _uiState.update { it.copy(errorMessage = "Please enter your full name.") }
                return
            }
            rollNo.isBlank() -> {
                _uiState.update { it.copy(errorMessage = "Please enter your Roll Number / College ID.") }
                return
            }
            !email.endsWith("@anurag.edu.in") -> {
                _uiState.update {
                    it.copy(errorMessage = "Signup restricted: Only @anurag.edu.in emails are accepted.")
                }
                return
            }
            password.length < 6 -> {
                _uiState.update { it.copy(errorMessage = "Password must be at least 6 characters.") }
                return
            }
            password != confirmPassword -> {
                _uiState.update { it.copy(errorMessage = "Passwords do not match.") }
                return
            }
        }

        _uiState.update { it.copy(isLoading = true, errorMessage = null, infoMessage = null) }
        auth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener { result ->
                val fbUser = result.user
                if (fbUser == null) {
                    _uiState.update { it.copy(isLoading = false, errorMessage = "Registration failed.") }
                    return@addOnSuccessListener
                }

                val profileReq = UserProfileChangeRequest.Builder()
                    .setDisplayName(fullName)
                    .build()
                fbUser.updateProfile(profileReq)

                val meta = parseAnuragEmail(email)
                val userDoc = mapOf(
                    "fullName" to fullName,
                    "name" to fullName,
                    "rollNo" to rollNo,
                    "collegeId" to rollNo,
                    "email" to email,
                    "joinedAt" to Timestamp.now(),
                    "lastLogin" to Timestamp.now(),
                    "role" to "student",
                    "hasVoted" to false,
                    "batch" to meta.batch,
                    "section" to meta.section,
                    "yearOfStudy" to meta.yearOfStudy,
                    "shortRoll" to meta.shortRoll
                )

                db.collection("users").document(fbUser.uid)
                    .set(userDoc, SetOptions.merge())
                    .addOnCompleteListener {
                        _uiState.update { state ->
                            state.copy(
                                isLoading = false,
                                user = state.user?.copy(
                                    fullName = fullName,
                                    rollNo = rollNo,
                                    yearOfStudy = meta.yearOfStudy,
                                    section = meta.section,
                                    batch = meta.batch
                                )
                            )
                        }
                    }
            }
            .addOnFailureListener { ex ->
                _uiState.update {
                    it.copy(isLoading = false, errorMessage = mapFirebaseAuthError(ex))
                }
            }
    }

    fun sendPasswordReset(rawEmail: String) {
        val email = rawEmail.trim().lowercase(Locale.ROOT)
        if (!email.endsWith("@anurag.edu.in")) {
            _uiState.update { it.copy(errorMessage = "Enter your @anurag.edu.in email first.") }
            return
        }
        _uiState.update { it.copy(isLoading = true, errorMessage = null, infoMessage = null) }
        auth.sendPasswordResetEmail(email)
            .addOnSuccessListener {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        infoMessage = "Password reset link sent to $email"
                    )
                }
            }
            .addOnFailureListener { ex ->
                _uiState.update {
                    it.copy(isLoading = false, errorMessage = mapFirebaseAuthError(ex))
                }
            }
    }

    fun signOut() {
        auth.signOut()
    }
}

