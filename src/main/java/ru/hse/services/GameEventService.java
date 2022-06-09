package ru.hse.services;

import io.grpc.stub.StreamObserver;
import ru.hse.GameEventServiceGrpc;
import ru.hse.GameEvents;
import ru.hse.controllers.RoomController;
import ru.hse.gameObjects.Room;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;

public class GameEventService extends GameEventServiceGrpc.GameEventServiceImplBase {
    RoomController roomController = new RoomController();

    public GameEventService(RoomController roomController) {
        this.roomController = roomController;
    }

    // запрос на вход в комнату
    // responceObserver - писать
    // если пустая строчка, то к public комнате
    // иначе подключиться к private комнате
    // подключение: абстракция про пользователей
    // нужно проверять, что игроков максимум => запускаем игру
    // должно быть два состояния у комнаты: waitingRoom и startRoom
    @Override
    public void joinToRoom(GameEvents.JoinToRoomRequest request, StreamObserver<GameEvents.RoomEvent> clientEventStream) {
        String playerLogin = request.getLogin();
        String roomName = request.getRoomName();

        roomController.joinToRoom(playerLogin, roomName, clientEventStream);
    }

    @Override
    public void moveEvent(GameEvents.JoinToRoomRequest request, StreamObserver<GameEvents.RoomEvent> responseObserver) {
        super.moveEvent(request, responseObserver);
    }
}
