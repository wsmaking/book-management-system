package com.example.bookmanagement.repository

import com.example.bookmanagement.dto.PublicationStatus
import com.example.bookmanagement.jooq.tables.Authors.Companion.AUTHORS
import com.example.bookmanagement.jooq.tables.BookAuthors.Companion.BOOK_AUTHORS
import com.example.bookmanagement.jooq.tables.Books.Companion.BOOKS
import com.example.bookmanagement.jooq.tables.records.AuthorsRecord
import com.example.bookmanagement.jooq.tables.records.BooksRecord
import org.jooq.DSLContext
import org.springframework.stereotype.Repository

@Repository
class BookRepository(
    private val dsl: DSLContext,
) {
    fun create(
        title: String,
        price: Int,
    ): Long {
        val record = dsl.newRecord(BOOKS)
        record.title = title
        record.price = price
        record.publicationStatus = PublicationStatus.UNPUBLISHED.name
        record.store()
        return record.id ?: throw IllegalStateException("書籍の作成に失敗しました")
    }

    fun findById(id: Long): BooksRecord? {
        return dsl.selectFrom(BOOKS)
            .where(BOOKS.ID.eq(id))
            .fetchOne()
    }

    fun update(
        id: Long,
        title: String,
        price: Int,
        publicationStatus: PublicationStatus,
    ): Boolean {
        val count =
            dsl.update(BOOKS)
                .set(BOOKS.TITLE, title)
                .set(BOOKS.PRICE, price)
                .set(BOOKS.PUBLICATION_STATUS, publicationStatus.name)
                .where(BOOKS.ID.eq(id))
                .execute()
        return count > 0
    }

    fun addAuthors(
        bookId: Long,
        authorIds: List<Long>,
    ) {
        authorIds.forEach { authorId ->
            val record = dsl.newRecord(BOOK_AUTHORS)
            record.bookId = bookId
            record.authorId = authorId
            record.store()
        }
    }

    fun removeAllAuthors(bookId: Long) {
        dsl.deleteFrom(BOOK_AUTHORS)
            .where(BOOK_AUTHORS.BOOK_ID.eq(bookId))
            .execute()
    }

    fun getAuthorsByBookId(bookId: Long): List<AuthorsRecord> {
        return dsl.select(AUTHORS.asterisk())
            .from(AUTHORS)
            .join(BOOK_AUTHORS).on(AUTHORS.ID.eq(BOOK_AUTHORS.AUTHOR_ID))
            .where(BOOK_AUTHORS.BOOK_ID.eq(bookId))
            .fetch()
            .into(AUTHORS)
    }
}
