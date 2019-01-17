package ru.mail.polis.dariam.controller;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Collections;

import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.conn.HttpHostConnectException;

import ru.mail.polis.dariam.ActionResponse;
import ru.mail.polis.dariam.HttpHelpers;
import ru.mail.polis.dariam.StorageContext;
import ru.mail.polis.dariam.httpclient.HttpQueryResult;
import ru.mail.polis.dariam.replicahelpers.ReplicasCollection;
import ru.mail.polis.dariam.replicahelpers.ReplicaAnswerResult;
import ru.mail.polis.dariam.replicahelpers.ReplicaAnswerResults;

public class PutController extends BaseController implements QueryProcessor {

    public PutController(StorageContext storageContext) {
        super(storageContext);
    }

    @Override
    public ActionResponse processQueryFromReplica(QueryContext queryContext) throws IOException {
        storageContext.getDao().upsert(queryContext.getId(), queryContext.getRequestBody());
        return new ActionResponse(HttpHelpers.STATUS_SUCCESS_PUT);
    }

    @Override
    public ActionResponse processQueryFromClient(QueryContext queryContext) throws IOException {
        final long timestamp = System.currentTimeMillis();

        byte[] value = queryContext.getRequestBody();
        String myReplicaHost = storageContext.getMyReplicaHost();

        ThreadPoolReplicasQuerys threadPool = storageContext.getThreadPool();

        ReplicaAnswerResults results = forEachReplica(queryContext, replicaHost -> {
            if (replicaHost.equals(myReplicaHost)) {
                return threadPool.addWork(() -> {
                    ReplicaAnswerResult result = new ReplicaAnswerResult(myReplicaHost);

                    storageContext.getDao().upsert(queryContext.getId(), value);
                    result.workingReplica();
                    result.successOperation();
                    return result;
                });
            } else {
                return threadPool.addWork(() -> {
                    ReplicaAnswerResult result = new ReplicaAnswerResult(myReplicaHost);

                    try {
                        HttpQueryResult httpQueryResult = storageContext.getHttpQueryCreator()
                                .put(sameQueryOnReplica(queryContext, replicaHost))
                                .withReplicas(new ReplicasCollection(Collections.singleton(myReplicaHost)))
                                .withTimestamp(timestamp)
                                .withBody(value)
                                .execute();

                        result.workingReplica();

                        if (httpQueryResult.getStatus() == HttpHelpers.STATUS_SUCCESS_PUT) {
                            result.successOperation();
                        }
                    } catch (URISyntaxException e) {
                        e.printStackTrace();
                    } catch (HttpHostConnectException | ConnectTimeoutException e){
                        // nothing
                    }

                    return result;
                });
            }
        });

        if (results.getSuccessOperations() < queryContext.topologyParameters.getAck()){
            return new ActionResponse(HttpHelpers.STATUS_NOT_ENOUGH_REPLICAS);
        } else {
            return new ActionResponse(HttpHelpers.STATUS_SUCCESS_PUT);
        }
    }
}
