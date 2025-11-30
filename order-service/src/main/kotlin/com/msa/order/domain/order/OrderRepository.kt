package com.msa.order.domain.order

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface OrderRepository : JpaRepository<Order, Long> {

    fun findByOrderCode(orderCode: String): Order?

    fun existsByOrderCode(orderCode: String): Boolean
}
