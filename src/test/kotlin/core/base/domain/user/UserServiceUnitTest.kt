package core.base.domain.user

import core.base.repository.UserRepository
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.every
import io.mockk.mockk

class UserServiceUnitTest : BehaviorSpec({

    val userRepository = mockk<UserRepository>()


    Given("존재하는 사용자 이름이 주어졌을 때") {
//        val user = UserFixture.createUser()

        val testUser = mockk<User>(relaxed = true) {
            every { id } returns 1L
            every { userName } returns "test"
            every { address } returns "123 Main St"
            every { status } returns UserStatus.ACTIVE
        }

        every { userRepository.findByUserName("test") } returns testUser

        When("사용자 이름으로 사용자를 조회하면") {
            val foundUser = userRepository.findByUserName("test")

            Then("사용자가 반환되어야 한다") {
                foundUser shouldNotBe null
                foundUser?.id shouldBe 1L
                foundUser?.userName shouldBe "test"
            }
        }
    }

})