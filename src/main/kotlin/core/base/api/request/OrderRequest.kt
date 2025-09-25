package core.base.api.request

data class OrderCreateRequest(
    val userId: Int,
    val productId: Int,
    val quantity: Int,
    val paymentMethod: String,
)