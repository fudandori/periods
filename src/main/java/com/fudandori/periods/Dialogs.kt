package com.fudandori.periods

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.view.View
import android.widget.CheckBox
import android.widget.EditText
import android.widget.TextView
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity

fun addDialog(value: String, f: (s: String) -> Unit, c: Context) {
    val day = value.substring(8).toInt().toString()

    AlertDialog.Builder(c)
        .setTitle(R.string.add_title)
        .setMessage(c.getString(R.string.add_body, day))
        .setPositiveButton(android.R.string.yes) { _, _ -> f(value) }
        .setNegativeButton(android.R.string.no) { _, _ -> }
        .show()
}

fun removeDialog(a: Activity, date: String, f: (s: String, b: Boolean) -> Unit) {
    val dialogView = View.inflate(a, R.layout.remove, null)
    val textView = dialogView.findViewById<TextView>(R.id.remove_text)
    val day = date.substring(8).toInt()
    val check = dialogView.findViewById<CheckBox>(R.id.delete_check)
    textView.text = a.getString(R.string.remove_body, day)

    AlertDialog.Builder(a)
        .setView(dialogView)
        .setTitle(R.string.delete_title)
        .setPositiveButton(android.R.string.yes) { _, _ -> f(date, check.isChecked) }
        .setNegativeButton(android.R.string.cancel) { dialog, _ -> dialog.dismiss() }
        .show()
}

fun periodDialog(a: AppCompatActivity, input: String, f: (i: Int) -> Unit) {
    val dialogView = View.inflate(a, R.layout.period_span, null)
    val editText = dialogView.findViewById<EditText>(R.id.period_span)

    editText.setText(input)

    AlertDialog.Builder(a)
        .setView(dialogView)
        .setPositiveButton(android.R.string.yes) { _, _ ->
            f(editText.text.toString().toInt())

        }
        .setNegativeButton(android.R.string.cancel) { dialog, _ -> dialog.dismiss() }
        .show()
}

fun alertDialog(c: Context, @StringRes title: Int, @StringRes body: Int) {
    AlertDialog.Builder(c)
        .setTitle(title)
        .setMessage(body)
        .setPositiveButton(android.R.string.yes) { _, _ ->
            //Close and do nothing
        }
        .show()
}