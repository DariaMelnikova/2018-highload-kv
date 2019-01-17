package ru.mail.polis.dariam.replicahelpers;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ReplicaAnswerResults {
    private Map<Long, ReplicasCollection> replicasByTimestamp;
    private Set<Long> deletedTimestamps;

    private int notFound = 0;
    private int workingReplicas = 0;
    private int successOperations = 0;

    public ReplicaAnswerResults(List<ReplicaAnswerResult> results){
        replicasByTimestamp = new HashMap<>();
        deletedTimestamps = new HashSet<>();

        for (ReplicaAnswerResult result : results){
            if (result.isNotFound()){
                notFound++;
            }

            if (result.isWorkingReplica()){
                workingReplicas++;
            }

            if (result.isSuccessOperation()){
                successOperations++;
            }

            Long valueTimestamp = result.getValueTimestamp();
            if (valueTimestamp != null){
                ReplicasCollection replicasCollection = replicasByTimestamp.get(valueTimestamp);
                if (replicasCollection == null) {
                    replicasCollection = new ReplicasCollection();
                    replicasByTimestamp.put(valueTimestamp, replicasCollection);
                }
                replicasCollection.add(result.getReplicaHost());
            }

            Long deletedTimestamp = result.getDeletedTimestamp();
            if (deletedTimestamp != null){
                deletedTimestamps.add(deletedTimestamp);
            }
        }
    }

    public int getNotFound() {
        return notFound;
    }

    public int getWorkingReplicas() {
        return workingReplicas;
    }

    public int getSuccessOperations() {
        return successOperations;
    }

    public Map<Long, ReplicasCollection> getReplicasByTimestamp() {
        return replicasByTimestamp;
    }

    public Set<Long> getDeletedTimestamps() {
        return deletedTimestamps;
    }
}
