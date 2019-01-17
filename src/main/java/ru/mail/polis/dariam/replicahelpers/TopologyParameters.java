package ru.mail.polis.dariam.replicahelpers;

public class TopologyParameters {
    private final int ack;
    private final int from;

    public static TopologyParameters fromQuery(String parameters) throws IllegalArgumentException {
        String[] parameter = parameters.split("/");
        if (parameter.length != 2) {
            throw new IllegalArgumentException();
        }
        return new TopologyParameters(Integer.parseInt(parameter[0]), Integer.parseInt(parameter[1]));
    }

    public TopologyParameters(int ack, int from){
        this.ack = ack;
        this.from = from;
    }

    public int getAck() {
        return ack;
    }

    public int getFrom() {
        return from;
    }
}
