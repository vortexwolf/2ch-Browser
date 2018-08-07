package ua.in.quireg.chan.di;

import android.annotation.SuppressLint;
import android.util.Log;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import dagger.Module;
import dagger.Provides;
import okhttp3.Cache;
import okhttp3.Credentials;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import timber.log.Timber;
import ua.in.quireg.chan.common.MainApplication;
import ua.in.quireg.chan.settings.ApplicationSettings;

@Module
public class NetModule {

    @Provides
    @AppScope
    OkHttpClient provideOkHttpClient(ApplicationSettings applicationSettings, Cache cache) {

        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        builder.retryOnConnectionFailure(true);
        builder.cache(cache);

        if (applicationSettings.isUseProxy()) {

            ApplicationSettings.ProxySettings proxySettings = applicationSettings.getProxySettings();

            if (!proxySettings.getServer().isEmpty() && !(proxySettings.getPort() == -1)) {
                builder.proxy(new Proxy(Proxy.Type.HTTP, InetSocketAddress.createUnresolved(proxySettings.getServer(), proxySettings.getPort())));
            }

            if (proxySettings.isUseAuth()) {
                builder.proxyAuthenticator((route, response) -> {
                    Request.Builder newRequest = response.request().newBuilder();
                    return newRequest
                            .header("Proxy-Authorization", Credentials.basic(proxySettings.getLogin(), proxySettings.getPassword()))
                            .build();
                });
            }
        }

        if (applicationSettings.isUnsafeSSL()) {
            try {
                // Create a trust manager that does not validate certificate chains
                @SuppressLint("TrustAllX509TrustManager") final TrustManager[] trustAllCerts = new TrustManager[]{
                        new X509TrustManager() {
                            @Override
                            public void checkClientTrusted(java.security.cert.X509Certificate[] chain,
                                                           String authType) throws CertificateException {
                            }

                            @Override
                            public void checkServerTrusted(java.security.cert.X509Certificate[] chain,
                                                           String authType) throws CertificateException {
                            }

                            @Override
                            public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                                return new X509Certificate[0];
                            }
                        }
                };
                // Install the all-trusting trust manager
                final SSLContext sslContext = SSLContext.getInstance("SSL");
                sslContext.init(null, trustAllCerts, new java.security.SecureRandom());
                // Create an ssl socket factory with our all-trusting manager
                final SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();

                builder.sslSocketFactory(sslSocketFactory, (X509TrustManager) trustAllCerts[0]);
                builder.hostnameVerifier((hostname, session) -> true);

            } catch (NoSuchAlgorithmException | KeyManagementException e) {
                e.printStackTrace();
            }
        }

        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BASIC);
        builder.addInterceptor(logging);

        return builder.build();
    }

    @Provides
    @AppScope
    Cache provideOkHttpCache(MainApplication application) {
        int cacheSize = 10 * 1024 * 1024; // 10 MiB
        return new Cache(application.getCacheDir(), cacheSize);
    }

}