package nerd.tuxmobil.fahrplan.camp11;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.zip.GZIPInputStream;

import javax.net.ssl.SSLException;

import nerd.tuxmobil.fahrplan.camp11.CustomHttpClient.HTTP_STATUS;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;

import android.os.AsyncTask;
import android.util.Log;

interface response_callback {
	void onGotResponse(HTTP_STATUS status, String response);
}

public class FetchFahrplan {
	private fetcher task;
	
	public FetchFahrplan() {
		task = null;
	}

	public void fetch(response_callback cb, String arg, MyApp global) {
		task = new fetcher(cb, global);
		task.execute(arg);
	}
	
	public void cancel() {
		if (task != null) task.cancel(true);
	}
}

class fetcher extends AsyncTask<String, Void, HTTP_STATUS> {
	private response_callback callback;
	private MyApp global;
	private String responseStr;
	private String LOG_TAG = "FetchFahrplan";

	public fetcher(response_callback cb, MyApp global) {
		this.global = global;
		this.callback = cb;
	}

	protected HTTP_STATUS doInBackground(String... args) {
		String box = CustomHttpClient.getAddr();

		return fetchthis(box, args[0]);

	}

	protected void onCancelled() {
		Log.d(LOG_TAG, "fetch cancelled");
	}

	protected void onPostExecute(HTTP_STATUS status) {
		if (status == HTTP_STATUS.HTTP_OK) {
			Log.d(LOG_TAG, "fetch done successfully");
			this.callback.onGotResponse(status, responseStr);
		} else {
			Log.d(LOG_TAG, "fetch failed");
			this.callback.onGotResponse(status, null);
		}
	}

	private HTTP_STATUS fetchthis(String addr, String arg) {
		HttpClient client;
		try {
			client = CustomHttpClient.createHttpClient(CustomHttpClient
					.normalize_addr(addr), true, CustomHttpClient
					.getHttpsPort());
		} catch (KeyManagementException e1) {
			return HTTP_STATUS.HTTP_SSL_SETUP_FAILURE;
		} catch (NoSuchAlgorithmException e1) {
			return HTTP_STATUS.HTTP_SSL_SETUP_FAILURE;
		}

		String address = "https://" + addr + arg;

		Log.d("Fetch", address);
		HttpGet getRequest = new HttpGet(address);

		getRequest.addHeader("Accept-Encoding", "gzip");

		HttpResponse response = null;

		try {
			response = client.execute(getRequest);
		} catch (SSLException e) {
			CustomHttpClient.setSSLException(e);
			e.printStackTrace();
			CustomHttpClient.close(client);
			return HTTP_STATUS.HTTP_LOGIN_FAIL_UNTRUSTED_CERTIFICATE;
		} catch (SocketTimeoutException e) {
			CustomHttpClient.close(client);
			return HTTP_STATUS.HTTP_CONNECT_TIMEOUT;
		} catch (UnknownHostException e) {
			e.printStackTrace();
			CustomHttpClient.close(client);
			return HTTP_STATUS.HTTP_DNS_FAILURE;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			CustomHttpClient.close(client);
			return HTTP_STATUS.HTTP_COULD_NOT_CONNECT;
		}

		int statusCode = response.getStatusLine().getStatusCode();
		if (statusCode != HttpStatus.SC_OK) {
			Log.w("Fetch", "Error " + statusCode
					+ " while retrieving json data");
			CustomHttpClient.close(client);
			if (statusCode == 401)
				return HTTP_STATUS.HTTP_WRONG_HTTP_CREDENTIALS;
			return HTTP_STATUS.HTTP_COULD_NOT_CONNECT;
		}

		HttpEntity entity = response.getEntity();

		if (entity == null) {
			Log.w("Fetch", "empty response??");
			CustomHttpClient.close(client);
			return HTTP_STATUS.HTTP_CANNOT_PARSE_CONTENT;
		}

		InputStream instream;
		try {
			instream = entity.getContent();
		} catch (IllegalStateException e) {
			Log.e(LOG_TAG, "IllegalStateException getting content");
			CustomHttpClient.close(client);
			return HTTP_STATUS.HTTP_CANNOT_PARSE_CONTENT;
		} catch (IOException e) {
			Log.e(LOG_TAG, "IOException getting content");
			CustomHttpClient.close(client);
			return HTTP_STATUS.HTTP_CANNOT_PARSE_CONTENT;
		}
		Header contentEncoding = response.getFirstHeader("Content-Encoding");
		if (contentEncoding != null
				&& contentEncoding.getValue().equalsIgnoreCase("gzip")) {
			try {
				instream = new GZIPInputStream(instream);
			} catch (IOException e) {
				e.printStackTrace();
				CustomHttpClient.close(client);
				return HTTP_STATUS.HTTP_CANNOT_PARSE_CONTENT;
			}
		}
		BufferedReader reader = new BufferedReader(new InputStreamReader(
				instream));
		StringBuilder sb = new StringBuilder();

		String line = null;
		try {
			while ((line = reader.readLine()) != null) {
				sb.append(line).append("\n");
			}
		} catch (IOException e) {
			Log.e(LOG_TAG, "Exception reading line");
			CustomHttpClient.close(client);
			return HTTP_STATUS.HTTP_CANNOT_PARSE_CONTENT;
		}

		responseStr = sb.toString();

		// Log.d("Fetch", "got Response " + responseStr);
		try {
			instream.close();
		} catch (IOException e) {
			Log.e(LOG_TAG, "Exception closing instream");
			CustomHttpClient.close(client);
			return HTTP_STATUS.HTTP_CANNOT_PARSE_CONTENT;
		}

		/*
		 * try { json = new JSONObject(responseStr); } catch (JSONException e) {
		 * Log.e(LOG_TAG, "Exception on creating JSON object"); if (client !=
		 * null) { client.close(); } return
		 * HTTP_STATUS.HTTP_CANNOT_PARSE_CONTENT; }
		 */

		CustomHttpClient.close(client);
		return HTTP_STATUS.HTTP_OK;
	}

}