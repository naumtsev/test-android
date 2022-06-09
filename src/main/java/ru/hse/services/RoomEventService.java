package ru.hse.services;

import com.google.protobuf.Empty;
import io.grpc.stub.StreamObserver;
import ru.hse.RoomEventServiceGrpc;
import ru.hse.GameEvents;
import ru.hse.controllers.RoomController;
import ru.hse.gameObjects.Room;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;

public class RoomEventService extends RoomEventServiceGrpc.RoomEventServiceImplBase {
    RoomController roomController = new RoomController();

    public RoomEventService(RoomController roomController) {
        this.roomController = roomController;
    }

    @Override
    public void joinToRoom(GameEvents.JoinToRoomRequest request, StreamObserver<GameEvents.RoomEvent> clientEventStream) {
        String playerLogin = request.getLogin();
        String roomName = request.getRoomName();

        roomController.joinToRoom(playerLogin, roomName, clientEventStream);
    }

    @Override
    public void attackBlock(GameEvents.AttackRequest request, StreamObserver<Empty> responseObserver) {
        super.attackBlock(request, responseObserver);
    }

    @Override
    public void surrender(GameEvents.SurrenderRequest request, StreamObserver<Empty> responseObserver) {
        super.surrender(request, responseObserver);
    }
}
