package nerd.tuxmobil.fahrplan.congress;

import nerd.tuxmobil.fahrplan.congress.TrustManagerFactory;

import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.conn.scheme.LayeredSocketFactory;
import org.apache.http.params.HttpParams;

//import android.util.Log;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.ArrayList;


// copied from K9-Mail


public class TrustedSocketFactory implements LayeredSocketFactory {
    private SSLSocketFactory mSocketFactory;
    private org.apache.http.conn.ssl.SSLSocketFactory mSchemeSocketFactory;

    public TrustedSocketFactory(String host, boolean secure) throws NoSuchAlgorithmException, KeyManagementException {
        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(null, new TrustManager[] {
                            TrustManagerFactory.get(host, secure)
                        }, new SecureRandom());
        mSocketFactory = sslContext.getSocketFactory();
        mSchemeSocketFactory = org.apache.http.conn.ssl.SSLSocketFactory.getSocketFactory();
        mSchemeSocketFactory.setHostnameVerifier(
            org.apache.http.conn.ssl.SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
    }

    public Socket connectSocket(Socket sock, String host, int port,
                                InetAddress localAddress, int localPort, HttpParams params)
    throws IOException, UnknownHostException, ConnectTimeoutException {
        return mSchemeSocketFactory.connectSocket(sock, host, port, localAddress, localPort, params);
    }

    public Socket createSocket() throws IOException {
        return mSocketFactory.createSocket();
    }

    public boolean isSecure(Socket sock) throws IllegalArgumentException {
        return mSchemeSocketFactory.isSecure(sock);
    }
    public Socket createSocket(
        final Socket socket,
        final String host,
        final int port,
        final boolean autoClose
    ) throws IOException, UnknownHostException {
        SSLSocket sslSocket = (SSLSocket) mSocketFactory.createSocket(
                                  socket,
                                  host,
                                  port,
                                  autoClose
                              );
        String supported_suites[] = sslSocket.getSupportedCipherSuites();
        String wanted_suites[] = { 	"TLS_RSA_WITH_AES_128_CBC_SHA", 
        		                   	"TLS_RSA_WITH_AES_256_CBC_SHA", 
        		                   	"TLS_DHE_RSA_WITH_AES_128_CBC_SHA",
									"TLS_DHE_RSA_WITH_AES_256_CBC_SHA",
									"TLS_DHE_DSS_WITH_AES_128_CBC_SHA",
									"TLS_DHE_DSS_WITH_AES_256_CBC_SHA"
						        };
        ArrayList<String> wanted_supported_suites = new ArrayList<String>();
        
        for (String suite:wanted_suites) {
//	        Log.d("TrustedSocketFactory", "ciphers: "+suite);
	        for (String supported:supported_suites) {
	        	if (suite.equals(supported)) { wanted_supported_suites.add(suite); }
	        }
        }
//        for (String suite:wanted_supported_suites.toArray(new String[]{})) {
//	        Log.d("TrustedSocketFactory", "using ciphers: "+suite);
//        }
        sslSocket.setEnabledCipherSuites(wanted_supported_suites.toArray(new String[]{}));
        //hostnameVerifier.verify(host, sslSocket);
        // verifyHostName() didn't blowup - good!
        return sslSocket;
    }
}
