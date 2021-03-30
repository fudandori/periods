package com.fudandori.periods

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.ColorStateList
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.EditText
import android.widget.TextView
import androidx.annotation.ColorRes
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import com.google.gson.Gson
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

const val fileName = "savedata"

class MainActivity : AppCompatActivity() {
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.FRANCE)
    private val currMonth = Calendar.getInstance()
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
        val inflater = getMenuInflater();
        inflater.inflate(R.menu.items, menu);
        return true;
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.add_days -> {

            val d = try {
                dateFormat.parse(config.lastDay())
            } catch (e: ParseException) {
                null
            }

            if (d != null) {
                val tCal = Calendar.getInstance()
                tCal.time = d
                update(tCal)
            }
            true
        }

        R.id.delete_data -> {
            config.reset()
            render()
            save()
            true
        }

        R.id.set_period -> {
            val dialogBuilder = AlertDialog.Builder(this)
            val inflater = layoutInflater
            val dialogView = inflater.inflate(R.layout.period_span, null)
            val editText = dialogView.findViewById<EditText>(R.id.period_span)
            editText.setText(config.length.toString())
            dialogBuilder.setView(dialogView)
                .setPositiveButton("OK") { _, _ ->
                    config.length = editText.text.toString().toInt()
                    render()
                    save()
                }
                .setNegativeButton("Cancel") { dialog, _ -> dialog.dismiss() }

            dialogBuilder.create().show()
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
     * Callback for the click event. Shows a pop up confirmation to add a new period date to the selected day
     */
    fun click(it: View) {
        val value = getDayValue(it)
        if (value.isNotBlank()) {
            if (config.length != 0) {
                addDialog(value, ::recalculate, this)
            } else {
                alertDialog(this, "Aviso", "Debes configurar cada cuántos días calcular el periodo")
            }
        }
    }

    /**
     * Retrieves the text from a TextView storing a day with a leading 0
     */
    private fun getDayValue(view: View): String {
        val day = elements.find { v -> v == view }
        return if(day?.text != null) day.text.toString() else ""
    }

    /**
     * Deletes all the stored period dates after the input day and generates new ones based on the selected day
     * @param day Value of the day to use for recalculating. It must be 2 characters long
     */
    private fun recalculate(day: String) {
        val date = clearNextMonths(day)
        updateNextMonths(date, day)
        render()
    }

    /**
     * Generates the prediction for the upcoming 20 period dates
     */
    private fun updateNextMonths(date: String, day: String) {
        val tCal = Calendar.getInstance()
        tCal.set(Calendar.DAY_OF_MONTH, day.toInt())
        tCal.set(Calendar.MONTH, date.substring(5, 7).toInt() - 1)
        tCal.set(Calendar.YEAR, date.substring(0, 4).toInt())

        update(tCal)
    }

    /**
     * Removes every period date after the input date
     */
    private fun clearNextMonths(day: String, clearDay: Boolean = false): String {
        val date = dayFormat(day)
        config.dates.add(date)
        config.dates.sort()
        config.dates.removeAll { s -> if (clearDay) s >= date else s > date }

        return date
    }

    /**
     * Formats the input day to the current's month Calendar date to a YYYY-MM-dd String
     */
    private fun dayFormat(input: String): String {
        val day = pad(input)
        val month = pad(currMonth.get(Calendar.MONTH) + 1)
        val year = currMonth.get(Calendar.YEAR).toString()
        return "$year-$month-$day"
    }

    /**
     * Pads the input String with a leading 0
     */
    private fun pad(input: String): String {
        return input.padStart(2, '0')
    }

    /**
     * Pads the input Int with a leading 0
     */
    private fun pad(input: Int): String {
        return input.toString().padStart(2, '0')
    }

    /**
     * Gives format to the calendar
     */
    private fun render() {
        setMonthName()

        val start = getFirstDay()
        val end = currMonth.getActualMaximum(Calendar.DAY_OF_MONTH)

        renderGaps(start, end)
        renderDays(start, end)
    }

    /**
     * Retrieves the column of the first day of the month
     */
    private fun getFirstDay(): Int {
        val weekDay = currMonth.get(Calendar.DAY_OF_WEEK)
        return if (weekDay == 1) 6 else weekDay - 2
    }

    /**
     * Renders invisible the gaps before the first day and after the last day of the month
     */
    private fun renderGaps(start: Int, end: Int) {
        for (x in 0 until start) empty(elements[x])
        for (x in end + start until elements.size) empty(elements[x])
    }

    /**
     * Gives format to the existing days in a month
     */
    private fun renderDays(start: Int, end: Int) {
        val c = Calendar.getInstance()

        val days = findDays()

        for (x in 1..end) {
            val highlight = isSelected(x, days)
            val elem = elements[x + start - 1]

            elem.text = x.toString()
            stylize(elem, highlight)

            if (!highlight && isSameMonth(c) && c.get(Calendar.DAY_OF_MONTH) == x) {
                setViewColor(elem, R.color.green)
            }
        }
    }

    /**
     * Checks if the input day is among the period days
     */
    private fun isSelected(day: Int, days: Array<Int>): Boolean {
        var n = 0
        var found = false
        while (n < days.size && !found) {
            found = days[n] == day
            n++
        }
        return found
    }

    /**
     * Sets the color and the animation of a given layout day
     */
    private fun stylize(view: View?, highlight: Boolean) {
        val color: Int
        if (highlight) {
            color = R.color.red
            view?.startAnimation(bounceAnim)

            if (view != null) {
                val day = getDayValue(view)
                view.setOnClickListener {
                    clearNextMonths(day, true)
                    render()
                }
            }
        } else {
            color = R.color.colorAccent
            view?.clearAnimation()
            view?.setOnClickListener { click(view) }
        }

        setViewColor(view, color)
    }

    /**
     * Retrieves the period dates for the current selected month
     * @return Array with 2 days maximum
     */
    private fun findDays(): Array<Int> {
        val days = config.dates.filter { m ->
            val c = Calendar.getInstance()
            c.time = dateFormat.parse(m) ?: Date()
            isSameMonth(c)
        }

        return Array(days.size) { i -> days[i].takeLast(2).toInt() }
    }

    /**
     * Checks if the input date is on the same month as the current selected month
     */
    private fun isSameMonth(input: Calendar): Boolean {
        return input.get(Calendar.MONTH) == currMonth.get(Calendar.MONTH)
                && input.get(Calendar.YEAR) == currMonth.get(Calendar.YEAR)
    }

    /**
     * Retrieves the name of the current month and prints it to the TextView storing it
     */
    private fun setMonthName() {
        val name = SimpleDateFormat("MMMM", Locale("ES"))
            .format(currMonth.time)
            .toUpperCase(Locale.getDefault())
        findViewById<TextView>(R.id.month).text = name
    }

    /**
     * Renders the day invisible on the layout
     */
    private fun empty(t: TextView?) {
        t?.text = ""
        setViewColor(t, R.color.transparent)
        t?.clearAnimation()
    }

    /**
     * Switches the current month
     * @param amount adds or subtracts months. A negative value switches to previous months
     */
    private fun switchMonth(amount: Int) {
        currMonth.add(Calendar.MONTH, amount)
        render()
    }

    fun previous(it: View) {
        switchMonth(-1)
    }

    fun next(it: View) {
        switchMonth(1)
    }

    /**
     * Sets the color of the input element
     * @param color Hex color to be painted
     */
    private fun setViewColor(view: View?, @ColorRes id: Int) {
        view?.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(this, id))
    }

    /**
     * Generates 20 period dates after the input date, stores them into the array and serializes them
     * @param cal Calendar with the starting date
     */
    private fun update(cal: Calendar) {
        for (i in 1..20) {
            cal.add(Calendar.DATE, config.length)
            config.dates.add(dateFormat.format(cal.time))
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
        currMonth.set(Calendar.DAY_OF_MONTH, 1)

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
