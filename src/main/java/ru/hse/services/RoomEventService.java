package ru.hse.services;

import com.google.protobuf.Empty;
import io.grpc.stub.StreamObserver;
import ru.hse.RoomEventServiceGrpc;
import ru.hse.GameEvents;
import ru.hse.controllers.GameController;
import ru.hse.controllers.PlayersController;
import ru.hse.controllers.RoomController;
import ru.hse.gameObjects.Room;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;

public class RoomEventService extends RoomEventServiceGrpc.RoomEventServiceImplBase {
    RoomController roomController;
    // запоминаем игры, которые сейчас играют
    private final ConcurrentHashMap<String, GameController> createdGames = new ConcurrentHashMap<>();

    public RoomEventService(RoomController roomController) {
        this.roomController = roomController;
    }

    @Override
    public void joinToRoom(GameEvents.JoinToRoomRequest request, StreamObserver<GameEvents.RoomEvent> clientEventStream) {
        String playerLogin = request.getLogin();
        String roomName = request.getRoomName();

        String nameToBeCreatedRoom = roomController.joinToRoom(playerLogin, roomName, clientEventStream);
        if(!nameToBeCreatedRoom.equals("")){
            // берём комнату
            Room room = roomController.getCreatedRooms().get(nameToBeCreatedRoom);
            // забираем всех участников
            PlayersController playersController = room.getPlayersController();

            // удаляем участников из комнаты ожидания
            for(String p : playersController.getPlayers()){
                roomController.getWaitingPlayers().remove(p);
            }
            // создаём игру с gameController
            String gameId = generateString();

            // присылаем им запросы на присоединение
            GameEvents.GameStartedEvent.Builder res = GameEvents.GameStartedEvent.newBuilder();
            res.setGameId(gameId);
            var r = GameEvents.RoomEvent.newBuilder().setGameStartedEvent(res.build());
            playersController.sendEventToAllPlayers(r);
        }
    }

    @Override
    public void attackBlock(GameEvents.AttackRequest request, StreamObserver<Empty> responseObserver) {
        super.attackBlock(request, responseObserver);
    }

    @Override
    public void surrender(GameEvents.SurrenderRequest request, StreamObserver<Empty> responseObserver) {
        super.surrender(request, responseObserver);
    }

    static public String generateString(){
        int leftLimit = 48; // numeral '0'
        int rightLimit = 122; // letter 'z'
        int targetStringLength = 10;
        Random random = new Random();

        String generatedString = random.ints(leftLimit, rightLimit + 1)
                .filter(i -> (i <= 57 || i >= 65) && (i <= 90 || i >= 97))
                .limit(targetStringLength)
                .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                .toString();

        System.out.println(generatedString);

        return generatedString;
    }
}
