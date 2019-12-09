package nerd.tuxmobil.fahrplan.congress.net;

import android.app.Activity;
import android.widget.Toast;

import nerd.tuxmobil.fahrplan.congress.BuildConfig;
import nerd.tuxmobil.fahrplan.congress.R;
import nerd.tuxmobil.fahrplan.congress.utils.AlertDialogHelper;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import okhttp3.logging.HttpLoggingInterceptor.Level;


public class CustomHttpClient {

    public static OkHttpClient createHttpClient() {
        OkHttpClient.Builder clientBuilder = new OkHttpClient.Builder();
        if (BuildConfig.DEBUG) {
            HttpLoggingInterceptor httpLoggingInterceptor = new HttpLoggingInterceptor();
            httpLoggingInterceptor.setLevel(Level.HEADERS);
            clientBuilder.addNetworkInterceptor(httpLoggingInterceptor);
        }
        return clientBuilder.build();
    }

    public static void showHttpError(final Activity ctx, HttpStatus status, String host) {
        switch (status) {
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
            case HTTP_NOT_FOUND:
                AlertDialogHelper.showErrorDialog(ctx,
                        R.string.dlg_err_connection_failed,
                        R.string.dlg_err_failed_not_found, (Object) null);
                break;
            case HTTP_CLEARTEXT_NOT_PERMITTED:
                AlertDialogHelper.showErrorDialog(ctx,
                        R.string.dlg_err_connection_failed,
                        R.string.dlg_err_failed_http_cleartext_not_permitted, (Object) null);
                break;
        }
    }
}
