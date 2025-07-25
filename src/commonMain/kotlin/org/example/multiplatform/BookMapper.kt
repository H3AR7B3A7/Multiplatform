@file:OptIn(kotlin.js.ExperimentalJsExport::class)

package org.example.multiplatform

import kotlin.js.JsExport
import kotlin.math.absoluteValue

private val BASE_URL = "https://fake-lib.com"

@JsExport
fun toOnlineBook(book: Book): OnlineBook {
    val format = if (book.isAvailable) "PDF" else "ePub"
    val downloadable = book.isAvailable
    val url = "$BASE_URL?library=${book.libraryName.replace(" ", "%20")}"
    return OnlineBook(
        title = book.title,
        author = book.author,
        url = url,
        digitalFormat = format,
        isDownloadable = downloadable
    )
}

@JsExport
fun toBook(onlineBook: OnlineBook): Book {
    val libraryName = extractLibraryNameFromUrl(onlineBook.url)
    val available = onlineBook.isDownloadable
    val shelfNumber = libraryName.hashCode().absoluteValue % 100
    return Book(
        title = onlineBook.title,
        author = onlineBook.author,
        libraryName = libraryName,
        shelfNumber = shelfNumber,
        isAvailable = available
    )
}

private fun extractLibraryNameFromUrl(url: String): String {
    val regex = Regex("library=([^&]+)")
    val match = regex.find(url)
    return match?.groupValues?.get(1)?.replace("%20", " ") ?: "Unknown Library"
}
