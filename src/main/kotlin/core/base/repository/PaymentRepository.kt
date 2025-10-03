package core.base.repository

import core.base.domain.payment.Payment
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface PaymentRepository: JpaRepository<Payment, Long> {

}
