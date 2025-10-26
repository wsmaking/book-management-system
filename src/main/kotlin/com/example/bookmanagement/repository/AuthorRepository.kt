package com.example.bookmanagement.repository

import com.example.bookmanagement.jooq.tables.Authors.Companion.AUTHORS
import com.example.bookmanagement.jooq.tables.BookAuthors.Companion.BOOK_AUTHORS
import com.example.bookmanagement.jooq.tables.Books.Companion.BOOKS
import com.example.bookmanagement.jooq.tables.records.AuthorsRecord
import org.jooq.DSLContext
import org.springframework.stereotype.Repository
import java.time.LocalDate

@Repository
class AuthorRepository(
    private val dsl: DSLContext,
) {
    fun create(
        name: String,
        birthDate: LocalDate,
    ): Long {
        val record = dsl.newRecord(AUTHORS)
        record.name = name
        record.birthDate = birthDate
        record.store()
        return record.id ?: throw IllegalStateException("著者の作成に失敗しました")
    }

    fun findById(id: Long): AuthorsRecord? {
        return dsl.selectFrom(AUTHORS)
            .where(AUTHORS.ID.eq(id))
            .fetchOne()
    }

    fun findByIds(ids: List<Long>): List<AuthorsRecord> {
        if (ids.isEmpty()) return listOf()

        return dsl.selectFrom(AUTHORS)
            .where(AUTHORS.ID.`in`(ids))
            .fetch()
    }

    fun update(
        id: Long,
        name: String,
        birthDate: LocalDate,
    ): Boolean {
        val count =
            dsl.update(AUTHORS)
                .set(AUTHORS.NAME, name)
                .set(AUTHORS.BIRTH_DATE, birthDate)
                .where(AUTHORS.ID.eq(id))
                .execute()
        return count > 0
    }

    fun getBooksByAuthorId(authorId: Long): List<BookInfo> {
        return dsl.select(
            BOOKS.ID,
            BOOKS.TITLE,
            BOOKS.PRICE,
            BOOKS.PUBLICATION_STATUS,
        )
            .from(BOOKS)
            .join(BOOK_AUTHORS).on(BOOKS.ID.eq(BOOK_AUTHORS.BOOK_ID))
            .where(BOOK_AUTHORS.AUTHOR_ID.eq(authorId))
            .fetchInto(BookInfo::class.java)
    }
}

data class BookInfo(
    val id: Long?,
    val title: String?,
    val price: Int?,
    val publicationStatus: String?,
)
