# Multiplatform

## Build

```
./gradlew build
```

Fir Java, you can find the artifact [here](build/libs/multiplatform-jvm-1.0.jar).
For Js, you can find the artifact [here](build/dist/js/productionLibrary).

## Publishing

### For Testing

To publish the jar to your local `.m2` folder:

```
./gradlew publishToMavenLocal
```

To link the npm package for testing:

```
cd .\build\dist\js\productionLibrary\
```

```
npm link multiplatform
```

## Example Usage

### Java

```java
package org.example.demo;

import org.example.multiplatform.Book;
import org.example.multiplatform.BookMapper;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.example.multiplatform.OnlineBook;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class DemoApplication {

	public static void main(String[] args) {
		SpringApplication.run(DemoApplication.class, args);
	}

	@Bean
	public BookMapper bookMapper() {
		return BookMapper.INSTANCE;
	}

	@Bean
	public CommandLineRunner run(BookMapper bookMapper) {
		return args -> {
			Book book = new Book(
					"1984", "George Orwell", "Central Library", 42, true
			);

			OnlineBook onlineBook = bookMapper.toOnlineBook(book);

			System.out.println("Online Book:");
			System.out.println("Title: " + onlineBook.getTitle());
			System.out.println("Author: " + onlineBook.getAuthor());
			System.out.println("URL: " + onlineBook.getUrl());
		};
	}
}
```

### Typescript

```ts
import { Book, toOnlineBook } from "@example/multiplatform";

const book = new Book("Test Title", "Test Author", "Main Library", 1, true);
const onlineBook = toOnlineBook(book);
console.log(onlineBook);
```

## Things We Learned

- In JavaScript, you can only export items that sit at the top level:
  For example, you might create an object with functions to act as a singleton, like a Spring Bean.
  This pattern works in Java, but it does not let you import those functions from a npm package.
- We can rename overloaded functions, so they can also work in Js.

## Top Level Problem?

In JavaScript/TypeScript, your BookMapper object becomes something like:

```js
javascript// Generated JS - not directly accessible functions
BookMapper.INSTANCE.toOnlineBook(book)
```

But TypeScript expects normal exports, not nested object properties.

### Solutions

#### Option 1: Separate Files (Recommended)

Create separate files for different target platforms:

BookMapperFunctions.kt (for JS/TS):

```kotlin
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
```

BookMapper.kt (for JVM/Java):

```kotlin
package org.example.multiplatform

import kotlin.math.absoluteValue

object BookMapper {
    private val BASE_URL = "https://fake-lib.com"

    @JvmStatic
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

    @JvmStatic
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
}
```

You can avoid this duplication and separate what is compiled easily in a single file, like this:

```kotlin
@file:OptIn(kotlin.js.ExperimentalJsExport::class)

package org.example.multiplatform

import kotlin.js.JsExport
import kotlin.math.absoluteValue

private const val BASE_URL = "https://fake-lib.com"

// For JavaScript/TypeScript
@JsExport
fun toOnlineBook(book: Book): OnlineBook = mapBookToOnlineInternal(book)

@JsExport
fun toBook(onlineBook: OnlineBook): Book = mapOnlineToBookInternal(onlineBook)

// For JVM (the object will be ignored by JS compilation)
object BookMapper {
    @JvmStatic
    fun toOnlineBook(book: Book): OnlineBook = mapBookToOnlineInternal(book)

    @JvmStatic
    fun toBook(onlineBook: OnlineBook): Book = mapOnlineToBookInternal(onlineBook)
}

// Shared implementation
private fun mapBookToOnlineInternal(book: Book): OnlineBook {
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

private fun mapOnlineToBookInternal(onlineBook: OnlineBook): Book {
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
```

#### Option 2: Platform-Specific Source Sets

Organize with platform-specific source sets:

```
src/
├── commonMain/kotlin/
│   ├── Book.kt
│   └── OnlineBook.kt
├── jsMain/kotlin/
│   └── BookMapperFunctions.kt  // Top-level functions with @JsExport
└── jvmMain/kotlin/
└── BookMapper.kt           // Object with @JvmStatic
```

Update your build.gradle.kts:

```kotlin
kotlinkotlin {
    sourceSets {
        commonMain {
            dependencies {}
        }
        jsMain {
            dependencies {}
        }
        jvmMain {
            dependencies {}
        }
    }
}
```

_However, platform-specific source sets completely defeat our usecase for Kotlin Multiplatform._

### Usage

#### TypeScript:

```ts
typescriptimport { toOnlineBook, toBook } from "multiplatform";
const onlineBook = toOnlineBook(book);
```

#### Java:

```java
import static org.example.multiplatform.BookMapper.toOnlineBook;
// Or
import org.example.multiplatform.BookMapper;

OnlineBook online = BookMapper.toOnlineBook(book);
```

### Why This Happens

The issue occurs because:

- JVM: Objects compile to singletons with static methods
- JS: Objects don't have the same static method concept, and @JsExport works best with top-level declarations

The platform-specific approach gives you the best of both worlds - clean object-oriented API for Java and clean
functional exports for TypeScript.



---


