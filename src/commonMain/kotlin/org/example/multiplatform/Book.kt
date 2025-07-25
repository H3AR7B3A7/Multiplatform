@file:OptIn(kotlin.js.ExperimentalJsExport::class)

package org.example.multiplatform

import kotlin.js.JsExport

@JsExport
data class Book(
    val title: String,
    val author: String,
    val libraryName: String,
    val shelfNumber: Int,
    val isAvailable: Boolean
)