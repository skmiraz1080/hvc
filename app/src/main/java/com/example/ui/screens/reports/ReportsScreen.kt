package com.example.ui.screens.reports

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.BusinessCenter
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Business
import com.example.data.model.Deposit
import com.example.data.model.Expense
import com.example.data.model.FinancialSummary
import com.example.data.model.User
import com.example.ui.components.MemberAvatar
import com.example.ui.components.MonthSelector
import com.example.ui.components.StatusBadge
import com.example.ui.theme.EmeraldContainer
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.GoldTertiary
import com.example.ui.theme.StatusPaid
import com.example.ui.theme.StatusUnpaid
import com.example.util.BanglaUtils

@Composable
fun ReportsScreen(
    members: List<User>,
    deposits: List<Deposit>,
    businesses: List<Business>,
    expenses: List<Expense>,
    summary: FinancialSummary,
    selectedMonth: String,
    selectedYear: Int,
    onMonthSelected: (String) -> Unit,
    onYearSelected: (Int) -> Unit
) {
    var selectedReportTab by remember { mutableIntStateOf(0) }
    // 0: সামগ্রিক আর্থিক বিবরণী (Overall Balance Sheet)
    // 1: মাসিক জমার রিপোর্ট (Monthly Deposit)
    // 2: সদস্যভিত্তিক সঞ্চয় (Member-wise)
    // 3: ব্যবসায়িক লাভ রিপোর্ট (Business Profit)
    // 4: খরচ রিপোর্ট (Expenses)

    val reportTitles = listOf(
        "সামগ্রিক রিপোর্ট",
        "মাসিক জমা",
        "সদস্যভিত্তিক",
        "ব্যবসায়িক লাভ",
        "খরচের হিসাব"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Tab Row
        ScrollableTabRow(
            selectedTabIndex = selectedReportTab,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = EmeraldPrimary,
            edgePadding = 16.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            reportTitles.forEachIndexed { index, title ->
                Tab(
                    selected = selectedReportTab == index,
                    onClick = { selectedReportTab = index },
                    text = { Text(title, fontWeight = if (selectedReportTab == index) FontWeight.Bold else FontWeight.Normal) }
                )
            }
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            when (selectedReportTab) {
                0 -> {
                    // Overall Financial Report (Balance Sheet / Audit statement)
                    item {
                        OverallFinancialReportView(
                            summary = summary,
                            totalMembers = members.size,
                            businessCount = businesses.size,
                            expenseCount = expenses.size
                        )
                    }
                }
                1 -> {
                    // Monthly Deposit Report
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                MonthSelector(
                                    selectedMonth = selectedMonth,
                                    selectedYear = selectedYear,
                                    onMonthSelected = onMonthSelected,
                                    onYearSelected = onYearSelected
                                )
                            }
                        }
                    }

                    val filteredDeposits = members.map { member ->
                        deposits.find { it.memberId == member.uid && it.month == selectedMonth && it.year == selectedYear }
                            ?: Deposit(
                                id = "",
                                memberId = member.uid,
                                memberName = member.name,
                                month = selectedMonth,
                                year = selectedYear,
                                amount = 1000.0,
                                status = Deposit.STATUS_UNPAID
                            )
                    }

                    val totalCollected = filteredDeposits.filter { it.isPaid }.sumOf { it.amount }
                    val paidCount = filteredDeposits.count { it.isPaid }

                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = EmeraldContainer)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        "$selectedMonth ${BanglaUtils.toBanglaDigits(selectedYear.toString())} সংগৃহীত মোট",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = EmeraldPrimary
                                    )
                                    Text(
                                        BanglaUtils.formatTaka(totalCollected),
                                        style = MaterialTheme.typography.titleLarge.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = EmeraldPrimary
                                        )
                                    )
                                }
                                Text(
                                    "পরিশোধ: ${BanglaUtils.formatNumber(paidCount)}/১০ জন",
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                    color = EmeraldPrimary
                                )
                            }
                        }
                    }

                    items(filteredDeposits) { dep ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    MemberAvatar(name = dep.memberName, size = 36)
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(dep.memberName, fontWeight = FontWeight.SemiBold)
                                        Text("বরাদ্দ: ${BanglaUtils.formatTaka(dep.amount)}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                                StatusBadge(isPaid = dep.isPaid)
                            }
                        }
                    }
                }
                2 -> {
                    // Member-wise deposit report
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text(
                                    text = "সদস্যভিত্তিক সঞ্চয় প্রতিবেদন",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = EmeraldPrimary
                                )
                                Text(
                                    text = "ক্লাবের প্রতিটি সদস্যের মোট জমাকৃত টাকা এবং পরিশোধিত মাসের হিসাব",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    items(members) { member ->
                        val memberDeposits = deposits.filter { it.memberId == member.uid }
                        val paidSum = memberDeposits.filter { it.isPaid }.sumOf { it.amount }
                        val paidMonths = memberDeposits.count { it.isPaid }
                        val unpaidMonths = memberDeposits.count { !it.isPaid }

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        MemberAvatar(name = member.name, isAdmin = member.isAdmin, size = 42)
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column {
                                            Text(member.name, fontWeight = FontWeight.Bold)
                                            Text(member.email, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                    }
                                    Text(
                                        BanglaUtils.formatTaka(paidSum),
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = StatusPaid
                                        )
                                    )
                                }

                                Spacer(modifier = Modifier.height(8.dp))
                                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                                Spacer(modifier = Modifier.height(6.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        "পরিশোধিত মাস: ${BanglaUtils.formatNumber(paidMonths)}টি",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = StatusPaid
                                    )
                                    Text(
                                        "বকেয়া মাস: ${BanglaUtils.formatNumber(unpaidMonths)}টি",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = if (unpaidMonths > 0) StatusUnpaid else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
                3 -> {
                    // Business Profit Report
                    val totalProfit = businesses.sumOf { it.profit }
                    val totalInvest = businesses.sumOf { it.investment }

                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text(
                                    text = "ব্যবসায়িক বিনিয়োগ ও লাভ প্রতিবেদন",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = GoldTertiary
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column {
                                        Text("মোট লাভ", style = MaterialTheme.typography.labelSmall)
                                        Text(BanglaUtils.formatTaka(totalProfit), style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = GoldTertiary))
                                    }
                                    Column(horizontalAlignment = Alignment.End) {
                                        Text("মোট বিনিয়োগ", style = MaterialTheme.typography.labelSmall)
                                        Text(BanglaUtils.formatTaka(totalInvest), style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                                    }
                                }
                            }
                        }
                    }

                    items(businesses) { biz ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(biz.businessName, fontWeight = FontWeight.Bold)
                                        if (biz.isInstallment) {
                                            Text(
                                                "কিস্তিতে বিক্রয় • ক্রেতা: ${biz.customerName.ifBlank { "নির্দিষ্ট নেই" }}",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = EmeraldPrimary
                                            )
                                        }
                                    }
                                    Text("+ ${BanglaUtils.formatTaka(biz.profit)}", color = GoldTertiary, fontWeight = FontWeight.Bold)
                                }
                                if (biz.isInstallment) {
                                    Text(
                                        "ক্রয়মূল্য: ${BanglaUtils.formatTaka(biz.costPrice)} • বিক্রয়মূল্য: ${BanglaUtils.formatTaka(biz.sellingPrice)} • কিস্তি আদায়: ${BanglaUtils.formatTaka(biz.totalCollected)} (বকেয়া: ${BanglaUtils.formatTaka(biz.remainingDue)})",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                } else if (biz.description.isNotBlank()) {
                                    Text(biz.description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                if (biz.date.isNotBlank()) {
                                    Text("তারিখ: ${biz.date}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                    }
                }
                4 -> {
                    // Expenses Report
                    val totalExpenses = expenses.sumOf { it.amount }

                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text(
                                    text = "সংগঠনের খরচের বিস্তারিত প্রতিবেদন",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = StatusUnpaid
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "মোট ক্লাব ব্যয়: ${BanglaUtils.formatTaka(totalExpenses)}",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = StatusUnpaid)
                                )
                            }
                        }
                    }

                    items(expenses) { exp ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(exp.title, fontWeight = FontWeight.Bold)
                                    Text("- ${BanglaUtils.formatTaka(exp.amount)}", color = StatusUnpaid, fontWeight = FontWeight.Bold)
                                }
                                Text("খাত: ${exp.category} • তারিখ: ${exp.date}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                if (exp.description.isNotBlank()) {
                                    Text(exp.description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(60.dp))
            }
        }
    }
}

@Composable
fun OverallFinancialReportView(
    summary: FinancialSummary,
    totalMembers: Int,
    businessCount: Int,
    expenseCount: Int
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("overall_balance_sheet_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Text(
                text = "হবিরবাড়ি ভলেন্টিয়ার্স ক্লাব",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = EmeraldPrimary
                )
            )
            Text(
                text = "সার্বিক আর্থিক নিরীক্ষা ও উদ্বৃত্ত বিবরণী (প্রতিষ্ঠা: ০১-০৭-২০২৫ হতে চলমান)",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(14.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(12.dp))

            // 1. Total Members
            ReportBalanceRow(
                label = "মোট সদস্য সংখ্যা",
                value = "${BanglaUtils.formatNumber(totalMembers)} জন (সর্বোচ্চ ১০ জন)",
                valueColor = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(10.dp))

            // 2. Total Deposited
            ReportBalanceRow(
                label = "(+) মোট সদস্যদের সঞ্চয় জমা",
                value = BanglaUtils.formatTaka(summary.totalDeposited),
                valueColor = StatusPaid
            )

            Spacer(modifier = Modifier.height(10.dp))

            // 3. Total Business Profit
            ReportBalanceRow(
                label = "(+) মোট ব্যবসার অর্জিত লাভ",
                value = BanglaUtils.formatTaka(summary.totalBusinessProfit),
                valueColor = GoldTertiary,
                subLabel = "$businessCount টি ব্যবসায়িক প্রকল্প"
            )

            Spacer(modifier = Modifier.height(10.dp))

            // 4. Total Expenses
            ReportBalanceRow(
                label = "(-) মোট সংগঠনের খরচ ও ব্যয়",
                value = BanglaUtils.formatTaka(summary.totalExpenses),
                valueColor = StatusUnpaid,
                subLabel = "$expenseCount টি খরচের ভাউচার"
            )

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(thickness = 2.dp, color = EmeraldPrimary)
            Spacer(modifier = Modifier.height(14.dp))

            // 5. Net Total Fund
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "(=) বর্তমান মোট রিজার্ভ ফান্ড",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                        color = EmeraldPrimary
                    )
                    Text(
                        text = "জমা + ব্যবসার লাভ - খরচ",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Text(
                    text = BanglaUtils.formatTaka(summary.currentTotalFund),
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.ExtraBold,
                        color = EmeraldPrimary,
                        fontSize = 24.sp
                    )
                )
            }
        }
    }
}

@Composable
fun ReportBalanceRow(
    label: String,
    value: String,
    valueColor: Color,
    subLabel: String? = null
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(label, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold))
            if (subLabel != null) {
                Text(subLabel, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        Text(value, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = valueColor))
    }
}
