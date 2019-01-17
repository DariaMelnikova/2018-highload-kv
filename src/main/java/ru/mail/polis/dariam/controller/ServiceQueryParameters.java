package ru.mail.polis.dariam.controller;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import ru.mail.polis.dariam.IllegalIdException;
import ru.mail.polis.dariam.replicahelpers.TopologyParameters;
import ru.mail.polis.dariam.replicahelpers.ReplicaParametersException;

public class ServiceQueryParameters {

    private final byte[] id;

    private final TopologyParameters topologyParameters;

    @NotNull
    public byte[] getId() {
        return id;
    }

    @NotNull
    public TopologyParameters getReplicaParameters(int replicasCount) throws ReplicaParametersException {
        if (topologyParameters == null){
            return new TopologyParameters(replicasCount / 2 + 1, replicasCount);
        } else if (
                topologyParameters.getFrom() > replicasCount ||
                topologyParameters.getAck() > topologyParameters.getFrom() ||
                topologyParameters.getAck() < 1){
            throw new ReplicaParametersException("Ack: " + topologyParameters.getAck() + ", getFrom: " + topologyParameters.getFrom());
        } else {
            return topologyParameters;
        }
    }

    public ServiceQueryParameters(byte[] id, TopologyParameters topologyParameters) {
        this.id = id;
        this.topologyParameters = topologyParameters;
    }

    public static ServiceQueryParameters parseQuery(@Nullable String query) throws IllegalIdException, IllegalArgumentException {
        if (query == null) {
            throw new IllegalIdException("id not specified");
        }

        byte[] id = null;
        TopologyParameters topologyParameters = null;

        String[] parameters = query.split("&");
        for (final String parameter : parameters){
            String[] parameterSplit = parameter.split("=");
            switch (parameterSplit[0]){
                case "id":
                    if (parameterSplit.length == 1){
                        throw new IllegalIdException("id key is null");
                    }
                    id = parameterSplit[1].getBytes();
                    break;
                case "replicas":
                    topologyParameters = TopologyParameters.fromQuery(parameterSplit[1]);
                    break;
            }
        }

        if (id == null) {
            throw new IllegalIdException("id not specified");
        }

        return new ServiceQueryParameters(id, topologyParameters);
    }
}
