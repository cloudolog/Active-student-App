package com.anurag.activestudent.ui.auth

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.anurag.activestudent.data.FirebaseRepo
import com.anurag.activestudent.ui.components.AppTextField
import com.anurag.activestudent.ui.components.ErrorBanner
import com.anurag.activestudent.ui.components.GlassCard
import com.anurag.activestudent.ui.theme.AccentOrange
import com.anurag.activestudent.ui.theme.BgDark
import com.anurag.activestudent.ui.theme.GreenSoft
import com.anurag.activestudent.ui.theme.GreenSuccess
import com.anurag.activestudent.ui.theme.TextMain
import com.anurag.activestudent.ui.theme.TextMuted
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.auth.api.signin.GoogleSignInStatusCodes
import com.google.android.gms.common.api.ApiException

@Composable
fun GoogleLogo(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val cx = w / 2f
        val cy = h / 2f
        val r = (w.coerceAtMost(h) / 2f) * 0.9f
        val stroke = r * 0.38f
        val arcRadius = r - stroke / 2f
        val arcSize = Size(arcRadius * 2f, arcRadius * 2f)
        val arcTopLeft = Offset(cx - arcRadius, cy - arcRadius)

        // Red (top)
        drawArc(
            color = Color(0xFFEA4335),
            startAngle = 180f,
            sweepAngle = 105f,
            useCenter = false,
            topLeft = arcTopLeft,
            size = arcSize,
            style = Stroke(width = stroke)
        )
        // Yellow (bottom-left)
        drawArc(
            color = Color(0xFFFBBC05),
            startAngle = 120f,
            sweepAngle = 60f,
            useCenter = false,
            topLeft = arcTopLeft,
            size = arcSize,
            style = Stroke(width = stroke)
        )
        // Green (bottom-right)
        drawArc(
            color = Color(0xFF34A853),
            startAngle = 30f,
            sweepAngle = 90f,
            useCenter = false,
            topLeft = arcTopLeft,
            size = arcSize,
            style = Stroke(width = stroke)
        )
        // Blue (right)
        drawArc(
            color = Color(0xFF4285F4),
            startAngle = -45f,
            sweepAngle = 75f,
            useCenter = false,
            topLeft = arcTopLeft,
            size = arcSize,
            style = Stroke(width = stroke)
        )
        // Blue crossbar
        drawLine(
            color = Color(0xFF4285F4),
            start = Offset(cx, cy),
            end = Offset(cx + arcRadius + stroke / 2f, cy),
            strokeWidth = stroke
        )
    }
}

@Composable
fun GoogleSignInButton(
    text: String,
    isLoading: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        enabled = !isLoading,
        shape = RoundedCornerShape(12.dp),
        color = Color(0xFF1E1E24),
        border = BorderStroke(1.dp, Color(0xFF33333E)),
        modifier = modifier
            .fillMaxWidth()
            .height(50.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            GoogleLogo(modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(12.dp))
            Text(
                text = text,
                color = TextMain,
                fontWeight = FontWeight.SemiBold,
                fontSize = 15.sp
            )
        }
    }
}

@Composable
fun LoginScreen(
    uiState: AuthUiState,
    onSignIn: (String, String) -> Unit,
    onGoogleSignIn: (String, String?, String?) -> Unit,
    onGoogleSignInError: (String) -> Unit,
    onForgotPassword: (String) -> Unit,
    onNavigateToRegister: () -> Unit,
    onDismissError: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val googleSignInClient = remember(context) {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(FirebaseRepo.GOOGLE_WEB_CLIENT_ID)
            .requestEmail()
            .build()
        GoogleSignIn.getClient(context, gso)
    }

    val googleSignInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                val account = task.getResult(ApiException::class.java)
                val idToken = account?.idToken
                if (!idToken.isNullOrBlank()) {
                    onGoogleSignIn(idToken, account.displayName, account.email)
                } else {
                    onGoogleSignInError("Unable to retrieve Google ID token.")
                }
            } catch (e: ApiException) {
                val msg = when (e.statusCode) {
                    GoogleSignInStatusCodes.SIGN_IN_CANCELLED -> null
                    GoogleSignInStatusCodes.NETWORK_ERROR -> "Network error during Google sign in."
                    10 -> "Configuration error (Code 10). Google SHA-1 is being synchronized."
                    12500 -> "Sign in failed (Code 12500). Please update Google Play Services."
                    else -> "Google sign-in error (${e.statusCode}): ${e.message}"
                }
                if (msg != null) onGoogleSignInError(msg)
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(BgDark, Color(0xFF14110D), BgDark)
                )
            )
            .statusBarsPadding()
            .imePadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 32.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = buildAnnotatedString {
                    withStyle(SpanStyle(color = TextMain, fontWeight = FontWeight.ExtraBold)) {
                        append("Active")
                    }
                    withStyle(SpanStyle(color = AccentOrange, fontWeight = FontWeight.ExtraBold)) {
                        append("Student")
                    }
                },
                fontSize = 30.sp,
                letterSpacing = (-0.5).sp
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = "Anurag University Campus Ecosystem",
                color = TextMuted,
                fontSize = 13.sp
            )

            Spacer(Modifier.height(28.dp))

            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Welcome Back",
                    color = TextMain,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "Sign in with your @anurag.edu.in account",
                    color = TextMuted,
                    fontSize = 13.sp
                )

                Spacer(Modifier.height(20.dp))

                ErrorBanner(
                    message = uiState.errorMessage,
                    onDismiss = onDismissError
                )
                if (!uiState.errorMessage.isNullOrBlank()) Spacer(Modifier.height(12.dp))

                if (!uiState.infoMessage.isNullOrBlank()) {
                    Surface(
                        color = GreenSoft,
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, GreenSuccess.copy(alpha = 0.4f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = uiState.infoMessage,
                            color = GreenSuccess,
                            fontSize = 13.sp,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                    Spacer(Modifier.height(12.dp))
                }

                AppTextField(
                    value = email,
                    onValueChange = { email = it; if (!uiState.errorMessage.isNullOrBlank()) onDismissError() },
                    label = "University Email",
                    placeholder = "24eg105r65@anurag.edu.in",
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Email,
                        imeAction = ImeAction.Next
                    )
                )

                Spacer(Modifier.height(14.dp))

                AppTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = "Password",
                    placeholder = "••••••••",
                    visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Done
                    ),
                    trailingIcon = {
                        IconButton(onClick = { showPassword = !showPassword }) {
                            Icon(
                                imageVector = if (showPassword) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = "Toggle password visibility",
                                tint = TextMuted
                            )
                        }
                    }
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(
                        onClick = { onForgotPassword(email) },
                        enabled = !uiState.isLoading
                    ) {
                        Text("Forgot password?", color = AccentOrange, fontSize = 12.sp)
                    }
                }

                Spacer(Modifier.height(8.dp))

                Button(
                    onClick = { onSignIn(email, password) },
                    enabled = !uiState.isLoading,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AccentOrange,
                        contentColor = Color.Black
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                ) {
                    if (uiState.isLoading) {
                        CircularProgressIndicator(
                            color = Color.Black,
                            strokeWidth = 2.5.dp,
                            modifier = Modifier.size(20.dp)
                        )
                    } else {
                        Text("Sign In", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    }
                }

                Spacer(Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        Modifier
                            .weight(1f)
                            .height(1.dp)
                            .background(Color(0xFF2E2E38))
                    )
                    Text(
                        text = "OR",
                        color = TextMuted,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 12.dp)
                    )
                    Box(
                        Modifier
                            .weight(1f)
                            .height(1.dp)
                            .background(Color(0xFF2E2E38))
                    )
                }

                Spacer(Modifier.height(16.dp))

                GoogleSignInButton(
                    text = "Continue with Google",
                    isLoading = uiState.isLoading,
                    onClick = {
                        onDismissError()
                        googleSignInClient.signOut().addOnCompleteListener {
                            googleSignInLauncher.launch(googleSignInClient.signInIntent)
                        }
                    }
                )
            }

            Spacer(Modifier.height(20.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Don't have an account? ", color = TextMuted, fontSize = 14.sp)
                Text(
                    text = "Sign Up",
                    color = AccentOrange,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    modifier = Modifier.clickable(enabled = !uiState.isLoading) {
                        onDismissError()
                        onNavigateToRegister()
                    }
                )
            }
        }
    }
}

@Composable
fun RegisterScreen(
    uiState: AuthUiState,
    onRegister: (String, String, String, String, String) -> Unit,
    onGoogleSignIn: (String, String?, String?) -> Unit,
    onGoogleSignInError: (String) -> Unit,
    onNavigateToLogin: () -> Unit,
    onDismissError: () -> Unit
) {
    var fullName by remember { mutableStateOf("") }
    var rollNo by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val googleSignInClient = remember(context) {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(FirebaseRepo.GOOGLE_WEB_CLIENT_ID)
            .requestEmail()
            .build()
        GoogleSignIn.getClient(context, gso)
    }

    val googleSignInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                val account = task.getResult(ApiException::class.java)
                val idToken = account?.idToken
                if (!idToken.isNullOrBlank()) {
                    onGoogleSignIn(idToken, account.displayName, account.email)
                } else {
                    onGoogleSignInError("Unable to retrieve Google ID token.")
                }
            } catch (e: ApiException) {
                val msg = when (e.statusCode) {
                    GoogleSignInStatusCodes.SIGN_IN_CANCELLED -> null
                    GoogleSignInStatusCodes.NETWORK_ERROR -> "Network error during Google sign up."
                    10 -> "Configuration error (Code 10). Google SHA-1 is being synchronized."
                    12500 -> "Sign up failed (Code 12500). Please update Google Play Services."
                    else -> "Google sign-up error (${e.statusCode}): ${e.message}"
                }
                if (msg != null) onGoogleSignInError(msg)
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(BgDark, Color(0xFF14110D), BgDark)
                )
            )
            .statusBarsPadding()
            .imePadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = buildAnnotatedString {
                    withStyle(SpanStyle(color = TextMain, fontWeight = FontWeight.ExtraBold)) {
                        append("Active")
                    }
                    withStyle(SpanStyle(color = AccentOrange, fontWeight = FontWeight.ExtraBold)) {
                        append("Student")
                    }
                },
                fontSize = 28.sp
            )
            Spacer(Modifier.height(20.dp))

            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Create Account",
                    color = TextMain,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "Join the Anurag University digital campus hub",
                    color = TextMuted,
                    fontSize = 13.sp
                )

                Spacer(Modifier.height(16.dp))

                ErrorBanner(
                    message = uiState.errorMessage,
                    onDismiss = onDismissError
                )
                if (!uiState.errorMessage.isNullOrBlank()) Spacer(Modifier.height(12.dp))

                AppTextField(
                    value = fullName,
                    onValueChange = { fullName = it; if (!uiState.errorMessage.isNullOrBlank()) onDismissError() },
                    label = "Full Name",
                    placeholder = "John Doe",
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Words,
                        imeAction = ImeAction.Next
                    )
                )

                Spacer(Modifier.height(12.dp))

                AppTextField(
                    value = rollNo,
                    onValueChange = { rollNo = it },
                    label = "Roll No / College ID",
                    placeholder = "24EG105R65",
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Characters,
                        imeAction = ImeAction.Next
                    )
                )

                Spacer(Modifier.height(12.dp))

                AppTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = "University Email (@anurag.edu.in)",
                    placeholder = "24eg105r65@anurag.edu.in",
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Email,
                        imeAction = ImeAction.Next
                    )
                )

                Spacer(Modifier.height(12.dp))

                AppTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = "Password",
                    placeholder = "At least 6 characters",
                    visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Next
                    ),
                    trailingIcon = {
                        IconButton(onClick = { showPassword = !showPassword }) {
                            Icon(
                                imageVector = if (showPassword) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = "Toggle password visibility",
                                tint = TextMuted
                            )
                        }
                    }
                )

                Spacer(Modifier.height(12.dp))

                AppTextField(
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it },
                    label = "Confirm Password",
                    placeholder = "••••••••",
                    visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Done
                    )
                )

                Spacer(Modifier.height(20.dp))

                Button(
                    onClick = { onRegister(fullName, rollNo, email, password, confirmPassword) },
                    enabled = !uiState.isLoading,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AccentOrange,
                        contentColor = Color.Black
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                ) {
                    if (uiState.isLoading) {
                        CircularProgressIndicator(
                            color = Color.Black,
                            strokeWidth = 2.5.dp,
                            modifier = Modifier.size(20.dp)
                        )
                    } else {
                        Text("Create Account", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    }
                }

                Spacer(Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        Modifier
                            .weight(1f)
                            .height(1.dp)
                            .background(Color(0xFF2E2E38))
                    )
                    Text(
                        text = "OR",
                        color = TextMuted,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 12.dp)
                    )
                    Box(
                        Modifier
                            .weight(1f)
                            .height(1.dp)
                            .background(Color(0xFF2E2E38))
                    )
                }

                Spacer(Modifier.height(16.dp))

                GoogleSignInButton(
                    text = "Sign up with Google",
                    isLoading = uiState.isLoading,
                    onClick = {
                        onDismissError()
                        googleSignInClient.signOut().addOnCompleteListener {
                            googleSignInLauncher.launch(googleSignInClient.signInIntent)
                        }
                    }
                )
            }

            Spacer(Modifier.height(20.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Already have an account? ", color = TextMuted, fontSize = 14.sp)
                Text(
                    text = "Sign In",
                    color = AccentOrange,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    modifier = Modifier.clickable(enabled = !uiState.isLoading) {
                        onDismissError()
                        onNavigateToLogin()
                    }
                )
            }
        }
    }
}

