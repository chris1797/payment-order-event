package com.msa.order.domain.outbox

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface OutboxRepository : JpaRepository<Outbox, Long> {

    fun findByStatusOrderByCreatedAtAsc(status: OutboxStatus): List<Outbox>
}