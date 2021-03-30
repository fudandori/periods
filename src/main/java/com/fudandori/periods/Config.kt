package com.fudandori.periods

class Config(
    var dates: ArrayList<String>,
    var length: Int
) {
    fun lastDay(): String {
        return if(dates.isNullOrEmpty()) "-" else dates.last()
    }

    fun reset() {
        dates = ArrayList()
    }
}