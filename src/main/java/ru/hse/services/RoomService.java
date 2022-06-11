package ru.hse.services;

import io.grpc.stub.StreamObserver;
import ru.hse.Room;
import ru.hse.RoomServiceGrpc;
import ru.hse.controllers.RoomController;

import java.util.HashMap;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class RoomService extends RoomServiceGrpc.RoomServiceImplBase {
    private final Set<String> createdRooms = new TreeSet<>();
    private final Set<String> publicRooms = new TreeSet();
    private final HashMap<String, RoomController> rooms = new HashMap<>();
    private final Lock lock = new ReentrantLock();
    @Override
    public void joinToRoom(Room.JoinToRoomRequest request, StreamObserver<Room.RoomEvent> responseObserver) {
        String playerLogin = request.getLogin();
        String roomName = request.getRoomName();

        lock.lock();

        try {
            if (roomName.equals("")) {
                roomName = getOrCreateFreePublicRoom();
            } else {
                // if room is fulled game will start and room will be removed from createdRoom
                if (!createdRooms.contains(roomName)) {
                    createRoom(roomName, false);
                }
            }

            RoomController roomController = rooms.get(roomName);
            boolean joined = roomController.joinPlayer(playerLogin, responseObserver);

            if (!joined) {
                // Send JoinToRoomResponse
                Room.JoinToRoomResponse response = Room.JoinToRoomResponse.newBuilder().setSuccess(false).build();
                Room.RoomEvent responseEvent = Room.RoomEvent.newBuilder().setJoinToRoomResponse(response).build();
                responseObserver.onNext(responseEvent);
                responseObserver.onCompleted();
            }

            if (roomController.isFilled()) {
                // create new Game

                String gameID = "GameID123";
                Room.GameStartedEvent response = Room.GameStartedEvent.newBuilder().setGameId(gameID).build();
                Room.RoomEvent responseEvent = Room.RoomEvent.newBuilder().setGameStartedEvent(response).build();
                roomController.broadcast(responseEvent);

                roomController.disconnectAllPlayers();
            }


        } finally {
            lock.unlock();
        }
    }

    private String getOrCreateFreePublicRoom() {
        String roomName = null;

        for (var publicRoomName: publicRooms) {
            RoomController roomController = rooms.get(publicRoomName);
            if (!roomController.isFilled()) {
                roomName = publicRoomName;
                break;
            }
        }

        if (roomName == null) {
            String nameNewRoom = generateRoomName();
            while (createdRooms.contains(nameNewRoom)) {
                nameNewRoom = generateRoomName();
            }

            createRoom(nameNewRoom, true);
            roomName = nameNewRoom;
        }
        return roomName;
    }

    private void createRoom(String roomName, boolean roomIsPublic) {
        createdRooms.add(roomName);
        rooms.put(roomName, new RoomController(roomName, 4));

        if (roomIsPublic) {
            publicRooms.add(roomName);
        }
    }

    private static String generateRoomName() {
        return UUID.randomUUID().toString();
    }



}
