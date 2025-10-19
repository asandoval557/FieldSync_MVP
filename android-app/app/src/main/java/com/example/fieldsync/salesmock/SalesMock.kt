package com.example.fieldsync.salesmock

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.random.Random

/**
 * Simple point for a day's sales.
 * We keep the raw Date so the UI layer can format labels as needed.
 */
data class SalesPoint(
    val date: Date,
    val units: Int
)

/**
 * Deterministic 30-day series per storeId (stable across loads).
 * Looks “real” with weekday/weekend variation + slight end-of-month lift + small noise.
 */
fun generateRolling30Days(
    storeId: Long,
    today: Date = Date()
): List<SalesPoint> {
    val cal = Calendar.getInstance().apply {
        time = today
        add(Calendar.DAY_OF_YEAR, -29) // start 29 days ago -> total 30 days including today
        // Zero out time-of-day for cleaner labels / grouping if you ever need it
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }

    val rng = Random(storeId.hashCode().toLong()) // deterministic seed per store
    val points = ArrayList<SalesPoint>(30)

    for (i in 0 until 30) {
        val d = cal.time
        val dow = cal.get(Calendar.DAY_OF_WEEK) // 1..7 (Sun..Sat)

        val base = 40 + rng.nextInt(20) // 40..59
        val weekendBoost = when (dow) {
            Calendar.FRIDAY -> 10
            Calendar.SATURDAY -> 18
            Calendar.SUNDAY -> 12
            else -> 0
        }
        val trend = ((i - 15).coerceAtLeast(0)) / 3 // slight lift later in the month
        val noise = rng.nextInt(-5, 6) // -5..+5

        val units = (base + weekendBoost + trend + noise).coerceAtLeast(0)
        points.add(SalesPoint(d, units))

        cal.add(Calendar.DAY_OF_YEAR, 1)
    }

    return points
}

/**
 * Optional helper: turn Dates into short labels like "09/28".
 * Use it from UI/ViewModel if you want prebuilt strings.
 */
fun SalesPoint.label(pattern: String = "MM/dd", locale: Locale = Locale.getDefault()): String {
    val sdf = SimpleDateFormat(pattern, locale)
    return sdf.format(date)
}
