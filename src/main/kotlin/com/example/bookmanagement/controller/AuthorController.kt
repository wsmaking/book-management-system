package com.example.bookmanagement.controller

import com.example.bookmanagement.dto.AuthorRequest
import com.example.bookmanagement.dto.AuthorResponse
import com.example.bookmanagement.repository.BookInfo
import com.example.bookmanagement.service.AuthorService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/authors")
class AuthorController(
    private val authorService: AuthorService,
) {
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun createAuthor(
        @Valid @RequestBody request: AuthorRequest,
    ): AuthorResponse {
        return authorService.createAuthor(request)
    }

    @GetMapping("/{id}")
    fun getAuthor(
        @PathVariable id: Long,
    ): AuthorResponse {
        return authorService.getAuthorById(id)
    }

    @PutMapping("/{id}")
    fun updateAuthor(
        @PathVariable id: Long,
        @Valid @RequestBody request: AuthorRequest,
    ): AuthorResponse {
        return authorService.updateAuthor(id, request)
    }

    @GetMapping("/{id}/books")
    fun getAuthorBooks(
        @PathVariable id: Long,
    ): List<BookInfo> {
        return authorService.getBooksByAuthor(id)
    }
}
