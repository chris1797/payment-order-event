package core.carrotkotlin.enums

enum class Role {
    USER,
    ADMIN;

    companion object {
        fun fromString(role: String): Role {
            return when (role.uppercase()) {
                "USER" -> USER
                "ADMIN" -> ADMIN
                else -> throw IllegalArgumentException("Unknown role: $role")
            }
        }
    }
}