package com.example.bookmanagement.service

import com.example.bookmanagement.dto.AuthorRequest
import com.example.bookmanagement.dto.CreateBookRequest
import com.example.bookmanagement.dto.PublicationStatus
import com.example.bookmanagement.dto.UpdateBookRequest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate

@SpringBootTest
@Transactional
class BookServiceTest {
    @Autowired
    private lateinit var bookService: BookService

    @Autowired
    private lateinit var authorService: AuthorService

    @Test
    fun `書籍を作成できる`() {
        // Given
        val author =
            authorService.createAuthor(
                AuthorRequest("夏目漱石", LocalDate.of(1867, 2, 9)),
            )
        val request =
            CreateBookRequest(
                title = "吾輩は猫である",
                price = 500,
                authorIds = listOf(author.id),
            )

        // When
        val response = bookService.createBook(request)

        // Then
        assertNotNull(response.id)
        assertEquals("吾輩は猫である", response.title)
        assertEquals(500, response.price)
        assertEquals(PublicationStatus.UNPUBLISHED, response.publicationStatus)
        assertEquals(1, response.authors.size)
        assertEquals(author.id, response.authors[0].id)
    }

    @Test
    fun `複数著者の書籍を作成できる`() {
        // Given
        val author1 =
            authorService.createAuthor(
                AuthorRequest("著者1", LocalDate.of(1900, 1, 1)),
            )
        val author2 =
            authorService.createAuthor(
                AuthorRequest("著者2", LocalDate.of(1900, 1, 1)),
            )
        val request =
            CreateBookRequest(
                title = "共著作品",
                price = 1000,
                authorIds = listOf(author1.id, author2.id),
            )

        // When
        val response = bookService.createBook(request)

        // Then
        assertEquals(2, response.authors.size)
    }

    @Test
    fun `存在しない著者での書籍作成は例外`() {
        // Given
        val request =
            CreateBookRequest(
                title = "テスト",
                price = 100,
                authorIds = listOf(999L),
            )

        // When & Then
        assertThrows<NoSuchElementException> {
            bookService.createBook(request)
        }
    }

    @Test
    fun `書籍を更新できる`() {
        // Given
        val author =
            authorService.createAuthor(
                AuthorRequest("芥川龍之介", LocalDate.of(1892, 3, 1)),
            )
        val created =
            bookService.createBook(
                CreateBookRequest("羅生門", 300, listOf(author.id)),
            )
        val updateRequest =
            UpdateBookRequest(
                title = "羅生門（改訂版）",
                price = 400,
                publicationStatus = PublicationStatus.UNPUBLISHED,
                authorIds = listOf(author.id),
            )

        // When
        val updated = bookService.updateBook(created.id, updateRequest)

        // Then
        assertEquals("羅生門（改訂版）", updated.title)
        assertEquals(400, updated.price)
    }

    @Test
    fun `未出版から出版済みへの変更ができる`() {
        // Given
        val author =
            authorService.createAuthor(
                AuthorRequest("太宰治", LocalDate.of(1909, 6, 19)),
            )
        val created =
            bookService.createBook(
                CreateBookRequest("人間失格", 600, listOf(author.id)),
            )

        // When
        val updated =
            bookService.updateBook(
                created.id,
                UpdateBookRequest(
                    title = "人間失格",
                    price = 600,
                    publicationStatus = PublicationStatus.PUBLISHED,
                    authorIds = listOf(author.id),
                ),
            )

        // Then
        assertEquals(PublicationStatus.PUBLISHED, updated.publicationStatus)
    }

    @Test
    fun `出版済みから未出版への変更は例外（最重要テスト）`() {
        // Given
        val author =
            authorService.createAuthor(
                AuthorRequest("村上春樹", LocalDate.of(1949, 1, 12)),
            )
        val created =
            bookService.createBook(
                CreateBookRequest("ノルウェイの森", 700, listOf(author.id)),
            )

        // 一度出版済みにする
        bookService.updateBook(
            created.id,
            UpdateBookRequest(
                title = "ノルウェイの森",
                price = 700,
                publicationStatus = PublicationStatus.PUBLISHED,
                authorIds = listOf(author.id),
            ),
        )

        // When & Then
        val exception =
            assertThrows<IllegalStateException> {
                bookService.updateBook(
                    created.id,
                    UpdateBookRequest(
                        title = "ノルウェイの森",
                        price = 700,
                        publicationStatus = PublicationStatus.UNPUBLISHED,
                        authorIds = listOf(author.id),
                    ),
                )
            }
        assertTrue(exception.message!!.contains("出版済みの書籍を未出版に変更"))
    }

    @Test
    fun `書籍取得ができる`() {
        // Given
        val author =
            authorService.createAuthor(
                AuthorRequest("川端康成", LocalDate.of(1899, 6, 14)),
            )
        val created =
            bookService.createBook(
                CreateBookRequest("雪国", 800, listOf(author.id)),
            )

        // When
        val retrieved = bookService.getBookById(created.id)

        // Then
        assertEquals(created.id, retrieved.id)
        assertEquals("雪国", retrieved.title)
    }

    @Test
    fun `存在しない書籍の取得は例外`() {
        // When & Then
        assertThrows<NoSuchElementException> {
            bookService.getBookById(999L)
        }
    }

    @Test
    fun `価格が0の書籍を作成できる`() {
        // Given
        val author =
            authorService.createAuthor(
                AuthorRequest("テスト著者", LocalDate.of(1900, 1, 1)),
            )
        val request =
            CreateBookRequest(
                title = "無料書籍",
                price = 0,
                authorIds = listOf(author.id),
            )

        // When
        val response = bookService.createBook(request)

        // Then
        assertEquals(0, response.price)
    }
}
