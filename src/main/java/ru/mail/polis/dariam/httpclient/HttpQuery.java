package ru.mail.polis.dariam.httpclient;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import org.apache.commons.io.IOUtils;
import org.apache.http.Header;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.InputStreamEntity;

import ru.mail.polis.dariam.HttpHelpers;
import ru.mail.polis.dariam.replicahelpers.ReplicasCollection;

public class HttpQuery {
    private HttpClientCreator clientPool;
    private HttpRequestBase request;

    HttpQuery(HttpRequestBase request, HttpClientCreator clientPool) {
        this.request = request;
        this.clientPool = clientPool;
    }

    public HttpQueryResult execute() throws IOException {
        CloseableHttpResponse response = null;

        HttpQueryResult httpQueryResult;

        try {
            response = clientPool.execute(request);

            Header timestampHeader = response.getFirstHeader(HttpHelpers.HEADER_TIMESTAMP);

            httpQueryResult = new HttpQueryResult(
                    response.getStatusLine().getStatusCode(),
                    IOUtils.toByteArray(response.getEntity().getContent()),
                    response.getFirstHeader(HttpHelpers.HEADER_VALUE_DELETED) != null,
                    timestampHeader == null ? null : Long.valueOf(timestampHeader.getValue())
            );
        } finally {
            request.releaseConnection();
            if (response != null) {
                response.close();
            }

        }

        return httpQueryResult;
    }

    public HttpQuery withReplicas(ReplicasCollection replicas){
        request.addHeader(HttpHelpers.HEADER_FROM_REPLICAS, replicas.toLine());
        return this;
    }

    public HttpQuery withTimestamp(long timestamp){
        request.addHeader(HttpHelpers.HEADER_TIMESTAMP, String.valueOf(timestamp));
        return this;
    }

    public HttpQuery withBody(byte[] value) throws IOException {
        if (this.request instanceof HttpEntityEnclosingRequest) {
            ((HttpEntityEnclosingRequest) this.request).setEntity(new InputStreamEntity(new ByteArrayInputStream(value), (long)value.length));
        } else {
            throw new IllegalStateException(this.request.getMethod() + " request cannot enclose an entity");
        }
        return this;
    }
}
