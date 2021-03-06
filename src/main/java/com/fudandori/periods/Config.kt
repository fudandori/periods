package com.fudandori.periods

class Config(
    var dates: ArrayList<String>,
    var span: Int
) {
    fun lastDay(): String {
        var result = "null"

        if (!dates.isNullOrEmpty()) {
            dates.sort()
            result = dates.last()
        }

        return result
    }

    fun reset() {
        dates = ArrayList()
    }

    fun empty() : Boolean {
        return dates.isNullOrEmpty()
    }
}