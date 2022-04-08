package info.metadude.android.eventfahrplan.network.fetching;

import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.net.UnknownServiceException;

import javax.net.ssl.SSLException;
import javax.net.ssl.SSLHandshakeException;

import info.metadude.android.eventfahrplan.commons.logging.Logging;
import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class FetchFahrplan {

    public interface OnDownloadCompleteListener {

        void onGotResponse(@NonNull FetchScheduleResult fetchScheduleResult);
    }

    @NonNull
    private final Logging logging;

    @Nullable
    private FetchFahrplanTask task;

    private OnDownloadCompleteListener listener;

    public FetchFahrplan(@NonNull Logging logging) {
        this.logging = logging;
        task = null;
    }

    public void fetch(@NonNull OkHttpClient okHttpClient, String url, String eTag) {
        task = new FetchFahrplanTask(okHttpClient, logging, this.listener);
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

    @NonNull
    private final Logging logging;

    private String responseStr;

    @NonNull
    private String eTagStr = "";

    private final String LOG_TAG = "FetchFahrplan";

    private FetchFahrplan.OnDownloadCompleteListener listener;

    private boolean completed;

    private HttpStatus status;

    @NonNull
    private String host = "";

    @NonNull
    private String exceptionMessage = "";

    FetchFahrplanTask(
            @NonNull OkHttpClient okHttpClient,
            @NonNull Logging logging,
            FetchFahrplan.OnDownloadCompleteListener listener
    ) {
        this.okHttpClient = okHttpClient;
        this.logging = logging;
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

        //noinspection ConstantConditions
        host = Uri.parse(url).getHost();
        if (host == null) {
            throw new NullPointerException("Host is null for url = '" + url + "'");
        }

        return fetch(url, eTag);
    }

    protected void onCancelled() {
        logging.d(LOG_TAG, "Fetch cancelled");
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
            logging.d(LOG_TAG, "Fetch done successfully");
            listener.onGotResponse(new FetchScheduleResult(status, responseStr, eTagStr, host, exceptionMessage));
        } else {
            logging.d(LOG_TAG, "Fetch failed");
            listener.onGotResponse(new FetchScheduleResult(status, EMPTY_RESPONSE_STRING, eTagStr, host, exceptionMessage));
        }
        completed = false; // notify only once
    }

    private HttpStatus fetch(String url, String eTag) {
        logging.d(LOG_TAG, url);
        logging.d(LOG_TAG, "ETag: '" + eTag + "'");
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
            customizeExceptionMessage(e);
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
            e.printStackTrace();
            return HttpStatus.HTTP_COULD_NOT_CONNECT;
        }

        int statusCode = response.code();
        if (statusCode == 304) {
            return HttpStatus.HTTP_NOT_MODIFIED;
        }

        if (statusCode != 200) {
            logging.e(LOG_TAG, "Error " + statusCode + " while retrieving XML data");
            if (statusCode == 401) {
                return HttpStatus.HTTP_WRONG_HTTP_CREDENTIALS;
            }
            if (statusCode == 404) {
                return HttpStatus.HTTP_NOT_FOUND;
            }
            return HttpStatus.HTTP_COULD_NOT_CONNECT;
        }

        //noinspection ConstantConditions
        eTagStr = response.header("ETag");
        eTagStr = eTagStr == null ? "" : eTagStr;
        if (!eTagStr.isEmpty()) {
            logging.d(LOG_TAG, "ETag: '" + eTagStr + "'");
        } else {
            logging.d(LOG_TAG, "ETag missing?");
        }

        try {
            //noinspection ConstantConditions
            responseStr = response.body().string();
        } catch (NullPointerException | IOException e) {
            return HttpStatus.HTTP_CANNOT_PARSE_CONTENT;
        } finally {
            if (response.body() != null) {
                response.body().close();
            }
        }

        return HttpStatus.HTTP_OK;
    }

    @SuppressWarnings("ConstantConditions")
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
        if (exceptionMessage == null) {
            exceptionMessage = "";
        }
    }

    private void customizeExceptionMessage(@NonNull SSLException exception) {
        if (exception instanceof SSLHandshakeException && Build.VERSION.SDK_INT == Build.VERSION_CODES.N) {
            // See https://github.com/EventFahrplan/EventFahrplan/issues/431
            exceptionMessage += "\n\nPlease note that server certificates using elliptic curves " +
                    "with a length > 256 bits are not supported on Android 7.0. This might cause " +
                    "this error.";
        }
    }

}
