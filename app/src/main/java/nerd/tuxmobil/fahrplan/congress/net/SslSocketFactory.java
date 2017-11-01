package nerd.tuxmobil.fahrplan.congress.net;

import android.os.Build;
import android.support.annotation.NonNull;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;

public class SslSocketFactory extends SSLSocketFactory {

    private static final String PROTOCOL_NAME_TLS = "TLS";
    private static final String PROTOCOL_NAME_TLS_1_1 = "TLSv1.1";
    private static final String PROTOCOL_NAME_TLS_1_2 = "TLSv1.2";

    private static final String[] ENABLED_PROTOCOLS = {PROTOCOL_NAME_TLS_1_1, PROTOCOL_NAME_TLS_1_2};

    private final SSLSocketFactory internalSslSocketFactory;

    @NonNull
    static SSLSocketFactory createSSLSocketFactory(@NonNull TrustManager trustManager)
            throws NoSuchAlgorithmException, KeyManagementException {
        SSLContext sslContext = SSLContext.getInstance(PROTOCOL_NAME_TLS);
        TrustManager[] trustManagers = {trustManager};
        sslContext.init(null, trustManagers, new SecureRandom());
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            return new SslSocketFactory(sslContext);
        } else {
            return sslContext.getSocketFactory();
        }
    }

    private SslSocketFactory(@NonNull SSLContext sslContext)
            throws KeyManagementException, NoSuchAlgorithmException {
        internalSslSocketFactory = sslContext.getSocketFactory();
    }

    @Override
    public String[] getDefaultCipherSuites() {
        return internalSslSocketFactory.getDefaultCipherSuites();
    }

    @Override
    public String[] getSupportedCipherSuites() {
        return internalSslSocketFactory.getSupportedCipherSuites();
    }

    @Override
    public Socket createSocket() throws IOException {
        return enableTlsOnSocket(internalSslSocketFactory.createSocket());
    }

    @Override
    public Socket createSocket(Socket socket, String host, int port, boolean autoClose) throws IOException {
        return enableTlsOnSocket(internalSslSocketFactory.createSocket(socket, host, port, autoClose));
    }

    @Override
    public Socket createSocket(String host, int port) throws IOException {
        return enableTlsOnSocket(internalSslSocketFactory.createSocket(host, port));
    }

    @Override
    public Socket createSocket(String host, int port, InetAddress localHost, int localPort) throws IOException {
        return enableTlsOnSocket(internalSslSocketFactory.createSocket(host, port, localHost, localPort));
    }

    @Override
    public Socket createSocket(InetAddress host, int port) throws IOException {
        return enableTlsOnSocket(internalSslSocketFactory.createSocket(host, port));
    }

    @Override
    public Socket createSocket(InetAddress address, int port, InetAddress localAddress, int localPort) throws IOException {
        return enableTlsOnSocket(internalSslSocketFactory.createSocket(address, port, localAddress, localPort));
    }

    private Socket enableTlsOnSocket(Socket socket) {
        if (socket != null && socket instanceof SSLSocket) {
            ((SSLSocket) socket).setEnabledProtocols(ENABLED_PROTOCOLS);
        }
        return socket;
    }

}
