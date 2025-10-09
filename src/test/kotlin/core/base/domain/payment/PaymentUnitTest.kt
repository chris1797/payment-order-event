package core.base.domain.payment

import core.base.domain.order.Order
import core.base.domain.order.OrderStatus
import core.base.domain.user.User
import core.base.domain.user.UserStatus
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.string.shouldStartWith
import io.mockk.every
import io.mockk.mockk
import java.math.BigDecimal


class PaymentUnitTest : BehaviorSpec({

    val testUser = mockk<User>(relaxed = true) {
        every { id } returns 1L
        every { userName } returns "Test User"
        every { address } returns "123 Main St"
        every { status } returns UserStatus.ACTIVE
    }

    val testOrder = mockk<Order>(relaxed = true) {
        every { id } returns 100L
        every { user } returns testUser
        every { productName } returns "Test Product"
        every { quantity } returns 2
        every { totalAmount } returns BigDecimal("300.00")
        every { status } returns OrderStatus.PENDING
        every { address } returns "123 Main St"
    }

    Given("Order와 User가 주어졌을 때") {
        When("Payment.of() 팩토리 메서드로 Payment를 생성하면") {
            val payment = Payment.of(testOrder, testUser)

            Then("Payment 객체가 정상적으로 생성되어야 한다") {
                payment shouldNotBe null
                payment.order shouldBe testOrder
                payment.user shouldBe testUser
            }

            Then("주문 금액이 Payment 금액으로 설정되어야 한다") {
                payment.amount shouldBe BigDecimal("300.00")
            }

            Then("기본 상태는 INITIATED여야 한다") {
                payment.status shouldBe PaymentStatus.INITIATED
            }

            Then("기본 결제 수단은 CREDIT_CARD여야 한다") {
                payment.method shouldBe PaymentMethod.CREDIT_CARD
            }

            Then("txId가 'TX'로 시작하는 고유값으로 생성되어야 한다") {
                payment.txId shouldStartWith "TX"
                payment.txId.length shouldBe 15 // "TX" + 13자리 타임스탬프
            }
        }
    }

    Given("커스텀 설정으로 Payment를 생성할 때") {
        When("모든 파라미터를 지정하여 Payment를 생성하면") {
            val payment = Payment(
                order = testOrder,
                user = testUser,
                amount = BigDecimal("500.00"),
                status = PaymentStatus.APPROVED,
                method = PaymentMethod.CASH,
                txId = "TX_CUSTOM_ID_123"
            )

            Then("지정된 금액으로 Payment가 생성되어야 한다") {
                payment.amount shouldBe BigDecimal("500.00")
            }

            Then("지정된 상태로 Payment가 생성되어야 한다") {
                payment.status shouldBe PaymentStatus.APPROVED
            }

            Then("지정된 결제 수단으로 Payment가 생성되어야 한다") {
                payment.method shouldBe PaymentMethod.CASH
            }

            Then("지정된 txId로 Payment가 생성되어야 한다") {
                payment.txId shouldBe "TX_CUSTOM_ID_123"
            }
        }
    }

    Given("다양한 결제 상태를 가진 Payment를 생성할 때") {
        When("INITIATED 상태로 Payment를 생성하면") {
            val payment = Payment(
                order = testOrder,
                user = testUser,
                amount = BigDecimal("100.00"),
                status = PaymentStatus.INITIATED,
                txId = "TX_INIT_123"
            )

            Then("상태가 INITIATED여야 한다") {
                payment.status shouldBe PaymentStatus.INITIATED
            }
        }

        When("APPROVED 상태로 Payment를 생성하면") {
            val payment = Payment(
                order = testOrder,
                user = testUser,
                amount = BigDecimal("100.00"),
                status = PaymentStatus.APPROVED,
                txId = "TX_APPROVED_123"
            )

            Then("상태가 APPROVED여야 한다") {
                payment.status shouldBe PaymentStatus.APPROVED
            }
        }

        When("DECLINED 상태로 Payment를 생성하면") {
            val payment = Payment(
                order = testOrder,
                user = testUser,
                amount = BigDecimal("100.00"),
                status = PaymentStatus.DECLINED,
                txId = "TX_DECLINED_123"
            )

            Then("상태가 DECLINED여야 한다") {
                payment.status shouldBe PaymentStatus.DECLINED
            }
        }
    }

    Given("다양한 결제 수단을 가진 Payment를 생성할 때") {
        When("CREDIT_CARD 결제 수단으로 Payment를 생성하면") {
            val payment = Payment(
                order = testOrder,
                user = testUser,
                amount = BigDecimal("200.00"),
                method = PaymentMethod.CREDIT_CARD,
                txId = "TX_CARD_123"
            )

            Then("결제 수단이 CREDIT_CARD여야 한다") {
                payment.method shouldBe PaymentMethod.CREDIT_CARD
            }
        }

        When("CASH 결제 수단으로 Payment를 생성하면") {
            val payment = Payment(
                order = testOrder,
                user = testUser,
                amount = BigDecimal("200.00"),
                method = PaymentMethod.CASH,
                txId = "TX_CASH_123"
            )

            Then("결제 수단이 CASH여야 한다") {
                payment.method shouldBe PaymentMethod.CASH
            }
        }
    }

    Given("동일한 Order로 여러 Payment를 생성할 때") {
        When("Payment.of()를 여러 번 호출하면") {
            val payment1 = Payment.of(testOrder, testUser)
            Thread.sleep(1) // txId 고유성을 보장하기 위한 최소 대기
            val payment2 = Payment.of(testOrder, testUser)

            Then("각각 고유한 txId를 가져야 한다") {
                payment1.txId shouldNotBe payment2.txId
            }

            Then("동일한 Order를 참조해야 한다") {
                payment1.order shouldBe testOrder
                payment2.order shouldBe testOrder
            }

            Then("동일한 금액을 가져야 한다") {
                payment1.amount shouldBe payment2.amount
                payment1.amount shouldBe BigDecimal("300.00")
            }
        }
    }

    Given("큰 금액의 Payment를 생성할 때") {
        val largeAmount = BigDecimal("9999999999999.99")

        When("최대 범위의 금액으로 Payment를 생성하면") {
            val payment = Payment(
                order = testOrder,
                user = testUser,
                amount = largeAmount,
                txId = "TX_LARGE_123"
            )

            Then("큰 금액이 정확하게 저장되어야 한다") {
                payment.amount shouldBe largeAmount
            }
        }
    }

    Given("소액의 Payment를 생성할 때") {
        val smallAmount = BigDecimal("0.01")

        When("최소 금액으로 Payment를 생성하면") {
            val payment = Payment(
                order = testOrder,
                user = testUser,
                amount = smallAmount,
                txId = "TX_SMALL_123"
            )

            Then("소액이 정확하게 저장되어야 한다") {
                payment.amount shouldBe smallAmount
            }
        }
    }
})