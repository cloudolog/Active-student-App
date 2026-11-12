package com.anurag.activestudent.ui.tracurrency

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.anurag.activestudent.data.LedgerTx
import com.anurag.activestudent.data.TracurrencyEvent
import com.anurag.activestudent.data.formatRupees
import com.anurag.activestudent.data.formatShortDate
import com.anurag.activestudent.ui.components.AppTextField
import com.anurag.activestudent.ui.components.ErrorBanner
import com.anurag.activestudent.ui.components.GlassCard
import com.anurag.activestudent.ui.components.LoadingBox
import com.anurag.activestudent.ui.components.QrCodeImage
import com.anurag.activestudent.ui.components.StatusPill
import com.anurag.activestudent.ui.theme.AccentOrange
import com.anurag.activestudent.ui.theme.BgDark
import com.anurag.activestudent.ui.theme.BlueInfo
import com.anurag.activestudent.ui.theme.CardBg
import com.anurag.activestudent.ui.theme.GreenSoft
import com.anurag.activestudent.ui.theme.GreenSuccess
import com.anurag.activestudent.ui.theme.RedDanger
import com.anurag.activestudent.ui.theme.TextMain
import com.anurag.activestudent.ui.theme.TextMuted
import java.util.Locale

@Composable
fun TracurrencyScreen(
    state: TracurrencyUiState,
    viewModel: TracurrencyViewModel
) {
    var showCreateDialog by remember { mutableStateOf(false) }
    val selectedEvent = state.selectedEvent

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
                if (selectedEvent != null) {
                    IconButton(onClick = { viewModel.exitEvent() }) {
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
                        text = selectedEvent?.fundTitle ?: "Tracurrency",
                        color = TextMain,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text(
                        text = if (state.isAdmin) "Admin Ledger Control" else "Transparent Event Ledger",
                        color = TextMuted,
                        fontSize = 12.sp
                    )
                }
            }

            if (state.isAdmin && selectedEvent == null) {
                Button(
                    onClick = { showCreateDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = AccentOrange, contentColor = Color.Black),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("New Room", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            } else if (state.isAdmin) {
                StatusPill(text = "ADMIN", color = AccentOrange)
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

        if (selectedEvent == null) {
            if (state.isLoading) {
                LoadingBox("Loading financial rooms...")
            } else if (state.events.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No active financial rooms found.", color = TextMuted)
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(state.events, key = { it.id }) { ev ->
                        TracurrencyLobbyCard(
                            event = ev,
                            isAdmin = state.isAdmin,
                            onOpen = { viewModel.openEvent(ev) },
                            onDelete = { viewModel.deleteRoom(ev.id) }
                        )
                    }
                }
            }
        } else {
            EventDetailContent(
                event = selectedEvent,
                state = state,
                viewModel = viewModel
            )
        }
    }

    if (showCreateDialog) {
        var title by remember { mutableStateOf("") }
        var budget by remember { mutableStateOf("") }
        var minContrib by remember { mutableStateOf("") }
        var fixedContrib by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showCreateDialog = false },
            containerColor = CardBg,
            title = { Text("Create Financial Room", color = TextMain, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    AppTextField(value = title, onValueChange = { title = it }, label = "Event / Room Title", placeholder = "e.g. InnoQuest #5")
                    AppTextField(
                        value = budget,
                        onValueChange = { budget = it },
                        label = "Initial Budget (₹)",
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                    AppTextField(
                        value = minContrib,
                        onValueChange = { minContrib = it },
                        label = "Min Contribution (₹, optional)",
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                    AppTextField(
                        value = fixedContrib,
                        onValueChange = { fixedContrib = it },
                        label = "Fixed Contribution (₹, optional)",
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.createRoom(title, budget, minContrib, fixedContrib)
                        showCreateDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AccentOrange, contentColor = Color.Black)
                ) { Text("Create Room", fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(onClick = { showCreateDialog = false }) {
                    Text("Cancel", color = TextMuted)
                }
            }
        )
    }
}

@Composable
private fun TracurrencyLobbyCard(
    event: TracurrencyEvent,
    isAdmin: Boolean,
    onOpen: () -> Unit,
    onDelete: () -> Unit
) {
    GlassCard(modifier = Modifier.fillMaxWidth(), onClick = onOpen) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(event.fundTitle, color = TextMain, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "Created: ${formatShortDate(event.createdAtMillis)}",
                    color = TextMuted,
                    fontSize = 12.sp
                )
            }
            if (isAdmin) {
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete Room", tint = RedDanger)
                }
            }
        }

        Spacer(Modifier.height(14.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
            Column {
                Text("INITIAL BUDGET", color = TextMuted, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                Text(
                    text = formatRupees(event.fundsGiven),
                    color = TextMain,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
            }
            if (event.fixedContribution > 0) {
                Column {
                    Text("FIXED CONTRIB", color = TextMuted, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    Text(
                        text = formatRupees(event.fixedContribution),
                        color = AccentOrange,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }
            } else if (event.minContribution > 0) {
                Column {
                    Text("MIN DEPOSIT", color = TextMuted, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    Text(
                        text = formatRupees(event.minContribution),
                        color = AccentOrange,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }
            }
        }

        Spacer(Modifier.height(12.dp))
        Text("View Ledger →", color = AccentOrange, fontWeight = FontWeight.Bold, fontSize = 13.sp)
    }
}

@Composable
private fun EventDetailContent(
    event: TracurrencyEvent,
    state: TracurrencyUiState,
    viewModel: TracurrencyViewModel
) {
    var activeTab by remember { mutableIntStateOf(0) }

    Column(modifier = Modifier.fillMaxSize()) {
        // KPI Summary Card
        GlassCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp),
            borderColor = AccentOrange.copy(alpha = 0.35f)
        ) {
            Text("LIVE BALANCE", color = TextMuted, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(4.dp))
            Text(
                text = formatRupees(state.liveBalance),
                color = AccentOrange,
                fontSize = 30.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.ExtraBold
            )
            Spacer(Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                MetricMini("Initial", formatRupees(event.fundsGiven), TextMain)
                MetricMini("Collected", "+${formatRupees(state.totalCollected)}", GreenSuccess)
                MetricMini("Spent", "-${formatRupees(state.totalSpent)}", RedDanger)
            }
        }

        Spacer(Modifier.height(10.dp))

        TabRow(
            selectedTabIndex = activeTab,
            containerColor = BgDark,
            contentColor = AccentOrange,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[activeTab]),
                    color = AccentOrange
                )
            }
        ) {
            Tab(selected = activeTab == 0, onClick = { activeTab = 0 }, text = { Text("Ledger", fontWeight = FontWeight.Bold) })
            Tab(
                selected = activeTab == 1,
                onClick = { activeTab = 1 },
                text = { Text(if (state.isAdmin) "Expense / Edit" else "Contribute", fontWeight = FontWeight.Bold) }
            )
            Tab(selected = activeTab == 2, onClick = { activeTab = 2 }, text = { Text("Analytics", fontWeight = FontWeight.Bold) })
        }

        when (activeTab) {
            0 -> LedgerTab(state.credits, state.debits, state.isAdmin, onDeleteTx = { viewModel.deleteTx(it) })
            1 -> TransactTab(event, state.isAdmin, state.isActionBusy, viewModel)
            2 -> AnalyticsTab(state.credits)
        }
    }
}

@Composable
private fun MetricMini(label: String, value: String, color: Color) {
    Column {
        Text(label.uppercase(), color = TextMuted, fontSize = 10.sp, fontWeight = FontWeight.Bold)
        Text(value, color = color, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, fontSize = 14.sp)
    }
}

@Composable
private fun LedgerTab(
    credits: List<LedgerTx>,
    debits: List<LedgerTx>,
    isAdmin: Boolean,
    onDeleteTx: (LedgerTx) -> Unit
) {
    var showCredits by remember { mutableStateOf(true) }
    val activeList = if (showCredits) credits else debits

    LazyColumn(
        contentPadding = PaddingValues(18.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                FilterChip(
                    selected = showCredits,
                    onClick = { showCredits = true },
                    label = { Text("Contributions (${credits.size})") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = GreenSoft,
                        selectedLabelColor = GreenSuccess
                    )
                )
                FilterChip(
                    selected = !showCredits,
                    onClick = { showCredits = false },
                    label = { Text("Expenditures (${debits.size})") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = RedDanger.copy(alpha = 0.2f),
                        selectedLabelColor = RedDanger
                    )
                )
            }
        }

        if (activeList.isEmpty()) {
            item {
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = if (showCredits) "No contributions recorded yet." else "No expenditures recorded yet.",
                        color = TextMuted,
                        fontSize = 13.sp
                    )
                }
            }
        } else {
            items(activeList, key = { "${it.collection}_${it.id}" }) { tx ->
                val isCredit = tx.type == "credit"
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (isCredit) tx.userName.ifBlank { tx.desc } else tx.desc.ifBlank { "Expense" },
                                color = TextMain,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                            Text(
                                text = if (isCredit) "${tx.desc} • ${tx.dateStr}" else "by ${tx.userName} • ${tx.dateStr}",
                                color = TextMuted,
                                fontSize = 12.sp
                            )
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "${if (isCredit) "+" else "-"}${formatRupees(tx.amount)}",
                                color = if (isCredit) GreenSuccess else RedDanger,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 15.sp
                            )
                            if (isAdmin) {
                                IconButton(
                                    onClick = { onDeleteTx(tx) },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(Icons.Default.Delete, contentDescription = "Reverse", tint = RedDanger, modifier = Modifier.size(16.dp))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TransactTab(
    event: TracurrencyEvent,
    isAdmin: Boolean,
    isBusy: Boolean,
    viewModel: TracurrencyViewModel
) {
    val context = LocalContext.current
    var amount by remember(event.id) {
        mutableStateOf(
            if (!isAdmin && event.fixedContribution > 0) event.fixedContribution.toInt().toString() else ""
        )
    }
    var purpose by remember(event.id) { mutableStateOf("") }

    var editTitle by remember(event.id, event.fundTitle) { mutableStateOf(event.fundTitle) }
    var editMin by remember(event.id, event.minContribution) {
        mutableStateOf(if (event.minContribution > 0) event.minContribution.toInt().toString() else "")
    }
    var editFixed by remember(event.id, event.fixedContribution) {
        mutableStateOf(if (event.fixedContribution > 0) event.fixedContribution.toInt().toString() else "")
    }

    val upiUri = "upi://pay?pa=tej.2334-6@waaxis&pn=Tracurrency&cu=INR"

    LazyColumn(
        contentPadding = PaddingValues(18.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = if (isAdmin) "Record Expenditure (Debit)" else "Record Contribution (Credit)",
                    color = TextMain,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(6.dp))
                if (!isAdmin && event.fixedContribution > 0) {
                    Text("Fixed contribution required: ${formatRupees(event.fixedContribution)}", color = AccentOrange, fontSize = 12.sp)
                } else if (!isAdmin && event.minContribution > 0) {
                    Text("Minimum contribution: ${formatRupees(event.minContribution)}", color = AccentOrange, fontSize = 12.sp)
                }
                Spacer(Modifier.height(12.dp))
                AppTextField(
                    value = amount,
                    onValueChange = { amount = it },
                    label = "Amount (₹)",
                    placeholder = "e.g. 500",
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                Spacer(Modifier.height(10.dp))
                AppTextField(
                    value = purpose,
                    onValueChange = { purpose = it },
                    label = if (isAdmin) "Expense Description" else "Note / Purpose",
                    placeholder = if (isAdmin) "e.g. Stage Lights" else "e.g. Event Pass"
                )
                Spacer(Modifier.height(14.dp))
                Button(
                    onClick = {
                        viewModel.submitTransaction(amount, purpose)
                        if (isAdmin || event.fixedContribution == 0.0) amount = ""
                        purpose = ""
                    },
                    enabled = !isBusy,
                    colors = ButtonDefaults.buttonColors(containerColor = AccentOrange, contentColor = Color.Black),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = if (isAdmin) "Record Expenditure" else "Submit Contribution",
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        if (!isAdmin) {
            item {
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "CONTRIBUTE VIA UPI",
                            color = TextMuted,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(Modifier.height(12.dp))
                        QrCodeImage(content = upiUri, size = 160.dp)
                        Spacer(Modifier.height(10.dp))
                        Text("Scan QR for direct UPI deposit", color = TextMuted, fontSize = 12.sp)
                        Spacer(Modifier.height(10.dp))
                        OutlinedButton(
                            onClick = {
                                try {
                                    context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(upiUri)))
                                } catch (_: Exception) {
                                    // Safely ignore if no UPI app installed on device/emulator
                                }
                            },
                            border = BorderStroke(1.dp, AccentOrange),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.AutoMirrored.Filled.OpenInNew, contentDescription = null, tint = AccentOrange, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Open UPI App", color = AccentOrange, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        } else {
            item {
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Text("Room Rules & Settings", color = TextMain, fontSize = 17.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(12.dp))
                    AppTextField(value = editTitle, onValueChange = { editTitle = it }, label = "Room Title")
                    Spacer(Modifier.height(8.dp))
                    AppTextField(
                        value = editMin,
                        onValueChange = { editMin = it },
                        label = "Min Contribution (₹)",
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                    Spacer(Modifier.height(8.dp))
                    AppTextField(
                        value = editFixed,
                        onValueChange = { editFixed = it },
                        label = "Fixed Contribution (₹)",
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                    Spacer(Modifier.height(12.dp))
                    Button(
                        onClick = { viewModel.saveSettings(event.id, editTitle, editMin, editFixed) },
                        enabled = !isBusy,
                        colors = ButtonDefaults.buttonColors(containerColor = AccentOrange, contentColor = Color.Black),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) { Text("Save Settings", fontWeight = FontWeight.Bold) }
                }
            }
        }
    }
}

private val EMAIL_REGEX = Regex("^(\\d{2})eg105([a-z])(\\d{2})@anurag\\.edu\\.in$")
private val CHART_PALETTE = listOf(
    Color(0xFFFF9A2C),
    Color(0xFF3B82F6),
    Color(0xFF10B981),
    Color(0xFFEF4444),
    Color(0xFF8B5CF6),
    Color(0xFFF59E0B),
    Color(0xFF06B6D4),
    Color(0xFFEC4899)
)

@Composable
private fun AnalyticsTab(credits: List<LedgerTx>) {
    var selectedYear by remember { mutableStateOf("") } // "", "26", "25", "24", "23", "22"
    var selectedSection by remember { mutableStateOf("") }

    val yearOptions = listOf("" to "All Years", "25" to "Batch 25", "24" to "Batch 24", "23" to "Batch 23", "22" to "Batch 22")
    val sectionOptions = listOf("") + ('A'..'V').map { it.toString() }

    val filteredStats = remember(credits, selectedYear, selectedSection) {
        var total = 0.0
        var count = 0
        val breakdown = linkedMapOf<String, Int>()

        credits.forEach { tx ->
            val match = EMAIL_REGEX.matchEntire(tx.userEmail.trim().lowercase(Locale.ROOT))
            if (match != null) {
                val batch = match.groupValues[1]
                val sec = match.groupValues[2].uppercase(Locale.ROOT)
                val matchY = selectedYear.isBlank() || batch == selectedYear
                val matchS = selectedSection.isBlank() || sec == selectedSection
                if (matchY && matchS) {
                    total += tx.amount
                    count++
                }
                if (selectedYear.isNotBlank()) {
                    if (batch == selectedYear) {
                        val key = "Section $sec"
                        breakdown[key] = (breakdown[key] ?: 0) + 1
                    }
                } else {
                    val key = when (batch) {
                        "26" -> "1st Year (26)"
                        "25" -> "2nd Year (25)"
                        "24" -> "3rd Year (24)"
                        "23" -> "4th Year (23)"
                        "22" -> "Batch 22"
                        else -> "Batch $batch"
                    }
                    breakdown[key] = (breakdown[key] ?: 0) + 1
                }
            } else if (selectedYear.isBlank() && selectedSection.isBlank()) {
                val key = "Other / Manual"
                breakdown[key] = (breakdown[key] ?: 0) + 1
                total += tx.amount
                count++
            }
        }
        Triple(total, count, breakdown.toList().sortedByDescending { it.second })
    }

    LazyColumn(
        contentPadding = PaddingValues(18.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Text("Filter by Batch / Year", color = TextMuted, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(6.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(yearOptions) { (code, label) ->
                        FilterChip(
                            selected = selectedYear == code,
                            onClick = { selectedYear = code },
                            label = { Text(label, fontSize = 12.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = AccentOrange.copy(alpha = 0.2f),
                                selectedLabelColor = AccentOrange
                            )
                        )
                    }
                }

                Spacer(Modifier.height(10.dp))
                Text("Filter by Section", color = TextMuted, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(6.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(sectionOptions) { sec ->
                        FilterChip(
                            selected = selectedSection == sec,
                            onClick = { selectedSection = sec },
                            label = { Text(if (sec.isBlank()) "All" else sec, fontSize = 12.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = BlueInfo.copy(alpha = 0.2f),
                                selectedLabelColor = BlueInfo
                            )
                        )
                    }
                }

                Spacer(Modifier.height(14.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("MATCHED COLLECTED", color = TextMuted, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        Text(
                            text = formatRupees(filteredStats.first),
                            color = GreenSuccess,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("CONTRIBUTORS", color = TextMuted, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        Text(
                            text = "${filteredStats.second}",
                            color = TextMain,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }
            }
        }

        item {
            val slices = filteredStats.third
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = if (selectedYear.isNotBlank()) "PAYMENTS BY SECTION" else "PAYMENTS BY BATCH",
                    color = AccentOrange,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(14.dp))
                if (slices.isEmpty()) {
                    Text("No contributor data to chart yet.", color = TextMuted, fontSize = 13.sp)
                } else {
                    val totalCount = slices.sumOf { it.second }.coerceAtLeast(1)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Canvas(modifier = Modifier.size(130.dp)) {
                            var startAngle = -90f
                            slices.forEachIndexed { idx, (_, count) ->
                                val sweep = (count.toFloat() / totalCount.toFloat()) * 360f
                                drawArc(
                                    color = CHART_PALETTE[idx % CHART_PALETTE.size],
                                    startAngle = startAngle,
                                    sweepAngle = sweep,
                                    useCenter = false,
                                    style = Stroke(width = 28f, cap = StrokeCap.Butt)
                                )
                                startAngle += sweep
                            }
                        }
                        Spacer(Modifier.width(18.dp))
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.weight(1f)) {
                            slices.forEachIndexed { idx, (label, count) ->
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(10.dp)
                                            .clip(CircleShape)
                                            .background(CHART_PALETTE[idx % CHART_PALETTE.size])
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    Text(
                                        text = "$label ($count)",
                                        color = TextMain,
                                        fontSize = 12.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

