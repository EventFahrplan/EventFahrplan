package nerd.tuxmobil.fahrplan.congress;

import java.security.cert.CertificateException;

public class CertificateDomainMismatchException extends CertificateException {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    public CertificateDomainMismatchException(String message) {
        super(message);
    }
}
