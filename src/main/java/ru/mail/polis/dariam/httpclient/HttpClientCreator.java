package ru.mail.polis.dariam.httpclient;

import java.io.IOException;

import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;

public class HttpClientCreator {
    private static final int CONNECT_TIMEOUT = 700;
    private static final int MAX_TOTAL = 400;
    private static final int MAX_PER_ROUTE = 100;

    private HttpClientBuilder httpClientBuilder;
    private CloseableHttpClient client;

    public void start(){
        RequestConfig.Builder requestBuilder = RequestConfig.custom()
                .setConnectTimeout(CONNECT_TIMEOUT);

        PoolingHttpClientConnectionManager syncConnectionManager = new PoolingHttpClientConnectionManager();
        syncConnectionManager.setMaxTotal(MAX_TOTAL);
        syncConnectionManager.setDefaultMaxPerRoute(MAX_PER_ROUTE);

        httpClientBuilder = HttpClients.custom()
                .disableAutomaticRetries()
                .setConnectionManager(syncConnectionManager)
                .setDefaultRequestConfig(requestBuilder.build());

        client = httpClientBuilder.build();
    }

    CloseableHttpResponse execute(HttpRequestBase requestBase) throws IOException {
        return client.execute(requestBase);
    }

    public void stop() {

    }
}
