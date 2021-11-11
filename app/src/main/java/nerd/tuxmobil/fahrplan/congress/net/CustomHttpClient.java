package nerd.tuxmobil.fahrplan.congress.net;

import nerd.tuxmobil.fahrplan.congress.BuildConfig;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import okhttp3.logging.HttpLoggingInterceptor.Level;


public class CustomHttpClient {

    public static OkHttpClient createHttpClient() {
        OkHttpClient.Builder clientBuilder = new OkHttpClient.Builder();
        String userAgent = BuildConfig.APPLICATION_ID + ", " + BuildConfig.VERSION_NAME;
        clientBuilder.addNetworkInterceptor(new UserAgentInterceptor(userAgent));
        if (BuildConfig.DEBUG) {
            HttpLoggingInterceptor httpLoggingInterceptor = new HttpLoggingInterceptor();
            httpLoggingInterceptor.setLevel(Level.HEADERS);
            clientBuilder.addNetworkInterceptor(httpLoggingInterceptor);
        }
        return clientBuilder.build();
    }

}
