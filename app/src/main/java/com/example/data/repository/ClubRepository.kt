package com.example.data.repository

import android.content.Context
import android.util.Log
import com.example.data.model.ActivityItem
import com.example.data.model.Business
import com.example.data.model.Deposit
import com.example.data.model.Expense
import com.example.data.model.FinancialSummary
import com.example.data.model.InstallmentPayment
import com.example.data.model.User
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.UUID

class ClubRepository(private val context: Context) {

    private val tag = "ClubRepository"

    private var isFirebaseAvailable: Boolean = false
    private var auth: FirebaseAuth? = null
    private var firestore: FirebaseFirestore? = null

    // Fallback in-memory state when Firebase is not configured yet
    private val localUsers = MutableStateFlow<List<User>>(emptyList())
    private val localDeposits = MutableStateFlow<List<Deposit>>(emptyList())
    private val localBusinesses = MutableStateFlow<List<Business>>(emptyList())
    private val localExpenses = MutableStateFlow<List<Expense>>(emptyList())
    private val localCurrentUser = MutableStateFlow<User?>(null)

    init {
        checkFirebase()
        if (!isFirebaseAvailable) {
            seedDefaultData()
        }
    }

    private fun checkFirebase() {
        try {
            val app = FirebaseApp.initializeApp(context) ?: FirebaseApp.getInstance()
            if (app != null) {
                auth = FirebaseAuth.getInstance()
                firestore = FirebaseFirestore.getInstance()
                isFirebaseAvailable = true
                Log.d(tag, "Firebase initialized successfully.")
            }
        } catch (e: Exception) {
            Log.w(tag, "Firebase not initialized. Running in local test/preview mode: ${e.message}")
            isFirebaseAvailable = false
        }
    }

    fun isCloudConnected(): Boolean = isFirebaseAvailable

    // ----------------------------------------------------
    // AUTHENTICATION
    // ----------------------------------------------------

    fun getCurrentUserFlow(): Flow<User?> = callbackFlow {
        if (isFirebaseAvailable && auth != null && firestore != null) {
            val authListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
                val fbUser = firebaseAuth.currentUser
                if (fbUser == null) {
                    trySend(null)
                } else {
                    firestore!!.collection("users").document(fbUser.uid)
                        .addSnapshotListener { snapshot, _ ->
                            if (snapshot != null && snapshot.exists()) {
                                val user = snapshot.toObject(User::class.java)
                                trySend(user)
                            } else {
                                // Default member profile if document not created yet
                                trySend(
                                    User(
                                        uid = fbUser.uid,
                                        email = fbUser.email ?: "",
                                        name = fbUser.displayName ?: "সদস্য",
                                        role = User.ROLE_MEMBER
                                    )
                                )
                            }
                        }
                }
            }
            auth!!.addAuthStateListener(authListener)
            awaitClose { auth?.removeAuthStateListener(authListener) }
        } else {
            // Local fallback flow
            val job = kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.Default).run {
                localCurrentUser.collect { user ->
                    trySend(user)
                }
            }
            awaitClose { }
        }
    }

    suspend fun login(email: String, pass: String): Result<User> {
        val cleanEmail = email.trim().lowercase()
        return try {
            if (isFirebaseAvailable && auth != null && firestore != null) {
                val authResult = auth!!.signInWithEmailAndPassword(cleanEmail, pass).await()
                val uid = authResult.user?.uid ?: throw Exception("ইউজার আইডি পাওয়া যায়নি")
                val doc = firestore!!.collection("users").document(uid).get().await()
                val user = doc.toObject(User::class.java) ?: User(
                    uid = uid,
                    email = cleanEmail,
                    name = cleanEmail.substringBefore("@"),
                    role = User.ROLE_MEMBER
                )
                Result.success(user)
            } else {
                // Local mode login: match by email or create mock session
                val found = localUsers.value.find { it.email.equals(cleanEmail, ignoreCase = true) }
                val user = found ?: User(
                    uid = "local_" + UUID.randomUUID().toString().take(6),
                    email = cleanEmail,
                    name = cleanEmail.substringBefore("@"),
                    role = if (localUsers.value.count { it.isAdmin } < 2) User.ROLE_ADMIN else User.ROLE_MEMBER
                )
                if (found == null && localUsers.value.size < 10) {
                    localUsers.value = localUsers.value + user
                }
                localCurrentUser.value = user
                Result.success(user)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun register(name: String, email: String, pass: String, phone: String): Result<User> {
        val cleanEmail = email.trim().lowercase()
        return try {
            // Check member count constraint (Max 10)
            val currentCount = getUsersCount()
            if (currentCount >= 10) {
                throw Exception("ক্লাবে সর্বোচ্চ ১০ জন সদস্য হতে পারে! নতুন সদস্য যোগ করা সম্ভব নয়।")
            }

            if (isFirebaseAvailable && auth != null && firestore != null) {
                val authResult = auth!!.createUserWithEmailAndPassword(cleanEmail, pass).await()
                val uid = authResult.user?.uid ?: throw Exception("ইউজার তৈরি করা সম্ভব হয়নি")
                val user = User(
                    uid = uid,
                    name = name.trim(),
                    email = cleanEmail,
                    phone = phone.trim(),
                    role = User.ROLE_MEMBER, // Security: never default to admin
                    joinedDate = "সেপ্টেম্বর ২০২৬",
                    createdAt = System.currentTimeMillis()
                )
                firestore!!.collection("users").document(uid).set(user).await()
                Result.success(user)
            } else {
                val user = User(
                    uid = "local_" + UUID.randomUUID().toString().take(6),
                    name = name.trim(),
                    email = cleanEmail,
                    phone = phone.trim(),
                    role = User.ROLE_MEMBER,
                    joinedDate = "সেপ্টেম্বর ২০২৬",
                    createdAt = System.currentTimeMillis()
                )
                localUsers.value = localUsers.value + user
                localCurrentUser.value = user
                Result.success(user)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun sendPasswordReset(email: String): Result<Unit> {
        val cleanEmail = email.trim().lowercase()
        return try {
            if (isFirebaseAvailable && auth != null) {
                auth!!.sendPasswordResetEmail(cleanEmail).await()
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun logout() {
        if (isFirebaseAvailable && auth != null) {
            auth!!.signOut()
        }
        localCurrentUser.value = null
    }

    // Switch active user in preview mode (handy for testing admin vs member view)
    fun switchLocalUser(user: User) {
        localCurrentUser.value = user
    }

    // ----------------------------------------------------
    // MEMBERS MANAGEMENT (Max 10 members, Max 2 admins)
    // ----------------------------------------------------

    fun getMembersFlow(): Flow<List<User>> = callbackFlow {
        if (isFirebaseAvailable && firestore != null) {
            val listener: ListenerRegistration = firestore!!.collection("users")
                .orderBy("createdAt", Query.Direction.ASCENDING)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        trySend(localUsers.value)
                        return@addSnapshotListener
                    }
                    if (snapshot != null) {
                        val list = snapshot.toObjects(User::class.java)
                        trySend(list)
                    }
                }
            awaitClose { listener.remove() }
        } else {
            val job = kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.Default).run {
                localUsers.collect { trySend(it) }
            }
            awaitClose { }
        }
    }

    private suspend fun getUsersCount(): Int {
        return if (isFirebaseAvailable && firestore != null) {
            try {
                val snapshot = firestore!!.collection("users").get().await()
                snapshot.size()
            } catch (e: Exception) {
                localUsers.value.size
            }
        } else {
            localUsers.value.size
        }
    }

    suspend fun addMember(
        name: String,
        email: String,
        phone: String,
        role: String = User.ROLE_MEMBER
    ): Result<Unit> {
        return try {
            val count = getUsersCount()
            if (count >= 10) {
                throw Exception("ক্লাবে সর্বোচ্চ ১০ জন সদস্যের সীমাবদ্ধতা রয়েছে।")
            }

            if (role == User.ROLE_ADMIN) {
                val adminCount = getAdminCount()
                if (adminCount >= 2) {
                    throw Exception("সংগঠনে সর্বোচ্চ ২ জন অ্যাডমিন থাকতে পারেন।")
                }
            }

            val uid = "user_" + UUID.randomUUID().toString().take(8)
            val newUser = User(
                uid = uid,
                name = name.trim(),
                email = email.trim().lowercase(),
                phone = phone.trim(),
                role = role,
                joinedDate = "সেপ্টেম্বর ২০২৬",
                createdAt = System.currentTimeMillis()
            )

            if (isFirebaseAvailable && firestore != null) {
                firestore!!.collection("users").document(uid).set(newUser).await()
            } else {
                localUsers.value = localUsers.value + newUser
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateMember(user: User, callerIsAdmin: Boolean): Result<Unit> {
        return try {
            if (isFirebaseAvailable && firestore != null) {
                val existingDoc = firestore!!.collection("users").document(user.uid).get().await()
                val existing = existingDoc.toObject(User::class.java)

                if (!callerIsAdmin && existing?.role != user.role) {
                    throw Exception("সাধারণ সদস্য নিজের পদবী পরিবর্তন করতে পারবেন না।")
                }

                if (callerIsAdmin && user.role == User.ROLE_ADMIN && existing?.role != User.ROLE_ADMIN) {
                    if (getAdminCount() >= 2) {
                        throw Exception("সর্বোচ্চ ২ জন অ্যাডমিন হতে পারে!")
                    }
                }

                firestore!!.collection("users").document(user.uid).set(user).await()
            } else {
                val existing = localUsers.value.find { it.uid == user.uid }
                if (!callerIsAdmin && existing?.role != user.role) {
                    throw Exception("সাধারণ সদস্য নিজের পদবী পরিবর্তন করতে পারবেন না।")
                }
                if (callerIsAdmin && user.role == User.ROLE_ADMIN && existing?.role != User.ROLE_ADMIN) {
                    if (getAdminCount() >= 2) {
                        throw Exception("সর্বোচ্চ ২ জন অ্যাডমিন হতে পারে!")
                    }
                }
                localUsers.value = localUsers.value.map { if (it.uid == user.uid) user else it }
                if (localCurrentUser.value?.uid == user.uid) {
                    localCurrentUser.value = user
                }
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun removeMember(userId: String): Result<Unit> {
        return try {
            if (isFirebaseAvailable && firestore != null) {
                firestore!!.collection("users").document(userId).delete().await()
            } else {
                localUsers.value = localUsers.value.filter { it.uid != userId }
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private suspend fun getAdminCount(): Int {
        return if (isFirebaseAvailable && firestore != null) {
            try {
                val snapshot = firestore!!.collection("users")
                    .whereEqualTo("role", User.ROLE_ADMIN)
                    .get()
                    .await()
                snapshot.size()
            } catch (e: Exception) {
                localUsers.value.count { it.isAdmin }
            }
        } else {
            localUsers.value.count { it.isAdmin }
        }
    }

    // ----------------------------------------------------
    // MONTHLY DEPOSIT SYSTEM
    // ----------------------------------------------------

    fun getDepositsFlow(): Flow<List<Deposit>> = callbackFlow {
        if (isFirebaseAvailable && firestore != null) {
            val listener = firestore!!.collection("deposits")
                .orderBy("year", Query.Direction.DESCENDING)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        trySend(localDeposits.value)
                        return@addSnapshotListener
                    }
                    if (snapshot != null) {
                        val list = snapshot.toObjects(Deposit::class.java)
                        trySend(list)
                    }
                }
            awaitClose { listener.remove() }
        } else {
            val job = kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.Default).run {
                localDeposits.collect { trySend(it) }
            }
            awaitClose { }
        }
    }

    suspend fun saveOrUpdateDeposit(deposit: Deposit): Result<Unit> {
        return try {
            val depositId = if (deposit.id.isBlank()) "dep_${UUID.randomUUID().toString().take(8)}" else deposit.id
            val finalDeposit = deposit.copy(
                id = depositId,
                updatedAt = System.currentTimeMillis()
            )

            if (isFirebaseAvailable && firestore != null) {
                firestore!!.collection("deposits").document(depositId).set(finalDeposit).await()
            } else {
                val current = localDeposits.value.toMutableList()
                val idx = current.indexOfFirst { it.id == depositId || (it.memberId == deposit.memberId && it.month == deposit.month && it.year == deposit.year) }
                if (idx != -1) {
                    current[idx] = finalDeposit
                } else {
                    current.add(0, finalDeposit)
                }
                localDeposits.value = current
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun toggleDepositStatus(
        memberId: String,
        memberName: String,
        month: String,
        year: Int,
        amount: Double = 1000.0,
        recordedBy: String
    ): Result<Unit> {
        return try {
            val existing = localDeposits.value.find { it.memberId == memberId && it.month == month && it.year == year }
            val newStatus = if (existing?.isPaid == true) Deposit.STATUS_UNPAID else Deposit.STATUS_PAID

            val deposit = (existing ?: Deposit(
                id = "dep_${UUID.randomUUID().toString().take(8)}",
                memberId = memberId,
                memberName = memberName,
                month = month,
                year = year,
                amount = amount,
                recordedBy = recordedBy
            )).copy(
                status = newStatus,
                amount = amount,
                updatedAt = System.currentTimeMillis(),
                recordedBy = recordedBy
            )

            saveOrUpdateDeposit(deposit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteDeposit(depositId: String): Result<Unit> {
        return try {
            if (isFirebaseAvailable && firestore != null) {
                firestore!!.collection("deposits").document(depositId).delete().await()
            } else {
                localDeposits.value = localDeposits.value.filter { it.id != depositId }
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ----------------------------------------------------
    // BUSINESS & PROFIT MODULE
    // ----------------------------------------------------

    fun getBusinessesFlow(): Flow<List<Business>> = callbackFlow {
        if (isFirebaseAvailable && firestore != null) {
            val listener = firestore!!.collection("businesses")
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        trySend(localBusinesses.value)
                        return@addSnapshotListener
                    }
                    if (snapshot != null) {
                        val list = snapshot.toObjects(Business::class.java)
                        trySend(list)
                    }
                }
            awaitClose { listener.remove() }
        } else {
            val job = kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.Default).run {
                localBusinesses.collect { trySend(it) }
            }
            awaitClose { }
        }
    }

    suspend fun saveBusiness(business: Business): Result<Unit> {
        return try {
            val bId = if (business.id.isBlank()) "biz_${UUID.randomUUID().toString().take(8)}" else business.id
            val finalBiz = business.copy(
                id = bId,
                updatedAt = System.currentTimeMillis()
            )

            if (isFirebaseAvailable && firestore != null) {
                firestore!!.collection("businesses").document(bId).set(finalBiz).await()
            } else {
                val current = localBusinesses.value.toMutableList()
                val idx = current.indexOfFirst { it.id == bId }
                if (idx != -1) {
                    current[idx] = finalBiz
                } else {
                    current.add(0, finalBiz)
                }
                localBusinesses.value = current
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun recordInstallmentPayment(
        businessId: String,
        payment: InstallmentPayment
    ): Result<Unit> {
        return try {
            val biz = (if (isFirebaseAvailable && firestore != null) {
                val doc = firestore!!.collection("businesses").document(businessId).get().await()
                doc.toObject(Business::class.java)
            } else {
                localBusinesses.value.find { it.id == businessId }
            }) ?: throw Exception("ব্যবসার হিসাব পাওয়া যায়নি")

            val updatedPayments = biz.payments + payment
            val updatedBiz = biz.copy(
                payments = updatedPayments,
                updatedAt = System.currentTimeMillis()
            )
            saveBusiness(updatedBiz)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteBusiness(businessId: String): Result<Unit> {
        return try {
            if (isFirebaseAvailable && firestore != null) {
                firestore!!.collection("businesses").document(businessId).delete().await()
            } else {
                localBusinesses.value = localBusinesses.value.filter { it.id != businessId }
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ----------------------------------------------------
    // EXPENSE MODULE
    // ----------------------------------------------------

    fun getExpensesFlow(): Flow<List<Expense>> = callbackFlow {
        if (isFirebaseAvailable && firestore != null) {
            val listener = firestore!!.collection("expenses")
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        trySend(localExpenses.value)
                        return@addSnapshotListener
                    }
                    if (snapshot != null) {
                        val list = snapshot.toObjects(Expense::class.java)
                        trySend(list)
                    }
                }
            awaitClose { listener.remove() }
        } else {
            val job = kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.Default).run {
                localExpenses.collect { trySend(it) }
            }
            awaitClose { }
        }
    }

    suspend fun saveExpense(expense: Expense): Result<Unit> {
        return try {
            val expId = if (expense.id.isBlank()) "exp_${UUID.randomUUID().toString().take(8)}" else expense.id
            val finalExp = expense.copy(
                id = expId,
                updatedAt = System.currentTimeMillis()
            )

            if (isFirebaseAvailable && firestore != null) {
                firestore!!.collection("expenses").document(expId).set(finalExp).await()
            } else {
                val current = localExpenses.value.toMutableList()
                val idx = current.indexOfFirst { it.id == expId }
                if (idx != -1) {
                    current[idx] = finalExp
                } else {
                    current.add(0, finalExp)
                }
                localExpenses.value = current
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteExpense(expenseId: String): Result<Unit> {
        return try {
            if (isFirebaseAvailable && firestore != null) {
                firestore!!.collection("expenses").document(expenseId).delete().await()
            } else {
                localExpenses.value = localExpenses.value.filter { it.id != expenseId }
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ----------------------------------------------------
    // FINANCIAL SUMMARY & RECENT ACTIVITIES
    // ----------------------------------------------------

    fun calculateSummary(
        members: List<User>,
        deposits: List<Deposit>,
        businesses: List<Business>,
        expenses: List<Expense>,
        currentMonth: String,
        currentYear: Int
    ): FinancialSummary {
        val totalMembers = members.size

        // Only paid deposits count towards total fund
        val totalDeposited = deposits.filter { it.isPaid }.sumOf { it.amount }

        // Total profit from all businesses
        val totalBusinessProfit = businesses.sumOf { it.profit }

        // Total expenses
        val totalExpenses = expenses.sumOf { it.amount }

        // Formula: Current Total Fund = Total Deposits + Total Business Profit - Total Expenses
        val currentTotalFund = (totalDeposited + totalBusinessProfit) - totalExpenses

        // Current month target (10 members * ৳1,000)
        val currentMonthTarget = 10 * 1000.0

        // Current month collection
        val currentMonthCollected = deposits
            .filter { it.month == currentMonth && it.year == currentYear && it.isPaid }
            .sumOf { it.amount }

        return FinancialSummary(
            totalMembers = totalMembers,
            totalDeposited = totalDeposited,
            totalBusinessProfit = totalBusinessProfit,
            totalExpenses = totalExpenses,
            currentTotalFund = currentTotalFund,
            currentMonthTarget = currentMonthTarget,
            currentMonthCollected = currentMonthCollected
        )
    }

    fun getRecentActivities(
        deposits: List<Deposit>,
        businesses: List<Business>,
        expenses: List<Expense>
    ): List<ActivityItem> {
        val activities = mutableListOf<ActivityItem>()

        deposits.filter { it.isPaid }.forEach { dep ->
            activities.add(
                ActivityItem.DepositActivity(
                    timestamp = dep.updatedAt,
                    title = "${dep.memberName} - মাসিক জমা",
                    subtitle = "${dep.month} ${dep.year}",
                    amount = dep.amount
                )
            )
        }

        businesses.forEach { biz ->
            activities.add(
                ActivityItem.ProfitActivity(
                    timestamp = biz.createdAt,
                    title = "${biz.businessName} - ব্যবসায়িক লাভ",
                    subtitle = biz.date.ifEmpty { "ব্যবসা প্রকল্প" },
                    amount = biz.profit
                )
            )
        }

        expenses.forEach { exp ->
            activities.add(
                ActivityItem.ExpenseActivity(
                    timestamp = exp.createdAt,
                    title = "${exp.title} - খরচ",
                    subtitle = exp.category,
                    amount = exp.amount
                )
            )
        }

        return activities.sortedByDescending { it.timestamp }.take(10)
    }

    // ----------------------------------------------------
    // INITIAL SEED DATA (Exactly 10 members, 2 admins)
    // ----------------------------------------------------

    private fun seedDefaultData() {
        val membersList = listOf(
            User("u1", "মিরাজ", "miraj.admin@hobirbari.org", "01711-234567", "", User.ROLE_ADMIN, "১ জুলাই ২০২৫"),
            User("u2", "রায়হান", "rayhan.admin@hobirbari.org", "01712-345678", "", User.ROLE_ADMIN, "১ জুলাই ২০২৫"),
            User("u3", "আরফান", "arfan@hobirbari.org", "01811-456789", "", User.ROLE_MEMBER, "১ জুলাই ২০২৫"),
            User("u4", "ওয়াহিদ", "wahid@hobirbari.org", "01911-567890", "", User.ROLE_MEMBER, "১ জুলাই ২০২৫"),
            User("u5", "রাতুল", "ratul@hobirbari.org", "01611-678901", "", User.ROLE_MEMBER, "১ জুলাই ২০২৫"),
            User("u6", "ওবায়দুল", "obaidul@hobirbari.org", "01722-789012", "", User.ROLE_MEMBER, "১ জুলাই ২০২৫"),
            User("u7", "তামান্না", "tamanna@hobirbari.org", "01822-890123", "", User.ROLE_MEMBER, "১ জুলাই ২০২৫"),
            User("u8", "মাহবুবা", "mahbuba@hobirbari.org", "01922-901234", "", User.ROLE_MEMBER, "১ জুলাই ২০২৫")
        )

        localUsers.value = membersList
        localCurrentUser.value = membersList[0] // Default to Admin 1 (মিরাজ)

        // Seed realistic monthly deposits starting from club start date (১ জুলাই ২০২৫)
        val depositsList = mutableListOf<Deposit>()
        var depId = 1

        // 2025 Months (July to December)
        val months2025 = listOf("জুলাই", "আগস্ট", "সেপ্টেম্বর", "অক্টোবর", "নভেম্বর", "ডিসেম্বর")
        months2025.forEachIndexed { mIdx, mName ->
            membersList.forEach { user ->
                depositsList.add(
                    Deposit(
                        id = "dep_${depId++}",
                        memberId = user.uid,
                        memberName = user.name,
                        month = mName,
                        year = 2025,
                        amount = 1000.0,
                        status = Deposit.STATUS_PAID,
                        updatedAt = System.currentTimeMillis() - (15 - mIdx) * 86400000L * 30,
                        recordedBy = "মিরাজ (অ্যাডমিন)"
                    )
                )
            }
        }

        // 2026 Months (January to September)
        val months2026 = listOf("জানুয়ারি", "ফেব্রুয়ারি", "মার্চ", "এপ্রিল", "মে", "জুন", "জুলাই", "আগস্ট", "সেপ্টেম্বর")
        months2026.forEachIndexed { mIdx, mName ->
            membersList.forEachIndexed { uIdx, user ->
                val isPaid = if (mName == "সেপ্টেম্বর") {
                    uIdx < 6 // 6 paid, 2 unpaid in current month
                } else {
                    uIdx != 7 || mIdx > 2
                }
                depositsList.add(
                    Deposit(
                        id = "dep_${depId++}",
                        memberId = user.uid,
                        memberName = user.name,
                        month = mName,
                        year = 2026,
                        amount = 1000.0,
                        status = if (isPaid) Deposit.STATUS_PAID else Deposit.STATUS_UNPAID,
                        updatedAt = System.currentTimeMillis() - (9 - mIdx) * 86400000L * 30,
                        recordedBy = "মিরাজ (অ্যাডমিন)"
                    )
                )
            }
        }
        localDeposits.value = depositsList

        // Seed business projects
        localBusinesses.value = listOf(
            Business(
                id = "biz_1",
                businessName = "হবিরবাড়ি মৎস্য চাষ প্রকল্প",
                description = "স্থানীয় পুকুরে রুই ও কাতলা মাছ চাষ যৌথ উদ্যোগ",
                investment = 25000.0,
                profit = 18500.0,
                date = "১৫ মার্চ ২০২৬",
                notes = "প্রথম ধাপের সফল বিক্রির পর লভ্যাংশ ক্লাবের ফান্ডে জমা হয়েছে",
                createdAt = System.currentTimeMillis() - 86400000L * 120,
                createdBy = "মিরাজ"
            ),
            Business(
                id = "biz_2",
                businessName = "আম ও লিচু বাগান লিজ",
                description = "মৌসুমি ফল বাগান লিজ ও পাইকারি বিক্রয়",
                investment = 15000.0,
                profit = 12000.0,
                date = "২০ মে ২০২৬",
                notes = "মৌসুম শেষে সকল খরচের পর নেট লাভ ক্লাবের হিসাবে যুক্ত হয়েছে",
                createdAt = System.currentTimeMillis() - 86400000L * 70,
                createdBy = "রায়হান"
            ),
            Business(
                id = "biz_3",
                businessName = "ওয়ালটন রেফ্রিজারেটর কিস্তিতে বিক্রয়",
                description = "সহজ মাসিক কিস্তিতে ফ্রিজ বিক্রয়",
                businessType = Business.TYPE_INSTALLMENT,
                productName = "ওয়ালটন রেফ্রিজারেটর (213L)",
                costPrice = 28000.0, // পণ্যের ক্রয়মূল্য
                sellingPrice = 34000.0, // পণ্যের বিক্রয়মূল্য
                investment = 28000.0,
                profit = 6000.0, // মোট লাভ (৩৪,০০০ - ২৮,০০০)
                customerName = "মোঃ আনোয়ার হোসেন",
                customerPhone = "01723-456789",
                customerAddress = "হবিরবাড়ি বাজার, ময়মনসিংহ",
                downPayment = 4000.0, // ডাউন পেমেন্ট
                totalInstallments = 6, // মোট ৬ কিস্তি
                installmentFrequency = "মাসিক",
                payments = listOf(
                    InstallmentPayment("pay_1", 1, 5000.0, "১৫ মে ২০২৬", "মিরাজ (অ্যাডমিন)", "১ম কিস্তি পরিশোধ"),
                    InstallmentPayment("pay_2", 2, 5000.0, "১৫ জুন ২০২৬", "রায়হান (অ্যাডমিন)", "২য় কিস্তি পরিশোধ"),
                    InstallmentPayment("pay_3", 3, 5000.0, "১৫ জুলাই ২০২৬", "মিরাজ (অ্যাডমিন)", "৩য় কিস্তি পরিশোধ")
                ),
                date = "০১ মে ২০২৬",
                notes = "ডাউন পেমেন্ট ৪,০০০ টাকা। অবশিষ্ট ৩০,০০০ টাকা প্রতি মাসে ৫,০০০ টাকা করে ৬ কিস্তিতে পরিশোধযোগ্য।",
                createdAt = System.currentTimeMillis() - 86400000L * 90,
                createdBy = "মিরাজ"
            )
        )

        // Seed Organization Expenses
        localExpenses.value = listOf(
            Expense(
                id = "exp_1",
                title = "মাসিক সভা ও আপ্যায়ন",
                amount = 1200.0,
                category = "সভা ও আপ্যায়ন",
                description = "ক্লাবের ত্রৈমাসিক সাধারণ সভার চা-নাশতা ও আপ্যায়ন খরচ",
                date = "১০ ফেব্রুয়ারি ২০২৬",
                createdAt = System.currentTimeMillis() - 86400000L * 150,
                createdBy = "রায়হান"
            ),
            Expense(
                id = "exp_2",
                title = "অফিস রেজিস্টার ও খাতা ক্রয়",
                amount = 850.0,
                category = "অফিস ও স্টেশনারি",
                description = "হিসাবের বালাম বই, ভাউচার ও পেনড্রাইভ ক্রয়",
                date = "০৫ মার্চ ২০২৬",
                createdAt = System.currentTimeMillis() - 86400000L * 130,
                createdBy = "মিরাজ"
            ),
            Expense(
                id = "exp_3",
                title = "স্থানীয় এতিমখানায় ইফতার বিতরণ",
                amount = 4500.0,
                category = "সেবা ও সমাজকল্যাণ",
                description = "রমজান উপলক্ষে হবিরবাড়ি এতিমখানায় পুষ্টিকর খাবার সরবরাহ",
                date = "২৮ মার্চ ২০২৬",
                createdAt = System.currentTimeMillis() - 86400000L * 110,
                createdBy = "মিরাজ"
            ),
            Expense(
                id = "exp_4",
                title = "যাতায়াত ও দলিল সত্যায়ন খরচ",
                amount = 600.0,
                category = "যাতায়াত খরচ",
                description = "উপজেলা সমবায় অফিসে ক্লাবের নথিপত্র জমাদান সংক্রান্ত খরচ",
                date = "১২ জুন ২০২৬",
                createdAt = System.currentTimeMillis() - 86400000L * 45,
                createdBy = "রায়হান"
            )
        )
    }
}
