package com.example.bookmanagement.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.PastOrPresent
import java.time.LocalDate

data class AuthorRequest(
    @field:NotBlank(message = "名前は必須です")
    val name: String,
    @field:PastOrPresent(message = "生年月日は現在または過去の日付である必要があります")
    val birthDate: LocalDate,
)

data class AuthorResponse(
    val id: Long,
    val name: String,
    val birthDate: LocalDate,
)
