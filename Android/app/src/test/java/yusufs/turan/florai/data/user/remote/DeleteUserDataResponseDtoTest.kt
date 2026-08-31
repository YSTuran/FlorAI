package yusufs.turan.florai.data.user.remote

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DeleteUserDataResponseDtoTest {
    @Test
    fun toDomainMapsDeletedCountAndAuthState() {
        val result = DeleteUserDataResponseDto(
            deletedCount = 4,
            authDeleted = true
        ).toDomain()

        assertEquals(4, result.deletedCount)
        assertTrue(result.authDeleted)
    }

    @Test
    fun toDomainUsesFalseWhenAuthDeletedIsMissing() {
        val result = DeleteUserDataResponseDto(
            deletedCount = 2,
            authDeleted = null
        ).toDomain()

        assertEquals(2, result.deletedCount)
        assertFalse(result.authDeleted)
    }
}
