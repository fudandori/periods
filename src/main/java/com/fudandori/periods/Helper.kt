package com.fudandori.periods

import android.app.Activity
import android.content.Context
import android.content.res.ColorStateList
import android.view.View
import androidx.annotation.ColorRes
import androidx.core.content.ContextCompat
import com.google.gson.Gson
import java.io.FileNotFoundException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

/**
 * Sets the color of the input element
 */
fun setViewColor(view: View?, @ColorRes id: Int, a: Activity) {
    view?.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(a, id))
}

/**
 * Extracts the day from a string date and converts it to Int
 * @param input YYYY-MM-dd formatted string date
 */
fun intDate(input: String): Int {
    return input.takeLast(2).toInt()
}

/**
 * Retrieves the column of the first day of the input date
 * @param input formatted string date based on fmt parameters
 * @param fmt formatter for the input string date
 */
fun getFirstDay(input: String, fmt: SimpleDateFormat): Int {
    val c = Calendar.getInstance()
    try {
        val d = fmt.parse(input)
        if (d != null) c.time = d
    } catch (e: Exception) {
        c.time = Date()
        c.set(Calendar.DAY_OF_MONTH, 1)
    }

    val weekDay = c.get(Calendar.DAY_OF_WEEK)
    return if (weekDay == 1) 6 else weekDay - 2
}

/**
 * Extracts the month part of a YYYY-MM-dd formatted string date
 */
fun extractMonth(input: String): String {
    return input.substring(5, 7)
}

/**
 * Extracts the year part of a YYYY-MM-dd formatted string date
 */
fun extractYear(input: String): String {
    return input.substring(0, 4)
}

/**
 * Pads the input String with a leading 0
 */
fun pad(input: String): String {
    return input.padStart(2, '0')
}

fun load(c: Context, act: Activity): Config {
    val data = try {
        c.openFileInput(fileName).bufferedReader().use { it.readLine() }
    } catch (e: FileNotFoundException) {
        ""
    }

    val result: Config

    if (data.isNotBlank()) {
        result = Gson().fromJson(data, Config::class.java)
    } else {
        result = Config(ArrayList(), 0)
        alertDialog(
            act,
            R.string.warning,
            R.string.warning_body_2
        )
    }

    return result
}
