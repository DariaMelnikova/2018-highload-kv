package ru.mail.polis.dariam;


import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.NoSuchElementException;
import java.util.concurrent.Executors;

import org.apache.commons.io.IOUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.sun.net.httpserver.HttpServer;

import ru.mail.polis.KVDao;
import ru.mail.polis.KVService;

public class MyService implements KVService {
    public final static String CONTEXT_ENTITY = "/v0/entity";
    public final static String CONTEXT_STATUS = "/v0/status";

    @Nullable
    private HttpServer httpServer;

    @NotNull
    private final KVDao dao;

    private final int port;

    public MyService(int port, @NotNull KVDao dao) throws IOException {
        this.port = port;
        this.dao = dao;
    }

    private void createRouters(){
        try {

            this.httpServer.createContext(CONTEXT_STATUS, httpExchange -> {
                httpExchange.sendResponseHeaders(HttpHelpers.STATUS_SUCCESS, 0);
                httpExchange.close();
            });

            this.httpServer.createContext(CONTEXT_ENTITY, httpExchange -> {
                try {
                    byte[] id = getId(httpExchange.getRequestURI().getQuery());

                    switch (httpExchange.getRequestMethod()) {
                        case "GET":
                            try {
                                byte[] value = dao.get(id);
                                httpExchange.sendResponseHeaders(HttpHelpers.STATUS_SUCCESS_GET, value.length);
                                IOUtils.write(value, httpExchange.getResponseBody());
                            } catch (NoSuchElementException e) {
                                httpExchange.sendResponseHeaders(HttpHelpers.STATUS_NOT_FOUND, 0);
                            } finally {
                                httpExchange.getResponseBody().close();
                                httpExchange.close();
                            }
                            break;

                        case "PUT":
                            try {
                                dao.upsert(id, IOUtils.toByteArray(httpExchange.getRequestBody()));
                                httpExchange.sendResponseHeaders(HttpHelpers.STATUS_SUCCESS_PUT, 0);
                            } finally {
                                httpExchange.close();
                            }
                            break;

                        case "DELETE":
                            try {
                                dao.remove(id);
                            } finally {
                                httpExchange.sendResponseHeaders(HttpHelpers.STATUS_SUCCESS_DELETE, 0);
                                httpExchange.close();
                            }
                            break;

                        default:
                            httpExchange.sendResponseHeaders(HttpHelpers.STATUS_NOT_FOUND, 0);
                            httpExchange.getResponseBody().close();
                            httpExchange.close();
                    }
                } catch (IllegalIdException e){
                    httpExchange.sendResponseHeaders(HttpHelpers.STATUS_BAD_ARGUMENT, 0);
                    httpExchange.close();
                } catch (IOException e) {
                    httpExchange.sendResponseHeaders(HttpHelpers.STATUS_INTERNAL_ERROR, 0);
                    httpExchange.getResponseBody().close();
                    httpExchange.close();
                }
            });

            this.httpServer.createContext("/", httpExchange -> {
                httpExchange.sendResponseHeaders(HttpHelpers.STATUS_BAD_ARGUMENT, 0);
                httpExchange.close();
            });
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    private byte[] getId(String query) throws IllegalIdException {
        if (query == null) {
            throw new IllegalIdException();
        }

        String[] queryParams = query.split("&");
        for (String queryParam : queryParams) {
            String[] parts = queryParam.split("=", 2);
            if (parts[0].equals("id") && parts.length == 2 && parts[1].length() > 0) {
                return parts[1].getBytes();
            }
        }
        throw new IllegalIdException();
    }

    @Override
    public void start() {
        try {
            httpServer = HttpServer.create(new InetSocketAddress(port), 0);
            httpServer.setExecutor(Executors.newFixedThreadPool(8));
            httpServer.start();
            createRouters();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void stop() {
        try {
            dao.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (httpServer != null) {
            httpServer.stop(0);
        }
    }
}