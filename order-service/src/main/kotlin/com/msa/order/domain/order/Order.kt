package com.msa.order.domain.order

import com.msa.order.domain.base.BaseEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EntityListeners
import jakarta.persistence.Table
import org.springframework.data.jpa.domain.support.AuditingEntityListener

@Entity
@Table(name = "orders")
@EntityListeners(AuditingEntityListener::class)
class Order (

    @Column(nullable = false)
    val orderCode: String,

): BaseEntity()