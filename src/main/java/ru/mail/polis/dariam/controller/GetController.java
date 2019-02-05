package ru.mail.polis.dariam.controller;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

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

public class GetController extends BaseController implements QueryProcessor {

    public GetController(StorageContext storageContext) {
        super(storageContext);
    }

    @Override
    public ActionResponse processQueryFromReplica(QueryContext queryContext) throws IOException {
        KVDao dao = storageContext.getDao();
        try {
            return new ActionResponse(HttpHelpers.STATUS_SUCCESS_GET, dao.get(queryContext.getId()))
                    .withTimestamp(dao.getUpdateTime(queryContext.getId()));
        } catch (NoSuchElementException e) {
            long deleteTime = dao.getUpdateTime(queryContext.getId());
            if (deleteTime > 0) {
                return new ActionResponse(HttpHelpers.STATUS_SUCCESS_GET)
                        .withTimestamp(deleteTime)
                        .withDeleted();
            } else {
                return new ActionResponse(HttpHelpers.STATUS_NOT_FOUND);
            }
        } catch (IllegalArgumentException e) {
            return new ActionResponse(HttpHelpers.STATUS_BAD_ARGUMENT);
        }
    }

    @Override
    public ActionResponse processQueryFromClient(QueryContext queryContext) throws IOException {
        byte[] id = queryContext.getId();
        String myReplicaHost = storageContext.getMyReplicaHost();
        ThreadPoolReplicasQuerys threadPool = storageContext.getThreadPool();
        KVDao dao = storageContext.getDao();

        ConcurrentMap<String, byte[]> replicasValue = new ConcurrentHashMap<>();
        ReplicaAnswerResults results = forEachReplica(queryContext, replicaHost -> {

            if (replicaHost.equals(myReplicaHost)) {

                return threadPool.addWork(() -> {
                    ReplicaAnswerResult result = new ReplicaAnswerResult(myReplicaHost);

                    try {
                        replicasValue.put(replicaHost, dao.get(id));
                        result.setValueTimestamp(dao.getUpdateTime(id));
                        result.workingReplica();
                    } catch (IllegalArgumentException e) {
                    } catch (NoSuchElementException e){
                        long deleteTime = dao.getUpdateTime(id);
                        if (deleteTime > 0) {
                            result.setDeleted(deleteTime);
                        } else {
                            result.notFound();
                        }
                        result.workingReplica();
                    }

                    return result;
                });

            } else {

                return threadPool.addWork(() -> {

                    ReplicaAnswerResult result = new ReplicaAnswerResult(replicaHost);

                    try {
                        HttpQueryResult getQueryResult = storageContext.getHttpQueryCreator()
                                .get(sameQueryOnReplica(queryContext, replicaHost))
                                .withReplicas(new ReplicasCollection(Collections.singleton(myReplicaHost)))
                                .execute();

                        result.workingReplica();

                        switch (getQueryResult.getStatus()) {
                            case HttpHelpers.STATUS_BAD_ARGUMENT:
                                break;
                            case HttpHelpers.STATUS_NOT_FOUND:
                                result.notFound();
                                break;
                            case HttpHelpers.STATUS_SUCCESS_GET:
                                Long timestamp = getQueryResult.getTimestamp();
                                if (getQueryResult.isDeleted()) {
                                    result.setDeleted(timestamp);
                                } else {
                                    replicasValue.put(replicaHost, getQueryResult.getData());
                                }
                                result.setValueTimestamp(timestamp);
                                break;
                        }
                    } catch (URISyntaxException | IOException e) {
                    }

                    return result;
                });
            }
        });

        if (results.getWorkingReplicas() < queryContext.topologyParameters.getAck()){
            return new ActionResponse(HttpHelpers.STATUS_NOT_ENOUGH_REPLICAS);
        }

        Map<Long, ReplicasCollection> replicasByTimestamp = results.getReplicasByTimestamp();

        long maxTimestamp = replicasByTimestamp.keySet().stream().max(Long::compareTo).orElse(0L);

        long timestampOfMaxReplicasCount = getTimestampOfMaxReplicasCount(replicasByTimestamp);

        Set<Long> deletedTimestamps = results.getDeletedTimestamps();
        if (deletedTimestamps != null && deletedTimestamps.contains(maxTimestamp)){
            return new ActionResponse(HttpHelpers.STATUS_NOT_FOUND);
        }

        ReplicasCollection replicasWithNeedingValue = replicasByTimestamp.get(timestampOfMaxReplicasCount);

        if (replicasWithNeedingValue == null){
            return new ActionResponse(HttpHelpers.STATUS_NOT_FOUND);
        } else if (results.getWorkingReplicas() >= queryContext.topologyParameters.getAck()) {
            if (replicasWithNeedingValue.contains(myReplicaHost)) {
                try {
                    return new ActionResponse(HttpHelpers.STATUS_SUCCESS_GET, dao.get(id));
                } catch (IllegalArgumentException e) {
                    return new ActionResponse(HttpHelpers.STATUS_BAD_ARGUMENT);
                } catch (NoSuchElementException e){
                    return new ActionResponse(HttpHelpers.STATUS_NOT_FOUND);
                }
            } else {
                return new ActionResponse(HttpHelpers.STATUS_SUCCESS_GET, replicasValue.get(replicasWithNeedingValue.toArray()[0]));
            }
        } else if (results.getNotFound() >= queryContext.topologyParameters.getAck()) {
            return new ActionResponse(HttpHelpers.STATUS_NOT_FOUND);
        } else {
            return new ActionResponse(HttpHelpers.STATUS_NOT_ENOUGH_REPLICAS);
        }
    }

    private long getTimestampOfMaxReplicasCount(Map<Long, ReplicasCollection> replicasByTimestamp) {
        int maxTimestampCount = -1;
        long timestampOfMaxCount = -1;
        for (Map.Entry<Long, ReplicasCollection> entry : replicasByTimestamp.entrySet()) {
            int entryReplicasSize = entry.getValue().size();
            long entryTimestamp = entry.getKey();

            if (entryReplicasSize > maxTimestampCount ||
                    entryReplicasSize == maxTimestampCount && entryTimestamp > timestampOfMaxCount) {
                maxTimestampCount = entryReplicasSize;
                timestampOfMaxCount = entryTimestamp;
            }
        }
        return timestampOfMaxCount;
    }
}
