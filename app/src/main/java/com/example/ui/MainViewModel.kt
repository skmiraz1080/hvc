package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.ActivityItem
import com.example.data.model.Business
import com.example.data.model.Deposit
import com.example.data.model.Expense
import com.example.data.model.FinancialSummary
import com.example.data.model.InstallmentPayment
import com.example.data.model.User
import com.example.data.repository.ClubRepository
import com.example.util.BanglaUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {

    val repository = ClubRepository(application.applicationContext)

    val currentUser: StateFlow<User?> = repository.getCurrentUserFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val members: StateFlow<List<User>> = repository.getMembersFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val deposits: StateFlow<List<Deposit>> = repository.getDepositsFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val businesses: StateFlow<List<Business>> = repository.getBusinessesFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val expenses: StateFlow<List<Expense>> = repository.getExpensesFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _selectedMonth = MutableStateFlow(BanglaUtils.getCurrentMonth())
    val selectedMonth: StateFlow<String> = _selectedMonth.asStateFlow()

    private val _selectedYear = MutableStateFlow(BanglaUtils.getCurrentYear())
    val selectedYear: StateFlow<Int> = _selectedYear.asStateFlow()

    private val _uiMessage = MutableStateFlow<String?>(null)
    val uiMessage: StateFlow<String?> = _uiMessage.asStateFlow()

    private data class RawFinancialData(
        val mems: List<User>,
        val deps: List<Deposit>,
        val bizs: List<Business>,
        val exps: List<Expense>
    )

    private val rawDataFlow = combine(members, deposits, businesses, expenses) { mems, deps, bizs, exps ->
        RawFinancialData(mems, deps, bizs, exps)
    }

    val financialSummary: StateFlow<FinancialSummary> = combine(
        rawDataFlow,
        selectedMonth,
        selectedYear
    ) { raw, month, year ->
        repository.calculateSummary(raw.mems, raw.deps, raw.bizs, raw.exps, month, year)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), FinancialSummary())

    val recentActivities: StateFlow<List<ActivityItem>> = combine(
        deposits,
        businesses,
        expenses
    ) { deps, bizs, exps ->
        repository.getRecentActivities(deps, bizs, exps)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun setSelectedMonth(month: String) {
        _selectedMonth.value = month
    }

    fun setSelectedYear(year: Int) {
        _selectedYear.value = year
    }

    fun clearUiMessage() {
        _uiMessage.value = null
    }

    fun setUiMessage(message: String) {
        _uiMessage.value = message
    }

    // ----------------------------------------------------
    // AUTHENTICATION
    // ----------------------------------------------------

    fun login(email: String, pass: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            val result = repository.login(email, pass)
            result.fold(
                onSuccess = { onSuccess() },
                onFailure = { onError(it.localizedMessage ?: "লগইন ব্যর্থ হয়েছে") }
            )
        }
    }

    fun register(
        name: String,
        email: String,
        pass: String,
        phone: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            val result = repository.register(name, email, pass, phone)
            result.fold(
                onSuccess = { onSuccess() },
                onFailure = { onError(it.localizedMessage ?: "নিবন্ধন ব্যর্থ হয়েছে") }
            )
        }
    }

    fun sendPasswordReset(email: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            val result = repository.sendPasswordReset(email)
            result.fold(
                onSuccess = { onSuccess() },
                onFailure = { onError(it.localizedMessage ?: "রিসেট ইমেইল পাঠানো যায়নি") }
            )
        }
    }

    fun logout() {
        repository.logout()
    }

    fun switchUser(user: User) {
        repository.switchLocalUser(user)
    }

    // ----------------------------------------------------
    // MEMBER ACTIONS
    // ----------------------------------------------------

    fun addMember(
        name: String,
        email: String,
        phone: String,
        role: String = User.ROLE_MEMBER,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            if (currentUser.value?.isAdmin != true) {
                onError("শুধুমাত্র অ্যাডমিন সদস্য যোগ করতে পারবেন।")
                return@launch
            }
            val result = repository.addMember(name, email, phone, role)
            result.fold(
                onSuccess = {
                    setUiMessage("নতুন সদস্য সফলভাবে যুক্ত করা হয়েছে")
                    onSuccess()
                },
                onFailure = { onError(it.localizedMessage ?: "সদস্য যোগ করতে ব্যর্থ") }
            )
        }
    }

    fun updateMember(
        user: User,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            val callerIsAdmin = currentUser.value?.isAdmin == true
            // If caller is normal member, they can only update their own profile
            if (!callerIsAdmin && currentUser.value?.uid != user.uid) {
                onError("আপনি শুধুমাত্র নিজের প্রোফাইল সম্পাদনা করতে পারবেন।")
                return@launch
            }

            val result = repository.updateMember(user, callerIsAdmin)
            result.fold(
                onSuccess = {
                    setUiMessage("তথ্য সফলভাবে আপডেট হয়েছে")
                    onSuccess()
                },
                onFailure = { onError(it.localizedMessage ?: "তথ্য আপডেট করা যায়নি") }
            )
        }
    }

    fun removeMember(
        userId: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            if (currentUser.value?.isAdmin != true) {
                onError("শুধুমাত্র অ্যাডমিন সদস্য মুছে ফেলতে পারবেন।")
                return@launch
            }
            val result = repository.removeMember(userId)
            result.fold(
                onSuccess = {
                    setUiMessage("সদস্য অপসারণ করা হয়েছে")
                    onSuccess()
                },
                onFailure = { onError(it.localizedMessage ?: "সদস্য মোছা সম্ভব হয়নি") }
            )
        }
    }

    // ----------------------------------------------------
    // DEPOSIT ACTIONS
    // ----------------------------------------------------

    fun toggleDepositStatus(
        member: User,
        month: String,
        year: Int,
        amount: Double = 1000.0,
        onError: (String) -> Unit = {}
    ) {
        viewModelScope.launch {
            if (currentUser.value?.isAdmin != true) {
                onError("আর্থিক তথ্য শুধুমাত্র অ্যাডমিন পরিবর্তন করতে পারবেন।")
                return@launch
            }
            val adminName = currentUser.value?.name ?: "অ্যাডমিন"
            val result = repository.toggleDepositStatus(
                memberId = member.uid,
                memberName = member.name,
                month = month,
                year = year,
                amount = amount,
                recordedBy = "$adminName (অ্যাডমিন)"
            )
            result.fold(
                onSuccess = {
                    setUiMessage("জমার স্ট্যাটাস পরিবর্তন করা হয়েছে")
                },
                onFailure = { onError(it.localizedMessage ?: "ত্রুটি হয়েছে") }
            )
        }
    }

    fun saveOrUpdateDeposit(
        deposit: Deposit,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            if (currentUser.value?.isAdmin != true) {
                onError("আর্থিক তথ্য শুধুমাত্র অ্যাডমিন এডিট করতে পারবেন।")
                return@launch
            }
            val result = repository.saveOrUpdateDeposit(deposit)
            result.fold(
                onSuccess = {
                    setUiMessage("জমার হিসাব সংরক্ষণ করা হয়েছে")
                    onSuccess()
                },
                onFailure = { onError(it.localizedMessage ?: "সংরক্ষণ ব্যর্থ") }
            )
        }
    }

    fun deleteDeposit(
        depositId: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            if (currentUser.value?.isAdmin != true) {
                onError("শুধুমাত্র অ্যাডমিন হিসাব মুছতে পারবেন।")
                return@launch
            }
            val result = repository.deleteDeposit(depositId)
            result.fold(
                onSuccess = {
                    setUiMessage("জমার রেকর্ড মুছে ফেলা হয়েছে")
                    onSuccess()
                },
                onFailure = { onError(it.localizedMessage ?: "মোছা সম্ভব হয়নি") }
            )
        }
    }

    // ----------------------------------------------------
    // BUSINESS ACTIONS
    // ----------------------------------------------------

    fun saveBusiness(
        business: Business,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            if (currentUser.value?.isAdmin != true) {
                onError("শুধুমাত্র অ্যাডমিন ব্যবসায়িক তথ্য যোগ বা এডিট করতে পারবেন।")
                return@launch
            }
            val adminName = currentUser.value?.name ?: "অ্যাডমিন"
            val toSave = if (business.createdBy.isBlank()) business.copy(createdBy = adminName) else business
            val result = repository.saveBusiness(toSave)
            result.fold(
                onSuccess = {
                    setUiMessage("ব্যবসা ও লাভের তথ্য সংরক্ষিত হয়েছে")
                    onSuccess()
                },
                onFailure = { onError(it.localizedMessage ?: "ব্যবসায়িক তথ্য সংরক্ষণ ব্যর্থ") }
            )
        }
    }

    fun recordInstallmentPayment(
        businessId: String,
        payment: InstallmentPayment,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            if (currentUser.value?.isAdmin != true) {
                onError("শুধুমাত্র অ্যাডমিন কিস্তি আদায়ের হিসাব এন্ট্রি করতে পারবেন।")
                return@launch
            }
            val result = repository.recordInstallmentPayment(businessId, payment)
            result.fold(
                onSuccess = {
                    setUiMessage("কিস্তির টাকা সফলভাবে আদায় ও সংরক্ষণ করা হয়েছে")
                    onSuccess()
                },
                onFailure = { onError(it.localizedMessage ?: "কিস্তি সংরক্ষণ ব্যর্থ") }
            )
        }
    }

    fun deleteBusiness(
        businessId: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            if (currentUser.value?.isAdmin != true) {
                onError("শুধুমাত্র অ্যাডমিন ব্যবসা মুছে ফেলতে পারবেন।")
                return@launch
            }
            val result = repository.deleteBusiness(businessId)
            result.fold(
                onSuccess = {
                    setUiMessage("ব্যবসা মুছে ফেলা হয়েছে")
                    onSuccess()
                },
                onFailure = { onError(it.localizedMessage ?: "মোছা সম্ভব হয়নি") }
            )
        }
    }

    // ----------------------------------------------------
    // EXPENSE ACTIONS
    // ----------------------------------------------------

    fun saveExpense(
        expense: Expense,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            if (currentUser.value?.isAdmin != true) {
                onError("শুধুমাত্র অ্যাডমিন খরচ রেকর্ড করতে পারবেন।")
                return@launch
            }
            val adminName = currentUser.value?.name ?: "অ্যাডমিন"
            val toSave = if (expense.createdBy.isBlank()) expense.copy(createdBy = adminName) else expense
            val result = repository.saveExpense(toSave)
            result.fold(
                onSuccess = {
                    setUiMessage("খরচের হিসাব সংরক্ষণ করা হয়েছে")
                    onSuccess()
                },
                onFailure = { onError(it.localizedMessage ?: "খরচ সংরক্ষণ ব্যর্থ") }
            )
        }
    }

    fun deleteExpense(
        expenseId: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            if (currentUser.value?.isAdmin != true) {
                onError("শুধুমাত্র অ্যাডমিন খরচের হিসাব মুছতে পারবেন।")
                return@launch
            }
            val result = repository.deleteExpense(expenseId)
            result.fold(
                onSuccess = {
                    setUiMessage("খরচের রেকর্ড মুছে ফেলা হয়েছে")
                    onSuccess()
                },
                onFailure = { onError(it.localizedMessage ?: "মোছা সম্ভব হয়নি") }
            )
        }
    }

    // ----------------------------------------------------
    // MEMBER STATS HELPER
    // ----------------------------------------------------

    data class MemberStats(
        val totalDeposited: Double,
        val paidMonthsCount: Int,
        val unpaidMonthsCount: Int,
        val history: List<Deposit>
    )

    fun getMemberStats(memberId: String): MemberStats {
        val memberDeposits = deposits.value.filter { it.memberId == memberId }
        val paidDeposits = memberDeposits.filter { it.isPaid }
        val totalDeposited = paidDeposits.sumOf { it.amount }
        val paidMonths = paidDeposits.size
        val unpaidMonths = memberDeposits.count { !it.isPaid }

        return MemberStats(
            totalDeposited = totalDeposited,
            paidMonthsCount = paidMonths,
            unpaidMonthsCount = unpaidMonths,
            history = memberDeposits.sortedByDescending { it.updatedAt }
        )
    }
}
