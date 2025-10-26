package com.example.bookmanagement.dto

enum class PublicationStatus {
    UNPUBLISHED,
    PUBLISHED,
    ;

    companion object {
        /**
         * 文字列からPublicationStatusに変換
         * nullまたは不正な値の場合はUNPUBLISHEDを返す
         */
        fun fromStringOrDefault(value: String?): PublicationStatus {
            return value?.let {
                try {
                    valueOf(it)
                } catch (e: IllegalArgumentException) {
                    UNPUBLISHED
                }
            } ?: UNPUBLISHED
        }
    }
}
