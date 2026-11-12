package com.anurag.activestudent

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.HowToVote
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.anurag.activestudent.data.FirebaseRepo
import com.anurag.activestudent.ui.auth.AuthViewModel
import com.anurag.activestudent.ui.auth.LoginScreen
import com.anurag.activestudent.ui.auth.RegisterScreen
import com.anurag.activestudent.ui.canteen.CanteenScreen
import com.anurag.activestudent.ui.canteen.CanteenViewModel
import com.anurag.activestudent.ui.components.LoadingBox
import com.anurag.activestudent.ui.hub.HubScreen
import com.anurag.activestudent.ui.hub.HubViewModel
import com.anurag.activestudent.ui.theme.AccentOrange
import com.anurag.activestudent.ui.theme.ActiveStudentTheme
import com.anurag.activestudent.ui.theme.BgDark
import com.anurag.activestudent.ui.theme.CardBg
import com.anurag.activestudent.ui.theme.TextMuted
import com.anurag.activestudent.ui.tracurrency.TracurrencyScreen
import com.anurag.activestudent.ui.tracurrency.TracurrencyViewModel
import com.anurag.activestudent.ui.voting.VotingScreen
import com.anurag.activestudent.ui.voting.VotingViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT)
        )
        setContent {
            ActiveStudentTheme {
                ActiveStudentRoot()
            }
        }
    }
}

@Composable
private fun ActiveStudentRoot() {
    val authVm: AuthViewModel = viewModel()
    val hubVm: HubViewModel = viewModel()
    val votingVm: VotingViewModel = viewModel()
    val tracurrencyVm: TracurrencyViewModel = viewModel()
    val canteenVm: CanteenViewModel = viewModel()

    val authState by authVm.uiState.collectAsState()
    var showRegister by remember { mutableStateOf(false) }

    when {
        authState.isInitializing -> {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(BgDark),
                contentAlignment = Alignment.Center
            ) {
                LoadingBox("Connecting to Active Student...")
            }
        }

        authState.user == null -> {
            LaunchedEffect(Unit) {
                hubVm.stopListening()
                votingVm.clearListeners()
                tracurrencyVm.clearListeners()
                canteenVm.clearListeners()
            }
            if (showRegister) {
                RegisterScreen(
                    uiState = authState,
                    onRegister = { name, roll, email, pass, confirm ->
                        authVm.register(name, roll, email, pass, confirm)
                    },
                    onNavigateToLogin = { showRegister = false },
                    onDismissError = { authVm.clearMessages() }
                )
            } else {
                LoginScreen(
                    uiState = authState,
                    onSignIn = { email, pass -> authVm.signIn(email, pass) },
                    onForgotPassword = { email -> authVm.sendPasswordReset(email) },
                    onNavigateToRegister = { showRegister = true },
                    onDismissError = { authVm.clearMessages() }
                )
            }
        }

        else -> {
            val user = authState.user!!
            var selectedTab by remember(user.uid) {
                mutableIntStateOf(
                    when {
                        FirebaseRepo.isVotingAdmin(user.email) -> 1
                        FirebaseRepo.isTracurrencyAdmin(user.email) -> 2
                        FirebaseRepo.isCanteenAdmin(user.email) -> 3
                        else -> 0
                    }
                )
            }

            LaunchedEffect(user.uid, user.email) {
                hubVm.ensureListening(user.uid)
                votingVm.bindUser(user)
                tracurrencyVm.bindUser(user)
                canteenVm.bindUser(user)
            }

            val hubState by hubVm.uiState.collectAsState()
            val votingState by votingVm.uiState.collectAsState()
            val tracurrencyState by tracurrencyVm.uiState.collectAsState()
            val canteenState by canteenVm.uiState.collectAsState()

            Scaffold(
                containerColor = BgDark,
                bottomBar = {
                    NavigationBar(containerColor = CardBg) {
                        val navColors = NavigationBarItemDefaults.colors(
                            selectedIconColor = AccentOrange,
                            selectedTextColor = AccentOrange,
                            indicatorColor = AccentOrange.copy(alpha = 0.16f),
                            unselectedIconColor = TextMuted,
                            unselectedTextColor = TextMuted
                        )
                        NavigationBarItem(
                            selected = selectedTab == 0,
                            onClick = { selectedTab = 0 },
                            icon = { Icon(Icons.Default.Home, contentDescription = "Hub") },
                            label = { Text("Hub", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                            colors = navColors
                        )
                        NavigationBarItem(
                            selected = selectedTab == 1,
                            onClick = { selectedTab = 1 },
                            icon = { Icon(Icons.Default.HowToVote, contentDescription = "Voting") },
                            label = { Text("Voting", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                            colors = navColors
                        )
                        NavigationBarItem(
                            selected = selectedTab == 2,
                            onClick = { selectedTab = 2 },
                            icon = { Icon(Icons.Default.AccountBalanceWallet, contentDescription = "Ledger") },
                            label = { Text("Ledger", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                            colors = navColors
                        )
                        NavigationBarItem(
                            selected = selectedTab == 3,
                            onClick = { selectedTab = 3 },
                            icon = { Icon(Icons.Default.Fastfood, contentDescription = "Canteen") },
                            label = { Text("Canteen", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                            colors = navColors
                        )
                    }
                }
            ) { innerPadding ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(BgDark)
                        .padding(innerPadding)
                ) {
                    when (selectedTab) {
                        0 -> HubScreen(
                            user = user,
                            hubState = hubState,
                            onOpenVoting = { selectedTab = 1 },
                            onOpenTracurrency = { selectedTab = 2 },
                            onOpenCanteen = { selectedTab = 3 },
                            onLogout = { authVm.signOut() }
                        )
                        1 -> VotingScreen(state = votingState, viewModel = votingVm)
                        2 -> TracurrencyScreen(state = tracurrencyState, viewModel = tracurrencyVm)
                        3 -> CanteenScreen(state = canteenState, viewModel = canteenVm)
                    }
                }
            }
        }
    }
}

