package com.example.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.User
import com.example.ui.theme.AccentGold
import com.example.ui.theme.EmeraldContainer
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.StatusPaid
import com.example.ui.theme.StatusPaidBg
import com.example.ui.theme.StatusUnpaid
import com.example.ui.theme.StatusUnpaidBg
import com.example.util.BanglaUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClubTopAppBar(
    currentUser: User?,
    isCloudConnected: Boolean,
    onOpenGuide: () -> Unit,
    onLogout: () -> Unit
) {
    TopAppBar(
        navigationIcon = {
            Image(
                painter = painterResource(id = R.drawable.ic_club_logo),
                contentDescription = "Club Logo",
                modifier = Modifier
                    .padding(start = 12.dp, end = 6.dp)
                    .size(38.dp)
                    .clip(CircleShape)
            )
        },
        title = {
            Column {
                Text(
                    text = "হবিরবাড়ি ভলেন্টিয়ার্স ক্লাব",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "প্রতিষ্ঠা: ১ জুলাই ২০২৫ • ১০ সদস্য সঞ্চয়",
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        actions = {
            // Cloud Status indicator
            IconButton(
                onClick = onOpenGuide,
                modifier = Modifier.testTag("cloud_status_button")
            ) {
                if (isCloudConnected) {
                    Icon(
                        imageVector = Icons.Default.CloudDone,
                        contentDescription = "Firebase Connected",
                        tint = EmeraldPrimary
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.CloudOff,
                        contentDescription = "Local Mode / Instructions",
                        tint = AccentGold
                    )
                }
            }

            // User Role badge
            if (currentUser != null) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (currentUser.isAdmin) EmeraldContainer else MaterialTheme.colorScheme.secondaryContainer,
                    modifier = Modifier.padding(end = 8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (currentUser.isAdmin) Icons.Default.AdminPanelSettings else Icons.Default.Person,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = if (currentUser.isAdmin) EmeraldPrimary else MaterialTheme.colorScheme.onSecondaryContainer
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (currentUser.isAdmin) "অ্যাডমিন" else "সদস্য",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = if (currentUser.isAdmin) EmeraldPrimary else MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    )
}

@Composable
fun StatCard(
    title: String,
    value: String,
    icon: ImageVector,
    accentColor: Color,
    bgColor: Color = MaterialTheme.colorScheme.surface,
    subtitle: String? = null,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = bgColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = value,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = accentColor,
                        fontSize = 20.sp
                    )
                )
                if (!subtitle.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Surface(
                shape = CircleShape,
                color = accentColor.copy(alpha = 0.12f),
                modifier = Modifier.size(46.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun MemberAvatar(
    name: String,
    isAdmin: Boolean = false,
    size: Int = 44,
    modifier: Modifier = Modifier
) {
    val initial = name.trim().firstOrNull()?.toString() ?: "স"
    Box(modifier = modifier) {
        Surface(
            shape = CircleShape,
            color = if (isAdmin) EmeraldPrimary else MaterialTheme.colorScheme.secondaryContainer,
            modifier = Modifier.size(size.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = initial,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = if (isAdmin) Color.White else MaterialTheme.colorScheme.onSecondaryContainer,
                        fontSize = (size * 0.4).sp
                    )
                )
            }
        }
        if (isAdmin) {
            Surface(
                shape = CircleShape,
                color = AccentGold,
                modifier = Modifier
                    .size((size * 0.38).dp)
                    .align(Alignment.BottomEnd)
                    .border(1.5.dp, Color.White, CircleShape)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.VerifiedUser,
                        contentDescription = "Admin",
                        tint = Color.White,
                        modifier = Modifier.size((size * 0.24).dp)
                    )
                }
            }
        }
    }
}

@Composable
fun StatusBadge(
    isPaid: Boolean,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = if (isPaid) StatusPaidBg else StatusUnpaidBg,
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (isPaid) Icons.Default.CheckCircle else Icons.Default.HourglassEmpty,
                contentDescription = null,
                modifier = Modifier.size(13.dp),
                tint = if (isPaid) StatusPaid else StatusUnpaid
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = if (isPaid) "পরিশোধিত" else "বকেয়া",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 11.sp
                ),
                color = if (isPaid) StatusPaid else StatusUnpaid
            )
        }
    }
}

@Composable
fun MonthSelector(
    selectedMonth: String,
    selectedYear: Int,
    onMonthSelected: (String) -> Unit,
    onYearSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    var expandedMonth by remember { mutableStateOf(false) }
    var expandedYear by remember { mutableStateOf(false) }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Month Picker
        Box(modifier = Modifier.weight(1f)) {
            FilterChip(
                selected = true,
                onClick = { expandedMonth = true },
                label = { Text("মাস: $selectedMonth") },
                modifier = Modifier.fillMaxWidth().testTag("month_filter_chip")
            )
            DropdownMenu(
                expanded = expandedMonth,
                onDismissRequest = { expandedMonth = false }
            ) {
                BanglaUtils.BENGALI_MONTHS.forEach { month ->
                    DropdownMenuItem(
                        text = { Text(month) },
                        onClick = {
                            onMonthSelected(month)
                            expandedMonth = false
                        }
                    )
                }
            }
        }

        // Year Picker
        Box(modifier = Modifier.weight(1f)) {
            FilterChip(
                selected = true,
                onClick = { expandedYear = true },
                label = { Text("বছর: ${BanglaUtils.toBanglaDigits(selectedYear.toString())}") },
                modifier = Modifier.fillMaxWidth().testTag("year_filter_chip")
            )
            DropdownMenu(
                expanded = expandedYear,
                onDismissRequest = { expandedYear = false }
            ) {
                listOf(2025, 2026, 2027).forEach { year ->
                    DropdownMenuItem(
                        text = { Text(BanglaUtils.toBanglaDigits(year.toString())) },
                        onClick = {
                            onYearSelected(year)
                            expandedYear = false
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun ConfirmationDialog(
    title: String,
    message: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title, fontWeight = FontWeight.Bold) },
        text = { Text(message) },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("নিশ্চিত করুন", color = MaterialTheme.colorScheme.error)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("বাতিল")
            }
        }
    )
}
