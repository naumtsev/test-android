package ru.hse;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import ru.hse.gameObjects.generateGameMap.*;
import ru.hse.gameObjects.GameMap;
import ru.hse.gameObjects.User;
import ru.hse.services.AccountService;
import ru.hse.services.LoggerInterceptor;
import ru.hse.services.RoomService;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

public class App {
        public static void main(String[] args) throws IOException, InterruptedException {
                ServerBuilder<?> serverBuilder = ServerBuilder.forPort(8080);
                serverBuilder.keepAliveTime(500, TimeUnit.MILLISECONDS);

                serverBuilder.intercept(new LoggerInterceptor());
                serverBuilder.addService(new AccountService());
                serverBuilder.addService(new RoomService());

                Server server = serverBuilder.build();
                server.start();


                Runtime.getRuntime().addShutdownHook(new Thread(server::shutdownNow));

                String ipAddresses = IpUtil.resolveIPAddresses();

                System.out.println("Server started listening on port " + server.getPort());
                System.out.println("Server IPs: " + ipAddresses);

                server.awaitTermination();
        }
}
