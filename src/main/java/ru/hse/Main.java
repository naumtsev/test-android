package ru.hse;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

public class Main {

    public static void main(String[] args) {
        ManagedChannel channel = ManagedChannelBuilder.forAddress("192.168.0.156", 8080).usePlaintext().build();
        LoginServiceGrpc.LoginServiceBlockingStub stub = LoginServiceGrpc.newBlockingStub(channel);


        Services.LoginRequest request = Services.LoginRequest
                .newBuilder().setName("123").setPassword("test").build();
//
        Services.LoginResponse res = stub.login(request);
//
//        System.out.println(res.getSuccess());
        channel.shutdownNow();
    }
}
