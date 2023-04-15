package org.my.dto

class TransliterationDto(
    var source: String,
    var transliteration: String,
): Comparable<TransliterationDto> {

    override fun toString(): String {
        return "$source - $transliteration"
    }

    override fun compareTo(other: TransliterationDto): Int {
        return this.source.compareTo(other.source)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as TransliterationDto

        if (source != other.source) return false

        return true
    }

    override fun hashCode(): Int {
        return source.hashCode()
    }
}