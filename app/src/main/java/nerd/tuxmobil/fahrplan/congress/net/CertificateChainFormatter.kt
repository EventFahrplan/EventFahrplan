package nerd.tuxmobil.fahrplan.congress.net

import android.content.Context
import androidx.annotation.VisibleForTesting
import info.metadude.android.eventfahrplan.commons.temporal.DateFormatter
import nerd.tuxmobil.fahrplan.congress.R
import java.security.cert.X509Certificate

/**
 * Formats the given chain of certificates and its associated fingerprints to be displayed to the
 * user in a readable format.
 *
 * The [pairs of certificates and fingerprints][fingerprintsByCertificates] are expected to be passed
 * in the original order of the certificates in the chain.
 */
internal class CertificateChainFormatter @VisibleForTesting constructor(

        private val fingerprintsByCertificates: List<Pair<X509Certificate, String>>,
        private val getHeadlineText: (Int) -> String,
        private val getSubjectText: (String) -> String,
        private val getIssuerText: (String) -> String,
        private val getIssuedOnText: (String) -> String,
        private val getExpiresOnText: (String) -> String,
        private val getSha1FingerprintText: (String) -> String,
        private val dateFormatter: DateFormatter = DateFormatter.newInstance()

) {

    companion object {

        @JvmStatic
        @JvmOverloads
        fun getNewInstance(
                fingerprintsByCertificates: List<Pair<X509Certificate, String>>,
                context: Context,
                dateFormatter: DateFormatter = DateFormatter.newInstance()
        ) = CertificateChainFormatter(
                fingerprintsByCertificates = fingerprintsByCertificates,
                getHeadlineText = { chainIndex: Int -> context.getString(R.string.certificate_info_headline, chainIndex) },
                getSubjectText = { subject: String -> context.getString(R.string.certificate_info_subject, subject) },
                getIssuerText = { issuer: String -> context.getString(R.string.certificate_info_issuer, issuer) },
                getIssuedOnText = { issuedOn: String -> context.getString(R.string.certificate_info_issued_on, issuedOn) },
                getExpiresOnText = { expiredOn: String -> context.getString(R.string.certificate_info_expires_on, expiredOn) },
                getSha1FingerprintText = { sha1: String -> context.getString(R.string.certificate_info_sha1_fingerprint, sha1) },
                dateFormatter = dateFormatter
        )

    }

    /**
     * Returns a formatted info text or an empty string if no certificates are present.
     */
    fun getCertificateChainInfo(): String {
        val chainLength = fingerprintsByCertificates.size
        if (chainLength > 0) {
            val buffer = StringBuffer(100)
            var index = 0
            for ((certificate, fingerprint) in fingerprintsByCertificates) {
                buffer.appendCertificateInfo(index, certificate, fingerprint)
                if (index + 1 < chainLength) {
                    buffer.append("\n")
                }
                index++
            }
            return buffer.toString()
        }
        return ""
    }

    private fun StringBuffer.appendCertificateInfo(index: Int, certificate: X509Certificate, fingerprint: String) {
        append(getHeadlineText(index)).append("\n")
        append(getSubjectText(certificate.subjectDN.toString())).append("\n")
        append(getIssuerText(certificate.issuerDN.toString())).append("\n")
        val issuedOn = dateFormatter.getFormattedDate(certificate.notBefore)
        val expiredOn = dateFormatter.getFormattedDate(certificate.notAfter)
        append(getIssuedOnText(issuedOn)).append("\n")
        append(getExpiresOnText(expiredOn)).append("\n")
        append(getSha1FingerprintText(fingerprint)).append("\n")
    }

}
