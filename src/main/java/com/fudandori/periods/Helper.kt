package com.fudandori.periods

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import com.google.gson.Gson
import java.io.FileNotFoundException

fun addDialog(value: String, f: (s: String) -> Unit, c: Context) {
    AlertDialog.Builder(c)
        .setTitle("Añadir día")
        .setMessage("¿Quieres añadir el día $value?")
        .setPositiveButton(android.R.string.yes) { _, _ ->
            f(value)
        }
        .setNegativeButton(android.R.string.no) { _, _ -> //Do nothing
        }
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
            "Aviso",
            "Todavía no se ha seleccionado ningún día. Debes seleccionar el primer día pulsando sobre él para empezar los cálculos"
        )
    }

    return result
}
