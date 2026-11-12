package com.anurag.activestudent.ui.voting

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Public
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.anurag.activestudent.data.Candidate
import com.anurag.activestudent.data.ElectionRoom
import com.anurag.activestudent.data.UserVote
import com.anurag.activestudent.data.formatShortDate
import com.anurag.activestudent.ui.components.AppTextField
import com.anurag.activestudent.ui.components.AvatarOrImage
import com.anurag.activestudent.ui.components.ErrorBanner
import com.anurag.activestudent.ui.components.GlassCard
import com.anurag.activestudent.ui.components.LoadingBox
import com.anurag.activestudent.ui.components.StatusPill
import com.anurag.activestudent.ui.theme.AccentOrange
import com.anurag.activestudent.ui.theme.AccentOrangeSoft
import com.anurag.activestudent.ui.theme.BgDark
import com.anurag.activestudent.ui.theme.CardBg
import com.anurag.activestudent.ui.theme.GreenSoft
import com.anurag.activestudent.ui.theme.GreenSuccess
import com.anurag.activestudent.ui.theme.RedDanger
import com.anurag.activestudent.ui.theme.TextMain
import com.anurag.activestudent.ui.theme.TextMuted
import com.anurag.activestudent.ui.theme.YellowWarn

@Composable
fun VotingScreen(
    state: VotingUiState,
    viewModel: VotingViewModel
) {
    var secretPromptRoom by remember { mutableStateOf<ElectionRoom?>(null) }
    var secretInput by remember { mutableStateOf("") }
    var secretError by remember { mutableStateOf<String?>(null) }
    var showCreateRoomDialog by remember { mutableStateOf(false) }

    val selectedRoom = state.selectedRoom

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BgDark)
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                if (selectedRoom != null) {
                    IconButton(onClick = { viewModel.exitRoom() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = TextMain
                        )
                    }
                    Spacer(Modifier.width(4.dp))
                }
                Column {
                    Text(
                        text = selectedRoom?.title ?: if (state.isAdmin) "Voting Admin Center" else "Campus Voting",
                        color = TextMain,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text(
                        text = if (state.isAdmin) "Manage rooms, candidates & live polls" else "Transparent digital elections",
                        color = TextMuted,
                        fontSize = 12.sp
                    )
                }
            }

            if (state.isAdmin && selectedRoom == null) {
                Button(
                    onClick = { showCreateRoomDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = AccentOrange, contentColor = Color.Black),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("New Room", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }
        }

        if (!state.errorMessage.isNullOrBlank()) {
            Box(Modifier.padding(horizontal = 18.dp, vertical = 4.dp)) {
                ErrorBanner(message = state.errorMessage, onDismiss = { viewModel.clearMessages() })
            }
        }
        if (!state.successMessage.isNullOrBlank()) {
            Surface(
                color = GreenSoft,
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, GreenSuccess.copy(alpha = 0.4f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp, vertical = 4.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(state.successMessage, color = GreenSuccess, fontSize = 13.sp, modifier = Modifier.weight(1f))
                    TextButton(onClick = { viewModel.clearMessages() }) {
                        Text("OK", color = GreenSuccess, fontSize = 12.sp)
                    }
                }
            }
        }

        if (selectedRoom == null) {
            if (!state.isAdmin) {
                TabRow(
                    selectedTabIndex = state.selectedTab,
                    containerColor = BgDark,
                    contentColor = AccentOrange,
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            modifier = Modifier.tabIndicatorOffset(tabPositions[state.selectedTab]),
                            color = AccentOrange
                        )
                    }
                ) {
                    Tab(
                        selected = state.selectedTab == 0,
                        onClick = { viewModel.setTab(0) },
                        text = { Text("Active Polls", fontWeight = FontWeight.Bold) }
                    )
                    Tab(
                        selected = state.selectedTab == 1,
                        onClick = { viewModel.setTab(1) },
                        text = { Text("Past Results", fontWeight = FontWeight.Bold) }
                    )
                }
            }

            if (state.isLoading) {
                LoadingBox("Syncing elections...")
            } else {
                val filteredRooms = when {
                    state.isAdmin -> state.allRooms
                    state.selectedTab == 0 -> state.allRooms.filter { it.status == "active" }
                    else -> state.allRooms.filter { it.status == "ended" }
                }

                if (filteredRooms.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = when {
                                    state.isAdmin -> "No Election Rooms Yet"
                                    state.selectedTab == 0 -> "No Active Elections"
                                    else -> "No Past Elections"
                                },
                                color = AccentOrange,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(Modifier.height(6.dp))
                            Text(
                                text = if (state.isAdmin) "Tap '+ New Room' to create one."
                                else "Check back when polls open or browse results.",
                                color = TextMuted,
                                fontSize = 13.sp
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(18.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(filteredRooms, key = { it.id }) { room ->
                            ElectionRoomCard(
                                room = room,
                                isAdmin = state.isAdmin,
                                onEnter = {
                                    if (!state.isAdmin && room.status == "active" && room.roomType == "private") {
                                        secretInput = ""
                                        secretError = null
                                        secretPromptRoom = room
                                    } else {
                                        viewModel.openRoom(room)
                                    }
                                },
                                onDelete = { viewModel.deleteElectionRoom(room.id) }
                            )
                        }
                    }
                }
            }
        } else {
            // Room Detail View
            if (state.isAdmin) {
                AdminRoomDetailView(
                    room = selectedRoom,
                    candidates = state.candidates,
                    isBusy = state.isActionBusy,
                    viewModel = viewModel
                )
            } else if (selectedRoom.status == "ended") {
                EndedResultsDetailView(
                    room = selectedRoom,
                    candidates = state.candidates
                )
            } else {
                StudentBallotView(
                    room = selectedRoom,
                    candidates = state.candidates,
                    userVote = state.userVote,
                    isBusy = state.isActionBusy,
                    onCastVote = { candidate -> viewModel.castVote(candidate, selectedRoom.id) }
                )
            }
        }
    }

    // Private Room Secret Key Dialog
    if (secretPromptRoom != null) {
        val target = secretPromptRoom!!
        AlertDialog(
            onDismissRequest = { secretPromptRoom = null },
            containerColor = CardBg,
            title = {
                Text("Private Election Room", color = TextMain, fontWeight = FontWeight.Bold)
            },
            text = {
                Column {
                    Text(
                        text = "Enter the access code for \"${target.title}\":",
                        color = TextMuted,
                        fontSize = 13.sp
                    )
                    Spacer(Modifier.height(12.dp))
                    AppTextField(
                        value = secretInput,
                        onValueChange = {
                            secretInput = it
                            secretError = null
                        },
                        label = "Access Code",
                        placeholder = "e.g. CS_A_2025"
                    )
                    if (!secretError.isNullOrBlank()) {
                        Spacer(Modifier.height(6.dp))
                        Text(secretError!!, color = RedDanger, fontSize = 12.sp)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (secretInput.trim().equals(target.secretKey.trim(), ignoreCase = true)) {
                            secretPromptRoom = null
                            viewModel.openRoom(target)
                        } else {
                            secretError = "Invalid Access Code."
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AccentOrange, contentColor = Color.Black)
                ) {
                    Text("Unlock & Enter", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { secretPromptRoom = null }) {
                    Text("Cancel", color = TextMuted)
                }
            }
        )
    }

    // Admin Create Room Dialog
    if (showCreateRoomDialog) {
        var title by remember { mutableStateOf("") }
        var isPrivate by remember { mutableStateOf(false) }
        var secret by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showCreateRoomDialog = false },
            containerColor = CardBg,
            title = { Text("Create Election Room", color = TextMain, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    AppTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = "Election Name",
                        placeholder = "e.g. Class Representative 2026"
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Private Room (Access Code)", color = TextMain, fontSize = 13.sp)
                        Switch(
                            checked = isPrivate,
                            onCheckedChange = { isPrivate = it },
                            colors = SwitchDefaults.colors(checkedThumbColor = AccentOrange)
                        )
                    }
                    if (isPrivate) {
                        AppTextField(
                            value = secret,
                            onValueChange = { secret = it },
                            label = "Secret Access Code",
                            placeholder = "e.g. CS_A_2026"
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.createElectionRoom(title, isPrivate, secret)
                        showCreateRoomDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AccentOrange, contentColor = Color.Black)
                ) {
                    Text("Create", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreateRoomDialog = false }) {
                    Text("Cancel", color = TextMuted)
                }
            }
        )
    }
}

@Composable
private fun ElectionRoomCard(
    room: ElectionRoom,
    isAdmin: Boolean,
    onEnter: () -> Unit,
    onDelete: () -> Unit
) {
    var confirmDelete by remember { mutableStateOf(false) }
    val isPrivate = room.roomType == "private"
    val statusColor = when (room.status) {
        "active" -> GreenSuccess
        "ended" -> RedDanger
        else -> YellowWarn
    }

    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        borderColor = if (isPrivate) AccentOrange.copy(alpha = 0.45f) else Color.White.copy(alpha = 0.08f),
        onClick = onEnter
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                Icon(
                    imageVector = if (isPrivate) Icons.Default.Lock else Icons.Default.Public,
                    contentDescription = null,
                    tint = if (isPrivate) AccentOrange else TextMuted,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = room.title,
                    color = TextMain,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            StatusPill(
                text = room.status.replace("_", " ").uppercase(),
                color = statusColor
            )
        }

        Spacer(Modifier.height(8.dp))
        Text(
            text = room.noticeMessage.ifBlank { "Tap to open election room." },
            color = TextMuted,
            fontSize = 13.sp
        )

        if (room.createdAtMillis > 0) {
            Spacer(Modifier.height(6.dp))
            Text(
                text = "Created: ${formatShortDate(room.createdAtMillis)}",
                color = TextMuted.copy(alpha = 0.7f),
                fontSize = 11.sp
            )
        }

        Spacer(Modifier.height(14.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (isAdmin) {
                OutlinedButton(
                    onClick = { confirmDelete = true },
                    border = BorderStroke(1.dp, RedDanger.copy(alpha = 0.6f)),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Delete", color = RedDanger, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            } else {
                Spacer(Modifier.width(1.dp))
            }

            Button(
                onClick = onEnter,
                colors = ButtonDefaults.buttonColors(containerColor = AccentOrange, contentColor = Color.Black),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text(
                    text = when {
                        isAdmin -> "Manage Room"
                        room.status == "ended" -> "View Results"
                        isPrivate -> "Unlock & Vote"
                        else -> "Enter Ballot"
                    },
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
            }
        }
    }

    if (confirmDelete) {
        AlertDialog(
            onDismissRequest = { confirmDelete = false },
            containerColor = CardBg,
            title = { Text("Delete Room?", color = TextMain, fontWeight = FontWeight.Bold) },
            text = { Text("This will delete \"${room.title}\" and all its candidates permanently.", color = TextMuted) },
            confirmButton = {
                Button(
                    onClick = {
                        confirmDelete = false
                        onDelete()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = RedDanger)
                ) {
                    Text("Delete", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { confirmDelete = false }) {
                    Text("Cancel", color = TextMuted)
                }
            }
        )
    }
}

@Composable
private fun StudentBallotView(
    room: ElectionRoom,
    candidates: List<Candidate>,
    userVote: UserVote?,
    isBusy: Boolean,
    onCastVote: (Candidate) -> Unit
) {
    var pendingVoteCandidate by remember { mutableStateOf<Candidate?>(null) }

    LazyColumn(
        contentPadding = PaddingValues(18.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        if (room.noticeMessage.isNotBlank()) {
            item {
                Surface(
                    color = AccentOrangeSoft,
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, AccentOrange.copy(alpha = 0.4f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Notice: ${room.noticeMessage}",
                        color = AccentOrange,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }
        }

        if (userVote != null) {
            item {
                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    borderColor = GreenSuccess.copy(alpha = 0.5f)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = GreenSuccess,
                            modifier = Modifier.size(56.dp)
                        )
                        Spacer(Modifier.height(12.dp))
                        Text(
                            text = "Vote Submitted!",
                            color = AccentOrange,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Spacer(Modifier.height(6.dp))
                        Text(
                            text = "You voted for ${userVote.candidateName} in this election.",
                            color = TextMain,
                            fontSize = 14.sp
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = "Thank you for participating.",
                            color = TextMuted,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        } else if (candidates.isEmpty()) {
            item {
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "No candidates added to this room yet.",
                        color = TextMuted,
                        fontSize = 14.sp
                    )
                }
            }
        } else {
            items(candidates, key = { it.id }) { candidate ->
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        AvatarOrImage(
                            url = candidate.img,
                            name = candidate.name,
                            isCircle = true,
                            accentBorder = AccentOrange,
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = candidate.name,
                                color = TextMain,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = candidate.position,
                                color = AccentOrange,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            if (candidate.motto.isNotBlank()) {
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    text = "\"${candidate.motto}\"",
                                    color = TextMuted,
                                    fontSize = 12.sp,
                                    fontStyle = FontStyle.Italic
                                )
                            }
                        }
                    }
                    Spacer(Modifier.height(14.dp))
                    Button(
                        onClick = { pendingVoteCandidate = candidate },
                        enabled = !isBusy,
                        colors = ButtonDefaults.buttonColors(containerColor = AccentOrange, contentColor = Color.Black),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Vote for ${candidate.name.substringBefore(" ")}",
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }

    if (pendingVoteCandidate != null) {
        val c = pendingVoteCandidate!!
        AlertDialog(
            onDismissRequest = { pendingVoteCandidate = null },
            containerColor = CardBg,
            title = { Text("Confirm Your Vote", color = TextMain, fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    text = "Confirm vote for ${c.name} (${c.position})? This action is anonymous and irreversible.",
                    color = TextMuted
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        pendingVoteCandidate = null
                        onCastVote(c)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AccentOrange, contentColor = Color.Black)
                ) {
                    Text("Confirm Vote", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { pendingVoteCandidate = null }) {
                    Text("Cancel", color = TextMuted)
                }
            }
        )
    }
}

@Composable
private fun EndedResultsDetailView(
    room: ElectionRoom,
    candidates: List<Candidate>
) {
    LazyColumn(
        contentPadding = PaddingValues(18.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Final Results", color = TextMain, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                StatusPill(text = "ENDED", color = RedDanger)
            }
        }

        if (candidates.isEmpty()) {
            item {
                Text("No candidates participated in this election.", color = TextMuted)
            }
        } else {
            itemsIndexed(candidates, key = { _, c -> c.id }) { index, c ->
                val isWinner = index == 0 && c.voteCount > 0
                val medal = when (index) {
                    0 -> "🥇"
                    1 -> "🥈"
                    2 -> "🥉"
                    else -> "#${index + 1}"
                }
                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    borderColor = if (isWinner) Color(0xFFFFD700) else Color.White.copy(alpha = 0.08f)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(medal, fontSize = 22.sp, modifier = Modifier.width(36.dp))
                        AvatarOrImage(
                            url = c.img,
                            name = c.name,
                            isCircle = true,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(c.name, color = TextMain, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                if (isWinner) {
                                    Spacer(Modifier.width(6.dp))
                                    StatusPill(text = "WINNER", color = Color(0xFFFFD700))
                                }
                            }
                            Text(c.position, color = TextMuted, fontSize = 12.sp)
                        }
                        Text(
                            text = "${c.voteCount}",
                            color = AccentOrange,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AdminRoomDetailView(
    room: ElectionRoom,
    candidates: List<Candidate>,
    isBusy: Boolean,
    viewModel: VotingViewModel
) {
    var noticeText by remember(room.id, room.noticeMessage) { mutableStateOf(room.noticeMessage) }
    var editTitle by remember(room.id, room.title) { mutableStateOf(room.title) }
    var editStart by remember(room.id, room.startTime) { mutableStateOf(room.startTime) }
    var editEnd by remember(room.id, room.endTime) { mutableStateOf(room.endTime) }
    var editSecret by remember(room.id, room.secretKey) { mutableStateOf(room.secretKey) }

    var showCandidateDialog by remember { mutableStateOf(false) }
    var editingCandidate by remember { mutableStateOf<Candidate?>(null) }

    val totalVotes = candidates.sumOf { it.voteCount }

    LazyColumn(
        contentPadding = PaddingValues(18.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Status & Live Votes
        item {
            GlassCard(modifier = Modifier.fillMaxWidth(), borderColor = AccentOrange.copy(alpha = 0.4f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("ELECTION STATUS", color = TextMuted, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = room.status.replace("_", " ").uppercase(),
                            color = when (room.status) {
                                "active" -> GreenSuccess
                                "ended" -> RedDanger
                                else -> YellowWarn
                            },
                            fontSize = 20.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("TOTAL VOTES", color = TextMuted, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Text(
                            text = "$totalVotes",
                            color = AccentOrange,
                            fontSize = 26.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }
                Spacer(Modifier.height(14.dp))
                when (room.status) {
                    "not_started" -> Button(
                        onClick = { viewModel.updateElectionStatus(room.id, "active") },
                        enabled = !isBusy,
                        colors = ButtonDefaults.buttonColors(containerColor = GreenSuccess, contentColor = Color.Black),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    ) { Text("Start Election", fontWeight = FontWeight.Bold) }

                    "active" -> Button(
                        onClick = { viewModel.updateElectionStatus(room.id, "ended") },
                        enabled = !isBusy,
                        colors = ButtonDefaults.buttonColors(containerColor = RedDanger, contentColor = Color.White),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    ) { Text("End Election", fontWeight = FontWeight.Bold) }

                    else -> Button(
                        onClick = { viewModel.updateElectionStatus(room.id, "active") },
                        enabled = !isBusy,
                        colors = ButtonDefaults.buttonColors(containerColor = AccentOrange, contentColor = Color.Black),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    ) { Text("Re-open Election", fontWeight = FontWeight.Bold) }
                }
            }
        }

        // Candidates Header + Add Button
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Candidates (${candidates.size})", color = TextMain, fontSize = 17.sp, fontWeight = FontWeight.Bold)
                Button(
                    onClick = {
                        editingCandidate = null
                        showCandidateDialog = true
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AccentOrange, contentColor = Color.Black),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Add Candidate", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }
        }

        items(candidates, key = { it.id }) { c ->
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    AvatarOrImage(
                        url = c.img,
                        name = c.name,
                        isCircle = true,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(c.name, color = TextMain, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        Text(c.position, color = AccentOrange, fontSize = 12.sp)
                        if (c.motto.isNotBlank()) {
                            Text(c.motto, color = TextMuted, fontSize = 11.sp, maxLines = 1)
                        }
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("${c.voteCount} votes", color = TextMain, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Row {
                            IconButton(
                                onClick = {
                                    editingCandidate = c
                                    showCandidateDialog = true
                                },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(Icons.Default.Edit, contentDescription = "Edit", tint = AccentOrange, modifier = Modifier.size(16.dp))
                            }
                            IconButton(
                                onClick = { viewModel.deleteCandidate(c.id) },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = RedDanger, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
            }
        }

        // Notice Board
        item {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Text("Notice Board", color = TextMain, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                Spacer(Modifier.height(10.dp))
                AppTextField(
                    value = noticeText,
                    onValueChange = { noticeText = it },
                    label = "Notice Message",
                    placeholder = "Voting begins at 10:00 AM...",
                    singleLine = false
                )
                Spacer(Modifier.height(10.dp))
                Button(
                    onClick = { viewModel.updateNotice(room.id, noticeText) },
                    enabled = !isBusy,
                    colors = ButtonDefaults.buttonColors(containerColor = AccentOrange, contentColor = Color.Black),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                ) { Text("Update Notice", fontWeight = FontWeight.Bold) }
            }
        }

        // Room Settings
        item {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Text("Room Settings", color = TextMain, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                Spacer(Modifier.height(10.dp))
                AppTextField(
                    value = editTitle,
                    onValueChange = { editTitle = it },
                    label = "Election Title"
                )
                Spacer(Modifier.height(8.dp))
                AppTextField(
                    value = editStart,
                    onValueChange = { editStart = it },
                    label = "Start Time (optional)",
                    placeholder = "e.g. 2026-11-15 10:00"
                )
                Spacer(Modifier.height(8.dp))
                AppTextField(
                    value = editEnd,
                    onValueChange = { editEnd = it },
                    label = "End Time (optional)",
                    placeholder = "e.g. 2026-11-15 17:00"
                )
                Spacer(Modifier.height(8.dp))
                AppTextField(
                    value = editSecret,
                    onValueChange = { editSecret = it },
                    label = "Access Key (leave blank for Public)",
                    placeholder = "Secret code"
                )
                Spacer(Modifier.height(12.dp))
                Button(
                    onClick = { viewModel.updateRoomSettings(room.id, editTitle, editStart, editEnd, editSecret) },
                    enabled = !isBusy,
                    colors = ButtonDefaults.buttonColors(containerColor = AccentOrange, contentColor = Color.Black),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                ) { Text("Save Settings", fontWeight = FontWeight.Bold) }
            }
        }
    }

    if (showCandidateDialog) {
        val editing = editingCandidate
        var name by remember(editing) { mutableStateOf(editing?.name.orEmpty()) }
        var position by remember(editing) { mutableStateOf(editing?.position.orEmpty()) }
        var motto by remember(editing) { mutableStateOf(editing?.motto.orEmpty()) }
        var img by remember(editing) { mutableStateOf(editing?.img.orEmpty()) }

        AlertDialog(
            onDismissRequest = { showCandidateDialog = false },
            containerColor = CardBg,
            title = {
                Text(
                    text = if (editing == null) "Add Candidate" else "Edit Candidate",
                    color = TextMain,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    AppTextField(value = name, onValueChange = { name = it }, label = "Name")
                    AppTextField(value = position, onValueChange = { position = it }, label = "Position")
                    AppTextField(value = motto, onValueChange = { motto = it }, label = "Motto")
                    AppTextField(value = img, onValueChange = { img = it }, label = "Image URL (optional)")
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.saveCandidate(room.id, editing?.id, name, position, motto, img)
                        showCandidateDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AccentOrange, contentColor = Color.Black)
                ) { Text("Save", fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(onClick = { showCandidateDialog = false }) {
                    Text("Cancel", color = TextMuted)
                }
            }
        )
    }
}

