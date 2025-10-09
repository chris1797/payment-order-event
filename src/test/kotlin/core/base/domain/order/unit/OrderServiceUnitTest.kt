package core.base.domain.order.unit

import core.base.api.request.OrderCreateRequest
import core.base.domain.order.Order
import core.base.domain.order.OrderRepository
import core.base.domain.order.OrderStatus
import core.base.domain.user.User
import core.base.domain.user.UserStatus
import core.base.service.OrderService
import core.base.service.PaymentService
import core.base.service.UserService
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import java.math.BigDecimal

class OrderServiceUnitTest : BehaviorSpec({

    val userService = mockk<UserService>()
    val paymentService = mockk<PaymentService>()
    val orderRepository = mockk<OrderRepository>()
    val orderService = OrderService(userService, paymentService, orderRepository)

    val testUser = mockk<User>(relaxed = true) {
        every { id } returns 1L
        every { userName } returns "Test User"
        every { address } returns "123 Main St"
        every { status } returns UserStatus.ACTIVE
    }

    Given("정상적인 주문 요청이 주어졌을 때") {
        val orderRequest = OrderCreateRequest(
            userId = 1L,
            productName = "Test Product",
            quantity = 2,
            totalAmount = BigDecimal("300.00")
        )

        val savedOrder = mockk<Order>(relaxed = true) {
            every { id } returns 100L
            every { user } returns testUser
            every { productName } returns "Test Product"
            every { quantity } returns 2
            every { totalAmount } returns BigDecimal("300.00")
            every { status } returns OrderStatus.COMPLETED
        }

        every { userService.getUserById(1L) } returns testUser
        every { orderRepository.save(any()) } returns savedOrder
        every { paymentService.processPayment(any(), any()) } returns savedOrder

        When("주문을 생성하면") {
            val result = orderService.saveOrder(orderRequest)

            Then("주문이 성공적으로 생성되어야 한다") {
                result shouldNotBe null
                result.id shouldBe 100L
                result.userId shouldBe 1L
                result.userName shouldBe "Test User"
                result.totalAmount shouldBe BigDecimal("300.00")
                result.status shouldBe OrderStatus.COMPLETED
            }

            Then("모든 의존성이 호출되어야 한다") {
                verify(exactly = 1) { userService.getUserById(1L) }
                verify(exactly = 1) { orderRepository.save(any()) }
                verify(exactly = 1) { paymentService.processPayment(any(), any()) }
            }
        }
    }

    Given("존재하지 않는 사용자 ID로 주문 요청이 주어졌을 때") {
        val orderRequest = OrderCreateRequest(
            userId = 999L,
            productName = "Test Product",
            quantity = 1,
            totalAmount = BigDecimal("100.00")
        )

        every { userService.getUserById(999L) } throws NoSuchElementException("User not found with id: 999")

        When("주문을 생성하면") {
            Then("NoSuchElementException이 발생해야 한다") {
                shouldThrow<NoSuchElementException> {
                    orderService.saveOrder(orderRequest)
                }
            }
        }
    }

    Given("빈 상품명으로 주문 요청이 주어졌을 때") {
        val orderRequest = OrderCreateRequest(
            userId = 1L,
            productName = "",
            quantity = 1,
            totalAmount = BigDecimal("100.00")
        )

        When("주문을 생성하면") {
            Then("IllegalArgumentException이 발생해야 한다") {
                val exception = shouldThrow<IllegalArgumentException> {
                    orderService.saveOrder(orderRequest)
                }
                exception.message shouldBe OrderCreateRequest.ExceptionMessage.PRODUCT_NAME_VALIDATION.message
            }
        }
    }

    Given("잘못된 수량(0 이하)으로 주문 요청이 주어졌을 때") {
        val orderRequest = OrderCreateRequest(
            userId = 1L,
            productName = "Test Product",
            quantity = 0,
            totalAmount = BigDecimal("100.00")
        )

        When("주문을 생성하면") {
            Then("IllegalArgumentException이 발생해야 한다") {
                val exception = shouldThrow<IllegalArgumentException> {
                    orderService.saveOrder(orderRequest)
                }
                exception.message shouldBe OrderCreateRequest.ExceptionMessage.QUANTITY_VALIDATION.message
            }
        }
    }

    Given("잘못된 금액(0 이하)으로 주문 요청이 주어졌을 때") {
        val orderRequest = OrderCreateRequest(
            userId = 1L,
            productName = "Test Product",
            quantity = 1,
            totalAmount = BigDecimal.ZERO
        )

        When("주문을 생성하면") {
            Then("IllegalArgumentException이 발생해야 한다") {
                val exception = shouldThrow<IllegalArgumentException> {
                    orderService.saveOrder(orderRequest)
                }
                exception.message shouldBe OrderCreateRequest.ExceptionMessage.TOTAL_AMOUNT_VALIDATION.message
            }
        }
    }

    Given("음수 수량으로 주문 요청이 주어졌을 때") {
        val orderRequest = OrderCreateRequest(
            userId = 1L,
            productName = "Test Product",
            quantity = -5,
            totalAmount = BigDecimal("100.00")
        )

        When("주문을 생성하면") {
            Then("IllegalArgumentException이 발생해야 한다") {
                val exception = shouldThrow<IllegalArgumentException> {
                    orderService.saveOrder(orderRequest)
                }
                exception.message shouldBe OrderCreateRequest.ExceptionMessage.QUANTITY_VALIDATION.message
            }
        }
    }

    Given("음수 금액으로 주문 요청이 주어졌을 때") {
        val orderRequest = OrderCreateRequest(
            userId = 1L,
            productName = "Test Product",
            quantity = 1,
            totalAmount = BigDecimal("-100.00")
        )

        When("주문을 생성하면") {
            Then("IllegalArgumentException이 발생해야 한다") {
                val exception = shouldThrow<IllegalArgumentException> {
                    orderService.saveOrder(orderRequest)
                }
                exception.message shouldBe OrderCreateRequest.ExceptionMessage.TOTAL_AMOUNT_VALIDATION.message
            }
        }
    }
})