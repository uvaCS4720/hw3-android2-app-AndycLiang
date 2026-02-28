package edu.nd.pmcburne.hwapp.one

import java.time.LocalDate

enum class Sport(val apiSportPath: String) {
    Men("basketball-men"),
    Women("basketball-women"),;

    fun apiScoreboardPath(date: LocalDate): String {
        val mm = date.monthValue.toString().padStart(2, '0')
        val dd = date.dayOfMonth.toString().padStart(2, '0')
        return "d1/${date.year}/$mm/$dd"
    }
}