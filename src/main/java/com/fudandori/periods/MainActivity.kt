package com.fudandori.periods

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import com.google.gson.Gson
import java.text.SimpleDateFormat
import java.util.*

const val fileName = "savedata"

class MainActivity : AppCompatActivity() {
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.FRANCE)
    private lateinit var selectedMonth: String
    private lateinit var bounceAnim: Animation
    private lateinit var elements: Array<TextView>
    private lateinit var config: Config

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        config = load(applicationContext, this)
        init()
        render()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.items, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.add_days -> {
            update(config.lastDay())
            render()
            true
        }

        R.id.delete_data -> {
            config.reset()
            render()
            save()
            true
        }

        R.id.set_period -> {
            periodDialog(this, config.span.toString(), ::savePeriodSpan)
            true
        }

        else -> {
            super.onOptionsItemSelected(item)
        }
    }

    /**
     * Saves config data into the save file.
     */
    private fun save() {
        val json = Gson().toJson(config)

        openFileOutput(fileName, Context.MODE_PRIVATE).use {
            it.write(json.toByteArray())
        }
    }

    /**
     * Modifies the span property
     */
    private fun savePeriodSpan(i: Int) {
        config.span = i
        save()
    }

    /**
     * Callback for the click event. Shows a pop up confirmation to add a new period date to the selected day
     */
    fun click(it: View) {
        val value = extractDate(it)
        if (value.isNotBlank()) {
            if (config.span != 0) {
                addDialog(value, ::recalculate, this)
            } else {
                alertDialog(this, "Aviso", "Debes configurar cada cuántos días calcular el periodo")
            }
        }
    }

    /**
     * Retrieves the selected date with YYYY-MM-dd format
     */
    private fun extractDate(view: View): String {
        val ele = elements.find { v -> v == view }
        var date = ""

        if (ele?.text != null && ele.text.isNotBlank()) {
            val day = pad(ele.text.toString())
            val month = extractMonth(selectedMonth)
            val year = extractYear(selectedMonth)
            date = "$year-$month-$day"
        }

        return date
    }

    /**
     * Deletes all subsequent highlighted dates from the input date and generates new ones
     * @param date YYYY-MM-dd formatted date
     */
    private fun recalculate(date: String) {
        config.dates.add(date)
        clearSubsequent(date)
        update(date)
        render()
    }

    /**
     * Removes the highlighted day and if specified, the subsequent days
     * @param date the day in the current month to be removed
     * @param all wheter or not remove the subsequent highlights
     */
    private fun clearDate(date: String, all: Boolean) {
        config.dates.remove(date)
        if (all) clearSubsequent(date)
        render()
        save()
    }

    /**
     * Removes every period highlight after the input date
     */
    private fun clearSubsequent(date: String) {
        config.dates.removeAll { s -> s > date }
    }

    /**
     * Draws the calendar
     */
    private fun render() {
        setMonthName()

        val start = getFirstDay(selectedMonth, dateFormat)

        val c = Calendar.getInstance()
        c.time = dateFormat.parse(selectedMonth) ?: Date()
        val end = c.getActualMaximum(Calendar.DAY_OF_MONTH)

        renderGaps(start, end)
        renderDays(start, end)
    }

    /**
     * Renders the gaps before the first day and after the last day of the month invisible
     */
    private fun renderGaps(start: Int, end: Int) {
        for (x in 0 until start) empty(elements[x])
        for (x in end + start until elements.size) empty(elements[x])
    }

    /**
     * Gives format to the existing days in a month
     */
    private fun renderDays(start: Int, end: Int) {
        val date = dateFormat.format(Calendar.getInstance().time)
        val today = intDate(date)
        val days = findDays()

        for (x in 1..end) {
            val highlight = days.contains(x)
            val elem = elements[x + start - 1]

            elem.text = x.toString()
            stylize(elem, highlight)

            if (!highlight && isSameMonth(date) && today == x) {
                setViewColor(elem, R.color.green, this)
            }
        }
    }

    /**
     * Sets the color and the animation of a given layout day
     */
    private fun stylize(view: View, highlight: Boolean) {
        val color: Int
        if (highlight) {
            color = R.color.red
            view.startAnimation(bounceAnim)

            val date = extractDate(view)
            view.setOnClickListener {
                removeDialog(this, date, ::clearDate)
            }

        } else {
            color = R.color.colorAccent
            view.clearAnimation()
            view.setOnClickListener { click(view) }
        }

        setViewColor(view, color, this)
    }

    /**
     * Retrieves the period dates for the current selected month
     * @return Array with 2 days maximum
     */
    private fun findDays(): Array<Int> {
        val days = config.dates.filter { m -> isSameMonth(m) }
        return Array(days.size) { i -> intDate(days[i]) }
    }

    /**
     * Checks if the input date is on the same month as the current selected month
     */
    private fun isSameMonth(input: String): Boolean {
        return input.substring(0, 7) == selectedMonth.substring(0, 7)
    }

    /**
     * Retrieves the name of the selected month and prints it to the TextView storing it
     */
    private fun setMonthName() {
        val d = dateFormat.parse(selectedMonth)
        if (d != null) {
            val name = SimpleDateFormat("MMMM", Locale("ES"))
                .format(d)
                .toUpperCase(Locale.getDefault())
            findViewById<TextView>(R.id.month).text = name
        }
    }

    /**
     * Renders the day invisible on the layout
     */
    private fun empty(t: TextView?) {
        t?.text = ""
        setViewColor(t, R.color.transparent, this)
        t?.clearAnimation()
    }

    /**
     * Switches the current month
     * @param amount adds or subtracts months. A negative value switches to previous months
     */
    private fun switchMonth(amount: Int) {
        val d = dateFormat.parse(selectedMonth)
        if (d != null) {
            val c = Calendar.getInstance()
            c.time = d
            c.add(Calendar.MONTH, amount)
            selectedMonth = dateFormat.format(c.time)

            render()
        }
    }

    fun previous(it: View) {
        switchMonth(-1)
    }

    fun next(it: View) {
        switchMonth(1)
    }

    /**
     * Generates 20 period dates after the input date, stores them into the array and serializes them
     * @param date YYYY-MM-dd formatted string date
     */
    private fun update(date: String) {
        val tCal = Calendar.getInstance()
        tCal.set(Calendar.DAY_OF_MONTH, date.substring(8).toInt())
        tCal.set(Calendar.MONTH, date.substring(5, 7).toInt() - 1)
        tCal.set(Calendar.YEAR, date.substring(0, 4).toInt())

        for (i in 1..20) {
            tCal.add(Calendar.DATE, config.span)
            config.dates.add(dateFormat.format(tCal.time))
        }

        save()
    }

    /**
     * Initializes the layout controls and the initial state of certain variables
     */
    @SuppressLint("ClickableViewAccessibility")
    private fun init() {
        setContentView(R.layout.activity_main)
        bounceAnim = AnimationUtils.loadAnimation(applicationContext, R.anim.bounce)

        val c = Calendar.getInstance()
        c.set(Calendar.DAY_OF_MONTH, 1)
        selectedMonth = dateFormat.format(c.time)

        elements = arrayOf(
            findViewById(R.id.one),
            findViewById(R.id.two),
            findViewById(R.id.three),
            findViewById(R.id.four),
            findViewById(R.id.five),
            findViewById(R.id.six),
            findViewById(R.id.seven),
            findViewById(R.id.tone),
            findViewById(R.id.ttwo),
            findViewById(R.id.tthree),
            findViewById(R.id.tfour),
            findViewById(R.id.tfive),
            findViewById(R.id.tsix),
            findViewById(R.id.tseven),
            findViewById(R.id.thone),
            findViewById(R.id.thtwo),
            findViewById(R.id.ththree),
            findViewById(R.id.thfour),
            findViewById(R.id.thfive),
            findViewById(R.id.thsix),
            findViewById(R.id.thseven),
            findViewById(R.id.fone),
            findViewById(R.id.ftwo),
            findViewById(R.id.fthree),
            findViewById(R.id.ffour),
            findViewById(R.id.ffive),
            findViewById(R.id.fsix),
            findViewById(R.id.fseven),
            findViewById(R.id.sone),
            findViewById(R.id.stwo),
            findViewById(R.id.sthree),
            findViewById(R.id.sfour),
            findViewById(R.id.sfive),
            findViewById(R.id.ssix),
            findViewById(R.id.sseven),
            findViewById(R.id.oone),
            findViewById(R.id.otwo),
            findViewById(R.id.othree),
            findViewById(R.id.ofour),
            findViewById(R.id.ofive),
            findViewById(R.id.osix),
            findViewById(R.id.oseven)
        )

        val layout: ConstraintLayout = findViewById(R.id.layout)
        layout.setOnTouchListener(object : OnSwipeTouchListener(applicationContext) {
            override fun onSwipeLeft() {
                switchMonth(1)
            }

            override fun onSwipeRight() {
                switchMonth(-1)
            }
        })
    }
}
