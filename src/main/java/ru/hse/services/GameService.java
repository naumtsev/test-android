package ru.hse.services;

import com.google.protobuf.Empty;
import io.grpc.stub.StreamObserver;
import ru.hse.Game;
import ru.hse.GameServiceGrpc;

public class GameService extends GameServiceGrpc.GameServiceImplBase {
    @Override
    public void joinToGame(Game.JoinToGameRequest request, StreamObserver<Game.GameEvent> responseObserver) {
        super.joinToGame(request, responseObserver);
    }

    @Override
    public void attackBlock(Game.AttackRequest request, StreamObserver<Empty> responseObserver) {
        super.attackBlock(request, responseObserver);
    }

    @Override
    public void surrender(Game.SurrenderRequest request, StreamObserver<Empty> responseObserver) {
        super.surrender(request, responseObserver);
    }
}
