package com.anurag.activestudent.ui.components

import android.graphics.Bitmap
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.SubcomposeAsyncImage
import com.anurag.activestudent.ui.theme.AccentOrange
import com.anurag.activestudent.ui.theme.CardBg
import com.anurag.activestudent.ui.theme.CardBorder
import com.anurag.activestudent.ui.theme.InputBg
import com.anurag.activestudent.ui.theme.RedDanger
import com.anurag.activestudent.ui.theme.RedSoft
import com.anurag.activestudent.ui.theme.TextMain
import com.anurag.activestudent.ui.theme.TextMuted
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import androidx.compose.foundation.text.KeyboardOptions

@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    borderColor: Color = CardBorder,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    if (onClick != null) {
        Card(
            onClick = onClick,
            modifier = modifier,
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = CardBg),
            border = BorderStroke(1.dp, borderColor)
        ) {
            Column(modifier = Modifier.padding(16.dp), content = content)
        }
    } else {
        Card(
            modifier = modifier,
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = CardBg),
            border = BorderStroke(1.dp, borderColor)
        ) {
            Column(modifier = Modifier.padding(16.dp), content = content)
        }
    }
}

@Composable
fun AppTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String = "",
    modifier: Modifier = Modifier,
    singleLine: Boolean = true,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    trailingIcon: (@Composable () -> Unit)? = null
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, color = TextMuted, fontSize = 12.sp) },
        placeholder = { Text(placeholder, color = TextMuted.copy(alpha = 0.5f)) },
        singleLine = singleLine,
        visualTransformation = visualTransformation,
        keyboardOptions = keyboardOptions,
        trailingIcon = trailingIcon,
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = AccentOrange,
            unfocusedBorderColor = CardBorder,
            focusedContainerColor = InputBg,
            unfocusedContainerColor = InputBg,
            focusedTextColor = TextMain,
            unfocusedTextColor = TextMain,
            cursorColor = AccentOrange
        ),
        modifier = modifier.fillMaxWidth()
    )
}

@Composable
fun StatusPill(
    text: String,
    color: Color,
    backgroundColor: Color = color.copy(alpha = 0.14f)
) {
    Surface(
        color = backgroundColor,
        shape = RoundedCornerShape(999.dp),
        border = BorderStroke(1.dp, color.copy(alpha = 0.35f))
    ) {
        Text(
            text = text,
            color = color,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
        )
    }
}

@Composable
fun ErrorBanner(
    message: String?,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (message.isNullOrBlank()) return
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = RedSoft,
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, RedDanger.copy(alpha = 0.45f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.ErrorOutline,
                contentDescription = null,
                tint = RedDanger,
                modifier = Modifier.size(18.dp)
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = message,
                color = Color(0xFFFCA5A5),
                fontSize = 13.sp,
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = onDismiss, modifier = Modifier.size(24.dp)) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Dismiss",
                    tint = Color(0xFFFCA5A5),
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Composable
fun LoadingBox(message: String = "Loading...") {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 40.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator(color = AccentOrange, strokeWidth = 3.dp)
            Spacer(Modifier.height(12.dp))
            Text(message, color = TextMuted, fontSize = 13.sp)
        }
    }
}

@Composable
fun AvatarOrImage(
    url: String,
    name: String,
    modifier: Modifier = Modifier,
    isCircle: Boolean = false,
    accentBorder: Color? = null
) {
    val shape = if (isCircle) CircleShape else RoundedCornerShape(12.dp)
    val initials = remember(name) {
        name.trim()
            .split(" ")
            .filter { it.isNotBlank() }
            .take(2)
            .joinToString("") { it.first().uppercaseChar().toString() }
            .ifBlank { "A" }
    }

    val boxModifier = modifier
        .clip(shape)
        .background(Color(0xFF232326))
        .let { if (accentBorder != null) it.border(2.dp, accentBorder, shape) else it }

    Box(modifier = boxModifier, contentAlignment = Alignment.Center) {
        if (url.isNotBlank() && (url.startsWith("http://") || url.startsWith("https://"))) {
            SubcomposeAsyncImage(
                model = url,
                contentDescription = name,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
                loading = {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(
                            color = AccentOrange,
                            strokeWidth = 2.dp,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                },
                error = {
                    MonogramFallback(initials)
                }
            )
        } else {
            MonogramFallback(initials)
        }
    }
}

@Composable
private fun MonogramFallback(initials: String) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF26221D)),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = initials,
            color = AccentOrange,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 18.sp
        )
    }
}

@Composable
fun QrCodeImage(
    content: String,
    size: Dp = 180.dp,
    modifier: Modifier = Modifier
) {
    val bitmap = remember(content) {
        generateQrBitmap(content, 512)
    }
    Surface(
        modifier = modifier.size(size),
        color = Color.White,
        shape = RoundedCornerShape(14.dp)
    ) {
        if (bitmap != null) {
            Image(
                bitmap = bitmap.asImageBitmap(),
                contentDescription = "QR Code for $content",
                modifier = Modifier
                    .fillMaxSize()
                    .padding(10.dp),
                contentScale = ContentScale.Fit
            )
        } else {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(content, color = Color.Black, fontSize = 11.sp)
            }
        }
    }
}

private fun generateQrBitmap(content: String, sizePx: Int): Bitmap? {
    if (content.isBlank()) return null
    return try {
        val bitMatrix = MultiFormatWriter().encode(
            content,
            BarcodeFormat.QR_CODE,
            sizePx,
            sizePx
        )
        val pixels = IntArray(sizePx * sizePx)
        for (y in 0 until sizePx) {
            val offset = y * sizePx
            for (x in 0 until sizePx) {
                pixels[offset + x] = if (bitMatrix[x, y]) android.graphics.Color.BLACK else android.graphics.Color.WHITE
            }
        }
        Bitmap.createBitmap(sizePx, sizePx, Bitmap.Config.ARGB_8888).apply {
            setPixels(pixels, 0, sizePx, 0, 0, sizePx, sizePx)
        }
    } catch (_: Exception) {
        null
    }
}

