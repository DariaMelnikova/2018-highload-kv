package ru.mail.polis.dariam.controller;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;

import ru.mail.polis.dariam.ActionResponse;
import ru.mail.polis.dariam.HttpHelpers;
import ru.mail.polis.dariam.IllegalIdException;
import ru.mail.polis.dariam.StorageContext;
import ru.mail.polis.dariam.replicahelpers.ReplicasCollection;
import ru.mail.polis.dariam.replicahelpers.ReplicaParametersException;

public class ServiceController {

    private final StorageContext storageContext;

    public ServiceController(StorageContext storageContext) {
        this.storageContext = storageContext;
    }

    public void execute(HttpExchange httpExchange, QueryProcessor queryProcessor) throws IOException, IllegalIdException, ReplicaParametersException {
        String fromStorage = httpExchange.getRequestHeaders().getFirst(HttpHelpers.HEADER_FROM_REPLICAS);
        ReplicasCollection fromReplicas = fromStorage != null
                ? new ReplicasCollection(Arrays.asList(fromStorage.split(",")))
                : new ReplicasCollection();

        QueryContext queryContext = new QueryContext(httpExchange, storageContext);

        ActionResponse response = fromReplicas.empty()
                ? queryProcessor.processQueryFromClient(queryContext)
                : queryProcessor.processQueryFromReplica(queryContext);

        sendResponse(httpExchange, response);
    }

    private void sendResponse(HttpExchange httpExchange, ActionResponse response) throws IOException {
        Headers responseHeaders = httpExchange.getResponseHeaders();
        if (response.isDeleted()) {
            responseHeaders.add(HttpHelpers.HEADER_VALUE_DELETED, "1");
        }

        Long timestamp = response.getTimestamp();
        if (timestamp != null) {
            responseHeaders.add(HttpHelpers.HEADER_TIMESTAMP, String.valueOf(timestamp));
        }

        byte[] responseData = response.getResponse();
        if (responseData != null) {
            OutputStream outputStream = httpExchange.getResponseBody();
            httpExchange.sendResponseHeaders(response.getStatus(), responseData.length);
            outputStream.write(responseData);
            outputStream.close();
        } else {
            httpExchange.sendResponseHeaders(response.getStatus(), 0);
        }

        httpExchange.close();
    }
}
