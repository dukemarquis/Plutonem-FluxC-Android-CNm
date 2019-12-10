package com.plutonem.android.fluxc.model.list

import java.util.*

enum class ListOrder(val value: String) {
    ASC("ASC"),
    DESC("DESC");

    companion object {
        fun fromValue(value: String): ListOrder? {
            return values().firstOrNull { it.value.toLowerCase(Locale.ROOT) == value.toLowerCase(
                Locale.ROOT) }
        }
    }
}
