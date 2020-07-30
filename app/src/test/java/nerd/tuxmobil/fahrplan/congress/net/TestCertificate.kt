package nerd.tuxmobil.fahrplan.congress.net

import java.security.Principal
import java.security.PublicKey
import java.security.cert.X509Certificate
import java.util.Date
import java.util.Objects

/**
 * X509 certificate for testing purposes.
 *
 * Use [TestCertificate.of] to create an instance.
 */
internal class TestCertificate private constructor(

        private val subject: String,
        private val issuer: String,
        private val notBeforeTimeMs: Long,
        private val notAfterTimeMs: Long

) : X509Certificate() {

    companion object {

        fun of(subject: String, issuer: String, notBeforeTimeMs: Long, notAfterTimeMs: Long) =
                TestCertificate(subject, issuer, notBeforeTimeMs, notAfterTimeMs)

    }

    private fun principalOf(principalName: String) = object : Principal {
        override fun getName() = principalName
        override fun toString() = name
    }

    override fun getSubjectDN() = principalOf(subject)
    override fun getIssuerDN() = principalOf(issuer)
    override fun getNotBefore() = Date(notBeforeTimeMs)
    override fun getNotAfter() = Date(notAfterTimeMs)
    override fun getEncoded(): ByteArray {
        val fakeHash = Objects.hash(subject, issuer, notBeforeTimeMs, notAfterTimeMs)
        return byteArrayOf(fakeHash.toByte())
    }

    override fun toString(): String {
        return "TestCertificate(" +
                "subjectDN=$subjectDN, " +
                "issuerDN=$issuerDN, " +
                "notBefore=$notBefore, " +
                "notAfter=$notAfter" +
                ")"
    }

    // Down from here: Not relevant for current test usage.

    override fun getPublicKey() = throw NotImplementedError()
    override fun getCriticalExtensionOIDs() = throw NotImplementedError()
    override fun getNonCriticalExtensionOIDs() = throw NotImplementedError()
    override fun getSubjectUniqueID() = throw NotImplementedError()
    override fun getSignature() = throw NotImplementedError()
    override fun getSigAlgName() = throw NotImplementedError()
    override fun getExtensionValue(oid: String?) = throw NotImplementedError()
    override fun getVersion() = throw NotImplementedError()
    override fun verify(key: PublicKey?) = throw NotImplementedError()
    override fun verify(key: PublicKey?, sigProvider: String?) = throw NotImplementedError()
    override fun getBasicConstraints() = throw NotImplementedError()
    override fun getSigAlgParams() = throw NotImplementedError()
    override fun getSigAlgOID() = throw NotImplementedError()
    override fun checkValidity() = throw NotImplementedError()
    override fun checkValidity(date: Date?) = throw NotImplementedError()
    override fun getTBSCertificate() = throw NotImplementedError()
    override fun getKeyUsage() = throw NotImplementedError()
    override fun hasUnsupportedCriticalExtension() = throw NotImplementedError()
    override fun getSerialNumber() = throw NotImplementedError()
    override fun getIssuerUniqueID() = throw NotImplementedError()
}