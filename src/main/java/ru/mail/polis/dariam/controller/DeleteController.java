package ru.mail.polis.dariam.controller;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Collections;

import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.conn.HttpHostConnectException;

import ru.mail.polis.KVDao;
import ru.mail.polis.dariam.ActionResponse;
import ru.mail.polis.dariam.HttpHelpers;
import ru.mail.polis.dariam.StorageContext;
import ru.mail.polis.dariam.httpclient.HttpQueryResult;
import ru.mail.polis.dariam.replicahelpers.ReplicasCollection;
import ru.mail.polis.dariam.replicahelpers.ReplicaAnswerResult;
import ru.mail.polis.dariam.replicahelpers.ReplicaAnswerResults;

public class DeleteController extends BaseController implements QueryProcessor {

    public DeleteController(StorageContext storageContext) {
        super(storageContext);
    }

    @Override
    public ActionResponse processQueryFromReplica(QueryContext queryContext) throws IOException {
        try {
            storageContext.getDao().remove(queryContext.getId());
            return new ActionResponse(HttpHelpers.STATUS_SUCCESS_DELETE);
        } catch (IllegalArgumentException e) {
            return new ActionResponse(HttpHelpers.STATUS_BAD_ARGUMENT);
        }
    }

    @Override
    public ActionResponse processQueryFromClient(QueryContext queryContext) throws IOException {
        final long timestamp = System.currentTimeMillis();
        String myReplicaHost = storageContext.getMyReplicaHost();
        ThreadPoolReplicasQuerys threadPool = storageContext.getThreadPool();
        KVDao dao = storageContext.getDao();

        ReplicaAnswerResults results = forEachReplica(queryContext, replicaHost -> {
            if (replicaHost.equals(myReplicaHost)) {
                return threadPool.addWork(() -> {
                    ReplicaAnswerResult result = new ReplicaAnswerResult(myReplicaHost);
                    dao.remove(queryContext.getId());
                    result.successOperation();
                    result.workingReplica();
                    return result;
                });
            } else {

                return threadPool.addWork(() -> {
                    ReplicaAnswerResult result = new ReplicaAnswerResult(myReplicaHost);
                    try {
                        HttpQueryResult httpQueryResult = storageContext.getHttpQueryCreator()
                                .delete(sameQueryOnReplica(queryContext, replicaHost))
                                .withReplicas(new ReplicasCollection(Collections.singleton(myReplicaHost)))
                                .withTimestamp(timestamp)
                                .execute();

                        result.workingReplica();

                        if (httpQueryResult.getStatus() == HttpHelpers.STATUS_SUCCESS_DELETE) {
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

        if (results.getWorkingReplicas() < queryContext.topologyParameters.getAck()){
            return new ActionResponse(HttpHelpers.STATUS_NOT_ENOUGH_REPLICAS);
        } else {
            return new ActionResponse(HttpHelpers.STATUS_SUCCESS_DELETE);
        }
    }
}
