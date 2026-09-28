package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.Business
import com.example.data.model.Deposit
import com.example.data.model.Expense
import com.example.data.model.User
import com.example.data.repository.ClubRepository
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("হবিরবাড়ি ভলেন্টিয়ার্স ক্লাব", appName)
    }

    @Test
    fun `financial formula calculates correctly`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val repo = ClubRepository(context)

        val members = listOf(
            User("u1", "Member 1", "m1@example.com"),
            User("u2", "Member 2", "m2@example.com")
        )

        val deposits = listOf(
            Deposit("d1", "u1", "Member 1", "সেপ্টেম্বর", 2026, 1000.0, Deposit.STATUS_PAID),
            Deposit("d2", "u2", "Member 2", "সেপ্টেম্বর", 2026, 1000.0, Deposit.STATUS_PAID),
            Deposit("d3", "u1", "Member 1", "আগস্ট", 2026, 1000.0, Deposit.STATUS_UNPAID) // unpaid should not be counted
        )

        val businesses = listOf(
            Business("b1", "Business 1", profit = 5000.0)
        )

        val expenses = listOf(
            Expense("e1", "Expense 1", amount = 1500.0)
        )

        val summary = repo.calculateSummary(
            members = members,
            deposits = deposits,
            businesses = businesses,
            expenses = expenses,
            currentMonth = "সেপ্টেম্বর",
            currentYear = 2026
        )

        // Total Deposited = 2000.0
        assertEquals(2000.0, summary.totalDeposited, 0.01)
        // Total Profit = 5000.0
        assertEquals(5000.0, summary.totalBusinessProfit, 0.01)
        // Total Expenses = 1500.0
        assertEquals(1500.0, summary.totalExpenses, 0.01)
        // Current Total Fund = 2000 + 5000 - 1500 = 5500.0
        assertEquals(5500.0, summary.currentTotalFund, 0.01)
        // This month collection = 2000.0
        assertEquals(2000.0, summary.currentMonthCollected, 0.01)
    }
}
