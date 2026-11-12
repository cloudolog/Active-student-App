package com.anurag.activestudent.ui.canteen

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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.anurag.activestudent.data.CanteenItem
import com.anurag.activestudent.data.CanteenOrder
import com.anurag.activestudent.data.formatDateTime
import com.anurag.activestudent.data.formatRupees
import com.anurag.activestudent.ui.components.AppTextField
import com.anurag.activestudent.ui.components.AvatarOrImage
import com.anurag.activestudent.ui.components.ErrorBanner
import com.anurag.activestudent.ui.components.GlassCard
import com.anurag.activestudent.ui.components.LoadingBox
import com.anurag.activestudent.ui.components.QrCodeImage
import com.anurag.activestudent.ui.components.StatusPill
import com.anurag.activestudent.ui.theme.AccentOrange
import com.anurag.activestudent.ui.theme.BgDark
import com.anurag.activestudent.ui.theme.CardBg
import com.anurag.activestudent.ui.theme.GreenSoft
import com.anurag.activestudent.ui.theme.GreenSuccess
import com.anurag.activestudent.ui.theme.RedDanger
import com.anurag.activestudent.ui.theme.RedSoft
import com.anurag.activestudent.ui.theme.TextMain
import com.anurag.activestudent.ui.theme.TextMuted
import com.anurag.activestudent.ui.theme.YellowSoft
import com.anurag.activestudent.ui.theme.YellowWarn
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CanteenScreen(
    state: CanteenUiState,
    viewModel: CanteenViewModel
) {
    if (state.isAdmin) {
        AdminCanteenView(state = state, viewModel = viewModel)
    } else {
        StudentCanteenView(state = state, viewModel = viewModel)
    }

    // Digital Receipt + Native QR Modal
    if (state.activeReceipt != null) {
        val receipt = state.activeReceipt
        val subtotal = receipt.totalAmount / 1.05
        val gst = receipt.totalAmount - subtotal

        AlertDialog(
            onDismissRequest = { viewModel.closeReceipt() },
            containerColor = Color.White,
            title = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("ANURAG E-CANTEEN", color = Color.Black, fontWeight = FontWeight.ExtraBold, fontSize = 16.sp)
                    Text("DIGITAL ORDER RECEIPT", color = Color.Gray, fontSize = 11.sp)
                }
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = receipt.orderId,
                        color = Color.Black,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 18.sp
                    )
                    Text(
                        text = formatDateTime(receipt.timestampMillis),
                        color = Color.DarkGray,
                        fontSize = 11.sp
                    )
                    Spacer(Modifier.height(12.dp))
                    QrCodeImage(content = receipt.orderId, size = 160.dp)
                    Spacer(Modifier.height(12.dp))

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFF3F4F6), RoundedCornerShape(10.dp))
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        receipt.items.forEach { item ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "${item.qty} x ${item.name}",
                                    color = Color.Black,
                                    fontSize = 12.sp,
                                    modifier = Modifier.weight(1f),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = formatRupees(item.price * item.qty, showDecimals = true),
                                    color = Color.Black,
                                    fontSize = 12.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }
                        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("GST (5%)", color = Color.DarkGray, fontSize = 11.sp)
                            Text(formatRupees(gst, showDecimals = true), color = Color.DarkGray, fontSize = 11.sp)
                        }
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("TOTAL", color = Color.Black, fontWeight = FontWeight.ExtraBold, fontSize = 14.sp)
                            Text(
                                text = formatRupees(receipt.totalAmount, showDecimals = true),
                                color = Color.Black,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 14.sp
                            )
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    Text("Show this QR at the canteen counter", color = Color.Gray, fontSize = 11.sp)
                }
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.closeReceipt() },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Black, contentColor = Color.White),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Done", fontWeight = FontWeight.Bold)
                }
            }
        )
    }

    // Order Claimed Celebration Dialog
    if (state.claimedPopupOrderId != null) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissClaimedPopup() },
            containerColor = CardBg,
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = GreenSuccess)
                    Spacer(Modifier.width(8.dp))
                    Text("Order Claimed!", color = TextMain, fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Text(
                    text = "Order ${state.claimedPopupOrderId} has been verified and picked up. Enjoy your meal!",
                    color = TextMuted
                )
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.dismissClaimedPopup() },
                    colors = ButtonDefaults.buttonColors(containerColor = AccentOrange, contentColor = Color.Black)
                ) { Text("Awesome!", fontWeight = FontWeight.Bold) }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun StudentCanteenView(
    state: CanteenUiState,
    viewModel: CanteenViewModel
) {
    var activeTab by remember { mutableIntStateOf(0) } // 0 = Menu, 1 = My Orders
    var activeFilter by remember { mutableStateOf("All") }
    var searchQuery by remember { mutableStateOf("") }
    var showBillSheet by remember { mutableStateOf(false) }

    val cartCount = state.cart.sumOf { it.qty }
    val cartSubtotal = state.cart.sumOf { it.price * it.qty }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BgDark)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Online Canteen", color = TextMain, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold)
                    Text("Skip the queue • Pre-order meals", color = TextMuted, fontSize = 12.sp)
                }
                Surface(
                    onClick = { if (cartCount > 0) showBillSheet = true else activeTab = 1 },
                    color = AccentOrange.copy(alpha = 0.16f),
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, AccentOrange.copy(alpha = 0.4f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.ShoppingBag, contentDescription = null, tint = AccentOrange, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Cart ($cartCount)", color = AccentOrange, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }

            if (!state.errorMessage.isNullOrBlank()) {
                Box(Modifier.padding(horizontal = 18.dp, vertical = 4.dp)) {
                    ErrorBanner(message = state.errorMessage, onDismiss = { viewModel.clearMessages() })
                }
            }

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
                Tab(selected = activeTab == 0, onClick = { activeTab = 0 }, text = { Text("Menu", fontWeight = FontWeight.Bold) })
                Tab(
                    selected = activeTab == 1,
                    onClick = { activeTab = 1 },
                    text = { Text("My Orders (${state.myOrders.size})", fontWeight = FontWeight.Bold) }
                )
            }

            if (activeTab == 0) {
                if (state.isLoading) {
                    LoadingBox("Loading today's menu...")
                } else {
                    val categories = listOf("All", "Veg", "Non-Veg", "Essentials")
                    val filteredMenu = state.menuItems.filter { item ->
                        val matchCat = activeFilter == "All" || item.category.equals(activeFilter, ignoreCase = true)
                        val matchSearch = searchQuery.isBlank() ||
                            item.name.contains(searchQuery, ignoreCase = true) ||
                            item.desc.contains(searchQuery, ignoreCase = true)
                        matchCat && matchSearch
                    }

                    LazyColumn(
                        contentPadding = PaddingValues(
                            start = 18.dp,
                            end = 18.dp,
                            top = 14.dp,
                            bottom = if (cartCount > 0) 88.dp else 20.dp
                        ),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        item {
                            AppTextField(
                                value = searchQuery,
                                onValueChange = { searchQuery = it },
                                label = "Search Menu",
                                placeholder = "Biryani, Chai, Samosa..."
                            )
                            Spacer(Modifier.height(10.dp))
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                items(categories) { cat ->
                                    FilterChip(
                                        selected = activeFilter == cat,
                                        onClick = { activeFilter = cat },
                                        label = { Text(cat, fontWeight = FontWeight.SemiBold) },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = AccentOrange.copy(alpha = 0.22f),
                                            selectedLabelColor = AccentOrange
                                        )
                                    )
                                }
                            }
                        }

                        if (filteredMenu.isEmpty()) {
                            item {
                                GlassCard(modifier = Modifier.fillMaxWidth()) {
                                    Text("No items match your filter.", color = TextMuted)
                                }
                            }
                        } else {
                            items(filteredMenu, key = { it.id }) { item ->
                                val inCartQty = state.cart.find { it.id == item.id }?.qty ?: 0
                                CanteenMenuCard(
                                    item = item,
                                    qtyInCart = inCartQty,
                                    onAdd = { viewModel.addToCart(item) },
                                    onDelta = { delta -> viewModel.updateCartQty(item.id, delta) }
                                )
                            }
                        }
                    }
                }
            } else {
                // My Orders Tab
                if (state.myOrders.isEmpty()) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No orders placed yet.", color = TextMuted)
                    }
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(18.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(state.myOrders, key = { it.docId }) { order ->
                            val statusColor = when (order.status) {
                                "ready" -> GreenSuccess
                                "picked_up" -> TextMuted
                                else -> YellowWarn
                            }
                            GlassCard(
                                modifier = Modifier.fillMaxWidth(),
                                onClick = { viewModel.showReceipt(order) }
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = order.orderId,
                                        color = TextMain,
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp
                                    )
                                    StatusPill(
                                        text = order.status.replace("_", " ").uppercase(),
                                        color = statusColor
                                    )
                                }
                                Spacer(Modifier.height(6.dp))
                                Text(
                                    text = order.items.joinToString(", ") { "${it.qty}x ${it.name}" },
                                    color = TextMuted,
                                    fontSize = 13.sp
                                )
                                Spacer(Modifier.height(10.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "${formatDateTime(order.timestampMillis)} • ${formatRupees(order.totalAmount, true)}",
                                        color = AccentOrange,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.QrCode2, contentDescription = null, tint = TextMain, modifier = Modifier.size(16.dp))
                                        Spacer(Modifier.width(4.dp))
                                        Text("QR Receipt", color = TextMain, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Bottom Checkout Bar
        if (activeTab == 0 && cartCount > 0) {
            Surface(
                color = AccentOrange,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp, vertical = 12.dp),
                onClick = { showBillSheet = true }
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 18.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("$cartCount item(s) in cart", color = Color.Black, fontWeight = FontWeight.ExtraBold, fontSize = 14.sp)
                        Text(
                            text = "Total (incl. GST): ${formatRupees(cartSubtotal * 1.05, true)}",
                            color = Color.Black.copy(alpha = 0.75f),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    Text("Checkout →", color = Color.Black, fontWeight = FontWeight.ExtraBold, fontSize = 15.sp)
                }
            }
        }
    }

    if (showBillSheet && state.cart.isNotEmpty()) {
        val gst = cartSubtotal * 0.05
        val grandTotal = cartSubtotal + gst
        ModalBottomSheet(
            onDismissRequest = { showBillSheet = false },
            containerColor = CardBg
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text("Order Summary", color = TextMain, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold)
                state.cart.forEach { cartItem ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(cartItem.name, color = TextMain, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text("${formatRupees(cartItem.price)} each", color = TextMuted, fontSize = 12.sp)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(
                                onClick = { viewModel.updateCartQty(cartItem.id, -1) },
                                modifier = Modifier.size(30.dp)
                            ) {
                                Icon(Icons.Default.Remove, contentDescription = "Decrease", tint = AccentOrange, modifier = Modifier.size(16.dp))
                            }
                            Text("${cartItem.qty}", color = TextMain, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 8.dp))
                            IconButton(
                                onClick = { viewModel.updateCartQty(cartItem.id, 1) },
                                modifier = Modifier.size(30.dp)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = "Increase", tint = AccentOrange, modifier = Modifier.size(16.dp))
                            }
                        }
                        Text(
                            text = formatRupees(cartItem.price * cartItem.qty),
                            color = AccentOrange,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.width(64.dp),
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
                HorizontalDivider(color = Color.White.copy(alpha = 0.08f))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Subtotal", color = TextMuted, fontSize = 13.sp)
                    Text(formatRupees(cartSubtotal, true), color = TextMuted, fontSize = 13.sp)
                }
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("GST (5%)", color = TextMuted, fontSize = 13.sp)
                    Text(formatRupees(gst, true), color = TextMuted, fontSize = 13.sp)
                }
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Grand Total", color = TextMain, fontWeight = FontWeight.ExtraBold, fontSize = 16.sp)
                    Text(formatRupees(grandTotal, true), color = AccentOrange, fontWeight = FontWeight.ExtraBold, fontSize = 18.sp)
                }
                Spacer(Modifier.height(6.dp))
                Button(
                    onClick = { viewModel.placeOrder { showBillSheet = false } },
                    enabled = !state.isActionBusy,
                    colors = ButtonDefaults.buttonColors(containerColor = AccentOrange, contentColor = Color.Black),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                ) {
                    Text(
                        text = if (state.isActionBusy) "Placing Order..." else "Pay & Place Order",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 15.sp
                    )
                }
                Spacer(Modifier.height(14.dp))
            }
        }
    }
}

@Composable
private fun CanteenMenuCard(
    item: CanteenItem,
    qtyInCart: Int,
    onAdd: () -> Unit,
    onDelta: (Int) -> Unit
) {
    val isOut = item.stock <= 0
    val catColor = when (item.category.lowercase(Locale.ROOT)) {
        "veg" -> GreenSuccess
        "non-veg" -> RedDanger
        else -> AccentOrange
    }

    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            AvatarOrImage(
                url = item.img,
                name = item.name,
                modifier = Modifier.size(84.dp)
            )
            Spacer(Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(catColor)
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = item.category.uppercase(),
                            color = catColor,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    StatusPill(
                        text = if (isOut) "OUT OF STOCK" else "${item.stock} left",
                        color = if (isOut) RedDanger else if (item.stock < 10) YellowWarn else GreenSuccess
                    )
                }
                Spacer(Modifier.height(4.dp))
                Text(item.name, color = TextMain, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                if (item.desc.isNotBlank()) {
                    Text(
                        text = item.desc,
                        color = TextMuted,
                        fontSize = 12.sp,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Spacer(Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = formatRupees(item.price),
                        color = AccentOrange,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 17.sp
                    )
                    if (isOut) {
                        OutlinedButton(onClick = {}, enabled = false, shape = RoundedCornerShape(10.dp)) {
                            Text("Sold Out", fontSize = 12.sp)
                        }
                    } else if (qtyInCart > 0) {
                        Surface(
                            color = AccentOrange,
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(onClick = { onDelta(-1) }, modifier = Modifier.size(32.dp)) {
                                    Icon(Icons.Default.Remove, contentDescription = "Less", tint = Color.Black, modifier = Modifier.size(16.dp))
                                }
                                Text("$qtyInCart", color = Color.Black, fontWeight = FontWeight.ExtraBold, modifier = Modifier.padding(horizontal = 4.dp))
                                IconButton(onClick = { onDelta(1) }, modifier = Modifier.size(32.dp)) {
                                    Icon(Icons.Default.Add, contentDescription = "More", tint = Color.Black, modifier = Modifier.size(16.dp))
                                }
                            }
                        }
                    } else {
                        Button(
                            onClick = onAdd,
                            colors = ButtonDefaults.buttonColors(containerColor = AccentOrange, contentColor = Color.Black),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                        ) {
                            Text("Add", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AdminCanteenView(
    state: CanteenUiState,
    viewModel: CanteenViewModel
) {
    var activeTab by remember { mutableIntStateOf(0) } // 0 = Queue & Verify, 1 = Menu, 2 = Stats
    var orderCodeInput by remember { mutableStateOf("") }
    var showMenuDialog by remember { mutableStateOf(false) }
    var editingItem by remember { mutableStateOf<CanteenItem?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BgDark)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("Canteen Staff", color = TextMain, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold)
                Text("Orders queue, verification & menu", color = TextMuted, fontSize = 12.sp)
            }
            if (activeTab == 1) {
                Button(
                    onClick = {
                        editingItem = null
                        showMenuDialog = true
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AccentOrange, contentColor = Color.Black),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Add Item", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            } else {
                StatusPill(text = "${state.adminActiveOrders.count { it.status == "pending" }} PENDING", color = AccentOrange)
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
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp, vertical = 4.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
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
            Tab(selected = activeTab == 0, onClick = { activeTab = 0 }, text = { Text("Orders", fontWeight = FontWeight.Bold) })
            Tab(selected = activeTab == 1, onClick = { activeTab = 1 }, text = { Text("Menu (${state.menuItems.size})", fontWeight = FontWeight.Bold) })
            Tab(selected = activeTab == 2, onClick = { activeTab = 2 }, text = { Text("Analytics", fontWeight = FontWeight.Bold) })
        }

        when (activeTab) {
            0 -> {
                LazyColumn(
                    contentPadding = PaddingValues(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        GlassCard(modifier = Modifier.fillMaxWidth(), borderColor = AccentOrange.copy(alpha = 0.4f)) {
                            Text("Order Verifier", color = TextMain, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            Spacer(Modifier.height(8.dp))
                            AppTextField(
                                value = orderCodeInput,
                                onValueChange = { orderCodeInput = it },
                                label = "Order ID",
                                placeholder = "e.g. ORD-123456-789"
                            )
                            Spacer(Modifier.height(10.dp))
                            Button(
                                onClick = { viewModel.verifyAndClaimOrder(orderCodeInput) },
                                colors = ButtonDefaults.buttonColors(containerColor = AccentOrange, contentColor = Color.Black),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Verify & Claim Order", fontWeight = FontWeight.Bold)
                            }

                            when (val res = state.scanResult) {
                                ScanResultState.Idle -> {}
                                ScanResultState.Checking -> {
                                    Spacer(Modifier.height(8.dp))
                                    Text("Checking order...", color = YellowWarn, fontSize = 13.sp)
                                }
                                is ScanResultState.Invalid -> {
                                    Spacer(Modifier.height(10.dp))
                                    Surface(color = RedSoft, shape = RoundedCornerShape(10.dp), modifier = Modifier.fillMaxWidth()) {
                                        Text("INVALID ID: ${res.orderId}", color = RedDanger, fontWeight = FontWeight.Bold, modifier = Modifier.padding(12.dp))
                                    }
                                }
                                is ScanResultState.AlreadyClaimed -> {
                                    Spacer(Modifier.height(10.dp))
                                    Surface(color = YellowSoft, shape = RoundedCornerShape(10.dp), modifier = Modifier.fillMaxWidth()) {
                                        Text(
                                            text = "ALREADY CLAIMED (${res.orderId}) at ${formatDateTime(res.claimedAtMillis)}",
                                            color = YellowWarn,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(12.dp)
                                        )
                                    }
                                }
                                is ScanResultState.Approved -> {
                                    Spacer(Modifier.height(10.dp))
                                    Surface(color = GreenSoft, shape = RoundedCornerShape(10.dp), modifier = Modifier.fillMaxWidth()) {
                                        Column(Modifier.padding(12.dp)) {
                                            Text("APPROVED ✅", color = GreenSuccess, fontWeight = FontWeight.ExtraBold, fontSize = 16.sp)
                                            Text("Student: ${res.order.userName}", color = TextMain, fontSize = 13.sp)
                                            Text("Total: ${formatRupees(res.order.totalAmount, true)}", color = AccentOrange, fontWeight = FontWeight.Bold)
                                            Text(res.order.items.joinToString(", ") { "${it.qty}x ${it.name}" }, color = TextMuted, fontSize = 12.sp)
                                        }
                                    }
                                }
                                is ScanResultState.Error -> {
                                    Spacer(Modifier.height(10.dp))
                                    Text(res.message, color = RedDanger, fontSize = 12.sp)
                                }
                            }
                        }
                    }

                    item {
                        Text("Active Orders Queue (${state.adminActiveOrders.size})", color = TextMain, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }

                    if (state.adminActiveOrders.isEmpty()) {
                        item {
                            GlassCard(modifier = Modifier.fillMaxWidth()) {
                                Text("No pending or ready orders in queue.", color = TextMuted, fontSize = 13.sp)
                            }
                        }
                    } else {
                        items(state.adminActiveOrders, key = { it.docId }) { order ->
                            val isReady = order.status == "ready"
                            GlassCard(
                                modifier = Modifier.fillMaxWidth(),
                                borderColor = if (isReady) GreenSuccess.copy(alpha = 0.4f) else Color.White.copy(alpha = 0.08f)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(order.orderId, color = TextMain, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                                    StatusPill(order.status.uppercase(), if (isReady) GreenSuccess else YellowWarn)
                                }
                                Spacer(Modifier.height(4.dp))
                                Text(order.items.joinToString(", ") { "${it.qty}x ${it.name}" }, color = TextMuted, fontSize = 13.sp)
                                Text("${order.userName} • ${formatRupees(order.totalAmount, true)}", color = TextMuted, fontSize = 11.sp)
                                Spacer(Modifier.height(10.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    if (!isReady) {
                                        Button(
                                            onClick = { viewModel.updateOrderStatus(order.docId, "ready") },
                                            colors = ButtonDefaults.buttonColors(containerColor = GreenSuccess, contentColor = Color.Black),
                                            shape = RoundedCornerShape(8.dp),
                                            modifier = Modifier.weight(1f)
                                        ) { Text("Mark Ready", fontWeight = FontWeight.Bold, fontSize = 12.sp) }
                                    }
                                    OutlinedButton(
                                        onClick = {
                                            orderCodeInput = order.orderId
                                            viewModel.verifyAndClaimOrder(order.orderId)
                                        },
                                        border = BorderStroke(1.dp, AccentOrange),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.weight(1f)
                                    ) { Text("Claim Now", color = AccentOrange, fontWeight = FontWeight.Bold, fontSize = 12.sp) }
                                }
                            }
                        }
                    }
                }
            }

            1 -> {
                LazyColumn(
                    contentPadding = PaddingValues(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(state.menuItems, key = { it.id }) { item ->
                        GlassCard(modifier = Modifier.fillMaxWidth()) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                AvatarOrImage(url = item.img, name = item.name, modifier = Modifier.size(60.dp))
                                Spacer(Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(item.name, color = TextMain, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                    Text("${item.category} • ${formatRupees(item.price)} • Stock: ${item.stock}", color = AccentOrange, fontSize = 12.sp)
                                }
                                IconButton(onClick = {
                                    editingItem = item
                                    showMenuDialog = true
                                }) {
                                    Icon(Icons.Default.Edit, contentDescription = "Edit", tint = AccentOrange)
                                }
                                IconButton(onClick = { viewModel.deleteMenuItem(item.id) }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = RedDanger)
                                }
                            }
                        }
                    }
                }
            }

            2 -> {
                val totalRevenue = state.adminCompletedOrders.sumOf { it.totalAmount }
                val totalCount = state.adminCompletedOrders.size
                val avgOrder = if (totalCount > 0) totalRevenue / totalCount else 0.0
                val topProducts = remember(state.adminCompletedOrders) {
                    val map = mutableMapOf<String, Int>()
                    state.adminCompletedOrders.forEach { o ->
                        o.items.forEach { i ->
                            map[i.name] = (map[i.name] ?: 0) + i.qty
                        }
                    }
                    map.entries.sortedByDescending { it.value }.take(8)
                }

                LazyColumn(
                    contentPadding = PaddingValues(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        GlassCard(modifier = Modifier.fillMaxWidth(), borderColor = AccentOrange.copy(alpha = 0.4f)) {
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Column {
                                    Text("REVENUE", color = TextMuted, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    Text(formatRupees(totalRevenue), color = AccentOrange, fontSize = 24.sp, fontWeight = FontWeight.ExtraBold)
                                }
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("ORDERS", color = TextMuted, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    Text("$totalCount", color = TextMain, fontSize = 24.sp, fontWeight = FontWeight.ExtraBold)
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text("AVG", color = TextMuted, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    Text(formatRupees(avgOrder), color = GreenSuccess, fontSize = 24.sp, fontWeight = FontWeight.ExtraBold)
                                }
                            }
                        }
                    }

                    item {
                        Text("Top Selling Products", color = TextMain, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }

                    if (topProducts.isEmpty()) {
                        item { Text("No completed sales yet.", color = TextMuted) }
                    } else {
                        val pieColors = listOf(
                            AccentOrange, GreenSuccess, Color(0xFF6366F1), RedDanger,
                            YellowWarn, Color(0xFFA855F7), Color(0xFF06B6D4), Color(0xFFEC4899)
                        )
                        val totalSold = topProducts.sumOf { it.value }.coerceAtLeast(1)
                        item {
                            GlassCard(modifier = Modifier.fillMaxWidth()) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Canvas(modifier = Modifier.size(120.dp)) {
                                        var startAngle = -90f
                                        topProducts.forEachIndexed { idx, entry ->
                                            val sweep = (entry.value.toFloat() / totalSold.toFloat()) * 360f
                                            drawArc(
                                                color = pieColors[idx % pieColors.size],
                                                startAngle = startAngle,
                                                sweepAngle = sweep,
                                                useCenter = true
                                            )
                                            startAngle += sweep
                                        }
                                    }
                                    Spacer(Modifier.width(16.dp))
                                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                        topProducts.forEachIndexed { idx, entry ->
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(10.dp)
                                                        .clip(CircleShape)
                                                        .background(pieColors[idx % pieColors.size])
                                                )
                                                Spacer(Modifier.width(8.dp))
                                                Text("${entry.key}: ${entry.value} sold", color = TextMain, fontSize = 12.sp)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showMenuDialog) {
        val editing = editingItem
        var name by remember(editing) { mutableStateOf(editing?.name.orEmpty()) }
        var price by remember(editing) { mutableStateOf(editing?.price?.toInt()?.toString().orEmpty()) }
        var stock by remember(editing) { mutableStateOf(editing?.stock?.toString() ?: "50") }
        var category by remember(editing) { mutableStateOf(editing?.category ?: "Veg") }
        var desc by remember(editing) { mutableStateOf(editing?.desc.orEmpty()) }
        var img by remember(editing) { mutableStateOf(editing?.img.orEmpty()) }

        AlertDialog(
            onDismissRequest = { showMenuDialog = false },
            containerColor = CardBg,
            title = { Text(if (editing == null) "Add Menu Item" else "Edit Menu Item", color = TextMain, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    AppTextField(value = name, onValueChange = { name = it }, label = "Item Name")
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        AppTextField(
                            value = price,
                            onValueChange = { price = it },
                            label = "Price (₹)",
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f)
                        )
                        AppTextField(
                            value = stock,
                            onValueChange = { stock = it },
                            label = "Stock",
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf("Veg", "Non-Veg", "Essentials").forEach { cat ->
                            FilterChip(
                                selected = category == cat,
                                onClick = { category = cat },
                                label = { Text(cat, fontSize = 11.sp) }
                            )
                        }
                    }
                    AppTextField(value = desc, onValueChange = { desc = it }, label = "Description")
                    AppTextField(value = img, onValueChange = { img = it }, label = "Image URL (optional)")
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.saveMenuItem(editing?.id, name, price, stock, category, desc, img)
                        showMenuDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AccentOrange, contentColor = Color.Black)
                ) { Text("Save", fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(onClick = { showMenuDialog = false }) {
                    Text("Cancel", color = TextMuted)
                }
            }
        )
    }
}

