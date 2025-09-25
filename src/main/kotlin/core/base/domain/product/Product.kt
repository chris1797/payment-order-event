package core.base.domain.product

import core.base.domain.common.BaseEntity
import jakarta.persistence.Entity
import jakarta.persistence.Table
import java.math.BigDecimal

@Entity
@Table(name = "products")
class Product(

    val name: String,
    val price: BigDecimal,
    val status: ProductStatus = ProductStatus.AVAILABLE,

): BaseEntity() {
}

enum class ProductStatus {
    AVAILABLE,
    OUT_OF_STOCK,
}
