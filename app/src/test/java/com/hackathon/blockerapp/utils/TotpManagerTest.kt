tapackage com.hackathon.blockerapp.utils

import org.junit.Test
import org.junit.Assert.*

/**
 * TOTP Manager Unit Tests
 *
 * To run: Right-click this file > Run 'TotpManagerTest'
 */
class TotpManagerTest {

    @Test
    fun testSecretKeyGeneration() {
        val secret1 = TotpManager.generateSecretKey()
        val secret2 = TotpManager.generateSecretKey()

        // Secret keys should be non-empty
        assertTrue(secret1.isNotEmpty())
        assertTrue(secret2.isNotEmpty())

        // Secret keys should be different
        assertNotEquals(secret1, secret2)

        // Secret keys should be Base32 format (only A-Z and 2-7)
        assertTrue(secret1.matches(Regex("[A-Z2-7]+")))

        println("✓ Generated secret key: $secret1")
    }

    @Test
    fun testCodeGeneration() {
        val secret = "JBSWY3DPEHPK3PXP" // RFC 6238 test vector
        val code = TotpManager.generateCode(secret)

        // Code should be 6 digits
        assertEquals(6, code.length)
        assertTrue(code.matches(Regex("\\d{6}")))

        println("✓ Generated code: $code")
    }

    @Test
    fun testCodeVerification() {
        val secret = TotpManager.generateSecretKey()
        val code = TotpManager.generateCode(secret)

        // Current code should verify successfully
        assertTrue(TotpManager.verifyCode(secret, code))

        // Wrong code should fail
        assertFalse(TotpManager.verifyCode(secret, "000000"))
        assertFalse(TotpManager.verifyCode(secret, "999999"))

        println("✓ Code verification works")
    }

    @Test
    fun testRemainingSeconds() {
        val remaining = TotpManager.getRemainingSeconds()

        // Should be between 1 and 30 seconds
        assertTrue(remaining in 1..30)

        println("✓ Remaining seconds: $remaining")
    }

    @Test
    fun testCodeConsistency() {
        val secret = "JBSWY3DPEHPK3PXP"
        val code1 = TotpManager.generateCode(secret)
        Thread.sleep(100) // Wait 100ms
        val code2 = TotpManager.generateCode(secret)

        // Codes should be same within time window
        assertEquals(code1, code2)

        println("✓ Code consistency verified")
    }
}

