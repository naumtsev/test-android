package ru.hse.services;

import com.google.protobuf.Empty;
import io.grpc.Server;
import io.grpc.stub.ServerCallStreamObserver;
import io.grpc.stub.StreamObserver;
import ru.hse.Game;
import ru.hse.GameObject;
import ru.hse.GameServiceGrpc;
import ru.hse.Room;
import ru.hse.gameObjects.GameSmth;
import ru.hse.objects.PlayerWithIO;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class GameService extends GameServiceGrpc.GameServiceImplBase {
    private final GameSmth gameSmth;
    private Server server;
    private final ArrayList<PlayerWithIO<Game.GameEvent>> joinedPlayers = new ArrayList<PlayerWithIO<Game.GameEvent>>();

    public GameService(int height, int width, List<GameObject.Player> players) {
        gameSmth = new GameSmth(height, width, players);
    }

    public void setServer(Server server) {
        this.server = server;
    }

    @Override
    public void joinToGame(Game.JoinToGameRequest request, StreamObserver<Game.GameEvent> playerEventStream) {
        ServerCallStreamObserver<Game.GameEvent> servEventStream = (ServerCallStreamObserver<Game.GameEvent>) playerEventStream;
        String playerLogin = request.getLogin();

        servEventStream.setOnCloseHandler(new Runnable() {
            @Override
            public void run() {
                System.out.println("Player Close");
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


    }


    @Override
    public void attackBlock(Game.AttackRequest request, StreamObserver<Empty> responseObserver) {
        super.attackBlock(request, responseObserver);
    }

    @Override
    public void surrender(Game.SurrenderRequest request, StreamObserver<Empty> responseObserver) {
        super.surrender(request, responseObserver);
    }



    public void broadcast(Game.GameEvent event) {
        synchronized(joinedPlayers) {
            for (var playerWithIO : joinedPlayers) {
                playerWithIO.getEventStream().onNext(event);
            }
        }
    }

    private void onPlayerError(String playerLogin) {
        disconnectPlayer(playerLogin, true);
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
                joinedPlayers.remove(playerWithIO);

            }
        }

        // Send DisconnectEvent
//        Game.GameEvent event = Game;
//        broadcast(Room.RoomEvent.newBuilder().setOtherPlayerDisconnectedEvent(event).build());
    }


    public Optional<PlayerWithIO<Game.GameEvent>> getPlayerWithIO(String playerLogin) {
        synchronized (joinedPlayers) {
            return joinedPlayers.stream().filter(playerWithIO -> playerWithIO.getPlayer().getLogin().equals(playerLogin)).findFirst();
        }
    }

    public void disconnectAllPlayers() {
        synchronized (joinedPlayers) {
            joinedPlayers.forEach(playerWithIO -> playerWithIO.getEventStream().onCompleted());
            joinedPlayers.clear();
        }
    }
}
