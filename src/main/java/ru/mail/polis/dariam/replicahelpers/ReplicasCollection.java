package ru.mail.polis.dariam.replicahelpers;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.Spliterator;
import java.util.stream.Collectors;

import org.jetbrains.annotations.NotNull;

public class ReplicasCollection implements Iterable<String> {

    @NotNull
    private Set<String> replicas;

    public ReplicasCollection(ReplicasCollection replicasCollection){
        replicas = new HashSet<>(replicasCollection.replicas);
    }

    public ReplicasCollection(@NotNull Collection<String> replicas) {
        this.replicas = new HashSet<>(replicas);
    }

    public ReplicasCollection() {
        replicas = new HashSet<>();
    }

    @NotNull
    public String toLine(){
        return replicas.stream().collect(Collectors.joining(","));
    }

    public void add(String value){
        replicas.add(value);
    }

    public void remove(String value){
        replicas.remove(value);
    }

    public int size(){
        return replicas.size();
    }

    public boolean empty(){
        return replicas.isEmpty();
    }

    public String[] toArray(){
        return replicas.toArray(new String[replicas.size()]);
    }

    public boolean contains(String value){
        return replicas.contains(value);
    }

    @NotNull
    @Override
    public Iterator<String> iterator() {
        return replicas.iterator();
    }

    @Override
    public Spliterator<String> spliterator() {
        return replicas.spliterator();
    }
}
