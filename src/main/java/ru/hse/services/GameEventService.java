package ru.hse.services;

import io.grpc.stub.StreamObserver;
import ru.hse.GameEventServiceGrpc;
import ru.hse.GameEvents;
import ru.hse.controllers.RoomController;
import ru.hse.gameObjects.Room;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;

public class GameEventService extends GameEventServiceGrpc.GameEventServiceImplBase {
    private String publicRoomName;
    private final ConcurrentHashMap<String, Room> createdRooms = new ConcurrentHashMap<>();

    public GameEventService(RoomController roomController) {

    }

    @Override
    public void joinToRoom(GameEvents.JoinToRoomRequest request, StreamObserver<GameEvents.RoomEvent> responseObserver) {
        String playerLogin = request.getLogin();
        String roomName = request.getRoomName();

    }
}
