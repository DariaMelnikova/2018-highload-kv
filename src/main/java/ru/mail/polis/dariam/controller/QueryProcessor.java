package ru.mail.polis.dariam.controller;

import java.io.IOException;

import ru.mail.polis.dariam.ActionResponse;

public interface QueryProcessor {

    ActionResponse processQueryFromReplica(QueryContext queryContext) throws IOException;

    ActionResponse processQueryFromClient(QueryContext queryContext) throws IOException;
}
