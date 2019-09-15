package info.metadude.android.eventfahrplan.network.fetching;

import android.net.Uri;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.net.UnknownServiceException;

import javax.net.ssl.SSLException;

import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class FetchFahrplan {

    public interface OnDownloadCompleteListener {

        void onGotResponse(@NonNull FetchScheduleResult fetchScheduleResult);
    }

    private FetchFahrplanTask task;

    private OnDownloadCompleteListener listener;

    public FetchFahrplan() {
        task = null;
    }

    public void fetch(@NonNull OkHttpClient okHttpClient, String url, String eTag) {
        task = new FetchFahrplanTask(okHttpClient, this.listener);
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

class FetchFahrplanTask extends AsyncTask<String, Void, HttpStatus> {

    private static final String EMPTY_RESPONSE_STRING = "";

    private final OkHttpClient okHttpClient;

    private String responseStr;

    private String eTagStr = "";

    private String LOG_TAG = "FetchFahrplan";

    private FetchFahrplan.OnDownloadCompleteListener listener;

    private boolean completed;

    private HttpStatus status;
    private String host;
    private String exceptionMessage = "";

    FetchFahrplanTask(@NonNull OkHttpClient okHttpClient, FetchFahrplan.OnDownloadCompleteListener listener) {
        this.okHttpClient = okHttpClient;
        this.listener = listener;
        this.completed = false;
    }

    public void setListener(FetchFahrplan.OnDownloadCompleteListener listener) {
        this.listener = listener;

        if (completed && listener != null) {
            notifyActivity();
        }
    }

    @Override
    protected HttpStatus doInBackground(String... args) {
        String url = args[0];
        String eTag = args[1];

        host = Uri.parse(url).getHost();

        return fetch(url, eTag);
    }

    protected void onCancelled() {
        Log.d(LOG_TAG, "fetch cancelled");
    }

    protected void onPostExecute(HttpStatus status) {
        completed = true;
        this.status = status;

        if (listener != null) {
            notifyActivity();
        }
    }

    private void notifyActivity() {
        if (status == HttpStatus.HTTP_OK) {
            Log.d(LOG_TAG, "fetch done successfully");
            listener.onGotResponse(new FetchScheduleResult(status, responseStr, eTagStr, host, exceptionMessage));
        } else {
            Log.d(LOG_TAG, "fetch failed");
            listener.onGotResponse(new FetchScheduleResult(status, EMPTY_RESPONSE_STRING, eTagStr, host, exceptionMessage));
        }
        completed = false; // notify only once
    }

    private HttpStatus fetch(String url, String eTag) {
        Log.d("Fetch", url);
        Log.d("Fetch", "ETag: " + eTag);
        Request.Builder requestBuilder = new Request.Builder()
                .url(url);

        if (!TextUtils.isEmpty(eTag)) {
            requestBuilder.addHeader("If-None-Match", eTag);
        }

        Response response;
        try {
            Call call = okHttpClient.newCall(requestBuilder.build());
            response = call.execute();
        } catch (SSLException e) {
            setExceptionMessage(e);
            e.printStackTrace();
            return HttpStatus.HTTP_LOGIN_FAIL_UNTRUSTED_CERTIFICATE;
        } catch (SocketTimeoutException e) {
            return HttpStatus.HTTP_CONNECT_TIMEOUT;
        } catch (UnknownHostException e) {
            e.printStackTrace();
            return HttpStatus.HTTP_DNS_FAILURE;
        } catch (UnknownServiceException e) {
            e.printStackTrace();
            return HttpStatus.HTTP_CLEARTEXT_NOT_PERMITTED;
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return HttpStatus.HTTP_COULD_NOT_CONNECT;
        }

        int statusCode = response.code();
        if (statusCode == 304) {
            return HttpStatus.HTTP_NOT_MODIFIED;
        }

        if (statusCode != 200) {
            Log.w("Fetch", "Error " + statusCode
                    + " while retrieving XML data");
            if (statusCode == 401) {
                return HttpStatus.HTTP_WRONG_HTTP_CREDENTIALS;
            }
            if (statusCode == 404) {
                return HttpStatus.HTTP_NOT_FOUND;
            }
            return HttpStatus.HTTP_COULD_NOT_CONNECT;
        }

        eTagStr = response.header("ETag");
        eTagStr = eTagStr == null ? "" : eTagStr;
        if (!eTagStr.isEmpty()) {
            Log.d(LOG_TAG, "ETag: " + eTagStr);
        } else {
            Log.d(LOG_TAG, "ETag missing?");
        }

        try {
            responseStr = response.body().string();
        } catch (IOException e) {
            return HttpStatus.HTTP_CANNOT_PARSE_CONTENT;
        }

        return HttpStatus.HTTP_OK;
    }

    private void setExceptionMessage(SSLException exception) {
        if (exception.getCause() == null) {
            exceptionMessage = exception.getMessage();
        } else {
            if (exception.getCause().getCause() == null) {
                exceptionMessage = exception.getCause().getMessage();
            } else {
                exceptionMessage = exception.getCause().getCause().getMessage();
            }
        }
    }


}
