package nerd.tuxmobil.fahrplan.camp11;

import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;

import javax.net.ssl.SSLException;

import org.apache.http.HttpVersion;
import org.apache.http.client.HttpClient;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.http.*;
import android.os.Build;
import android.util.Base64;
import android.util.Log;

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
		HTTP_CONNECT_TIMEOUT
	}
	
	private static String httpCredentials = "";
	private static SSLException lastSSLException = null;

	public static HttpClient createHttpClient(String addr, boolean secure, int https_port)
			throws KeyManagementException, NoSuchAlgorithmException {
		
		Log.d("CustomHttpClient", addr + " " + secure + " " + https_port);
		
		HttpClient client;
		
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO) {
			client = AndroidHttpClient.newInstance("FahrplanDroid");
	
			SchemeRegistry scheme = client.getConnectionManager()
					.getSchemeRegistry();
			scheme.unregister("https");
			scheme.register(new Scheme("https",
					new TrustedSocketFactory(addr, true), https_port));
		} else {
	        final HttpParams params = new BasicHttpParams();
	        HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
	        HttpProtocolParams.setContentCharset(params, "UTF-8");
	        HttpProtocolParams.setUserAgent(params, "FahrplanDroid");

	        final SchemeRegistry registry = new SchemeRegistry();
	        registry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
	        registry.register(new Scheme("https", new TrustedSocketFactory(addr, true), https_port));

	        final ThreadSafeClientConnManager manager =
	                new ThreadSafeClientConnManager(params, registry);

	        client = new DefaultHttpClient(manager, params);
		}

		httpCredentials = "";
		return client;
	}

	public static String getHttpCredentials(Context context) {
		if (httpCredentials.length() == 0) {
			StringBuilder auth = new StringBuilder();
			auth.append("user").append(":");
			auth.append("pass");

			StringBuilder sb = new StringBuilder();
			sb.append("Basic ");
			sb.append(Base64.encodeToString(auth.toString().getBytes(),
					Base64.DEFAULT));
			sb.deleteCharAt(sb.length() - 1); // \n am Ende löschen
			httpCredentials = sb.toString();
		}
		return httpCredentials;
	}
	
	public static void setSSLException(SSLException e) {
		lastSSLException = e;
	}
	
	public static SSLException getSSLException() {
		return lastSSLException;
	}
	
	public static void showErrorDialog(final Activity ctx, final int msgTitle, final int msgText, final Object... args) {
		new AlertDialog.Builder(ctx).setTitle(
				ctx.getString(msgTitle))
				.setMessage(ctx.getString(msgText, args))
				.setPositiveButton(ctx.getString(R.string.OK),
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int which) {
								//ctx.finish();
							}
						}).show();
	}

	public static String getAddr() {
		return "events.ccc.de";
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
		if (client == null) return;
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO) {
			((AndroidHttpClient)client).close(); 
		}
	}
	
	public static void showHttpError(final Activity ctx, MyApp global, HTTP_STATUS status) {
		switch (status) {
				case HTTP_LOGIN_FAIL_WRONG_PASSWORD:
					CustomHttpClient.showErrorDialog(ctx,
							R.string.dlg_err_connection_failed,
							R.string.dlg_err_failed_wrong_password, (Object) null);
					break;
				case HTTP_DNS_FAILURE:
					CustomHttpClient.showErrorDialog(ctx,
							R.string.dlg_err_connection_failed,
							R.string.dlg_err_failed_unknown_host, CustomHttpClient.getAddr());
					break;
				case HTTP_WRONG_HTTP_CREDENTIALS:
					CustomHttpClient.showErrorDialog(ctx,
							R.string.dlg_err_connection_failed,
							R.string.dlg_err_failed_wrong_http_credentials, (Object) null);
					break;
				case HTTP_CONNECT_TIMEOUT:
					CustomHttpClient.showErrorDialog(ctx,
							R.string.dlg_err_connection_failed,
							R.string.dlg_err_failed_timeout, (Object) null);
					break;
				case HTTP_COULD_NOT_CONNECT:
					CustomHttpClient.showErrorDialog(ctx,
							R.string.dlg_err_connection_failed,
							R.string.dlg_err_failed_connect_failure, (Object) null);
					break;
				case HTTP_ENTITY_ENCODING_FAILURE:
					CustomHttpClient.showErrorDialog(ctx,
							R.string.dlg_err_connection_failed,
							R.string.dlg_err_failed_encoding_failure, (Object) null);
					break;
				case HTTP_CANNOT_PARSE_CONTENT:
					CustomHttpClient.showErrorDialog(ctx,
							R.string.dlg_err_connection_failed,
							R.string.dlg_err_failed_parse_failure, (Object) null);
					break;
				case HTTP_SSL_SETUP_FAILURE:
					CustomHttpClient.showErrorDialog(ctx,
							R.string.dlg_err_connection_failed,
							R.string.dlg_err_failed_ssl_failure, (Object) null);
					break;
		}
	}
}
