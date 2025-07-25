import { Book, OnlineBook, toOnlineBook } from "multiplatform";
const book = new Book("Test Title", "Test Author", "Main Library", 1, true);
const onlineBook = toOnlineBook(book);
// const onlineBook2: Book = toOnlineBook(book);
console.log(onlineBook);
console.log(onlineBook instanceof OnlineBook); // true
