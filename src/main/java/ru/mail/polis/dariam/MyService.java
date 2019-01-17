package ru.mail.polis.dariam;


import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Set;
import java.util.concurrent.Executors;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.sun.net.httpserver.HttpServer;

import ru.mail.polis.KVDao;
import ru.mail.polis.KVService;
import ru.mail.polis.dariam.httpclient.HttpQueryCreator;
import ru.mail.polis.dariam.controller.DeleteController;
import ru.mail.polis.dariam.controller.GetController;
import ru.mail.polis.dariam.controller.PutController;
import ru.mail.polis.dariam.controller.ServiceController;
import ru.mail.polis.dariam.controller.ThreadPoolReplicasQuerys;
import ru.mail.polis.dariam.replicahelpers.ReplicasCollection;
import ru.mail.polis.dariam.replicahelpers.ReplicaParametersException;

public class MyService implements KVService {
    public final static String CONTEXT_ENTITY = "/v0/entity";
    public final static String CONTEXT_STATUS = "/v0/status";

    @Nullable
    private HttpServer httpServer;

    @NotNull
    private final ReplicasCollection replicasHosts;

    @NotNull
    private final ThreadPoolReplicasQuerys threadPool;

    @NotNull
    private final HttpQueryCreator httpQueryCreator;

    @NotNull
    private final String myReplicaHost;

    @NotNull
    private final KVDao dao;

    private final int port;

    public MyService(int port, @NotNull KVDao dao, Set<String> replicas) throws IOException {
        this.port = port;

        this.dao = dao;

        this.myReplicaHost = "http://localhost:" + port;

        this.replicasHosts = new ReplicasCollection(replicas);
        this.replicasHosts.remove(this.myReplicaHost);

        this.httpQueryCreator = new HttpQueryCreator();

        this.threadPool = new ThreadPoolReplicasQuerys();
    }

    private void createRouters(){
        StorageContext storageContext = new StorageContext(
                dao,
                httpQueryCreator,
                replicasHosts,
                myReplicaHost,
                threadPool
        );

        ServiceController serviceController = new ServiceController(storageContext);

        DeleteController deleteProcessor = new DeleteController(storageContext);
        GetController getProcessor = new GetController(storageContext);
        PutController putProcessor = new PutController(storageContext);

        try {

            this.httpServer.createContext(CONTEXT_STATUS, httpExchange -> {
                httpExchange.sendResponseHeaders(HttpHelpers.STATUS_SUCCESS, 0);
                httpExchange.close();
            });

            this.httpServer.createContext(CONTEXT_ENTITY, httpExchange -> {
                try {
                    switch (httpExchange.getRequestMethod()) {
                        case "GET":
                            serviceController.execute(httpExchange, getProcessor);
                            break;

                        case "PUT":
                            serviceController.execute(httpExchange, putProcessor);
                            break;

                        case "DELETE":
                            serviceController.execute(httpExchange, deleteProcessor);
                            break;

                        default:
                            httpExchange.sendResponseHeaders(HttpHelpers.STATUS_NOT_FOUND, 0);
                            httpExchange.getResponseBody().close();
                            httpExchange.close();
                    }
                } catch (IllegalIdException | ReplicaParametersException e){
                    e.printStackTrace();
                    httpExchange.sendResponseHeaders(HttpHelpers.STATUS_BAD_ARGUMENT, 0);
                    httpExchange.close();
                } catch (IOException e) {
                    e.printStackTrace();
                    httpExchange.sendResponseHeaders(HttpHelpers.STATUS_INTERNAL_ERROR, 0);
                    httpExchange.getResponseBody().close();
                    httpExchange.close();
                }
            });

            this.httpServer.createContext("/", httpExchange -> {
                httpExchange.sendResponseHeaders(HttpHelpers.STATUS_BAD_ARGUMENT, 0);
                httpExchange.close();
            });
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public void start() {
        threadPool.start();
        try {
            httpServer = HttpServer.create(new InetSocketAddress(port), 0);
            httpServer.setExecutor(Executors.newFixedThreadPool(8));
            httpServer.start();
            createRouters();
        } catch (IOException e) {
            e.printStackTrace();
        }
        httpQueryCreator.start();
    }

    @Override
    public void stop() {
        try {
            dao.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (httpServer != null) {
            httpServer.stop(0);
        }
        httpQueryCreator.stop();
        threadPool.stop();
    }
}