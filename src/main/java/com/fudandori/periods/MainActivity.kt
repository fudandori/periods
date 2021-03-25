package com.fudandori.periods

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.ColorStateList
import android.os.Bundle
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.FileNotFoundException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

const val fileName = "dates45"
const val red = 0xFFFF0000.toInt()
const val blue = 0xFF3B79B7.toInt()
const val green = 0xFF00AB00.toInt()

class MainActivity : AppCompatActivity() {
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.FRANCE)
    private val currMonth = Calendar.getInstance()
    private lateinit var bounceAnim: Animation
    private var elements: Array<TextView?> = arrayOfNulls(42)
    private var months = ArrayList<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        load()
        init()
        render()
    }

    private fun load() {
        val data = try {
            openFileInput(fileName).bufferedReader().use { it.readLine() }
        } catch (e: FileNotFoundException) {
            ""
        }

        if (data.isNotBlank()) {
            months = Gson().fromJson(data, object : TypeToken<ArrayList<String>>() {}.type)
        } else {
            val builder = AlertDialog.Builder(this)
            builder.setTitle("Aviso")
            builder.setMessage("Todavía no se ha seleccionado ningún día. Debes seleccionar el primer día pulsando sobre él para empezar los cálculos")

            builder.setPositiveButton(android.R.string.yes) { _, _ ->
                //Close
            }


            builder.show()
        }
    }


    private fun save(input: String) {
        openFileOutput(fileName, Context.MODE_PRIVATE).use {
            it.write(input.toByteArray())
        }
    }

    fun click(it: View) {
        val day = elements.find { v -> v?.equals(it) ?: false }
        val value = day?.text?.padStart(2, '0').toString()
        if ("00" != value) {
            val builder = AlertDialog.Builder(this)
            builder.setTitle("Añadir día")
            builder.setMessage("¿Quieres añadir el día " + day?.text.toString() + "?")

            builder.setPositiveButton(android.R.string.yes) { _, _ ->
                recalculate(value)
            }

            builder.setNegativeButton(android.R.string.no) { _, _ -> //Do nothing
            }

            builder.show()
        }
    }

    private fun recalculate(day: String) {
        val date = clearNextMonths(day)
        updateNextMonths(date, day)
        render()
    }

    private fun updateNextMonths(date: String, day: String) {
        val tCal = Calendar.getInstance()
        tCal.set(Calendar.DAY_OF_MONTH, day.toInt())
        tCal.set(Calendar.MONTH, date.substring(5, 7).toInt() - 1)
        tCal.set(Calendar.YEAR, date.substring(0, 4).toInt())

        update(tCal)
    }

    private fun clearNextMonths(day: String): String {
        val date = monthString() + day
        months.add(date)
        months.sort()
        months.removeAll { s -> s > date }

        return date
    }

    private fun monthString(): String {
        return currMonth.get(Calendar.YEAR).toString() + "-" +
                (currMonth.get(Calendar.MONTH) + 1).toString().padStart(2, '0') + "-"
    }

    private fun render() {
        setMonthName()

        val start = getFirstDay()
        val end = currMonth.getActualMaximum(Calendar.DAY_OF_MONTH)

        renderGaps(start, end)
        renderDays(start, end)
    }

    private fun getFirstDay(): Int {
        val weekDay = currMonth.get(Calendar.DAY_OF_WEEK)
        return if (weekDay == 1) 6 else weekDay - 2
    }

    private fun renderGaps(start: Int, end: Int) {
        for (x in 0 until start) empty(elements[x])
        for (x in end + start until elements.size) empty(elements[x])
    }

    private fun renderDays(start: Int, end: Int) {
        val c = Calendar.getInstance()

        val days = findDays()

        for (x in 1..end) {
            val highlight = isSelected(x, days)
            val elem = elements[x + start - 1]

            elem?.text = x.toString()
            stylize(elem, highlight)

            if (!highlight && isSameMonth(c) && c.get(Calendar.DAY_OF_MONTH) == x) {
                setViewColor(elem, green)
            }
        }
    }

    private fun isSelected(day: Int, days: Array<Int>): Boolean {
        var n = 0
        var found = false
        while (n < days.size && !found) {
            found = days[n] == day
            n++
        }
        return found
    }

    private fun stylize(view: View?, highlight: Boolean) {
        val color: Int
        if (highlight) {
            color = red
            view?.startAnimation(bounceAnim)
        } else {
            color = blue
            view?.clearAnimation()
        }

        setViewColor(view, color)
    }

    private fun findDays(): Array<Int> {
        val days = months.filter { m ->
            val c = Calendar.getInstance()
            c.time = dateFormat.parse(m) ?: Date()
            isSameMonth(c)
        }

        return Array(days.size) { i -> days[i].takeLast(2).toInt() }
    }

    private fun isSameMonth(input: Calendar): Boolean {
        return input.get(Calendar.MONTH) == currMonth.get(Calendar.MONTH)
                && input.get(Calendar.YEAR) == currMonth.get(Calendar.YEAR)
    }

    private fun setMonthName() {
        val name = SimpleDateFormat("MMMM", Locale("ES"))
            .format(currMonth.time)
            .toUpperCase(Locale.getDefault())
        findViewById<TextView>(R.id.month).text = name
    }

    private fun empty(t: TextView?) {
        t?.text = ""
        setViewColor(t, 0xFFFFFF)
        t?.clearAnimation()
    }

    fun nextMonth() {
        currMonth.add(Calendar.MONTH, 1)
        render()
    }

    private fun prevMonth(): Boolean {
        currMonth.add(Calendar.MONTH, -1)
        render()
        return true
    }

    private fun setViewColor(view: View?, color: Int) {
        view?.backgroundTintList = ColorStateList.valueOf(color)
    }

    private fun update(cal: Calendar) {
        for (i in 1..20) {
            cal.add(Calendar.DATE, 27)
            months.add(dateFormat.format(cal.time))
        }

        val json = Gson().toJson(months)
        save(json)
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun init() {
        setContentView(R.layout.activity_main)
        bounceAnim = AnimationUtils.loadAnimation(applicationContext, R.anim.bounce)
        currMonth.set(Calendar.DAY_OF_MONTH, 1)
        elements[0] = findViewById(R.id.one)
        elements[1] = findViewById(R.id.two)
        elements[2] = findViewById(R.id.three)
        elements[3] = findViewById(R.id.four)
        elements[4] = findViewById(R.id.five)
        elements[5] = findViewById(R.id.six)
        elements[6] = findViewById(R.id.seven)

        elements[7] = findViewById(R.id.tone)
        elements[8] = findViewById(R.id.ttwo)
        elements[9] = findViewById(R.id.tthree)
        elements[10] = findViewById(R.id.tfour)
        elements[11] = findViewById(R.id.tfive)
        elements[12] = findViewById(R.id.tsix)
        elements[13] = findViewById(R.id.tseven)

        elements[14] = findViewById(R.id.thone)
        elements[15] = findViewById(R.id.thtwo)
        elements[16] = findViewById(R.id.ththree)
        elements[17] = findViewById(R.id.thfour)
        elements[18] = findViewById(R.id.thfive)
        elements[19] = findViewById(R.id.thsix)
        elements[20] = findViewById(R.id.thseven)

        elements[21] = findViewById(R.id.fone)
        elements[22] = findViewById(R.id.ftwo)
        elements[23] = findViewById(R.id.fthree)
        elements[24] = findViewById(R.id.ffour)
        elements[25] = findViewById(R.id.ffive)
        elements[26] = findViewById(R.id.fsix)
        elements[27] = findViewById(R.id.fseven)

        elements[28] = findViewById(R.id.sone)
        elements[29] = findViewById(R.id.stwo)
        elements[30] = findViewById(R.id.sthree)
        elements[31] = findViewById(R.id.sfour)
        elements[32] = findViewById(R.id.sfive)
        elements[33] = findViewById(R.id.ssix)
        elements[34] = findViewById(R.id.sseven)

        elements[35] = findViewById(R.id.oone)
        elements[36] = findViewById(R.id.otwo)
        elements[37] = findViewById(R.id.othree)
        elements[38] = findViewById(R.id.ofour)
        elements[39] = findViewById(R.id.ofive)
        elements[40] = findViewById(R.id.osix)
        elements[41] = findViewById(R.id.oseven)

        val layout: ConstraintLayout = findViewById(R.id.layout)
        layout.setOnTouchListener(object : OnSwipeTouchListener(applicationContext) {
            override fun onSwipeLeft() {
                nextMonth()
            }

            override fun onSwipeRight() {
                prevMonth()
            }
        })
    }
}
