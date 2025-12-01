package com.msa.order.domain.order

import com.msa.order.api.CreateOrderRequest
import com.msa.order.domain.base.BaseEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EntityListeners
import jakarta.persistence.FetchType
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.math.BigDecimal

@Entity
@Table(name = "orders")
@EntityListeners(AuditingEntityListener::class)
class Order (

    @Column(name = "user_id", nullable = false)
    val userId: Long,

    @Column(nullable = false)
    val orderCode: String,

    @Column(name = "quantity")
    val quantity: Int = 0,

    @Column(name = "total_amount")
    val totalAmount: BigDecimal = BigDecimal.ZERO,

    @Column(name = "product_name")
    val productName: String,

    @Column(name = "address")
    val address: String = "",

    @Column(name = "status")
    var status: Status = Status.CREATED


): BaseEntity() {


    enum class Status(val value: String) {
        CREATED("CREATED"),
        PROCESSING("PROCESSING"),
        COMPLETED("COMPLETED"),
        CANCELLED("CANCELLED");

        override fun toString(): String {
            return value
        }
    }

    companion object {
        fun createNewOrder(orderRequest: CreateOrderRequest): Order {
            return Order(
                userId = orderRequest.userId,
                address = orderRequest.address,
                quantity = orderRequest.quantity,
                totalAmount = orderRequest.totalAmount,
                productName = orderRequest.productName,
                orderCode = orderRequest.orderCode,
                status = Status.CREATED
            )
        }
    }
}