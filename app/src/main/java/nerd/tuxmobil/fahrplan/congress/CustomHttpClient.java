package nerd.tuxmobil.fahrplan.congress;

import org.apache.http.client.HttpClient;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;

import android.app.Activity;
import android.net.http.AndroidHttpClient;
import android.widget.Toast;

import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;

import javax.net.ssl.SSLException;

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

    public static HttpClient createHttpClient(String addr, boolean secure, int https_port)
            throws KeyManagementException, NoSuchAlgorithmException {

        MyApp.LogDebug("CustomHttpClient", addr + " " + secure + " " + https_port);

        HttpClient client = AndroidHttpClient.newInstance("FahrplanDroid");

        SchemeRegistry scheme = client.getConnectionManager()
                .getSchemeRegistry();
        scheme.unregister("https");
        scheme.register(new Scheme("https",
                new TrustedSocketFactory(addr, true), https_port));

        return client;
    }

    public static void setSSLException(SSLException e) {
        lastSSLException = e;
    }

    public static SSLException getSSLException() {
        return lastSSLException;
    }

    public static String normalize_addr(String addr) {
        if (addr.contains(":")) {
            return addr.split(":")[0];
        }
        return addr;
    }

    public static int getHttpsPort() {
        int port;
        port = 443;
        return port;
    }

    public static void close(HttpClient client) {
        if (client != null) {
            ((AndroidHttpClient) client).close();
        }
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
