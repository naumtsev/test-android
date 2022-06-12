package ru.hse.controllers;

import io.grpc.stub.ServerCallStreamObserver;
import io.grpc.stub.StreamObserver;
import org.checkerframework.checker.nullness.qual.NonNull;
import ru.hse.GameObject;
import ru.hse.Room;
import ru.hse.objects.PlayerWithIO;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class RoomController {
    private final ArrayList<PlayerWithIO<Room.RoomEvent>> joinedPlayers = new ArrayList<PlayerWithIO<Room.RoomEvent>>();

    private final ArrayList<String> freeColors = new ArrayList<>(
                                            Arrays.asList("#D51216", "#4361D9", "#087B02",
                                                          "#01847F", "#F98434", "#F92BE5",
                                                          "#7E0181", "#830101", "#B39E2B",
                                                          "#9A602E", "#0100fB", "#483E83"));
    private final String roomName;
    private final int numberPlayersToStart;

    public RoomController(@NonNull String roomName, int numberPlayersToStart) {
        this.roomName = roomName;
        this.numberPlayersToStart = numberPlayersToStart;
    }

    public int getJoinedPlayersCount() {
        synchronized (joinedPlayers) {
            return joinedPlayers.size();
        }
    }

    public boolean joinPlayer(String playerLogin, StreamObserver<Room.RoomEvent> playerEventStream) {

        ServerCallStreamObserver<Room.RoomEvent> servEventStream = (ServerCallStreamObserver<Room.RoomEvent>) playerEventStream;
        servEventStream.setOnCloseHandler(new Runnable() {
            @Override
            public void run() {
                System.out.println("Player Close");
                onPlayerError(playerLogin);
            }
        });

        System.out.println("TEST CONN1");

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
                playerEventStream.onNext(value);
            }
            @Override
            public void onError(Throwable t) {
                playerEventStream.onError(t);
                onPlayerError(playerLogin);
            }
            @Override
            public void onCompleted() {
                playerEventStream.onCompleted();
            }
        };

        // Send JoinEvent
        String color = getFreeColor();
        GameObject.Player player = GameObject.Player.newBuilder().setColor(color).setLogin(playerLogin).build();

        Room.OtherPlayerJoinedEvent joinEvent = Room.OtherPlayerJoinedEvent.newBuilder().setPlayer(player).build();
        Room.RoomEvent event = Room.RoomEvent.newBuilder().setOtherPlayerJoinedEvent(joinEvent).build();
        broadcast(event);

        System.out.println("TEST CONN2");

        synchronized (joinedPlayers) {
            joinedPlayers.add(new PlayerWithIO<Room.RoomEvent>(player, eventStream));
        }
    
        // Send JoinResponse
        Room.JoinToRoomResponse response = Room.JoinToRoomResponse.newBuilder().setNumberPlayersToStart(numberPlayersToStart).addAllPlayer(getRoomPlayers()).setSuccess(true).build();
        Room.RoomEvent responseEvent = Room.RoomEvent.newBuilder().setJoinToRoomResponse(response).build();
        eventStream.onNext(responseEvent);
        System.out.println("TEST CONN3");

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

    private List<GameObject.Player> getRoomPlayers() {
        List<GameObject.Player> playerList = new ArrayList<>();
        synchronized(joinedPlayers) {
            joinedPlayers.forEach(playerWithIO -> {
                playerList.add(playerWithIO.getPlayer());
            });
        }

        return playerList;
    }

    private void disconnectPlayer(String playerLogin, boolean onError) {
        synchronized (joinedPlayers) {
            Optional<PlayerWithIO<Room.RoomEvent>> optionalPlayerWithIO = getPlayerWithIO(playerLogin);
            if (optionalPlayerWithIO.isPresent()) {
                PlayerWithIO<Room.RoomEvent> playerWithIO = optionalPlayerWithIO.get();

                if (!onError) {
                    try {
                        playerWithIO.getEventStream().onCompleted();
                    } catch (Exception ignored) {

                    }
                }
                freeColors.add(0, playerWithIO.getPlayer().getColor());
                joinedPlayers.remove(playerWithIO);

            }
        }

        // Send DisconnectEvent
        Room.OtherPlayerDisconnectedEvent event = Room.OtherPlayerDisconnectedEvent.newBuilder().setPlayerLogin(playerLogin).build();
        broadcast(Room.RoomEvent.newBuilder().setOtherPlayerDisconnectedEvent(event).build());
    }

    public Optional<PlayerWithIO<Room.RoomEvent>> getPlayerWithIO(String playerLogin) {
        synchronized (joinedPlayers) {
            return joinedPlayers.stream().filter(playerWithIO -> playerWithIO.getPlayer().getLogin().equals(playerLogin)).findFirst();
        }
    }

    public List<GameObject.Player> getPlayers() {
        synchronized (joinedPlayers) {
            return joinedPlayers.stream().map(PlayerWithIO::getPlayer).collect(Collectors.toList());
        }
    }

    public void disconnectAllPlayers() {
        synchronized (joinedPlayers) {
            joinedPlayers.forEach(playerWithIO -> playerWithIO.getEventStream().onCompleted());
            joinedPlayers.clear();
        }
    }

    public String getFreeColor() {
        synchronized (freeColors) {
            if (freeColors.isEmpty()) {
                return "#FF1864";
            } else {
                String color = freeColors.get(0);
                freeColors.remove(color);
                return color;
            }
        }
    }
}

