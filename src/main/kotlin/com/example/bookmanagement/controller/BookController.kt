package com.example.bookmanagement.controller

import com.example.bookmanagement.dto.BookResponse
import com.example.bookmanagement.dto.CreateBookRequest
import com.example.bookmanagement.dto.UpdateBookRequest
import com.example.bookmanagement.service.BookService
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
@RequestMapping("/books")
class BookController(
    private val bookService: BookService,
) {
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun createBook(
        @Valid @RequestBody request: CreateBookRequest,
    ): BookResponse {
        return bookService.createBook(request)
    }

    @GetMapping("/{id}")
    fun getBook(
        @PathVariable id: Long,
    ): BookResponse {
        return bookService.getBookById(id)
    }

    @PutMapping("/{id}")
    fun updateBook(
        @PathVariable id: Long,
        @Valid @RequestBody request: UpdateBookRequest,
    ): BookResponse {
        return bookService.updateBook(id, request)
    }
}
