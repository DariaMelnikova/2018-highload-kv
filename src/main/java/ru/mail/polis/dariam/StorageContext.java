package ru.mail.polis.dariam;

import ru.mail.polis.KVDao;
import ru.mail.polis.dariam.httpclient.HttpQueryCreator;
import ru.mail.polis.dariam.controller.ThreadPoolReplicasQuerys;
import ru.mail.polis.dariam.replicahelpers.ReplicasCollection;

public class StorageContext {
    private final KVDao dao;

    private final HttpQueryCreator httpQueryCreator;

    private final ReplicasCollection replicasHosts;

    private final String myReplicaHost;

    private final ThreadPoolReplicasQuerys threadPool;

    public StorageContext(KVDao dao, HttpQueryCreator httpQueryCreator, ReplicasCollection replicasHosts, String myReplicaHost, ThreadPoolReplicasQuerys threadPool) {
        this.dao = dao;
        this.httpQueryCreator = httpQueryCreator;
        this.replicasHosts = replicasHosts;
        this.myReplicaHost = myReplicaHost;
        this.threadPool = threadPool;
    }

    public KVDao getDao() {
        return dao;
    }

    public HttpQueryCreator getHttpQueryCreator() {
        return httpQueryCreator;
    }

    public ReplicasCollection getReplicasHosts() {
        return replicasHosts;
    }

    public String getMyReplicaHost() {
        return myReplicaHost;
    }

    public ThreadPoolReplicasQuerys getThreadPool() {
        return threadPool;
    }
}
