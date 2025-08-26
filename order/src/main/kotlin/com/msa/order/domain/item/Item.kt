package com.msa.order.domain.item

import com.msa.order.domain.base.BaseEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EntityListeners
import jakarta.persistence.Table
import org.springframework.data.jpa.domain.support.AuditingEntityListener

@Entity
@Table(name = "items")
@EntityListeners(AuditingEntityListener::class)
class Item (

    @Column(nullable = false)
    val name: String,

    @Column(nullable = false)
    val price: Int = 0

): BaseEntity()