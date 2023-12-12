package edu.temple.flossplayer

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import org.json.JSONArray
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.net.URL

class BookViewModel : ViewModel() {

    val bookList: BookList by lazy {
        BookList()
    }

    private val selectedBook: MutableLiveData<Book>? by lazy {
        MutableLiveData()
    }

    private val playingBook: MutableLiveData<Book>? by lazy {
        MutableLiveData()
    }

    // This item serves only as a notifier. We don't actually
    // care about the data it's storing. It's just a means to
    // have an observer be notified that something (new books have been added)
    // has happened
    private val updatedBookList : MutableLiveData<Int> by lazy {
        MutableLiveData()
    }

    // Flag to determine if one-off event should fire
    private var viewedBook = false

    // Flag to determine if one-off event should fire
    private var playedBook = false

    fun getSelectedBook(): LiveData<Book>? {
        return selectedBook
    }

    fun setSelectedBook(selectedBook: Book) {
        viewedBook = false
        this.selectedBook?.value = selectedBook
    }

    fun clearSelectedBook () {
        selectedBook?.value = null
    }

    fun markSelectedBookViewed () {
        viewedBook = true
    }

    fun hasViewedSelectedBook() : Boolean {
        return viewedBook
    }

    fun getPlayingBook(): LiveData<Book>? {
        return playingBook
    }

    fun setBookPlayed (state: Boolean) {
        playedBook = state
    }

    fun hasBookBeenPlayed(): Boolean {
        return playedBook
    }

    fun setPlayingBook(playingBook: Book) {
        this.playingBook?.value = playingBook
    }

    fun updateBooks (books: JSONArray) {
        bookList.clear()
        for (i in 0 until books.length()) {
            bookList.add(Book(books.getJSONObject(i)))
        }
        notifyUpdatedBookList()
    }

    // The indirect observable for those that want to know when
    // the book list has changed
    fun getUpdatedBookList() : LiveData<out Any> {
        return updatedBookList
    }

    // A trivial update used to indirectly notify observers that the Booklist has changed
    private fun notifyUpdatedBookList() {
        updatedBookList.value = updatedBookList.value?.plus(1)
    }

    fun downloadBook(bookId: Int, context: Context, onDownloadComplete: (File?) -> Unit) {
        val downloadURL = "https://kamorris.com/lab/flossplayer/downloadbook.php?id=$bookId"
        val bookFile = File(context.filesDir, "$bookId.mp3")

        Thread {
            try {
                URL(downloadURL).openStream().use { input ->
                    FileOutputStream(bookFile).use { output ->
                        input.copyTo(output)
                    }
                }
                onDownloadComplete(bookFile)
            } catch (e: IOException) {
                e.printStackTrace()
                onDownloadComplete(null)
            }
        }.start()
    }

    fun updateBookFile(bookId: Int, file: File?) {
        bookList.find { it.book_id == bookId }?.bookFile = file
    }

    fun updateBookPosition(bookId: Int, position: Int) {
        bookList.find { it.book_id == bookId }?.let { book ->
            book.currentPosition = position
            selectedBook?.value = book
        }
    }

}