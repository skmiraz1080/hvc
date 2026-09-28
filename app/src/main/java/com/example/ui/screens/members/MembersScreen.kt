package com.example.ui.screens.members

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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.User
import com.example.ui.MainViewModel
import com.example.ui.components.ConfirmationDialog
import com.example.ui.components.MemberAvatar
import com.example.ui.components.StatusBadge
import com.example.ui.theme.EmeraldContainer
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.StatusPaid
import com.example.ui.theme.StatusUnpaid
import com.example.util.BanglaUtils

@Composable
fun MembersScreen(
    members: List<User>,
    currentUser: User?,
    viewModel: MainViewModel
) {
    var selectedMemberForDetail by remember { mutableStateOf<User?>(null) }
    var memberToEdit by remember { mutableStateOf<User?>(null) }
    var memberToDelete by remember { mutableStateOf<User?>(null) }
    var showAddMemberDialog by remember { mutableStateOf(false) }

    val adminCount = members.count { it.isAdmin }

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
                // Top Header info
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "সদস্য তালিকা (${BanglaUtils.formatNumber(members.size)}/১০ জন)",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = EmeraldPrimary
                            )
                            Text(
                                text = "সংগঠনের নির্ধারিত সদস্য সংখ্যা ১০ জন (অ্যাডমিন: ${BanglaUtils.formatNumber(adminCount)}/২)",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Surface(
                            shape = CircleShape,
                            color = EmeraldContainer,
                            modifier = Modifier.size(40.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Group,
                                    contentDescription = null,
                                    tint = EmeraldPrimary,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                    }
                }
            }

            items(members) { member ->
                val stats = viewModel.getMemberStats(member.uid)
                MemberListItem(
                    member = member,
                    stats = stats,
                    currentUser = currentUser,
                    onClick = { selectedMemberForDetail = member },
                    onEdit = { memberToEdit = member },
                    onDelete = { memberToDelete = member }
                )
            }

            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }

        // Add Member FAB (Admin only & if members < 10)
        if (currentUser?.isAdmin == true && members.size < 10) {
            FloatingActionButton(
                onClick = { showAddMemberDialog = true },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(20.dp)
                    .testTag("add_member_fab"),
                containerColor = EmeraldPrimary,
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Member")
            }
        }
    }

    // Member Details Dialog
    if (selectedMemberForDetail != null) {
        val member = selectedMemberForDetail!!
        val stats = viewModel.getMemberStats(member.uid)
        MemberDetailDialog(
            member = member,
            stats = stats,
            onDismiss = { selectedMemberForDetail = null }
        )
    }

    // Add Member Dialog
    if (showAddMemberDialog) {
        AddMemberDialog(
            adminCount = adminCount,
            onDismiss = { showAddMemberDialog = false },
            onAdd = { name, email, phone, role ->
                viewModel.addMember(
                    name = name,
                    email = email,
                    phone = phone,
                    role = role,
                    onSuccess = { showAddMemberDialog = false },
                    onError = { viewModel.setUiMessage(it) }
                )
            }
        )
    }

    // Edit Member Dialog
    if (memberToEdit != null) {
        val member = memberToEdit!!
        EditMemberDialog(
            member = member,
            callerIsAdmin = currentUser?.isAdmin == true,
            currentAdminCount = adminCount,
            onDismiss = { memberToEdit = null },
            onSave = { updated ->
                viewModel.updateMember(
                    user = updated,
                    onSuccess = { memberToEdit = null },
                    onError = { viewModel.setUiMessage(it) }
                )
            }
        )
    }

    // Delete Member Confirmation
    if (memberToDelete != null) {
        val member = memberToDelete!!
        ConfirmationDialog(
            title = "সদস্য অপসারণ নিশ্চিত করুন",
            message = "আপনি কি '${member.name}' কে সংগঠন থেকে মুছে ফেলতে চান?",
            onConfirm = {
                viewModel.removeMember(
                    userId = member.uid,
                    onSuccess = { memberToDelete = null },
                    onError = { viewModel.setUiMessage(it) }
                )
            },
            onDismiss = { memberToDelete = null }
        )
    }
}

@Composable
fun MemberListItem(
    member: User,
    stats: MainViewModel.MemberStats,
    currentUser: User?,
    onClick: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("member_card_${member.uid}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    MemberAvatar(name = member.name, isAdmin = member.isAdmin, size = 46)
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = member.name,
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                maxLines = 1
                            )
                            if (member.isAdmin) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = EmeraldContainer
                                ) {
                                    Text(
                                        text = "অ্যাডমিন",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        color = EmeraldPrimary,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                        Text(
                            text = member.email,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Actions if admin or if user is viewing their own profile
                Row {
                    if (currentUser?.isAdmin == true || currentUser?.uid == member.uid) {
                        IconButton(onClick = onEdit) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit", tint = EmeraldPrimary, modifier = Modifier.size(20.dp))
                        }
                    }
                    if (currentUser?.isAdmin == true && currentUser.uid != member.uid) {
                        IconButton(onClick = onDelete) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(20.dp))
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(8.dp))

            // Financial stats row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "মোট জমা",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = BanglaUtils.formatTaka(stats.totalDeposited),
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = StatusPaid
                        )
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = StatusPaid.copy(alpha = 0.1f)
                    ) {
                        Text(
                            text = "পরিশোধ: ${BanglaUtils.formatNumber(stats.paidMonthsCount)} মাস",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                            color = StatusPaid,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }

                    if (stats.unpaidMonthsCount > 0) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = StatusUnpaid.copy(alpha = 0.1f)
                        ) {
                            Text(
                                text = "বকেয়া: ${BanglaUtils.formatNumber(stats.unpaidMonthsCount)} মাস",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                                color = StatusUnpaid,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MemberDetailDialog(
    member: User,
    stats: MainViewModel.MemberStats,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                MemberAvatar(name = member.name, isAdmin = member.isAdmin, size = 42)
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(member.name, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    Text(
                        text = if (member.isAdmin) "দায়িত্ব: প্রশাসক (অ্যাডমিন)" else "দায়িত্ব: সাধারণ সদস্য",
                        style = MaterialTheme.typography.bodySmall,
                        color = EmeraldPrimary
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                if (member.phone.isNotBlank()) {
                    Text("মোবাইল: ${member.phone}", style = MaterialTheme.typography.bodyMedium)
                }
                Text("ইমেইল: ${member.email}", style = MaterialTheme.typography.bodyMedium)
                Text("যোগদানের সময়: ${member.joinedDate.ifBlank { "২০২৬" }}", style = MaterialTheme.typography.bodySmall)

                Spacer(modifier = Modifier.height(14.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = EmeraldContainer)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("মোট সঞ্চয় জমা", style = MaterialTheme.typography.labelMedium, color = EmeraldPrimary)
                        Text(
                            text = BanglaUtils.formatTaka(stats.totalDeposited),
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = EmeraldPrimary
                            )
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "পরিশোধিত: ${BanglaUtils.formatNumber(stats.paidMonthsCount)} মাস | বকেয়া: ${BanglaUtils.formatNumber(stats.unpaidMonthsCount)} মাস",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
                Text(
                    text = "মাসিক জমার ইতিহাস:",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                )
                Spacer(modifier = Modifier.height(8.dp))

                if (stats.history.isEmpty()) {
                    Text("কোন জমার রেকর্ড পাওয়া যায়নি", style = MaterialTheme.typography.bodySmall)
                } else {
                    stats.history.forEach { dep ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "${dep.month} ${dep.year}",
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                                )
                                Text(
                                    text = BanglaUtils.formatTaka(dep.amount),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            StatusBadge(isPaid = dep.isPaid)
                        }
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("বন্ধ করুন")
            }
        }
    )
}

@Composable
fun AddMemberDialog(
    adminCount: Int,
    onDismiss: () -> Unit,
    onAdd: (name: String, email: String, phone: String, role: String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var makeAdmin by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("নতুন সদস্য যোগ করুন", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("সদস্যের নাম") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("add_member_name")
                )
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("ইমেইল (লগইনের জন্য)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("add_member_email")
                )
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("মোবাইল নম্বর") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("add_member_phone")
                )

                if (adminCount < 2) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("প্রশাসক (অ্যাডমিন) হিসেবে নির্ধারণ করুন")
                        Switch(
                            checked = makeAdmin,
                            onCheckedChange = { makeAdmin = it },
                            colors = SwitchDefaults.colors(checkedThumbColor = EmeraldPrimary)
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank() && email.isNotBlank()) {
                        val role = if (makeAdmin) User.ROLE_ADMIN else User.ROLE_MEMBER
                        onAdd(name, email, phone, role)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary)
            ) {
                Text("যুক্ত করুন")
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
fun EditMemberDialog(
    member: User,
    callerIsAdmin: Boolean,
    currentAdminCount: Int,
    onDismiss: () -> Unit,
    onSave: (User) -> Unit
) {
    var name by remember { mutableStateOf(member.name) }
    var phone by remember { mutableStateOf(member.phone) }
    var isAdmin by remember { mutableStateOf(member.isAdmin) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("সদস্যের তথ্য সম্পাদনা", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("নাম") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("edit_member_name")
                )
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("মোবাইল নম্বর") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("edit_member_phone")
                )

                if (callerIsAdmin) {
                    val canToggleAdmin = member.isAdmin || currentAdminCount < 2
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("অ্যাডমিন পদবী")
                            if (!canToggleAdmin && !member.isAdmin) {
                                Text("সর্বোচ্চ ২ জন অ্যাডমিন পূর্ণ", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.error)
                            }
                        }
                        Switch(
                            checked = isAdmin,
                            enabled = canToggleAdmin,
                            onCheckedChange = { isAdmin = it },
                            colors = SwitchDefaults.colors(checkedThumbColor = EmeraldPrimary)
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val updated = member.copy(
                        name = name.trim(),
                        phone = phone.trim(),
                        role = if (isAdmin) User.ROLE_ADMIN else User.ROLE_MEMBER
                    )
                    onSave(updated)
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
