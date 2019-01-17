package ru.mail.polis.dariam.replicahelpers;

import org.jetbrains.annotations.NotNull;

public class ReplicaAnswerResult {

    @NotNull
    private final String replicaHost;

    private boolean workingReplica = false;
    private boolean notFound = false;
    private boolean successOperation = false;
    private Long deletedTimestamp;
    private Long valueTimestamp;

    public ReplicaAnswerResult(@NotNull String replicaHost) {
        this.replicaHost = replicaHost;
    }

    public void notFound(){
        notFound = true;
    }

    public void setDeleted(long timestamp){
        deletedTimestamp = timestamp;
    }

    public void workingReplica(){
        workingReplica = true;
    }

    public void successOperation(){
        successOperation = true;
    }

    public void setValueTimestamp(long timestamp){
        valueTimestamp = timestamp;
    }

    @NotNull
    public String getReplicaHost() {
        return replicaHost;
    }

    public boolean isWorkingReplica() {
        return workingReplica;
    }

    public boolean isNotFound() {
        return notFound;
    }

    public boolean isSuccessOperation() {
        return successOperation;
    }

    public Long getDeletedTimestamp() {
        return deletedTimestamp;
    }

    public Long getValueTimestamp() {
        return valueTimestamp;
    }
}
