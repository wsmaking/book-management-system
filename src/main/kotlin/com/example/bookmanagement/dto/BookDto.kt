package com.example.bookmanagement.dto

import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotEmpty

data class CreateBookRequest(
    @field:NotBlank(message = "タイトルは必須です")
    val title: String,
    @field:Min(value = 0, message = "価格は0以上である必要があります")
    val price: Int,
    @field:NotEmpty(message = "最低1人の著者が必要です")
    val authorIds: List<Long>,
)

data class UpdateBookRequest(
    @field:NotBlank(message = "タイトルは必須です")
    val title: String,
    @field:Min(value = 0, message = "価格は0以上である必要があります")
    val price: Int,
    val publicationStatus: PublicationStatus,
    @field:NotEmpty(message = "最低1人の著者が必要です")
    val authorIds: List<Long>,
)

data class BookResponse(
    val id: Long,
    val title: String,
    val price: Int,
    val publicationStatus: PublicationStatus,
    val authors: List<BookAuthorInfo>,
)

data class BookAuthorInfo(
    val id: Long,
    val name: String,
)
