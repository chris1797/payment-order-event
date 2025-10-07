package core.base.api.request

import java.math.BigDecimal

data class OrderCreateRequest(
    val userId: Long,
    val productName: String,
    val quantity: Int,
    val totalAmount: BigDecimal,
) {

    fun validateOrderRequest() {
        require(this.productName.isNotBlank()) { ExceptionMessage.PRODUCT_NAME_VALIDATION }
        require(this.quantity > 0) { ExceptionMessage.QUANTITY_VALIDATION }
        require(this.totalAmount > BigDecimal.ZERO) { ExceptionMessage.TOTAL_AMOUNT_VALIDATION }
    }

    enum class ExceptionMessage(string: String) {
        PRODUCT_NAME_VALIDATION("상품명은 비어 있을 수 없습니다"),
        QUANTITY_VALIDATION("주문 수량은 0보다 커야 합니다"),
        TOTAL_AMOUNT_VALIDATION("주문 수량은 0보다 커야 합니다"),
    }
}