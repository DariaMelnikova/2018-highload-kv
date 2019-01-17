package ru.mail.polis.dariam.controller;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import ru.mail.polis.dariam.replicahelpers.ReplicaAnswerResult;

public class ThreadPoolReplicasQuerys {
    private ExecutorService executor = null;

    public void start(){
        executor = Executors.newFixedThreadPool(8 * 3);
    }

    public void stop(){
        if (executor != null) {
            executor.shutdown();
            executor = null;
        }
    }

    public Future<ReplicaAnswerResult> addWork(Callable<ReplicaAnswerResult> work){
        return executor.submit(work);
    }
}
