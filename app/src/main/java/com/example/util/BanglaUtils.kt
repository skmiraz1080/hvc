package com.example.util

import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object BanglaUtils {

    val BENGALI_MONTHS = listOf(
        "জানুয়ারি",
        "ফেব্রুয়ারি",
        "মার্চ",
        "এপ্রিল",
        "মে",
        "জুন",
        "জুলাই",
        "আগস্ট",
        "সেপ্টেম্বর",
        "অক্টোবর",
        "নভেম্বর",
        "ডিসেম্বর"
    )

    val EXPENSE_CATEGORIES = listOf(
        "অফিস ও স্টেশনারি",
        "সেবা ও সমাজকল্যাণ",
        "সভা ও আপ্যায়ন",
        "যাতায়াত খরচ",
        "ব্যাংক ও নথিপত্র",
        "জরুরি সহায়তা",
        "অন্যান্য খরচ"
    )

    fun toBanglaDigits(numberStr: String): String {
        val banglaDigits = charArrayOf('০', '১', '২', '৩', '৪', '৫', '৬', '৭', '৮', '৯')
        val sb = StringBuilder()
        for (ch in numberStr) {
            if (ch in '0'..'9') {
                sb.append(banglaDigits[ch - '0'])
            } else {
                sb.append(ch)
            }
        }
        return sb.toString()
    }

    fun formatTaka(amount: Double, useBanglaDigits: Boolean = true): String {
        val formatter = DecimalFormat("#,##,###")
        val formatted = formatter.format(amount)
        return if (useBanglaDigits) {
            "৳ " + toBanglaDigits(formatted)
        } else {
            "৳ $formatted"
        }
    }

    fun formatNumber(number: Int, useBanglaDigits: Boolean = true): String {
        return if (useBanglaDigits) toBanglaDigits(number.toString()) else number.toString()
    }

    fun formatDate(timestamp: Long): String {
        val sdf = SimpleDateFormat("dd MMM, yyyy", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }

    fun getCurrentMonth(): String {
        val monthIndex = java.util.Calendar.getInstance().get(java.util.Calendar.MONTH)
        return BENGALI_MONTHS.getOrElse(monthIndex) { "জানুয়ারি" }
    }

    fun getCurrentYear(): Int {
        return java.util.Calendar.getInstance().get(java.util.Calendar.YEAR)
    }
}
