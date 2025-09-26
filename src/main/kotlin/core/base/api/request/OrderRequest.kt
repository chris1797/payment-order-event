package core.base.api.request

import java.math.BigDecimal

data class OrderCreateRequest(
    val userId: Int,
    val productId: Int,
    val quantity: Int,
    val totalAmount: BigDecimal,
    val paymentMethod: String,
)