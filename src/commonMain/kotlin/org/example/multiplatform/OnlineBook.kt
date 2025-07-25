@file:OptIn(kotlin.js.ExperimentalJsExport::class)

package org.example.multiplatform

import kotlin.js.JsExport

@JsExport
data class OnlineBook(
    val title: String,
    val author: String,
    val url: String,
    val digitalFormat: String,
    val isDownloadable: Boolean
)