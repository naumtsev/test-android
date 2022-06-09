package ru.hse.controllers;

import io.grpc.stub.StreamObserver;
import ru.hse.GameEvents;
import ru.hse.gameObjects.Room;
import ru.hse.services.RoomEventService;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

public class RoomController {
    private final int MAX_COUNT_PLAYERS = 2;
    private String publicRoomName;
    private final ConcurrentHashMap<String, Room> createdRooms = new ConcurrentHashMap<>();
    // playerName -> roomName
    private final ConcurrentHashMap<String, String> waitingPlayers = new ConcurrentHashMap<>();

    public RoomController() {
        createPublicRoom(MAX_COUNT_PLAYERS);
    }

    // Пустая строка: никакая игра не была создана
    // Непустая строка: какая-то игра сформирована, нужно её создать

    public String joinToRoom(String playerLogin, String roomName, StreamObserver<GameEvents.RoomEvent> clientEventStream) {
        // общая комната или приватная
        String nameToBeCreatedGame = "";
        if(roomName.equals("")){
            // тут не нужно ничего проверять, т.к. комнаты сразу отсоединяем
            if(createdRooms.get(publicRoomName).fullRoom()){
                // создаём новую комнату
                createPublicRoom(MAX_COUNT_PLAYERS);
            }
            // добавляем игрока
            if (createdRooms.containsKey(publicRoomName)) {
                createdRooms.get(publicRoomName).addPlayer(playerLogin, clientEventStream);
                waitingPlayers.put(playerLogin, publicRoomName);
            }

        } else {
            // добавляем игрока
            if(!createdRooms.containsKey(roomName)){
                createdRooms.put(roomName, new Room(publicRoomName, MAX_COUNT_PLAYERS));
            }
            createdRooms.get(roomName).addPlayer(playerLogin, clientEventStream);
            waitingPlayers.put(playerLogin, roomName);
        }

        // сохраняем имя игры, которую нужно будет создать
        if(createdRooms.get(publicRoomName).getCountPlayersWaiting() == MAX_COUNT_PLAYERS){
            nameToBeCreatedGame = publicRoomName;
        }

        try {
            GameEvents.JoinToRoomResponse.Builder res = GameEvents.JoinToRoomResponse.newBuilder();
            res.setComment("Connect successful!");
            var r = GameEvents.RoomEvent.newBuilder().setJoinToRoomResponse(res.build());
            clientEventStream.onNext(r.build());
            clientEventStream.onCompleted();
        } catch (Exception e){
            // удалить игрока
        }

        // создать запрос ping, который будет отправлять запрос
        // при каждом подключении игроков, слать запрос всем пользователям и проверять, что они подключены
        pingAllWaitingPlayers();
        deleteEmptyRooms();

        return nameToBeCreatedGame;
    }

    public ConcurrentHashMap<String, Room> getCreatedRooms(){
        return createdRooms;
    }

    public ConcurrentHashMap<String, String> getWaitingPlayers(){
        return waitingPlayers;
    }

    // проверяем, что игроки всё ещё присоединены к комнате
    private void pingAllWaitingPlayers(){
        // удалить всех игроков из комнат
        // получить всех игроков, которые отсоединились и удалить из массива игроков
        ArrayList<String> disconnectPlayers = new ArrayList<>();
        for(Room room : createdRooms.values()){
            disconnectPlayers.addAll(room.ping());
        }

        for(String playerName : disconnectPlayers){
            waitingPlayers.remove(playerName);
        }
    }

    private void createPublicRoom(int countPlayers){
        publicRoomName = RoomEventService.generateString();
        createdRooms.put(publicRoomName, new Room(publicRoomName, countPlayers));
    }

    // метод, который удаляет комнаты, если в них уже нет игроков
    // вот тут нужно его создать
    // вызываем его, когда смотрим, что компанты могут измениться

    private void deleteEmptyRooms(){
        ArrayList<String> nameEmptyRooms = new ArrayList<>();
        for(Room room : createdRooms.values()){
            if(room.getCountPlayersWaiting() == 0 && !room.getRoomName().equals(publicRoomName)) {
                nameEmptyRooms.add(room.getRoomName());
            }
        }

        for(String roomName : nameEmptyRooms){
            createdRooms.remove(nameEmptyRooms);
        }
    }
}
