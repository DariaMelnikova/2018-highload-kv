package ru.mail.polis.dariam.httpclient;

import java.net.URI;

import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPut;
import org.jetbrains.annotations.NotNull;

public class HttpQueryCreator {
    private final HttpClientCreator httpClientCreator;

    public HttpQueryCreator() {
        httpClientCreator = new HttpClientCreator();
    }

    @NotNull
    public HttpQuery put(URI uri){
        return new HttpQuery(new HttpPut(uri), httpClientCreator);
    }

    @NotNull
    public HttpQuery get(URI uri){
        return new HttpQuery(new HttpGet(uri), httpClientCreator);
    }

    @NotNull
    public HttpQuery delete(URI uri){
        return new HttpQuery(new HttpDelete(uri), httpClientCreator);
    }

    public void stop() {
        httpClientCreator.stop();
    }

    public void start() {
        httpClientCreator.start();
    }
}
