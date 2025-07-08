// DelivererOrderStatusDeserializer.kt
package com.example.gogomarket.model

import com.google.gson.*
import java.lang.reflect.Type

class DelivererOrderStatusDeserializer : JsonDeserializer<DelivererOrderStatus> {
    override fun deserialize(
        json: JsonElement?,
        typeOfT: Type?,
        context: JsonDeserializationContext?
    ): DelivererOrderStatus {
        val statusInt = json?.asInt ?: -1
        return DelivererOrderStatus.fromInt(statusInt)
    }
}