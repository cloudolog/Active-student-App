package com.anurag.activestudent.ui.hub

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.HowToVote
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.anurag.activestudent.data.FirebaseRepo
import com.anurag.activestudent.data.UserProfile
import com.anurag.activestudent.data.formatRupees
import com.anurag.activestudent.ui.components.GlassCard
import com.anurag.activestudent.ui.components.StatusPill
import com.anurag.activestudent.ui.theme.AccentOrange
import com.anurag.activestudent.ui.theme.AccentOrangeSoft
import com.anurag.activestudent.ui.theme.BgDark
import com.anurag.activestudent.ui.theme.CardBg
import com.anurag.activestudent.ui.theme.GreenSuccess
import com.anurag.activestudent.ui.theme.RedDanger
import com.anurag.activestudent.ui.theme.TextMain
import com.anurag.activestudent.ui.theme.TextMuted

@Composable
fun HubScreen(
    user: UserProfile,
    hubState: HubUiState,
    onOpenVoting: () -> Unit,
    onOpenTracurrency: () -> Unit,
    onOpenCanteen: () -> Unit,
    onLogout: () -> Unit
) {
    var showLogoutDialog by remember { mutableStateOf(false) }

    val roleLabel = when {
        FirebaseRepo.isSuperAdmin(user.email) -> "SUPER ADMIN"
        FirebaseRepo.isVotingAdmin(user.email, user.role) -> "VOTING ADMIN"
        FirebaseRepo.isTracurrencyAdmin(user.email, user.role) -> "TRACURRENCY ADMIN"
        FirebaseRepo.isCanteenAdmin(user.email, user.role) -> "CANTEEN ADMIN"
        user.role.isNotBlank() && user.role != "student" -> user.role.replace('_', ' ').uppercase()
        else -> user.rollNo.ifBlank { "STUDENT" }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(BgDark),
        contentPadding = PaddingValues(18.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = buildAnnotatedString {
                            withStyle(SpanStyle(color = TextMain, fontWeight = FontWeight.ExtraBold)) {
                                append("Active")
                            }
                            withStyle(SpanStyle(color = AccentOrange, fontWeight = FontWeight.ExtraBold)) {
                                append("Student")
                            }
                        },
                        fontSize = 24.sp
                    )
                    Text(
                        text = "Anurag University • Connected Hub",
                        color = TextMuted,
                        fontSize = 12.sp
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = user.fullName,
                            color = TextMain,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                        Text(
                            text = roleLabel,
                            color = AccentOrange,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(Modifier.width(8.dp))
                    IconButton(
                        onClick = { showLogoutDialog = true },
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(CardBg)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Logout,
                            contentDescription = "Sign Out",
                            tint = TextMuted,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }

        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(
                        Brush.linearGradient(
                            listOf(Color(0xFF1F160E), Color(0xFF141416))
                        )
                    )
                    .padding(20.dp)
            ) {
                Column {
                    StatusPill(text = "LIVE FIREBASE BACKEND", color = GreenSuccess)
                    Spacer(Modifier.height(12.dp))
                    Text(
                        text = "Everything You Need,\nOne Campus App.",
                        color = TextMain,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.ExtraBold,
                        lineHeight = 29.sp
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = "Real-time campus voting, transparent event ledgers, and cashless canteen pre-ordering.",
                        color = TextMuted,
                        fontSize = 13.sp,
                        lineHeight = 18.sp
                    )
                }
            }
        }

        item {
            HubModuleCard(
                icon = Icons.Default.HowToVote,
                title = "Campus Voting",
                description = "Secure, encrypted digital election system. Vote in public or private campus rooms.",
                badgeText = when {
                    !hubState.votingConnected -> "Connecting..."
                    hubState.activeElectionsCount > 0 -> "● LIVE NOW (${hubState.activeElectionsCount})"
                    else -> "No Active Polls"
                },
                badgeColor = if (hubState.activeElectionsCount > 0) GreenSuccess else TextMuted,
                ctaText = if (FirebaseRepo.isVotingAdmin(user.email)) "Open Voting Admin" else "Launch Voting",
                onClick = onOpenVoting
            )
        }

        item {
            HubModuleCard(
                icon = Icons.Default.AccountBalanceWallet,
                title = "Tracurrency",
                description = "Track every event contribution and expenditure in real time with section analytics.",
                badgeText = if (hubState.ledgerConnected) formatRupees(hubState.ledgerBalance) else "Syncing...",
                badgeColor = AccentOrange,
                useMonospaceBadge = true,
                ctaText = "View Tracurrency",
                onClick = onOpenTracurrency
            )
        }

        item {
            HubModuleCard(
                icon = Icons.Default.Fastfood,
                title = "Online Canteen",
                description = "Skip the queue! Browse the live menu, pre-order meals, and claim with your QR receipt.",
                badgeText = if (hubState.pendingOrdersCount > 0) {
                    "${hubState.pendingOrdersCount} Active Order(s)"
                } else {
                    "${hubState.menuItemsCount} Menu Items"
                },
                badgeColor = if (hubState.pendingOrdersCount > 0) AccentOrange else TextMuted,
                ctaText = if (FirebaseRepo.isCanteenAdmin(user.email)) "Open Canteen Staff" else "Order Food",
                onClick = onOpenCanteen
            )
        }
    }

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            containerColor = CardBg,
            title = { Text("Sign Out?", color = TextMain, fontWeight = FontWeight.Bold) },
            text = { Text("You are signed in as ${user.email}.", color = TextMuted) },
            confirmButton = {
                Button(
                    onClick = {
                        showLogoutDialog = false
                        onLogout()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = RedDanger)
                ) {
                    Text("Sign Out", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text("Cancel", color = TextMuted)
                }
            }
        )
    }
}

@Composable
private fun HubModuleCard(
    icon: ImageVector,
    title: String,
    description: String,
    badgeText: String,
    badgeColor: Color,
    useMonospaceBadge: Boolean = false,
    ctaText: String,
    onClick: () -> Unit
) {
    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                color = AccentOrangeSoft,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.size(44.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = title,
                        tint = AccentOrange,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Surface(
                color = Color.White.copy(alpha = 0.06f),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = badgeText,
                    color = badgeColor,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = if (useMonospaceBadge) FontFamily.Monospace else FontFamily.Default,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                )
            }
        }

        Spacer(Modifier.height(14.dp))
        Text(text = title, color = TextMain, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(6.dp))
        Text(text = description, color = TextMuted, fontSize = 13.sp, lineHeight = 19.sp)
        Spacer(Modifier.height(16.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(Color.White.copy(alpha = 0.05f))
                .padding(horizontal = 14.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = ctaText, color = AccentOrange, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = null,
                tint = AccentOrange,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

