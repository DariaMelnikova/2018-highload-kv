package ru.mail.polis.dariam.controller;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import ru.mail.polis.dariam.MyService;
import ru.mail.polis.dariam.StorageContext;
import ru.mail.polis.dariam.replicahelpers.ReplicasCollection;
import ru.mail.polis.dariam.replicahelpers.ReplicaAnswerResult;
import ru.mail.polis.dariam.replicahelpers.ReplicaAnswerResults;

abstract public class BaseController {

    protected final StorageContext storageContext;

    public BaseController(StorageContext storageContext) {
        this.storageContext = storageContext;
    }

    protected URI sameQueryOnReplica(QueryContext queryContext, String replicaHost) throws URISyntaxException {
        return new URI(replicaHost + MyService.CONTEXT_ENTITY + "?" + queryContext.getQuery());
    }

    protected ReplicaAnswerResults forEachReplica(
            QueryContext queryContext,
            ForEachReplicaInQueryFromClient forEachReplica
    ) throws IOException {
        int from = queryContext.topologyParameters.getFrom();

        List<Future<ReplicaAnswerResult>> futures = new ArrayList<>(from);

        for (String replica : findReplicas(queryContext.getId()).subList(0, from)) {
            futures.add(forEachReplica.execute(replica));
        }

        List<ReplicaAnswerResult> listOfResults = new ArrayList<>(from);

        for (Future<ReplicaAnswerResult> future : futures){
            try {
                listOfResults.add(future.get());
            } catch (ExecutionException | InterruptedException e){
                throw new IOException(e);
            }
        }

        return new ReplicaAnswerResults(listOfResults);
    }

    private List<String> findReplicas(byte[] key){
        ReplicasCollection allReplicas = new ReplicasCollection(storageContext.getReplicasHosts());
        allReplicas.add(storageContext.getMyReplicaHost());
        List<String> listOfAllReplicas = Arrays.asList(allReplicas.toArray());

        int hash = new String(key).hashCode();
        listOfAllReplicas.sort(Comparator.comparingInt(string -> string.hashCode() ^ hash));

        return listOfAllReplicas;
    }

    interface ForEachReplicaInQueryFromClient{
        Future<ReplicaAnswerResult> execute(String replica) throws IOException;
    }
}
