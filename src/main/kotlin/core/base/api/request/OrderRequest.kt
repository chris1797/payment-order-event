package core.base.api.request

import java.math.BigDecimal

data class OrderCreateRequest(
    val userId: Long,
    val productName: String,
    val quantity: Int,
    val totalAmount: BigDecimal,
)