@file:JvmName("Logger")

package com.nan.asmplugin.util

import java.util.*

private const val TAG = "ASMPlugin"

fun log(fmt: String, vararg args: Any) {
    println("$TAG: ${format(fmt, args)}")
}

private fun format(fmt: String, vararg args: Any): String {
    return if (args.isEmpty()) {
        fmt
    } else {
        String.format(Locale.getDefault(), fmt, *args)
    }
}