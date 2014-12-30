package nerd.tuxmobil.fahrplan.congress;

import android.net.Uri;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.Log;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;

import com.squareup.okhttp.Call;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request.Builder;
import com.squareup.okhttp.Response;
import javax.net.ssl.SSLException;

import nerd.tuxmobil.fahrplan.congress.CustomHttpClient.HTTP_STATUS;

interface OnDownloadCompleteListener {

    public void onGotResponse(HTTP_STATUS status, String response, String eTagStr, String host);
}

public class FetchFahrplan {

    private fetcher task;

    private OnDownloadCompleteListener listener;

    public FetchFahrplan() {
        task = null;
        MyApp.fetcher = this;
    }

    public void fetch(String url, String eTag) {
        task = new fetcher(this.listener);
        task.execute(url, eTag);
    }

    public void cancel() {
        if (task != null) {
            task.cancel(true);
        }
    }

    public void setListener(OnDownloadCompleteListener listener) {
        this.listener = listener;
        if (task != null) {
            task.setListener(listener);
        }
    }
}

class fetcher extends AsyncTask<String, Void, HTTP_STATUS> {

    private String responseStr;

    private String eTagStr;

    private String LOG_TAG = "FetchFahrplan";

    private OnDownloadCompleteListener listener;

    private boolean completed;

    private HTTP_STATUS status;
    private String host;

    public fetcher(OnDownloadCompleteListener listener) {
        this.listener = listener;
        this.completed = false;
    }

    public void setListener(OnDownloadCompleteListener listener) {
        this.listener = listener;

        if (completed && (listener != null)) {
            notifyActivity();
        }
    }

    protected HTTP_STATUS doInBackground(String... args) {
        String url = args[0];
        String eTag = args[1];

        host = Uri.parse(url).getHost();

        return fetchthis(url, eTag);
    }

    protected void onCancelled() {
        MyApp.LogDebug(LOG_TAG, "fetch cancelled");
    }

    protected void onPostExecute(HTTP_STATUS status) {
        completed = true;
        this.status = status;

        if (listener != null) {
            notifyActivity();
        }
    }

    private void notifyActivity() {
        if (status == HTTP_STATUS.HTTP_OK) {
            MyApp.LogDebug(LOG_TAG, "fetch done successfully");
            listener.onGotResponse(status, responseStr, eTagStr, host);
        } else {
            MyApp.LogDebug(LOG_TAG, "fetch failed");
            listener.onGotResponse(status, null, eTagStr, host);
        }
        completed = false;                // notifiy only once
    }

    private HTTP_STATUS fetchthis(String url, String eTag) {
        OkHttpClient client;
        try {
            client = CustomHttpClient.createHttpClient(host);
        } catch (KeyManagementException e1) {
            return HTTP_STATUS.HTTP_SSL_SETUP_FAILURE;
        } catch (NoSuchAlgorithmException e1) {
            return HTTP_STATUS.HTTP_SSL_SETUP_FAILURE;
        }

        MyApp.LogDebug("Fetch", url);
        MyApp.LogDebug("Fetch", "ETag: " + eTag);
        Builder requestBuilder = new Builder().url(url);

        if (!TextUtils.isEmpty(eTag)) {
            requestBuilder.addHeader("If-None-Match", eTag);
        }

        Response response;
        try {
            Call call = client.newCall(requestBuilder.build());
            response = call.execute();
        } catch (SSLException e) {
            CustomHttpClient.setSSLException(e);
            e.printStackTrace();
            return HTTP_STATUS.HTTP_LOGIN_FAIL_UNTRUSTED_CERTIFICATE;
        } catch (SocketTimeoutException e) {
            return HTTP_STATUS.HTTP_CONNECT_TIMEOUT;
        } catch (UnknownHostException e) {
            e.printStackTrace();
            return HTTP_STATUS.HTTP_DNS_FAILURE;
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return HTTP_STATUS.HTTP_COULD_NOT_CONNECT;
        }

        int statusCode = response.code();
        if (statusCode == 304) {
            return HTTP_STATUS.HTTP_NOT_MODIFIED;
        }

        if (statusCode != 200) {
            Log.w("Fetch", "Error " + statusCode
                    + " while retrieving XML data");
            if (statusCode == 401) {
                return HTTP_STATUS.HTTP_WRONG_HTTP_CREDENTIALS;
            }
            return HTTP_STATUS.HTTP_COULD_NOT_CONNECT;
        }

        eTagStr = response.header("ETag");
        if (eTagStr != null) {
            MyApp.LogDebug(LOG_TAG, "ETag: " + eTagStr);
        } else {
            MyApp.LogDebug(LOG_TAG, "ETag missing?");
        }

        try {
            responseStr = response.body().string();
        } catch (IOException e) {
            return HTTP_STATUS.HTTP_CANNOT_PARSE_CONTENT;
        }

        return HTTP_STATUS.HTTP_OK;
    }

}
