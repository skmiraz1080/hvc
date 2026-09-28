package com.example.ui.screens.dashboard

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.BusinessCenter
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ActivityItem
import com.example.data.model.FinancialSummary
import com.example.data.model.User
import com.example.ui.components.StatCard
import com.example.ui.theme.AccentGold
import com.example.ui.theme.EmeraldContainer
import com.example.ui.theme.EmeraldOnContainer
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.GoldTertiary
import com.example.ui.theme.StatusPaid
import com.example.ui.theme.StatusUnpaid
import com.example.util.BanglaUtils

@Composable
fun DashboardScreen(
    summary: FinancialSummary,
    activities: List<ActivityItem>,
    currentUser: User?,
    selectedMonth: String,
    selectedYear: Int,
    onNavigateToDeposits: () -> Unit,
    onNavigateToBusiness: () -> Unit,
    onNavigateToExpenses: () -> Unit,
    onNavigateToMembers: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(6.dp))

            // Organization Hero Header Card (Current Total Fund)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("current_total_fund_card"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = EmeraldPrimary),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "বর্তমান মোট ফান্ড (Current Fund)",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(alpha = 0.85f)
                        )
                        Surface(
                            shape = CircleShape,
                            color = Color.White.copy(alpha = 0.2f),
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.AccountBalance,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = BanglaUtils.formatTaka(summary.currentTotalFund),
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White,
                            fontSize = 32.sp
                        )
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "হিসাব সূত্র: মোট জমা + ব্যবসার লাভ - মোট খরচ",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                }
            }
        }

        // Monthly Target Progress Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("monthly_progress_card"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "এই মাসের জমা ($selectedMonth)",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "লক্ষ্যমাত্রা: ৳ ১০,০০০ (১০ জন × ৳ ১,০০০)",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Text(
                            text = "${BanglaUtils.formatTaka(summary.currentMonthCollected)} / ৳ ১০,০০০",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = EmeraldPrimary
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    val progress = (summary.currentMonthCollected / 10000.0).coerceIn(0.0, 1.0).toFloat()
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = EmeraldPrimary,
                        trackColor = EmeraldContainer
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    val paidCount = (summary.currentMonthCollected / 1000.0).toInt().coerceAtMost(10)
                    Text(
                        text = "${BanglaUtils.formatNumber(paidCount)} জন পরিশোধ করেছেন, ${BanglaUtils.formatNumber(10 - paidCount)} জন বকেয়া",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // 4 Core Financial Cards (2x2 Grid)
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    StatCard(
                        title = "মোট সদস্য",
                        value = "${BanglaUtils.formatNumber(summary.totalMembers)} জন",
                        icon = Icons.Default.Group,
                        accentColor = EmeraldPrimary,
                        subtitle = "সর্বোচ্চ ১০ জন",
                        modifier = Modifier
                            .weight(1f)
                            .testTag("stat_total_members"),
                        onClick = onNavigateToMembers
                    )

                    StatCard(
                        title = "মোট জমা",
                        value = BanglaUtils.formatTaka(summary.totalDeposited),
                        icon = Icons.Default.Savings,
                        accentColor = StatusPaid,
                        subtitle = "সদস্যদের সঞ্চয়",
                        modifier = Modifier
                            .weight(1f)
                            .testTag("stat_total_deposited"),
                        onClick = onNavigateToDeposits
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    StatCard(
                        title = "মোট ব্যবসার লাভ",
                        value = BanglaUtils.formatTaka(summary.totalBusinessProfit),
                        icon = Icons.Default.TrendingUp,
                        accentColor = GoldTertiary,
                        subtitle = "যৌথ লাভ",
                        modifier = Modifier
                            .weight(1f)
                            .testTag("stat_business_profit"),
                        onClick = onNavigateToBusiness
                    )

                    StatCard(
                        title = "মোট খরচ",
                        value = BanglaUtils.formatTaka(summary.totalExpenses),
                        icon = Icons.Default.ReceiptLong,
                        accentColor = StatusUnpaid,
                        subtitle = "ক্লাব ব্যয়",
                        modifier = Modifier
                            .weight(1f)
                            .testTag("stat_total_expenses"),
                        onClick = onNavigateToExpenses
                    )
                }
            }
        }

        // Admin Quick Actions (If Admin)
        if (currentUser?.isAdmin == true) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "অ্যাডমিন দ্রুত পদক্ষেপ (Quick Actions)",
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                            color = EmeraldPrimary
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = onNavigateToDeposits,
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("জমা", fontSize = 12.sp)
                            }
                            OutlinedButton(
                                onClick = onNavigateToBusiness,
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("ব্যবসা", fontSize = 12.sp)
                            }
                            OutlinedButton(
                                onClick = onNavigateToExpenses,
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("খরচ", fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        }

        // Recent Activities Section Header
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "সাম্প্রতিক লেনদেন (Recent Activities)",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        if (activities.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "কোন সাম্প্রতিক কার্যক্রম নেই",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            items(activities) { activity ->
                ActivityRow(activity)
            }
        }

        item {
            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
fun ActivityRow(activity: ActivityItem) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                val (icon, iconColor) = when (activity) {
                    is ActivityItem.DepositActivity -> Pair(Icons.Default.Savings, StatusPaid)
                    is ActivityItem.ProfitActivity -> Pair(Icons.Default.TrendingUp, AccentGold)
                    is ActivityItem.ExpenseActivity -> Pair(Icons.Default.ArrowDownward, StatusUnpaid)
                }

                Surface(
                    shape = CircleShape,
                    color = iconColor.copy(alpha = 0.12f),
                    modifier = Modifier.size(40.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = iconColor,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = activity.title,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                        maxLines = 1
                    )
                    Text(
                        text = activity.subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            val prefix = when (activity) {
                is ActivityItem.ExpenseActivity -> "-"
                else -> "+"
            }
            val amountColor = when (activity) {
                is ActivityItem.ExpenseActivity -> StatusUnpaid
                is ActivityItem.ProfitActivity -> GoldTertiary
                else -> StatusPaid
            }

            Text(
                text = "$prefix ${BanglaUtils.formatTaka(activity.amount)}",
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = amountColor
                )
            )
        }
    }
}
