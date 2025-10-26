package com.example.bookmanagement.service

import com.example.bookmanagement.dto.BookAuthorInfo
import com.example.bookmanagement.dto.BookResponse
import com.example.bookmanagement.dto.CreateBookRequest
import com.example.bookmanagement.dto.PublicationStatus
import com.example.bookmanagement.dto.UpdateBookRequest
import com.example.bookmanagement.jooq.tables.records.AuthorsRecord
import com.example.bookmanagement.jooq.tables.records.BooksRecord
import com.example.bookmanagement.repository.AuthorRepository
import com.example.bookmanagement.repository.BookRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class BookService(
    private val bookRepository: BookRepository,
    private val authorRepository: AuthorRepository,
) {
    @Transactional
    fun createBook(request: CreateBookRequest): BookResponse {
        validateAuthorsExist(request.authorIds)

        val bookId = bookRepository.create(request.title, request.price)
        bookRepository.addAuthors(bookId, request.authorIds)

        return getBookById(bookId)
    }

    @Transactional(readOnly = true)
    fun getBookById(id: Long): BookResponse {
        val book =
            bookRepository.findById(id)
                ?: throw NoSuchElementException("書籍が見つかりません: ID=$id")

        val authors = bookRepository.getAuthorsByBookId(id)

        return toBookResponse(book, authors)
    }

    @Transactional
    fun updateBook(
        id: Long,
        request: UpdateBookRequest,
    ): BookResponse {
        val currentBook =
            bookRepository.findById(id)
                ?: throw NoSuchElementException("書籍が見つかりません: ID=$id")

        validatePublicationStatusChange(currentBook, request.publicationStatus)
        validateAuthorsExist(request.authorIds)

        bookRepository.update(id, request.title, request.price, request.publicationStatus)
        bookRepository.removeAllAuthors(id)
        bookRepository.addAuthors(id, request.authorIds)

        return getBookById(id)
    }

    private fun validatePublicationStatusChange(
        currentBook: BooksRecord,
        newStatus: PublicationStatus,
    ) {
        val currentStatus = PublicationStatus.fromStringOrDefault(currentBook.publicationStatus)

        if (currentStatus == PublicationStatus.PUBLISHED &&
            newStatus == PublicationStatus.UNPUBLISHED
        ) {
            throw IllegalStateException("出版済みの書籍を未出版に変更することはできません")
        }
    }

    private fun validateAuthorsExist(authorIds: List<Long>) {
        val existingIds =
            authorRepository.findByIds(authorIds)
                .mapNotNull { it.id }
                .toSet()

        val missingIds = authorIds.filterNot { it in existingIds }
        if (missingIds.isNotEmpty()) {
            throw NoSuchElementException("著者が見つかりません: ID=${missingIds.joinToString()}")
        }
    }

    private fun toBookResponse(
        book: BooksRecord,
        authors: List<AuthorsRecord>,
    ): BookResponse {
        return BookResponse(
            id = book.id ?: throw IllegalStateException("書籍IDがnullです"),
            title = book.title ?: throw IllegalStateException("タイトルがnullです"),
            price = book.price ?: throw IllegalStateException("価格がnullです"),
            publicationStatus = PublicationStatus.fromStringOrDefault(book.publicationStatus),
            authors = authors.mapNotNull { toBookAuthorInfo(it) },
        )
    }

    private fun toBookAuthorInfo(author: AuthorsRecord): BookAuthorInfo? {
        val id = author.id ?: return null
        val name = author.name ?: return null
        return BookAuthorInfo(id, name)
    }
}
