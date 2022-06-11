package ru.hse.controllers;

import io.grpc.stub.ServerCallStreamObserver;
import io.grpc.stub.StreamObserver;
import org.checkerframework.checker.nullness.compatqual.NonNullType;
import org.checkerframework.checker.nullness.qual.NonNull;
import ru.hse.Room;
import ru.hse.objects.PlayerWithIO;

import java.util.ArrayList;
import java.util.Optional;
import java.util.stream.Stream;

public class RoomController {
    private final ArrayList<PlayerWithIO> joinedPlayers = new ArrayList<PlayerWithIO>();

    private final String roomName;
    private final int numberPlayersToStart;

    public RoomController(@NonNull String roomName, int numberPlayersToStart) {
        this.roomName = roomName;
        this.numberPlayersToStart = numberPlayersToStart;
    }

    public synchronized int getJoinedPlayersCount() {
        return joinedPlayers.size();
    }

    public boolean joinPlayer(String playerLogin, StreamObserver<Room.RoomEvent> playerEventStream) {

        ServerCallStreamObserver<Room.RoomEvent> servEventStream = (ServerCallStreamObserver<Room.RoomEvent>) playerEventStream;
        servEventStream.setOnCloseHandler(new Runnable() {
            @Override
            public void run() {
                System.out.println("Player Disconnect");
                onPlayerError(playerLogin);
            }
        });

        servEventStream.setOnCancelHandler(new Runnable() {
            @Override
            public void run() {
                System.out.println("Player Canceled");
                onPlayerError(playerLogin);
            }
        });



        StreamObserver<Room.RoomEvent> eventStream = new StreamObserver<Room.RoomEvent>() {
            @Override
            public void onNext(Room.RoomEvent value) {
                try {
                    playerEventStream.onNext(value);
                } catch (Exception e) {
                    onError(e);
                }
            }
            @Override
            public void onError(Throwable t) {
                onPlayerError(playerLogin);
            }
            @Override
            public void onCompleted() {
                playerEventStream.onCompleted();
            }
        };

        // Send JoinEvent
        Room.OtherPlayerJoinedEvent joinEvent = Room.OtherPlayerJoinedEvent.newBuilder().setPlayerLogin(playerLogin).build();
        Room.RoomEvent event = Room.RoomEvent.newBuilder().setOtherPlayerJoinedEvent(joinEvent).build();
        broadcast(event);

        synchronized (joinedPlayers) {
            joinedPlayers.add(new PlayerWithIO(playerLogin, eventStream));
        }

        // Send JoinResponse
        Room.JoinToRoomResponse response = Room.JoinToRoomResponse.newBuilder().setRoomPlayers(getRoomPlayers()).setSuccess(true).build();
        Room.RoomEvent responseEvent = Room.RoomEvent.newBuilder().setJoinToRoomResponse(response).build();
        eventStream.onNext(responseEvent);

        return true;
    }

    public String getRoomName() {
        return roomName;
    }
    public void broadcast(Room.RoomEvent roomEvent) {
        synchronized(joinedPlayers) {
            for (var playerWithIO : joinedPlayers) {
                playerWithIO.getEventStream().onNext(roomEvent);
            }
        }
    }

    private void onPlayerError(String playerLogin) {
        disconnectPlayer(playerLogin, true);
    }

    public boolean isFilled() {
        synchronized (joinedPlayers) {
            return joinedPlayers.size() >= numberPlayersToStart;
        }
    }

    private Room.RoomPlayers getRoomPlayers() {
        Room.RoomPlayers.Builder roomPlayersBuilder = Room.RoomPlayers.newBuilder();
        synchronized(joinedPlayers) {
            joinedPlayers.forEach(playerWithIO -> {
                roomPlayersBuilder.addPlayerLogin(playerWithIO.getLogin());
            });
        }

        return roomPlayersBuilder.build();
    }

    private void disconnectPlayer(String playerLogin, boolean onError) {
        synchronized (joinedPlayers) {
            Optional<PlayerWithIO> optionalPlayerWithIO = getPlayerWithIO(playerLogin);
            if (optionalPlayerWithIO.isPresent()) {
                PlayerWithIO playerWithIO = optionalPlayerWithIO.get();

                if (!onError) {
                   playerWithIO.getEventStream().onCompleted();
                }

                joinedPlayers.remove(playerWithIO);
            }
        }

        // Send DisconnectEvent
        Room.OtherPlayerDisconnectedEvent event = Room.OtherPlayerDisconnectedEvent.newBuilder().setPlayerLogin(playerLogin).build();
        broadcast(Room.RoomEvent.newBuilder().setOtherPlayerDisconnectedEvent(event).build());
    }

    public Optional<PlayerWithIO> getPlayerWithIO(String playerLogin) {
        synchronized (joinedPlayers) {
            return joinedPlayers.stream().filter(playerWithIO -> playerWithIO.getLogin().equals(playerLogin)).findFirst();
        }
    }

    public void disconnectAllPlayers() {
        synchronized (joinedPlayers) {
            joinedPlayers.forEach(playerWithIO -> playerWithIO.getEventStream().onCompleted());
            joinedPlayers.clear();
        }
    }
}

