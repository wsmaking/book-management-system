package com.example.bookmanagement.service

import com.example.bookmanagement.dto.AuthorRequest
import com.example.bookmanagement.dto.AuthorResponse
import com.example.bookmanagement.repository.AuthorRepository
import com.example.bookmanagement.repository.BookInfo
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class AuthorService(
    private val authorRepository: AuthorRepository,
) {
    @Transactional
    fun createAuthor(request: AuthorRequest): AuthorResponse {
        val authorId = authorRepository.create(request.name, request.birthDate)
        val author =
            authorRepository.findById(authorId)
                ?: throw IllegalStateException("作成した著者が見つかりません")

        return AuthorResponse(
            id = author.id ?: throw IllegalStateException("著者IDがnullです"),
            name = author.name ?: throw IllegalStateException("著者名がnullです"),
            birthDate = author.birthDate ?: throw IllegalStateException("生年月日がnullです"),
        )
    }

    @Transactional(readOnly = true)
    fun getAuthorById(id: Long): AuthorResponse {
        val author =
            authorRepository.findById(id)
                ?: throw NoSuchElementException("著者が見つかりません: ID=$id")

        return AuthorResponse(
            id = author.id ?: throw IllegalStateException("著者IDがnullです"),
            name = author.name ?: throw IllegalStateException("著者名がnullです"),
            birthDate = author.birthDate ?: throw IllegalStateException("生年月日がnullです"),
        )
    }

    @Transactional
    fun updateAuthor(
        id: Long,
        request: AuthorRequest,
    ): AuthorResponse {
        if (!authorRepository.update(id, request.name, request.birthDate)) {
            throw NoSuchElementException("著者が見つかりません: ID=$id")
        }

        return AuthorResponse(
            id = id,
            name = request.name,
            birthDate = request.birthDate,
        )
    }

    @Transactional(readOnly = true)
    fun getBooksByAuthor(authorId: Long): List<BookInfo> {
        authorRepository.findById(authorId)
            ?: throw NoSuchElementException("著者が見つかりません: ID=$authorId")

        return authorRepository.getBooksByAuthorId(authorId)
    }
}
