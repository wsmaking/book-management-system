package com.example.bookmanagement.service

import com.example.bookmanagement.dto.AuthorRequest
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
class AuthorServiceTest {
    @Autowired
    private lateinit var authorService: AuthorService

    @Test
    fun `著者を作成できる`() {
        // Given
        val request =
            AuthorRequest(
                name = "夏目漱石",
                birthDate = LocalDate.of(1867, 2, 9),
            )

        // When
        val response = authorService.createAuthor(request)

        // Then
        assertNotNull(response.id)
        assertEquals("夏目漱石", response.name)
        assertEquals(LocalDate.of(1867, 2, 9), response.birthDate)
    }

    @Test
    fun `著者を取得できる`() {
        // Given
        val created =
            authorService.createAuthor(
                AuthorRequest("芥川龍之介", LocalDate.of(1892, 3, 1)),
            )

        // When
        val response = authorService.getAuthorById(created.id)

        // Then
        assertEquals(created.id, response.id)
        assertEquals("芥川龍之介", response.name)
    }

    @Test
    fun `存在しない著者の取得は例外`() {
        // When & Then
        assertThrows<NoSuchElementException> {
            authorService.getAuthorById(999L)
        }
    }

    @Test
    fun `著者を更新できる`() {
        // Given
        val created =
            authorService.createAuthor(
                AuthorRequest("太宰治", LocalDate.of(1909, 6, 19)),
            )
        val updateRequest =
            AuthorRequest(
                name = "太宰治（本名：津島修治）",
                birthDate = LocalDate.of(1909, 6, 19),
            )

        // When
        val updated = authorService.updateAuthor(created.id, updateRequest)

        // Then
        assertEquals(created.id, updated.id)
        assertEquals("太宰治（本名：津島修治）", updated.name)
    }

    @Test
    fun `存在しない著者の更新は例外`() {
        // Given
        val updateRequest =
            AuthorRequest(
                name = "存在しない著者",
                birthDate = LocalDate.of(1900, 1, 1),
            )

        // When & Then
        assertThrows<NoSuchElementException> {
            authorService.updateAuthor(999L, updateRequest)
        }
    }

    @Test
    fun `著者の書籍一覧を取得できる`() {
        // Given
        val author =
            authorService.createAuthor(
                AuthorRequest("村上春樹", LocalDate.of(1949, 1, 12)),
            )

        // When
        val books = authorService.getBooksByAuthor(author.id)

        // Then
        assertTrue(books.isEmpty()) // まだ書籍がない
    }

    @Test
    fun `存在しない著者の書籍一覧取得は例外`() {
        // When & Then
        assertThrows<NoSuchElementException> {
            authorService.getBooksByAuthor(999L)
        }
    }
}
