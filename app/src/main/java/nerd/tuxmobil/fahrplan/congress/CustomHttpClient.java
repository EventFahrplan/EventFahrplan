package nerd.tuxmobil.fahrplan.congress;

import com.squareup.okhttp.OkHttpClient;

import android.app.Activity;
import android.widget.Toast;

import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;


public class CustomHttpClient {

    enum HTTP_STATUS {
        HTTP_OK,
        HTTP_LOGIN_FAIL_UNTRUSTED_CERTIFICATE,
        HTTP_LOGIN_FAIL_WRONG_PASSWORD,
        HTTP_DNS_FAILURE,
        HTTP_COULD_NOT_CONNECT,
        HTTP_SSL_SETUP_FAILURE,
        HTTP_CANNOT_PARSE_CONTENT,
        HTTP_ENTITY_ENCODING_FAILURE,
        HTTP_WRONG_HTTP_CREDENTIALS,
        HTTP_CONNECT_TIMEOUT,
        HTTP_CANCELLED,
        HTTP_NOT_MODIFIED
    }

    private static SSLException lastSSLException = null;

    public static OkHttpClient createHttpClient(String host)
            throws KeyManagementException, NoSuchAlgorithmException {

        OkHttpClient client = new OkHttpClient();
        client.setSslSocketFactory(createSSLSocketFactory(host));

        return client;
    }

    private static SSLSocketFactory createSSLSocketFactory(String host)
            throws NoSuchAlgorithmException, KeyManagementException {

        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(null, new TrustManager[] {
                TrustManagerFactory.get(host, true)
        }, new SecureRandom());

        return sslContext.getSocketFactory();
    }

    public static void setSSLException(SSLException e) {
        lastSSLException = e;
    }

    public static SSLException getSSLException() {
        return lastSSLException;
    }

    public static void showHttpError(final Activity ctx, MyApp global, HTTP_STATUS status, String host) {
        switch (status) {
            case HTTP_LOGIN_FAIL_WRONG_PASSWORD:
                AlertDialogHelper.showErrorDialog(ctx,
                        R.string.dlg_err_connection_failed,
                        R.string.dlg_err_failed_wrong_password, (Object) null);
                break;
            case HTTP_DNS_FAILURE:
                AlertDialogHelper.showErrorDialog(ctx,
                        R.string.dlg_err_connection_failed,
                        R.string.dlg_err_failed_unknown_host,
                        host);
                break;
            case HTTP_WRONG_HTTP_CREDENTIALS:
                AlertDialogHelper.showErrorDialog(ctx,
                        R.string.dlg_err_connection_failed,
                        R.string.dlg_err_failed_wrong_http_credentials, (Object) null);
                break;
            case HTTP_CONNECT_TIMEOUT:
                AlertDialogHelper.showErrorDialog(ctx,
                        R.string.dlg_err_connection_failed,
                        R.string.dlg_err_failed_timeout, (Object) null);
                break;
            case HTTP_COULD_NOT_CONNECT:
                AlertDialogHelper.showErrorDialog(ctx,
                        R.string.dlg_err_connection_failed,
                        R.string.dlg_err_failed_connect_failure, (Object) null);
                break;
            case HTTP_ENTITY_ENCODING_FAILURE:
                AlertDialogHelper.showErrorDialog(ctx,
                        R.string.dlg_err_connection_failed,
                        R.string.dlg_err_failed_encoding_failure, (Object) null);
                break;
            case HTTP_CANNOT_PARSE_CONTENT:
                AlertDialogHelper.showErrorDialog(ctx,
                        R.string.dlg_err_connection_failed,
                        R.string.dlg_err_failed_parse_failure, (Object) null);
                break;
            case HTTP_SSL_SETUP_FAILURE:
                AlertDialogHelper.showErrorDialog(ctx,
                        R.string.dlg_err_connection_failed,
                        R.string.dlg_err_failed_ssl_failure, (Object) null);
                break;
            case HTTP_NOT_MODIFIED:
                Toast.makeText(ctx, R.string.uptodate, Toast.LENGTH_SHORT).show();
                break;
        }
    }
}
