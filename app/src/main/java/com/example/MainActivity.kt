package com.example

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.BusinessCenter
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.outlined.Assessment
import androidx.compose.material.icons.outlined.BusinessCenter
import androidx.compose.material.icons.outlined.Group
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.ReceiptLong
import androidx.compose.material.icons.outlined.Savings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.User
import com.example.ui.MainViewModel
import com.example.ui.components.ClubTopAppBar
import com.example.ui.screens.auth.LoginScreen
import com.example.ui.screens.auth.RegisterScreen
import com.example.ui.screens.business.BusinessScreen
import com.example.ui.screens.dashboard.DashboardScreen
import com.example.ui.screens.deposits.DepositsScreen
import com.example.ui.screens.expenses.ExpensesScreen
import com.example.ui.screens.members.MembersScreen
import com.example.ui.screens.profile.FirebaseSetupInstructionsDialog
import com.example.ui.screens.profile.ProfileScreen
import com.example.ui.screens.reports.ReportsScreen
import com.example.ui.theme.EmeraldContainer
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.MyApplicationTheme

enum class Screen(
    val titleBn: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val adminOnly: Boolean = false
) {
    HOME("হোম", Icons.Filled.Home, Icons.Outlined.Home),
    MEMBERS("সদস্য", Icons.Filled.Group, Icons.Outlined.Group),
    DEPOSITS("জমা", Icons.Filled.Savings, Icons.Outlined.Savings),
    BUSINESS("ব্যবসা", Icons.Filled.BusinessCenter, Icons.Outlined.BusinessCenter),
    EXPENSES("খরচ", Icons.Filled.ReceiptLong, Icons.Outlined.ReceiptLong, adminOnly = true),
    REPORTS("রিপোর্ট", Icons.Filled.Assessment, Icons.Outlined.Assessment),
    PROFILE("প্রোফাইল", Icons.Filled.Person, Icons.Outlined.Person)
}

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MyApplicationTheme {
                MainApp(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun MainApp(viewModel: MainViewModel) {
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()
    val members by viewModel.members.collectAsStateWithLifecycle()
    val deposits by viewModel.deposits.collectAsStateWithLifecycle()
    val businesses by viewModel.businesses.collectAsStateWithLifecycle()
    val expenses by viewModel.expenses.collectAsStateWithLifecycle()
    val summary by viewModel.financialSummary.collectAsStateWithLifecycle()
    val activities by viewModel.recentActivities.collectAsStateWithLifecycle()
    val selectedMonth by viewModel.selectedMonth.collectAsStateWithLifecycle()
    val selectedYear by viewModel.selectedYear.collectAsStateWithLifecycle()
    val uiMessage by viewModel.uiMessage.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }
    var currentScreen by remember { mutableStateOf(Screen.HOME) }
    var isRegistering by remember { mutableStateOf(false) }
    var authLoading by remember { mutableStateOf(false) }
    var authError by remember { mutableStateOf<String?>(null) }
    var showGuideDialog by remember { mutableStateOf(false) }

    LaunchedEffect(uiMessage) {
        if (!uiMessage.isNullOrBlank()) {
            snackbarHostState.showSnackbar(uiMessage!!)
            viewModel.clearUiMessage()
        }
    }

    if (currentUser == null) {
        // Auth Flow
        if (isRegistering) {
            RegisterScreen(
                onRegister = { name, email, pass, phone ->
                    authLoading = true
                    authError = null
                    viewModel.register(
                        name = name,
                        email = email,
                        pass = pass,
                        phone = phone,
                        onSuccess = {
                            authLoading = false
                            isRegistering = false
                        },
                        onError = {
                            authLoading = false
                            authError = it
                        }
                    )
                },
                onBackToLogin = {
                    isRegistering = false
                    authError = null
                },
                currentMemberCount = members.size,
                isLoading = authLoading,
                errorMessage = authError
            )
        } else {
            LoginScreen(
                onLogin = { email, pass ->
                    authLoading = true
                    authError = null
                    viewModel.login(
                        email = email,
                        pass = pass,
                        onSuccess = { authLoading = false },
                        onError = {
                            authLoading = false
                            authError = it
                        }
                    )
                },
                onNavigateToRegister = {
                    isRegistering = true
                    authError = null
                },
                onResetPassword = { email ->
                    viewModel.sendPasswordReset(
                        email = email,
                        onSuccess = { viewModel.setUiMessage("পাসওয়ার্ড রিসেট লিংক পাঠানো হয়েছে") },
                        onError = { viewModel.setUiMessage(it) }
                    )
                },
                onQuickSwitchUser = { user ->
                    viewModel.switchUser(user)
                },
                sampleUsers = members,
                isLoading = authLoading,
                errorMessage = authError
            )
        }
    } else {
        // Authenticated App Shell
        val isAdmin = currentUser?.isAdmin == true
        val navItems = Screen.entries.filter { !it.adminOnly || isAdmin }

        // BackHandler to return to HOME before exiting
        BackHandler(enabled = currentScreen != Screen.HOME) {
            currentScreen = Screen.HOME
        }

        Scaffold(
            modifier = Modifier.fillMaxSize(),
            snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
            topBar = {
                ClubTopAppBar(
                    currentUser = currentUser,
                    isCloudConnected = viewModel.repository.isCloudConnected(),
                    onOpenGuide = { showGuideDialog = true },
                    onLogout = { viewModel.logout() }
                )
            },
            bottomBar = {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = EmeraldPrimary
                ) {
                    navItems.forEach { screen ->
                        val isSelected = currentScreen == screen
                        NavigationBarItem(
                            selected = isSelected,
                            onClick = { currentScreen = screen },
                            icon = {
                                Icon(
                                    imageVector = if (isSelected) screen.selectedIcon else screen.unselectedIcon,
                                    contentDescription = screen.titleBn
                                )
                            },
                            label = {
                                Text(
                                    text = screen.titleBn,
                                    fontSize = 10.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    maxLines = 1
                                )
                            },
                            alwaysShowLabel = true,
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = EmeraldPrimary,
                                selectedTextColor = EmeraldPrimary,
                                indicatorColor = EmeraldContainer
                            ),
                            modifier = Modifier.testTag("nav_item_${screen.name.lowercase()}")
                        )
                    }
                }
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                AnimatedContent(
                    targetState = currentScreen,
                    transitionSpec = { fadeIn() togetherWith fadeOut() },
                    label = "ScreenTransition"
                ) { targetScreen ->
                    when (targetScreen) {
                        Screen.HOME -> {
                            DashboardScreen(
                                summary = summary,
                                activities = activities,
                                currentUser = currentUser,
                                selectedMonth = selectedMonth,
                                selectedYear = selectedYear,
                                onNavigateToDeposits = { currentScreen = Screen.DEPOSITS },
                                onNavigateToBusiness = { currentScreen = Screen.BUSINESS },
                                onNavigateToExpenses = { currentScreen = Screen.EXPENSES },
                                onNavigateToMembers = { currentScreen = Screen.MEMBERS }
                            )
                        }

                        Screen.MEMBERS -> {
                            MembersScreen(
                                members = members,
                                currentUser = currentUser,
                                viewModel = viewModel
                            )
                        }

                        Screen.DEPOSITS -> {
                            DepositsScreen(
                                members = members,
                                deposits = deposits,
                                currentUser = currentUser,
                                selectedMonth = selectedMonth,
                                selectedYear = selectedYear,
                                viewModel = viewModel
                            )
                        }

                        Screen.BUSINESS -> {
                            BusinessScreen(
                                businesses = businesses,
                                currentUser = currentUser,
                                viewModel = viewModel
                            )
                        }

                        Screen.EXPENSES -> {
                            ExpensesScreen(
                                expenses = expenses,
                                currentUser = currentUser,
                                viewModel = viewModel
                            )
                        }

                        Screen.REPORTS -> {
                            ReportsScreen(
                                members = members,
                                deposits = deposits,
                                businesses = businesses,
                                expenses = expenses,
                                summary = summary,
                                selectedMonth = selectedMonth,
                                selectedYear = selectedYear,
                                onMonthSelected = { viewModel.setSelectedMonth(it) },
                                onYearSelected = { viewModel.setSelectedYear(it) }
                            )
                        }

                        Screen.PROFILE -> {
                            ProfileScreen(
                                currentUser = currentUser,
                                members = members,
                                viewModel = viewModel,
                                onLogout = { viewModel.logout() },
                                isCloudConnected = viewModel.repository.isCloudConnected()
                            )
                        }
                    }
                }
            }
        }

        if (showGuideDialog) {
            FirebaseSetupInstructionsDialog(onDismiss = { showGuideDialog = false })
        }
    }
}
