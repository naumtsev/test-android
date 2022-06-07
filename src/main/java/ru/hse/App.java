package ru.hse;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import ru.hse.AccountServiceGrpc;
import ru.hse.controllers.AccountController;
import ru.hse.controllers.RoomController;
import ru.hse.services.AccountService;
import ru.hse.services.GameEventService;

import java.io.IOException;
import java.net.Inet4Address;

public class App {
        public static void main(String[] args) throws IOException, InterruptedException {
                AccountController accountController = new AccountController();
                RoomController roomController = new RoomController();


                ServerBuilder<?> serverBuilder = ServerBuilder.forPort(8080);
                serverBuilder.addService(new AccountService(accountController));
                serverBuilder.addService(new GameEventService(roomController));


                Server server = serverBuilder.build();
                server.start();

                System.out.println("Server started on port " + server.getPort());
                System.out.println("Server ip: " + Inet4Address.getLocalHost().getHostAddress());

                server.awaitTermination();
    }
}
