package com.example.ui.screens.deposits

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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Deposit
import com.example.data.model.User
import com.example.ui.MainViewModel
import com.example.ui.components.ConfirmationDialog
import com.example.ui.components.MemberAvatar
import com.example.ui.components.MonthSelector
import com.example.ui.components.StatusBadge
import com.example.ui.theme.EmeraldContainer
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.StatusPaid
import com.example.ui.theme.StatusUnpaid
import com.example.util.BanglaUtils

@Composable
fun DepositsScreen(
    members: List<User>,
    deposits: List<Deposit>,
    currentUser: User?,
    selectedMonth: String,
    selectedYear: Int,
    viewModel: MainViewModel
) {
    var selectedTab by remember { mutableIntStateOf(0) } // 0: মাসিক তালিকা, 1: বকেয়া তালিকা, 2: ইতিহাস
    var depositToEdit by remember { mutableStateOf<Deposit?>(null) }
    var depositToDelete by remember { mutableStateOf<Deposit?>(null) }
    var showRecordPastDialog by remember { mutableStateOf(false) }

    // Map each member to their deposit record for current selected month & year
    val monthDeposits = members.map { member ->
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

    val totalCollectedThisMonth = monthDeposits.filter { it.isPaid }.sumOf { it.amount }
    val paidMembersCount = monthDeposits.count { it.isPaid }
    val unpaidMembersList = monthDeposits.filter { !it.isPaid }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Month Selector Header Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "সংগঠনের যাত্রা শুরু: ১ জুলাই ২০২৫",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = EmeraldPrimary
                        )
                        if (currentUser?.isAdmin == true) {
                            TextButton(onClick = { showRecordPastDialog = true }) {
                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("পূর্ববর্তী জমা এন্ট্রি", fontSize = 12.sp)
                            }
                        }
                    }

                    MonthSelector(
                        selectedMonth = selectedMonth,
                        selectedYear = selectedYear,
                        onMonthSelected = { viewModel.setSelectedMonth(it) },
                        onYearSelected = { viewModel.setSelectedYear(it) }
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "$selectedMonth ${BanglaUtils.toBanglaDigits(selectedYear.toString())} এর আদায়",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = BanglaUtils.formatTaka(totalCollectedThisMonth),
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = EmeraldPrimary
                                )
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = EmeraldContainer
                        ) {
                            Text(
                                text = "পরিশোধ: ${BanglaUtils.formatNumber(paidMembersCount)}/১০ জন",
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = EmeraldPrimary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    val progress = (totalCollectedThisMonth / 10000.0).coerceIn(0.0, 1.0).toFloat()
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = EmeraldPrimary,
                        trackColor = EmeraldContainer
                    )
                }
            }

            // Tabs
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = EmeraldPrimary,
                modifier = Modifier.fillMaxWidth()
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("সকল সদস্য (${BanglaUtils.formatNumber(members.size)})") }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("বকেয়া তালিকা (${BanglaUtils.formatNumber(unpaidMembersList.size)})") }
                )
                Tab(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    text = { Text("সম্পূর্ণ ইতিহাস (${BanglaUtils.formatNumber(deposits.size)})") }
                )
            }

            // Content
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                when (selectedTab) {
                    0 -> {
                        // All members for selected month & year
                        items(monthDeposits) { deposit ->
                            val member = members.find { it.uid == deposit.memberId }
                            DepositItemRow(
                                deposit = deposit,
                                member = member,
                                callerIsAdmin = currentUser?.isAdmin == true,
                                onToggleStatus = {
                                    if (member != null) {
                                        viewModel.toggleDepositStatus(
                                            member = member,
                                            month = selectedMonth,
                                            year = selectedYear,
                                            amount = deposit.amount,
                                            onError = { viewModel.setUiMessage(it) }
                                        )
                                    }
                                },
                                onEdit = { depositToEdit = deposit }
                            )
                        }
                    }
                    1 -> {
                        // Unpaid list
                        if (unpaidMembersList.isEmpty()) {
                            item {
                                EmptyStateNotice(
                                    title = "কোন বকেয়া নেই!",
                                    subtitle = "$selectedMonth ${BanglaUtils.toBanglaDigits(selectedYear.toString())} এর সকল সদস্য তাদের সঞ্চয় জমা সম্পন্ন করেছেন।"
                                )
                            }
                        } else {
                            items(unpaidMembersList) { deposit ->
                                val member = members.find { it.uid == deposit.memberId }
                                DepositItemRow(
                                    deposit = deposit,
                                    member = member,
                                    callerIsAdmin = currentUser?.isAdmin == true,
                                    onToggleStatus = {
                                        if (member != null) {
                                            viewModel.toggleDepositStatus(
                                                member = member,
                                                month = selectedMonth,
                                                year = selectedYear,
                                                amount = deposit.amount,
                                                onError = { viewModel.setUiMessage(it) }
                                            )
                                        }
                                    },
                                    onEdit = { depositToEdit = deposit }
                                )
                            }
                        }
                    }
                    2 -> {
                        // Full deposit history across all months (2025 to 2026)
                        val allDeposits = deposits.sortedWith(
                            compareByDescending<Deposit> { it.year }
                                .thenByDescending { BanglaUtils.BENGALI_MONTHS.indexOf(it.month) }
                        )
                        if (allDeposits.isEmpty()) {
                            item {
                                EmptyStateNotice(
                                    title = "কোন জমার ইতিহাস পাওয়া যায়নি",
                                    subtitle = "জমা এন্ট্রি যুক্ত হলে এখানে বিস্তারিত ইতিহাস দেখা যাবে।"
                                )
                            }
                        } else {
                            items(allDeposits) { deposit ->
                                HistoryDepositRow(
                                    deposit = deposit,
                                    callerIsAdmin = currentUser?.isAdmin == true,
                                    onEdit = { depositToEdit = deposit },
                                    onDelete = { depositToDelete = deposit }
                                )
                            }
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(80.dp))
                }
            }
        }

        // Admin FAB: Record Past Deposit
        if (currentUser?.isAdmin == true) {
            FloatingActionButton(
                onClick = { showRecordPastDialog = true },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(20.dp)
                    .testTag("add_past_deposit_fab"),
                containerColor = EmeraldPrimary,
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, contentDescription = "Record Deposit")
            }
        }
    }

    // Record Past Month Deposit Dialog
    if (showRecordPastDialog) {
        RecordPastDepositDialog(
            members = members,
            currentUser = currentUser,
            onDismiss = { showRecordPastDialog = false },
            onSave = { dep ->
                viewModel.saveOrUpdateDeposit(
                    deposit = dep,
                    onSuccess = { showRecordPastDialog = false },
                    onError = { viewModel.setUiMessage(it) }
                )
            }
        )
    }

    // Edit Deposit Amount & Info Dialog
    if (depositToEdit != null) {
        val dep = depositToEdit!!
        EditDepositDialog(
            deposit = dep,
            onDismiss = { depositToEdit = null },
            onSave = { updated ->
                viewModel.saveOrUpdateDeposit(
                    deposit = updated,
                    onSuccess = { depositToEdit = null },
                    onError = { viewModel.setUiMessage(it) }
                )
            }
        )
    }

    // Delete Deposit Confirmation
    if (depositToDelete != null) {
        val dep = depositToDelete!!
        ConfirmationDialog(
            title = "জমার রেকর্ড মুছুন",
            message = "আপনি কি '${dep.memberName}' এর '${dep.month} ${BanglaUtils.toBanglaDigits(dep.year.toString())}' এর জমার হিসাব মুছে ফেলতে চান?",
            onConfirm = {
                viewModel.deleteDeposit(
                    depositId = dep.id,
                    onSuccess = { depositToDelete = null },
                    onError = { viewModel.setUiMessage(it) }
                )
            },
            onDismiss = { depositToDelete = null }
        )
    }
}

@Composable
fun DepositItemRow(
    deposit: Deposit,
    member: User?,
    callerIsAdmin: Boolean,
    onToggleStatus: () -> Unit,
    onEdit: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("deposit_row_${deposit.memberId}"),
        shape = RoundedCornerShape(12.dp),
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
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                MemberAvatar(name = deposit.memberName, isAdmin = member?.isAdmin == true, size = 40)
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = deposit.memberName,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                        maxLines = 1
                    )
                    Text(
                        text = "পরিমাণ: ${BanglaUtils.formatTaka(deposit.amount)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                StatusBadge(isPaid = deposit.isPaid)

                if (callerIsAdmin) {
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(
                        onClick = onToggleStatus,
                        modifier = Modifier
                            .size(36.dp)
                            .testTag("toggle_status_${deposit.memberId}")
                    ) {
                        Icon(
                            imageVector = if (deposit.isPaid) Icons.Default.CheckCircle else Icons.Default.HourglassEmpty,
                            contentDescription = "Toggle Paid/Unpaid",
                            tint = if (deposit.isPaid) StatusPaid else StatusUnpaid
                        )
                    }

                    IconButton(
                        onClick = onEdit,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit Amount",
                            tint = EmeraldPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun HistoryDepositRow(
    deposit: Deposit,
    callerIsAdmin: Boolean,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
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
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "${deposit.memberName} (${deposit.month} ${BanglaUtils.toBanglaDigits(deposit.year.toString())})",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                )
                Text(
                    text = "পরিমাণ: ${BanglaUtils.formatTaka(deposit.amount)} • সংগ্রহকারী: ${deposit.recordedBy.ifEmpty { "অ্যাডমিন" }}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                StatusBadge(isPaid = deposit.isPaid)

                if (callerIsAdmin) {
                    IconButton(onClick = onEdit) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit", tint = EmeraldPrimary, modifier = Modifier.size(18.dp))
                    }
                    IconButton(onClick = onDelete) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(18.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun EditDepositDialog(
    deposit: Deposit,
    onDismiss: () -> Unit,
    onSave: (Deposit) -> Unit
) {
    var amountStr by remember { mutableStateOf(deposit.amount.toInt().toString()) }
    var selectedMonth by remember { mutableStateOf(deposit.month) }
    var selectedYear by remember { mutableIntStateOf(deposit.year) }
    var isPaid by remember { mutableStateOf(deposit.isPaid) }
    var monthMenuOpen by remember { mutableStateOf(false) }
    var yearMenuOpen by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("জমার সকল তথ্য সম্পাদনা (অ্যাডমিন)", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("সদস্য: ${deposit.memberName}", fontWeight = FontWeight.SemiBold, color = EmeraldPrimary)

                // Month & Year Picker
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(modifier = Modifier.weight(1f)) {
                        OutlinedButton(
                            onClick = { monthMenuOpen = true },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(selectedMonth)
                        }
                        DropdownMenu(
                            expanded = monthMenuOpen,
                            onDismissRequest = { monthMenuOpen = false }
                        ) {
                            BanglaUtils.BENGALI_MONTHS.forEach { m ->
                                DropdownMenuItem(
                                    text = { Text(m) },
                                    onClick = {
                                        selectedMonth = m
                                        monthMenuOpen = false
                                    }
                                )
                            }
                        }
                    }

                    Box(modifier = Modifier.weight(1f)) {
                        OutlinedButton(
                            onClick = { yearMenuOpen = true },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(BanglaUtils.toBanglaDigits(selectedYear.toString()))
                        }
                        DropdownMenu(
                            expanded = yearMenuOpen,
                            onDismissRequest = { yearMenuOpen = false }
                        ) {
                            listOf(2025, 2026, 2027).forEach { y ->
                                DropdownMenuItem(
                                    text = { Text(BanglaUtils.toBanglaDigits(y.toString())) },
                                    onClick = {
                                        selectedYear = y
                                        yearMenuOpen = false
                                    }
                                )
                            }
                        }
                    }
                }

                OutlinedTextField(
                    value = amountStr,
                    onValueChange = { amountStr = it },
                    label = { Text("জমার পরিমাণ (টাকা)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("deposit_amount_input")
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("স্ট্যাটাস: ${if (isPaid) "পরিশোধিত" else "বকেয়া"}")
                    OutlinedButton(onClick = { isPaid = !isPaid }) {
                        Text(if (isPaid) "বকেয়া করুন" else "পরিশোধ করুন")
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amt = amountStr.toDoubleOrNull() ?: 1000.0
                    onSave(
                        deposit.copy(
                            amount = amt,
                            month = selectedMonth,
                            year = selectedYear,
                            status = if (isPaid) Deposit.STATUS_PAID else Deposit.STATUS_UNPAID,
                            updatedAt = System.currentTimeMillis()
                        )
                    )
                },
                colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary)
            ) {
                Text("সংরক্ষণ করুন")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("বাতিল")
            }
        }
    )
}

@Composable
fun RecordPastDepositDialog(
    members: List<User>,
    currentUser: User?,
    onDismiss: () -> Unit,
    onSave: (Deposit) -> Unit
) {
    var selectedMember by remember { mutableStateOf(members.firstOrNull()) }
    var selectedMonth by remember { mutableStateOf("জুলাই") }
    var selectedYear by remember { mutableIntStateOf(2025) }
    var amountStr by remember { mutableStateOf("1000") }
    var isPaid by remember { mutableStateOf(true) }

    var memberMenuOpen by remember { mutableStateOf(false) }
    var monthMenuOpen by remember { mutableStateOf(false) }
    var yearMenuOpen by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("পূর্ববর্তী মাসের জমা রেকর্ড (১-৭-২০২৫ হতে)", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                // Member Selector
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedButton(
                        onClick = { memberMenuOpen = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("সদস্য: ${selectedMember?.name ?: "নির্বাচন করুন"}")
                    }
                    DropdownMenu(
                        expanded = memberMenuOpen,
                        onDismissRequest = { memberMenuOpen = false }
                    ) {
                        members.forEach { m ->
                            DropdownMenuItem(
                                text = { Text(m.name) },
                                onClick = {
                                    selectedMember = m
                                    memberMenuOpen = false
                                }
                            )
                        }
                    }
                }

                // Month & Year Selector
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(modifier = Modifier.weight(1f)) {
                        OutlinedButton(
                            onClick = { monthMenuOpen = true },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("মাস: $selectedMonth")
                        }
                        DropdownMenu(
                            expanded = monthMenuOpen,
                            onDismissRequest = { monthMenuOpen = false }
                        ) {
                            BanglaUtils.BENGALI_MONTHS.forEach { m ->
                                DropdownMenuItem(
                                    text = { Text(m) },
                                    onClick = {
                                        selectedMonth = m
                                        monthMenuOpen = false
                                    }
                                )
                            }
                        }
                    }

                    Box(modifier = Modifier.weight(1f)) {
                        OutlinedButton(
                            onClick = { yearMenuOpen = true },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("বছর: ${BanglaUtils.toBanglaDigits(selectedYear.toString())}")
                        }
                        DropdownMenu(
                            expanded = yearMenuOpen,
                            onDismissRequest = { yearMenuOpen = false }
                        ) {
                            listOf(2025, 2026, 2027).forEach { y ->
                                DropdownMenuItem(
                                    text = { Text(BanglaUtils.toBanglaDigits(y.toString())) },
                                    onClick = {
                                        selectedYear = y
                                        yearMenuOpen = false
                                    }
                                )
                            }
                        }
                    }
                }

                OutlinedTextField(
                    value = amountStr,
                    onValueChange = { amountStr = it },
                    label = { Text("জমার পরিমাণ (টাকা)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("অবস্থা: ${if (isPaid) "পরিশোধিত" else "বকেয়া"}")
                    OutlinedButton(onClick = { isPaid = !isPaid }) {
                        Text(if (isPaid) "বকেয়া করুন" else "পরিশোধ করুন")
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (selectedMember != null) {
                        val amt = amountStr.toDoubleOrNull() ?: 1000.0
                        val adminName = currentUser?.name ?: "অ্যাডমিন"
                        onSave(
                            Deposit(
                                id = "",
                                memberId = selectedMember!!.uid,
                                memberName = selectedMember!!.name,
                                month = selectedMonth,
                                year = selectedYear,
                                amount = amt,
                                status = if (isPaid) Deposit.STATUS_PAID else Deposit.STATUS_UNPAID,
                                recordedBy = "$adminName (অ্যাডমিন)"
                            )
                        )
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary)
            ) {
                Text("সংরক্ষণ করুন")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("বাতিল")
            }
        }
    )
}

@Composable
fun EmptyStateNotice(
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(
                shape = CircleShape,
                color = EmeraldContainer,
                modifier = Modifier.size(48.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Savings,
                        contentDescription = null,
                        tint = EmeraldPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = EmeraldPrimary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
