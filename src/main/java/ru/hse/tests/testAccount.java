package ru.hse.tests;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import ru.hse.Account;
import ru.hse.AccountServiceGrpc;
import ru.hse.services.AccountService;

public class testAccount {

    public static void main(String[] args) {
        ManagedChannel channel = ManagedChannelBuilder.forAddress("172.18.0.1", 8080)
                .usePlaintext()
                .build();

        AccountServiceGrpc.AccountServiceBlockingStub stub = AccountServiceGrpc.newBlockingStub(channel);


        Account.LoginResponse lres = stub.login(Account.LoginRequest.newBuilder().setLogin("anton").setPassword("123").build());

        System.out.println("LOGIN REQ: " + lres.getSuccess() + " " + lres.getComment());

        Account.RegisterAccountResponse res1 = stub.registerAccount(Account.RegisterAccountRequest.newBuilder().setLogin("anton").setPassword("123").build());
        System.out.println("FIRST REQ: " + String.valueOf(res1.getSuccess()) + " " + res1.getComment());

        Account.RegisterAccountResponse res2 = stub.registerAccount(Account.RegisterAccountRequest.newBuilder().setLogin("anton").setPassword("321").build());
        System.out.println("SECOND REQ: " + String.valueOf(res2.getSuccess()) + " " + res2.getComment());


        Account.LoginResponse loginResponsees = stub.login(Account.LoginRequest.newBuilder().setLogin("anton").setPassword("123").build());

        System.out.println("LOGIN REQ: " + loginResponsees.getSuccess() + " " + loginResponsees.getComment());
    }
}
