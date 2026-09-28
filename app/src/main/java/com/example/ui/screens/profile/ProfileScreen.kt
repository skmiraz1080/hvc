package com.example.ui.screens.profile

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.User
import com.example.ui.MainViewModel
import com.example.ui.components.MemberAvatar
import com.example.ui.theme.AccentGold
import com.example.ui.theme.EmeraldContainer
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.StatusPaid
import com.example.ui.theme.StatusUnpaid
import com.example.util.BanglaUtils

@Composable
fun ProfileScreen(
    currentUser: User?,
    members: List<User>,
    viewModel: MainViewModel,
    onLogout: () -> Unit,
    isCloudConnected: Boolean
) {
    var showEditProfileDialog by remember { mutableStateOf(false) }
    var showFirebaseGuideDialog by remember { mutableStateOf(false) }
    var switchMenuExpanded by remember { mutableStateOf(false) }

    val userStats = currentUser?.let { viewModel.getMemberStats(it.uid) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // User Profile Hero Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                MemberAvatar(
                    name = currentUser?.name ?: "সদস্য",
                    isAdmin = currentUser?.isAdmin == true,
                    size = 72
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = currentUser?.name ?: "অজ্ঞাত",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                )

                Spacer(modifier = Modifier.height(4.dp))

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (currentUser?.isAdmin == true) EmeraldContainer else MaterialTheme.colorScheme.secondaryContainer
                ) {
                    Text(
                        text = if (currentUser?.isAdmin == true) "প্রশাসক (অ্যাডমিন)" else "সাধারণ সদস্য",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = if (currentUser?.isAdmin == true) EmeraldPrimary else MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = currentUser?.email ?: "",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                if (!currentUser?.phone.isNullOrBlank()) {
                    Text(
                        text = "মোবাইল: ${currentUser?.phone}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "যোগদান: ${currentUser?.joinedDate ?: "২০২৬"}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedButton(
                    onClick = { showEditProfileDialog = true },
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("নিজের প্রোফাইল সম্পাদনা")
                }
            }
        }

        // Personal Financial Record Card
        if (userStats != null) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "আমার ব্যক্তিগত সঞ্চয় হিসাব",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = EmeraldPrimary
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("মোট জমা", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(
                                BanglaUtils.formatTaka(userStats.totalDeposited),
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = StatusPaid)
                            )
                        }

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("পরিশোধিত", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(
                                "${BanglaUtils.formatNumber(userStats.paidMonthsCount)} মাস",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = StatusPaid)
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text("বকেয়া", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(
                                "${BanglaUtils.formatNumber(userStats.unpaidMonthsCount)} মাস",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = if (userStats.unpaidMonthsCount > 0) StatusUnpaid else StatusPaid)
                            )
                        }
                    }
                }
            }
        }

        // Firebase Cloud Status & Integration Guide
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (isCloudConnected) Icons.Default.CloudDone else Icons.Default.CloudOff,
                            contentDescription = null,
                            tint = if (isCloudConnected) EmeraldPrimary else AccentGold
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isCloudConnected) "ফায়ারবেস ক্লাউড সংযুক্ত" else "স্থানীয় প্রিভিউ মোড (Local Mode)",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    }

                    TextButton(onClick = { showFirebaseGuideDialog = true }) {
                        Text("নির্দেশনা দেখুন")
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = if (isCloudConnected) {
                        "ক্লাবের সকল সঞ্চয়, ব্যবসা ও খরচের ডেটা ফায়ারবেস ফায়ারস্টোর ক্লাউডের সাথে রিয়েল-টাইমে সিঙ্ক হচ্ছে।"
                    } else {
                        "অ্যাপটি সফলভাবে চলছে। ক্লাউড সিঙ্ক চালু করতে ফায়ারবেস প্রজেক্টের google-services.json ফাইলটি অ্যাপে যুক্ত করুন।"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Role Switcher for live testing
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "ভূমিকা ও ব্যবহারকারী পরিবর্তন (Role Switcher)",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
                Text(
                    text = "অ্যাডমিন ও সাধারণ সদস্যের পারমিশন এবং ভিউ পরীক্ষা করতে নিচে যেকোনো সদস্য বেছে নিন:",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(10.dp))

                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedButton(
                        onClick = { switchMenuExpanded = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.SwapHoriz, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("সদস্য বেছে নিন: ${currentUser?.name ?: ""}")
                    }

                    DropdownMenu(
                        expanded = switchMenuExpanded,
                        onDismissRequest = { switchMenuExpanded = false }
                    ) {
                        members.forEach { m ->
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        "${m.name} (${if (m.isAdmin) "অ্যাডমিন" else "সদস্য"})",
                                        fontWeight = if (m.uid == currentUser?.uid) FontWeight.Bold else FontWeight.Normal
                                    )
                                },
                                onClick = {
                                    viewModel.switchUser(m)
                                    switchMenuExpanded = false
                                }
                            )
                        }
                    }
                }
            }
        }

        // Logout Button
        Button(
            onClick = onLogout,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .testTag("logout_button"),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.Default.ExitToApp, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("লগআউট করুন (Logout)")
        }

        Spacer(modifier = Modifier.height(60.dp))
    }

    // Edit Profile Dialog
    if (showEditProfileDialog && currentUser != null) {
        var name by remember { mutableStateOf(currentUser.name) }
        var phone by remember { mutableStateOf(currentUser.phone) }

        AlertDialog(
            onDismissRequest = { showEditProfileDialog = false },
            title = { Text("প্রোফাইল সম্পাদনা", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("পূর্ণ নাম") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = phone,
                        onValueChange = { phone = it },
                        label = { Text("মোবাইল নম্বর") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val updated = currentUser.copy(name = name.trim(), phone = phone.trim())
                        viewModel.updateMember(
                            user = updated,
                            onSuccess = { showEditProfileDialog = false },
                            onError = { viewModel.setUiMessage(it) }
                        )
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary)
                ) {
                    Text("সংরক্ষণ")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditProfileDialog = false }) {
                    Text("বাতিল")
                }
            }
        )
    }

    // Firebase Setup Instructions Dialog
    if (showFirebaseGuideDialog) {
        FirebaseSetupInstructionsDialog(onDismiss = { showFirebaseGuideDialog = false })
    }
}

@Composable
fun FirebaseSetupInstructionsDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Security, contentDescription = null, tint = EmeraldPrimary)
                Spacer(modifier = Modifier.width(8.dp))
                Text("ফায়ারবেস সেটআপ নির্দেশিকা", fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    "১. ফায়ারবেস প্রজেক্ট তৈরি করুন:",
                    fontWeight = FontWeight.Bold,
                    color = EmeraldPrimary
                )
                Text("• console.firebase.google.com এ যান এবং নতুন প্রজেক্ট তৈরি করুন।\n• Android App যুক্ত করুন এবং প্যাকেজ নাম দিন:\n  com.aistudio.hobirbariclub.vntr")

                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "২. google-services.json ফাইল সংযোজন:",
                    fontWeight = FontWeight.Bold,
                    color = EmeraldPrimary
                )
                Text("• কনসোল থেকে google-services.json ডাউনলোড করে /app ফোল্ডারে রাখুন।")

                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "৩. Authentication চালু করুন:",
                    fontWeight = FontWeight.Bold,
                    color = EmeraldPrimary
                )
                Text("• Firebase Authentication এ গিয়ে 'Email/Password' সাইন-ইন পদ্ধতি এনাবল করুন।")

                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "৪. Firestore Database ও রুলস:",
                    fontWeight = FontWeight.Bold,
                    color = EmeraldPrimary
                )
                Text("• Cloud Firestore চালু করুন এবং এই প্রজেক্টের root এ থাকা 'firestore.rules' ফাইলের রুলস পেস্ট করুন।")

                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "৫. ২ জন অ্যাডমিন ও ১০ সদস্য সীমাবদ্ধতা:",
                    fontWeight = FontWeight.Bold,
                    color = EmeraldPrimary
                )
                Text("• অ্যাপে স্বয়ংক্রিয়ভাবে সর্বোচ্চ ১০ জন সদস্য ও সর্বোচ্চ ২ জন অ্যাডমিনের রুলস কার্যকর রয়েছে।")
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("বুঝেছি (Close)")
            }
        }
    )
}
