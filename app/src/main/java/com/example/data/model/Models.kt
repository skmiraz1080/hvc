package com.example.data.model

data class User(
    val uid: String = "",
    val name: String = "",
    val email: String = "",
    val phone: String = "",
    val profileImage: String = "",
    val role: String = ROLE_MEMBER, // "admin" or "member"
    val joinedDate: String = "",
    val createdAt: Long = System.currentTimeMillis()
) {
    val isAdmin: Boolean get() = role == ROLE_ADMIN

    companion object {
        const val ROLE_ADMIN = "admin"
        const val ROLE_MEMBER = "member"
    }
}

data class Deposit(
    val id: String = "",
    val memberId: String = "",
    val memberName: String = "",
    val month: String = "", // e.g. "জানুয়ারি" or "01"
    val year: Int = 2026,
    val amount: Double = 1000.0,
    val status: String = STATUS_PAID, // "paid" or "unpaid"
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val recordedBy: String = ""
) {
    val isPaid: Boolean get() = status.equals(STATUS_PAID, ignoreCase = true)

    companion object {
        const val STATUS_PAID = "paid"
        const val STATUS_UNPAID = "unpaid"
    }
}

data class InstallmentPayment(
    val id: String = "",
    val installmentNumber: Int = 1,
    val amount: Double = 0.0,
    val date: String = "",
    val collectedBy: String = "",
    val notes: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

data class Business(
    val id: String = "",
    val businessName: String = "",
    val description: String = "",
    val businessType: String = TYPE_GENERAL, // "general" or "installment"
    val investment: Double = 0.0, // For general: মোট বিনিয়োগ
    val profit: Double = 0.0, // মোট লাভ
    // কিস্তিতে পণ্য বিক্রয়ের তথ্য (Installment Sales Fields)
    val productName: String = "", // পণ্যের নাম
    val costPrice: Double = 0.0, // পণ্যের ক্রয়মূল্য
    val sellingPrice: Double = 0.0, // পণ্যের বিক্রয়মূল্য
    val customerName: String = "", // ক্রেতার নাম
    val customerPhone: String = "", // ক্রেতার ফোন
    val customerAddress: String = "", // ক্রেতার ঠিকানা
    val downPayment: Double = 0.0, // ডাউন পেমেন্ট / অগ্রিম
    val totalInstallments: Int = 1, // মোট কিস্তির সংখ্যা
    val installmentFrequency: String = "মাসিক", // "মাসিক" বা "সাপ্তাহিক"
    val payments: List<InstallmentPayment> = emptyList(), // আদায়কৃত কিস্তিসমূহ
    val date: String = "",
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val createdBy: String = ""
) {
    val isInstallment: Boolean get() = businessType == TYPE_INSTALLMENT
    val totalCollected: Double get() = if (isInstallment) downPayment + payments.sumOf { it.amount } else profit
    val remainingDue: Double get() = if (isInstallment) (sellingPrice - totalCollected).coerceAtLeast(0.0) else 0.0
    val installmentAmount: Double get() = if (isInstallment && totalInstallments > 0) {
        (sellingPrice - downPayment).coerceAtLeast(0.0) / totalInstallments
    } else 0.0
    val isCompleted: Boolean get() = if (isInstallment) remainingDue <= 0 else true

    companion object {
        const val TYPE_GENERAL = "general"
        const val TYPE_INSTALLMENT = "installment"
    }
}

data class Expense(
    val id: String = "",
    val title: String = "",
    val amount: Double = 0.0,
    val category: String = "অন্যান্য",
    val description: String = "",
    val date: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val createdBy: String = ""
)

data class FinancialSummary(
    val totalMembers: Int = 0,
    val totalDeposited: Double = 0.0,
    val totalBusinessProfit: Double = 0.0,
    val totalExpenses: Double = 0.0,
    val currentTotalFund: Double = 0.0,
    val currentMonthTarget: Double = 10000.0,
    val currentMonthCollected: Double = 0.0
)

sealed class ActivityItem {
    abstract val timestamp: Long
    abstract val title: String
    abstract val subtitle: String
    abstract val amount: Double
    abstract val type: String // "deposit", "profit", "expense"

    data class DepositActivity(
        override val timestamp: Long,
        override val title: String,
        override val subtitle: String,
        override val amount: Double
    ) : ActivityItem() {
        override val type: String = "deposit"
    }

    data class ProfitActivity(
        override val timestamp: Long,
        override val title: String,
        override val subtitle: String,
        override val amount: Double
    ) : ActivityItem() {
        override val type: String = "profit"
    }

    data class ExpenseActivity(
        override val timestamp: Long,
        override val title: String,
        override val subtitle: String,
        override val amount: Double
    ) : ActivityItem() {
        override val type: String = "expense"
    }
}
