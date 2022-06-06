package ru.hse;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import ru.hse.services.LoginServiceImpl;

import java.io.IOException;
import java.net.Inet4Address;

public class App {
        public static void main(String[] args) throws IOException, InterruptedException {
        Server server = ServerBuilder.forPort(8080).addService(new LoginServiceImpl()).build();

        server.start();
        System.out.println("Server started on port " + String.valueOf(server.getPort()));
        System.out.println("Server ip: " + Inet4Address.getLocalHost().getHostAddress());
        server.awaitTermination();
    }
}
