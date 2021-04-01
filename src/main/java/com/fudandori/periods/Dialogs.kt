package com.fudandori.periods

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.view.View
import android.widget.CheckBox
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

fun addDialog(value: String, f: (s: String) -> Unit, c: Context) {
    val day = value.substring(8).toInt().toString()

    AlertDialog.Builder(c)
        .setTitle("Añadir día")
        .setMessage("¿Quieres añadir el día $day?")
        .setPositiveButton(android.R.string.yes) { _, _ -> f(value) }
        .setNegativeButton(android.R.string.no) { _, _ -> }
        .show()
}

fun removeDialog(a: Activity, date: String, f: (s: String, b: Boolean) -> Unit) {
    val dialogView = View.inflate(a, R.layout.remove, null)
    val textView = dialogView.findViewById<TextView>(R.id.remove_text)
    val day = date.substring(8).toInt()
    val check = dialogView.findViewById<CheckBox>(R.id.delete_check)
    textView.setText("¿Deseas eliminar el día $day?")

    AlertDialog.Builder(a)
        .setView(dialogView)
        .setTitle("Eliminar día")
        .setPositiveButton(android.R.string.yes) { _, _ -> f(date, check.isChecked) }
        .setNegativeButton("Cancel") { dialog, _ -> dialog.dismiss() }
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
        .setNegativeButton("Cancel") { dialog, _ -> dialog.dismiss() }
        .show()
}

fun alertDialog(c: Context, title: String, body: String) {
    AlertDialog.Builder(c)
        .setTitle(title)
        .setMessage(body)
        .setPositiveButton(android.R.string.yes) { _, _ ->
            //Close and do nothing
        }
        .show()
}