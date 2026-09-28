package com.example.ui.screens.business

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BusinessCenter
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
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
import com.example.data.model.Business
import com.example.data.model.InstallmentPayment
import com.example.data.model.User
import com.example.ui.MainViewModel
import com.example.ui.components.ConfirmationDialog
import com.example.ui.theme.AccentGold
import com.example.ui.theme.EmeraldContainer
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.GoldContainer
import com.example.ui.theme.GoldTertiary
import com.example.ui.theme.StatusPaid
import com.example.ui.theme.StatusUnpaid
import com.example.ui.theme.StatusUnpaidBg
import com.example.util.BanglaUtils
import java.util.UUID

@Composable
fun BusinessScreen(
    businesses: List<Business>,
    currentUser: User?,
    viewModel: MainViewModel
) {
    var selectedFilterTab by remember { mutableIntStateOf(0) } // 0: সকল, 1: কিস্তিতে পণ্য বিক্রি, 2: সাধারণ প্রকল্প
    var showAddDialog by remember { mutableStateOf(false) }
    var businessToEdit by remember { mutableStateOf<Business?>(null) }
    var businessToDelete by remember { mutableStateOf<Business?>(null) }
    var businessForInstallmentPayment by remember { mutableStateOf<Business?>(null) }

    val filteredBusinesses = when (selectedFilterTab) {
        1 -> businesses.filter { it.isInstallment }
        2 -> businesses.filter { !it.isInstallment }
        else -> businesses
    }

    val totalProfit = businesses.sumOf { it.profit }
    val installmentSales = businesses.filter { it.isInstallment }
    val totalInstallmentValue = installmentSales.sumOf { it.sellingPrice }
    val totalInstallmentCollected = installmentSales.sumOf { it.totalCollected }
    val totalInstallmentDue = installmentSales.sumOf { it.remainingDue }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(4.dp))
                // Top Summary Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "মোট ব্যবসার লাভ",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = BanglaUtils.formatTaka(totalProfit),
                                    style = MaterialTheme.typography.headlineSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = GoldTertiary
                                    )
                                )
                            }
                            Surface(
                                shape = CircleShape,
                                color = GoldContainer,
                                modifier = Modifier.size(44.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.TrendingUp,
                                        contentDescription = null,
                                        tint = GoldTertiary,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }
                        }

                        // Installment Summary Breakdown
                        if (installmentSales.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(10.dp))
                            HorizontalDivider()
                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = "কিস্তিতে পণ্য বিক্রয়ের সারসংক্ষেপ (${BanglaUtils.formatNumber(installmentSales.size)}টি বিক্রয়)",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = EmeraldPrimary
                            )
                            Spacer(modifier = Modifier.height(6.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text("মোট বিক্রয়মূল্য", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text(BanglaUtils.formatTaka(totalInstallmentValue), style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
                                }
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("মোট আদায়", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text(BanglaUtils.formatTaka(totalInstallmentCollected), style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold, color = StatusPaid))
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text("মোট বকেয়া", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text(BanglaUtils.formatTaka(totalInstallmentDue), style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold, color = if (totalInstallmentDue > 0) StatusUnpaid else StatusPaid))
                                }
                            }
                        }
                    }
                }
            }

            // Tabs / Filters
            item {
                TabRow(
                    selectedTabIndex = selectedFilterTab,
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = EmeraldPrimary,
                    modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp))
                ) {
                    Tab(
                        selected = selectedFilterTab == 0,
                        onClick = { selectedFilterTab = 0 },
                        text = { Text("সকল (${BanglaUtils.formatNumber(businesses.size)})") }
                    )
                    Tab(
                        selected = selectedFilterTab == 1,
                        onClick = { selectedFilterTab = 1 },
                        text = { Text("কিস্তিতে বিক্রয় (${BanglaUtils.formatNumber(installmentSales.size)})") }
                    )
                    Tab(
                        selected = selectedFilterTab == 2,
                        onClick = { selectedFilterTab = 2 },
                        text = { Text("সাধারণ প্রকল্প") }
                    )
                }
            }

            if (filteredBusinesses.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.BusinessCenter,
                                contentDescription = null,
                                tint = EmeraldPrimary,
                                modifier = Modifier.size(44.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("কোন ব্যবসায়িক রেকর্ড পাওয়া যায়নি", fontWeight = FontWeight.Bold)
                            Text(
                                "অ্যাডমিন নতুন ব্যবসায়িক উদ্যোগ বা কিস্তিতে পণ্য বিক্রি যোগ করতে পারেন।",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            } else {
                items(filteredBusinesses) { business ->
                    if (business.isInstallment) {
                        InstallmentProductCard(
                            business = business,
                            callerIsAdmin = currentUser?.isAdmin == true,
                            onEdit = { businessToEdit = business },
                            onDelete = { businessToDelete = business },
                            onRecordPayment = { businessForInstallmentPayment = business }
                        )
                    } else {
                        GeneralBusinessCard(
                            business = business,
                            callerIsAdmin = currentUser?.isAdmin == true,
                            onEdit = { businessToEdit = business },
                            onDelete = { businessToDelete = business }
                        )
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }

        // Add Business / Installment Product FAB (Admin only)
        if (currentUser?.isAdmin == true) {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(20.dp)
                    .testTag("add_business_fab"),
                containerColor = EmeraldPrimary,
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Business")
            }
        }
    }

    // Add Business / Product Dialog
    if (showAddDialog) {
        AddEditBusinessDialog(
            existing = null,
            onDismiss = { showAddDialog = false },
            onSave = { newBiz ->
                viewModel.saveBusiness(
                    business = newBiz,
                    onSuccess = { showAddDialog = false },
                    onError = { viewModel.setUiMessage(it) }
                )
            }
        )
    }

    // Edit Business Dialog
    if (businessToEdit != null) {
        AddEditBusinessDialog(
            existing = businessToEdit,
            onDismiss = { businessToEdit = null },
            onSave = { updatedBiz ->
                viewModel.saveBusiness(
                    business = updatedBiz,
                    onSuccess = { businessToEdit = null },
                    onError = { viewModel.setUiMessage(it) }
                )
            }
        )
    }

    // Record Installment Payment Dialog
    if (businessForInstallmentPayment != null) {
        RecordInstallmentDialog(
            business = businessForInstallmentPayment!!,
            currentUser = currentUser,
            onDismiss = { businessForInstallmentPayment = null },
            onSave = { payment ->
                viewModel.recordInstallmentPayment(
                    businessId = businessForInstallmentPayment!!.id,
                    payment = payment,
                    onSuccess = { businessForInstallmentPayment = null },
                    onError = { viewModel.setUiMessage(it) }
                )
            }
        )
    }

    // Delete Confirmation
    if (businessToDelete != null) {
        val biz = businessToDelete!!
        ConfirmationDialog(
            title = "ব্যবসায়িক রেকর্ড মুছুন",
            message = "আপনি কি '${biz.businessName}' এর রেকর্ড মুছে ফেলতে চান?",
            onConfirm = {
                viewModel.deleteBusiness(
                    businessId = biz.id,
                    onSuccess = { businessToDelete = null },
                    onError = { viewModel.setUiMessage(it) }
                )
            },
            onDismiss = { businessToDelete = null }
        )
    }
}

@Composable
fun InstallmentProductCard(
    business: Business,
    callerIsAdmin: Boolean,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onRecordPayment: () -> Unit
) {
    var isExpanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header with badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = EmeraldContainer
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.ShoppingBag, contentDescription = null, modifier = Modifier.size(12.dp), tint = EmeraldPrimary)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "কিস্তিতে পণ্য বিক্রয়",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = EmeraldPrimary
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = if (business.isCompleted) StatusPaid.copy(alpha = 0.12f) else AccentGold.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = if (business.isCompleted) "পরিশোধ সম্পন্ন" else "চলমান কিস্তি",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                                color = if (business.isCompleted) StatusPaid else GoldTertiary,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = business.productName.ifBlank { business.businessName },
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }

                if (callerIsAdmin) {
                    Row {
                        IconButton(onClick = onEdit) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit", tint = EmeraldPrimary, modifier = Modifier.size(18.dp))
                        }
                        IconButton(onClick = onDelete) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(18.dp))
                        }
                    }
                }
            }

            // Customer Details Card
            if (business.customerName.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(14.dp), tint = EmeraldPrimary)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "ক্রেতা: ${business.customerName}",
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold)
                            )
                            if (business.customerPhone.isNotBlank()) {
                                Spacer(modifier = Modifier.width(12.dp))
                                Icon(Icons.Default.Phone, contentDescription = null, modifier = Modifier.size(14.dp), tint = EmeraldPrimary)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(business.customerPhone, style = MaterialTheme.typography.bodySmall)
                            }
                        }
                        if (business.customerAddress.isNotBlank()) {
                            Spacer(modifier = Modifier.height(2.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.LocationOn, contentDescription = null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(business.customerAddress, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(8.dp))

            // Pricing 3-Column: ক্রয়মূল্য | বিক্রয়মূল্য | লাভ
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("পণ্যের ক্রয়মূল্য", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(BanglaUtils.formatTaka(business.costPrice), style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("পণ্যের বিক্রয়মূল্য", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(BanglaUtils.formatTaka(business.sellingPrice), style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold, color = EmeraldPrimary))
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("নিট লাভ", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("+ ${BanglaUtils.formatTaka(business.profit)}", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold, color = GoldTertiary))
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Installment Progress & Terms
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "ডাউন পেমেন্ট: ${BanglaUtils.formatTaka(business.downPayment)}",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        text = "কিস্তি: ${BanglaUtils.formatNumber(business.totalInstallments)}টি (${business.installmentFrequency} ${BanglaUtils.formatTaka(business.installmentAmount)})",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold)
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "আদায়: ${BanglaUtils.formatTaka(business.totalCollected)} / ${BanglaUtils.formatTaka(business.sellingPrice)}",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold, color = StatusPaid)
                    )
                    Text(
                        text = "বকেয়া: ${BanglaUtils.formatTaka(business.remainingDue)}",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = if (business.remainingDue > 0) StatusUnpaid else StatusPaid
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Progress Bar
            val progress = if (business.sellingPrice > 0) {
                (business.totalCollected / business.sellingPrice).coerceIn(0.0, 1.0).toFloat()
            } else 0f

            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = if (progress >= 1f) StatusPaid else EmeraldPrimary,
                trackColor = EmeraldContainer
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Installment Actions Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = { isExpanded = !isExpanded }) {
                    Icon(
                        imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "কিস্তির ইতিহাস (${BanglaUtils.formatNumber(business.payments.size)}টি আদায়)",
                        style = MaterialTheme.typography.labelMedium
                    )
                }

                if (callerIsAdmin && !business.isCompleted) {
                    Button(
                        onClick = onRecordPayment,
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary)
                    ) {
                        Icon(Icons.Default.Payment, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("কিস্তি আদায় এন্ট্রি", fontSize = 12.sp)
                    }
                }
            }

            // Expandable List of Installment Payments
            AnimatedVisibility(visible = isExpanded) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                ) {
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                    Spacer(modifier = Modifier.height(6.dp))

                    if (business.downPayment > 0) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("অগ্রিম / ডাউন পেমেন্ট", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold))
                            Text(BanglaUtils.formatTaka(business.downPayment), style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold, color = StatusPaid))
                        }
                    }

                    if (business.payments.isEmpty()) {
                        Text(
                            text = "এখনও কোন কিস্তি আদায় রেকর্ড করা হয়নি",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                    } else {
                        business.payments.forEach { pay ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "${BanglaUtils.formatNumber(pay.installmentNumber)}ম কিস্তি (${pay.date})",
                                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold)
                                    )
                                    if (pay.collectedBy.isNotBlank()) {
                                        Text(
                                            text = "আদায়কারী: ${pay.collectedBy}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                                Text(
                                    text = "+ ${BanglaUtils.formatTaka(pay.amount)}",
                                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold, color = StatusPaid)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun GeneralBusinessCard(
    business: Business,
    callerIsAdmin: Boolean,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = business.businessName,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = EmeraldPrimary
                    )
                    if (business.description.isNotBlank()) {
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = business.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                if (callerIsAdmin) {
                    Row {
                        IconButton(onClick = onEdit) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit", tint = EmeraldPrimary, modifier = Modifier.size(18.dp))
                        }
                        IconButton(onClick = onDelete) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(18.dp))
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "অর্জিত লাভ",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "+ ${BanglaUtils.formatTaka(business.profit)}",
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = GoldTertiary
                        )
                    )
                }

                if (business.investment > 0) {
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "বিনিয়োগ",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = BanglaUtils.formatTaka(business.investment),
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                        )
                    }
                }
            }

            if (business.date.isNotBlank() || business.notes.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    if (business.date.isNotBlank()) {
                        Text(
                            text = "তারিখ: ${business.date}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    if (business.createdBy.isNotBlank()) {
                        Text(
                            text = "রেকর্ডকারী: ${business.createdBy}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                if (business.notes.isNotBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "মন্তব্য: ${business.notes}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun RecordInstallmentDialog(
    business: Business,
    currentUser: User?,
    onDismiss: () -> Unit,
    onSave: (InstallmentPayment) -> Unit
) {
    val nextInstallmentNumber = business.payments.size + 1
    var installmentNumStr by remember { mutableStateOf(nextInstallmentNumber.toString()) }
    var amountStr by remember {
        val suggestedAmount = if (business.installmentAmount > 0) business.installmentAmount.toInt().toString() else "1000"
        mutableStateOf(suggestedAmount)
    }
    var date by remember { mutableStateOf("২৮ সেপ্টেম্বর ২০২৬") }
    var notes by remember { mutableStateOf("${BanglaUtils.formatNumber(nextInstallmentNumber)}ম কিস্তি আদায়") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Payment, contentDescription = null, tint = EmeraldPrimary)
                Spacer(modifier = Modifier.width(8.dp))
                Text("কিস্তি আদায় এন্ট্রি", fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "পণ্য: ${business.productName}",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                )
                Text(
                    text = "ক্রেতা: ${business.customerName} • বকেয়া: ${BanglaUtils.formatTaka(business.remainingDue)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                OutlinedTextField(
                    value = installmentNumStr,
                    onValueChange = { installmentNumStr = it },
                    label = { Text("কিস্তি ক্রম/নম্বর *") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = amountStr,
                    onValueChange = { amountStr = it },
                    label = { Text("আদায়কৃত টাকার পরিমাণ *") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = date,
                    onValueChange = { date = it },
                    label = { Text("আদায়ের তারিখ") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("মন্তব্য / রশিদ নম্বর") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amt = amountStr.toDoubleOrNull() ?: 0.0
                    val num = installmentNumStr.toIntOrNull() ?: nextInstallmentNumber
                    val collector = currentUser?.name ?: "অ্যাডমিন"
                    if (amt > 0) {
                        onSave(
                            InstallmentPayment(
                                id = "pay_${UUID.randomUUID().toString().take(6)}",
                                installmentNumber = num,
                                amount = amt,
                                date = date.trim(),
                                collectedBy = "$collector (অ্যাডমিন)",
                                notes = notes.trim(),
                                timestamp = System.currentTimeMillis()
                            )
                        )
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary)
            ) {
                Text("কিস্তি জমা নিন")
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
fun AddEditBusinessDialog(
    existing: Business?,
    onDismiss: () -> Unit,
    onSave: (Business) -> Unit
) {
    // Mode: "installment" or "general"
    var isInstallment by remember {
        mutableStateOf(existing?.isInstallment ?: true)
    }

    // Common fields
    var businessName by remember { mutableStateOf(existing?.businessName ?: "") }
    var description by remember { mutableStateOf(existing?.description ?: "") }
    var date by remember { mutableStateOf(existing?.date ?: "সেপ্টেম্বর ২০২৬") }
    var notes by remember { mutableStateOf(existing?.notes ?: "") }

    // General project fields
    var investmentStr by remember { mutableStateOf(existing?.investment?.let { if (it > 0) it.toInt().toString() else "" } ?: "") }
    var profitStr by remember { mutableStateOf(existing?.profit?.let { if (it > 0) it.toInt().toString() else "" } ?: "") }

    // Installment specific fields
    var productName by remember { mutableStateOf(existing?.productName ?: "") }
    var costPriceStr by remember { mutableStateOf(existing?.costPrice?.let { if (it > 0) it.toInt().toString() else "" } ?: "") }
    var sellingPriceStr by remember { mutableStateOf(existing?.sellingPrice?.let { if (it > 0) it.toInt().toString() else "" } ?: "") }
    var customerName by remember { mutableStateOf(existing?.customerName ?: "") }
    var customerPhone by remember { mutableStateOf(existing?.customerPhone ?: "") }
    var customerAddress by remember { mutableStateOf(existing?.customerAddress ?: "") }
    var downPaymentStr by remember { mutableStateOf(existing?.downPayment?.let { if (it > 0) it.toInt().toString() else "" } ?: "") }
    var totalInstallmentsStr by remember { mutableStateOf(existing?.totalInstallments?.toString() ?: "6") }
    var installmentFrequency by remember { mutableStateOf(existing?.installmentFrequency ?: "মাসিক") }

    // Derived profit for installment
    val costPrice = costPriceStr.toDoubleOrNull() ?: 0.0
    val sellingPrice = sellingPriceStr.toDoubleOrNull() ?: 0.0
    val calculatedProfit = (sellingPrice - costPrice).coerceAtLeast(0.0)

    val downPayment = downPaymentStr.toDoubleOrNull() ?: 0.0
    val totalInstallments = totalInstallmentsStr.toIntOrNull() ?: 1
    val calculatedPerInstallment = if (totalInstallments > 0) {
        (sellingPrice - downPayment).coerceAtLeast(0.0) / totalInstallments
    } else 0.0

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                if (existing == null) "নতুন ব্যবসা / কিস্তিতে পণ্য বিক্রয়" else "ব্যবসায়িক তথ্য সম্পাদনা",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Radio buttons to choose between Installment Sale and General Business
                Text("ব্যবসার ধরন নির্বাচন করুন:", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(
                            selected = isInstallment,
                            onClick = { isInstallment = true },
                            colors = RadioButtonDefaults.colors(selectedColor = EmeraldPrimary)
                        )
                        Text("কিস্তিতে পণ্য বিক্রয়", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold))
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(
                            selected = !isInstallment,
                            onClick = { isInstallment = false },
                            colors = RadioButtonDefaults.colors(selectedColor = EmeraldPrimary)
                        )
                        Text("সাধারণ প্রকল্প", style = MaterialTheme.typography.bodySmall)
                    }
                }

                HorizontalDivider()

                if (isInstallment) {
                    // Installment Product Form
                    OutlinedTextField(
                        value = productName,
                        onValueChange = {
                            productName = it
                            if (businessName.isBlank()) businessName = "$it কিস্তিতে বিক্রয়"
                        },
                        label = { Text("পণ্যের নাম * (যেমন: ফ্রিজ, সেলাই মেশিন)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = customerName,
                        onValueChange = { customerName = it },
                        label = { Text("ক্রেতার নাম *") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = customerPhone,
                            onValueChange = { customerPhone = it },
                            label = { Text("মোবাইল নম্বর") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = customerAddress,
                            onValueChange = { customerAddress = it },
                            label = { Text("ঠিকানা") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    // Cost Price & Selling Price
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = costPriceStr,
                            onValueChange = { costPriceStr = it },
                            label = { Text("পণ্যের ক্রয়মূল্য (টাকা) *") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = sellingPriceStr,
                            onValueChange = { sellingPriceStr = it },
                            label = { Text("বিক্রয়মূল্য (টাকা) *") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    // Auto Profit display card
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = GoldContainer.copy(alpha = 0.5f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("প্রত্যাশিত লাভ (বিক্রয় - ক্রয়):", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold))
                            Text(BanglaUtils.formatTaka(calculatedProfit), style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold, color = GoldTertiary))
                        }
                    }

                    // Installment Terms
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = downPaymentStr,
                            onValueChange = { downPaymentStr = it },
                            label = { Text("ডাউন পেমেন্ট / অগ্রিম") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = totalInstallmentsStr,
                            onValueChange = { totalInstallmentsStr = it },
                            label = { Text("মোট কিস্তির সংখ্যা") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    // Per Installment amount display
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = EmeraldContainer.copy(alpha = 0.5f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("প্রতি কিস্তির পরিমাণ:", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold))
                            Text(BanglaUtils.formatTaka(calculatedPerInstallment), style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold, color = EmeraldPrimary))
                        }
                    }

                    OutlinedTextField(
                        value = date,
                        onValueChange = { date = it },
                        label = { Text("বিক্রয়ের তারিখ") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text("মন্তব্য / কিস্তির শর্তাবলী") },
                        modifier = Modifier.fillMaxWidth()
                    )

                } else {
                    // General Business Form
                    OutlinedTextField(
                        value = businessName,
                        onValueChange = { businessName = it },
                        label = { Text("প্রকল্পের নাম *") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = profitStr,
                        onValueChange = { profitStr = it },
                        label = { Text("লাভের পরিমাণ (টাকা) *") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = investmentStr,
                        onValueChange = { investmentStr = it },
                        label = { Text("বিনিয়োগ পরিমাণ (টাকা)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = date,
                        onValueChange = { date = it },
                        label = { Text("তারিখ") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("বিবরণ") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text("মন্তব্য") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (isInstallment) {
                        val cPrice = costPriceStr.toDoubleOrNull() ?: 0.0
                        val sPrice = sellingPriceStr.toDoubleOrNull() ?: 0.0
                        val dPay = downPaymentStr.toDoubleOrNull() ?: 0.0
                        val tInst = totalInstallmentsStr.toIntOrNull() ?: 1
                        val prof = (sPrice - cPrice).coerceAtLeast(0.0)

                        val finalName = if (businessName.isNotBlank()) businessName.trim() else "$productName কিস্তিতে বিক্রয়"
                        onSave(
                            (existing ?: Business()).copy(
                                businessName = finalName,
                                businessType = Business.TYPE_INSTALLMENT,
                                productName = productName.trim(),
                                customerName = customerName.trim(),
                                customerPhone = customerPhone.trim(),
                                customerAddress = customerAddress.trim(),
                                costPrice = cPrice,
                                sellingPrice = sPrice,
                                investment = cPrice,
                                profit = prof,
                                downPayment = dPay,
                                totalInstallments = tInst,
                                installmentFrequency = installmentFrequency,
                                date = date.trim(),
                                notes = notes.trim()
                            )
                        )
                    } else {
                        val profitVal = profitStr.toDoubleOrNull() ?: 0.0
                        val investVal = investmentStr.toDoubleOrNull() ?: 0.0
                        onSave(
                            (existing ?: Business()).copy(
                                businessName = businessName.trim(),
                                businessType = Business.TYPE_GENERAL,
                                description = description.trim(),
                                investment = investVal,
                                profit = profitVal,
                                date = date.trim(),
                                notes = notes.trim()
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
