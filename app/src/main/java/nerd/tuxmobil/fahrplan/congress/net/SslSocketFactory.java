package nerd.tuxmobil.fahrplan.congress.net;

import android.support.annotation.NonNull;

import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;

class SslSocketFactory {

    private static final String PROTOCOL_NAME_TLS = "TLS";

    @NonNull
    static SSLSocketFactory createSSLSocketFactory(@NonNull TrustManager trustManager)
            throws NoSuchAlgorithmException, KeyManagementException {
        SSLContext sslContext = SSLContext.getInstance(PROTOCOL_NAME_TLS);
        TrustManager[] trustManagers = {trustManager};
        sslContext.init(null, trustManagers, new SecureRandom());
        return sslContext.getSocketFactory();
    }

}
