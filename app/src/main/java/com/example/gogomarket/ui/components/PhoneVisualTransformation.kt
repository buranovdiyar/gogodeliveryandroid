// PhoneVisualTransformation.kt
package com.example.gogomarket.ui.components

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation

class PhoneVisualTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        // Форматируем строку вида "123456789" в "12 345 67 89"
        val trimmed = if (text.text.length >= 9) text.text.substring(0..8) else text.text
        var out = ""
        for (i in trimmed.indices) {
            out += trimmed[i]
            if (i == 1 || i == 4 || i == 6) {
                out += " "
            }
        }

        // Этот объект помогает правильно перемещать курсор при вводе и удалении
        val phoneOffsetTranslator = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int {
                if (offset <= 1) return offset
                if (offset <= 4) return offset + 1
                if (offset <= 6) return offset + 2
                if (offset <= 8) return offset + 3
                return 12
            }

            override fun transformedToOriginal(offset: Int): Int {
                if (offset <= 2) return offset
                if (offset <= 6) return offset - 1
                if (offset <= 9) return offset - 2
                if (offset <= 12) return offset - 3
                return 9
            }
        }

        return TransformedText(AnnotatedString(out), phoneOffsetTranslator)
    }
}