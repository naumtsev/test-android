package ru.hse.services;

import io.grpc.stub.StreamObserver;
import ru.hse.LoginServiceGrpc;
import ru.hse.Services;

public class LoginServiceImpl extends LoginServiceGrpc.LoginServiceImplBase {

    @Override
    public void login(Services.LoginRequest request, StreamObserver<Services.LoginResponse> responseObserver) {
        Services.LoginResponse res = Services.LoginResponse.newBuilder().setSuccess(true).build();
        System.out.println(request.getName() + " " + request.getPassword());
        responseObserver.onNext(res);
        responseObserver.onCompleted();
    }
}