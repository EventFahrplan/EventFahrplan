package nerd.tuxmobil.fahrplan.congress.net

import com.google.common.truth.Truth.assertThat
import info.metadude.android.eventfahrplan.commons.temporal.DateFormatter
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.security.cert.X509Certificate
import java.util.Locale
import java.util.TimeZone

class CertificateChainFormatterTest {

    private companion object {

        /**
         * Milliseconds representation of March 1, 2020 01:00:00 AM GMT.
         */
        private const val MAR_01_2020_01_00 = 1583024400000L

        /**
         * Milliseconds representation of November 1, 2020 01:00:00 AM GMT
         */
        private const val NOV_01_2020_01_00 = 1604192400000

        /**
         * Milliseconds representation of December 1, 2020 01:00:00 AM GMT
         */
        private const val DEC_01_2020_01_00 = 1606784400000L

    }

    private val systemLocale = Locale.getDefault()
    private val systemTimezone = TimeZone.getDefault()

    @Before
    fun setUp() {
        // Fixate locale & time zone to prevent date formatting issues on certain systems.
        Locale.setDefault(Locale.US)
        TimeZone.setDefault(TimeZone.getTimeZone("GMT"))
    }

    @After
    fun tearDown() {
        Locale.setDefault(systemLocale)
        TimeZone.setDefault(systemTimezone)
    }

    @Test
    fun `getCertificateChainInfo returns empty string when passing empty collection`() {
        val formatter = createFormatter(emptyList())
        assertThat(formatter.getCertificateChainInfo()).isEqualTo("")
    }

    @Test
    fun `getCertificateChainInfo returns formatted info text block for single certificate and fingerprint`() {
        val expectedInfo = """
            Certificate chain[0]
            Subject: test.ccc.de
            Issuer: Let's certificate all the things
            Issued on: 3/1/20
            Expires on: 12/1/20
            SHA1 fingerprint: 95 36 C0 C8 6E 0B C5 AE 1F 3E 4B 37 58 D4 8E 3E DE 2F 67 E5

        """.trimIndent()
        val certificate = TestCertificate.of(
                subject = "test.ccc.de",
                issuer = "Let's certificate all the things",
                notBeforeTimeMs = MAR_01_2020_01_00,
                notAfterTimeMs = DEC_01_2020_01_00)
        val fingerprintByCertificate = certificate to "95 36 C0 C8 6E 0B C5 AE 1F 3E 4B 37 58 D4 8E 3E DE 2F 67 E5"
        val formatter = createFormatter(listOf(fingerprintByCertificate))
        assertThat(formatter.getCertificateChainInfo()).isEqualTo(expectedInfo)
    }

    @Test
    fun `getCertificateChainInfo returns formatted info text block for chain`() {
        val expectedInfo = """
            Certificate chain[0]
            Subject: test.ccc.de
            Issuer: Let's certificate all the things
            Issued on: 3/1/20
            Expires on: 12/1/20
            SHA1 fingerprint: 95 36 C0 C8 6E 0B C5 AE 1F 3E 4B 37 58 D4 8E 3E DE 2F 67 E5

            Certificate chain[1]
            Subject: test.eff.org
            Issuer: Let's free all the things
            Issued on: 3/1/20
            Expires on: 11/1/20
            SHA1 fingerprint: C4 36 C0 C8 6E 0B C5 AE 1F 3E 4B 37 58 D4 8E 3E DE 2F C3 AA

        """.trimIndent()
        val certificate1 = TestCertificate.of(
                subject = "test.ccc.de",
                issuer = "Let's certificate all the things",
                notBeforeTimeMs = MAR_01_2020_01_00,
                notAfterTimeMs = DEC_01_2020_01_00)
        val certificate2 = TestCertificate.of(
                subject = "test.eff.org",
                issuer = "Let's free all the things",
                notBeforeTimeMs = MAR_01_2020_01_00,
                notAfterTimeMs = NOV_01_2020_01_00)
        val formatter = createFormatter(listOf(
                certificate1 to "95 36 C0 C8 6E 0B C5 AE 1F 3E 4B 37 58 D4 8E 3E DE 2F 67 E5",
                certificate2 to "C4 36 C0 C8 6E 0B C5 AE 1F 3E 4B 37 58 D4 8E 3E DE 2F C3 AA"
        ))
        assertThat(formatter.getCertificateChainInfo()).isEqualTo(expectedInfo)
    }

    private fun createFormatter(fingerprintsByCertificates: List<Pair<X509Certificate, String>>) = CertificateChainFormatter(
            fingerprintsByCertificates = fingerprintsByCertificates,
            getHeadlineText = { chainIndex -> "Certificate chain[$chainIndex]" },
            getSubjectText = { subject -> "Subject: $subject" },
            getIssuerText = { issuer -> "Issuer: $issuer" },
            getIssuedOnText = { issuedOn -> "Issued on: $issuedOn" },
            getExpiresOnText = { expiresOn -> "Expires on: $expiresOn" },
            getSha1FingerprintText = { sha1 -> "SHA1 fingerprint: $sha1" },
            dateFormatter = DateFormatter.newInstance()
    )

}
