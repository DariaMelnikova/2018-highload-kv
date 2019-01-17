package ru.mail.polis.dariam.controller;

import java.io.IOException;

import org.apache.commons.io.IOUtils;
import org.jetbrains.annotations.NotNull;

import com.sun.net.httpserver.HttpExchange;

import ru.mail.polis.dariam.IllegalIdException;
import ru.mail.polis.dariam.StorageContext;
import ru.mail.polis.dariam.replicahelpers.ReplicaParametersException;
import ru.mail.polis.dariam.replicahelpers.TopologyParameters;

public class QueryContext {

    private HttpExchange httpExchange;

    protected TopologyParameters topologyParameters;

    private final byte[] id;
    private final byte[] body;

    public QueryContext(HttpExchange httpExchange, StorageContext storageContext) throws IOException, IllegalIdException, ReplicaParametersException {
        this.httpExchange = httpExchange;
        this.body = IOUtils.toByteArray(httpExchange.getRequestBody());

        ServiceQueryParameters serviceQueryParameters = ServiceQueryParameters.parseQuery(httpExchange.getRequestURI().getQuery());
        this.topologyParameters = serviceQueryParameters.getReplicaParameters(storageContext.getReplicasHosts().size() + 1);
        this.id = serviceQueryParameters.getId();
    }

    @NotNull
    public byte[] getId() {
        return id;
    }

    public String getQuery() {
        return httpExchange.getRequestURI().getQuery();
    }

    protected byte[] getRequestBody() throws IOException {
        return body;
    }
}
