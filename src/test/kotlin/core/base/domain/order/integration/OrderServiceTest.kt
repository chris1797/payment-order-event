package core.base.domain.order.integration

import core.base.api.request.OrderCreateRequest
import core.base.domain.order.OrderStatus
import core.base.domain.user.User
import core.base.domain.user.UserFixture
import core.base.repository.UserRepository
import core.base.service.OrderService
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal

@SpringBootTest
@Transactional
class OrderServiceTest {

    @Autowired
    private lateinit var orderService: OrderService

    @Autowired
    private lateinit var userRepository: UserRepository

    private lateinit var testUser: User

    @BeforeEach
    fun setUp() {
        testUser = UserFixture.createUser(name = "Test User")
        userRepository.save(testUser)
    }

    @Test
    @DisplayName("정상적인 주문 생성 - 주문이 성공적으로 생성되고 결제 처리됨")
    fun `주문 생성 성공 테스트`() {
        // given
        val orderRequest = OrderCreateRequest(
            userId = testUser.id!!,
            productName = "Test Product",
            quantity = 2,
            totalAmount = BigDecimal("300.00")
        )

        // when
        val orderResponse = orderService.saveOrder(orderRequest)

        // then
        Assertions.assertNotNull(orderResponse)
        Assertions.assertNotNull(orderResponse.id)
        Assertions.assertEquals(testUser.id, orderResponse.userId)
        Assertions.assertEquals(testUser.userName, orderResponse.userName)
        Assertions.assertEquals(BigDecimal("300.00"), orderResponse.totalAmount)
        Assertions.assertEquals(OrderStatus.COMPLETED, orderResponse.status)
    }

    @Test
    @DisplayName("존재하지 않는 사용자로 주문 생성 시도 - 예외 발생")
    fun `존재하지 않는 사용자 주문 실패 테스트`() {
        // given
        val nonExistentUserId = 999999L
        val orderRequest = OrderCreateRequest(
            userId = nonExistentUserId,
            productName = "Test Product",
            quantity = 1,
            totalAmount = BigDecimal("100.00")
        )

        // when & then
        assertThrows<NoSuchElementException> {
            orderService.saveOrder(orderRequest)
        }
    }

    @Test
    @DisplayName("빈 상품명으로 주문 생성 시도 - 검증 예외 발생")
    fun `빈 상품명 주문 실패 테스트`() {
        // given
        val orderRequest = OrderCreateRequest(
            userId = testUser.id!!,
            productName = "",
            quantity = 1,
            totalAmount = BigDecimal("100.00")
        )

        // when & then
        val exception = assertThrows<IllegalArgumentException> {
            orderService.saveOrder(orderRequest)
        }
        Assertions.assertEquals(OrderCreateRequest.ExceptionMessage.PRODUCT_NAME_VALIDATION.message, exception.message)
    }

    @Test
    @DisplayName("잘못된 수량(0 이하)으로 주문 생성 시도 - 검증 예외 발생")
    fun `잘못된 수량 주문 실패 테스트`() {
        // given
        val orderRequest = OrderCreateRequest(
            userId = testUser.id!!,
            productName = "Test Product",
            quantity = 0,
            totalAmount = BigDecimal("100.00")
        )

        // when & then
        val exception = assertThrows<IllegalArgumentException> {
            orderService.saveOrder(orderRequest)
        }
        Assertions.assertEquals(OrderCreateRequest.ExceptionMessage.QUANTITY_VALIDATION.message, exception.message)
    }

    @Test
    @DisplayName("잘못된 금액(0 이하)으로 주문 생성 시도 - 검증 예외 발생")
    fun `잘못된 금액 주문 실패 테스트`() {
        // given
        val orderRequest = OrderCreateRequest(
            userId = testUser.id!!,
            productName = "Test Product",
            quantity = 1,
            totalAmount = BigDecimal.ZERO
        )

        // when & then
        val exception = assertThrows<IllegalArgumentException> {
            orderService.saveOrder(orderRequest)
        }
        Assertions.assertEquals(OrderCreateRequest.ExceptionMessage.TOTAL_AMOUNT_VALIDATION.message, exception.message)
    }

    @Test
    @DisplayName("여러 건의 주문 생성 - 모두 정상 처리됨")
    fun `여러 주문 생성 성공 테스트`() {
        // given
        val orderRequest1 = OrderCreateRequest(
            userId = testUser.id!!,
            productName = "Product 1",
            quantity = 1,
            totalAmount = BigDecimal("100.00")
        )
        val orderRequest2 = OrderCreateRequest(
            userId = testUser.id!!,
            productName = "Product 2",
            quantity = 3,
            totalAmount = BigDecimal("450.00")
        )

        // when
        val orderResponse1 = orderService.saveOrder(orderRequest1)
        val orderResponse2 = orderService.saveOrder(orderRequest2)

        // then
        Assertions.assertNotNull(orderResponse1.id)
        Assertions.assertNotNull(orderResponse2.id)
        Assertions.assertNotEquals(orderResponse1.id, orderResponse2.id)
        Assertions.assertEquals(OrderStatus.COMPLETED, orderResponse1.status)
        Assertions.assertEquals(OrderStatus.COMPLETED, orderResponse2.status)
    }
}