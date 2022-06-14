package ru.hse.services;

import com.google.protobuf.Empty;
import io.grpc.Server;
import io.grpc.stub.ServerCallStreamObserver;
import io.grpc.stub.StreamObserver;
import ru.hse.Game;
import ru.hse.GameObject;
import ru.hse.GameServiceGrpc;
import ru.hse.controllers.GameController;
import ru.hse.objects.PlayerWithIO;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class GameService extends GameServiceGrpc.GameServiceImplBase {
    private final GameController gameController;
    private Server server;
    List<GameObject.Player> players;
    public GameService(int height, int width, List<GameObject.Player> players) {
        this.gameController = new GameController(height, width, players);
        this.players = players;
    }

    public void setServer(Server server) {
        this.server = server;
    }

    @Override
    public void joinToGame(Game.JoinToGameRequest request, StreamObserver<Game.GameEvent> eventStream) {
        ServerCallStreamObserver<Game.GameEvent> servEventStream = (ServerCallStreamObserver<Game.GameEvent>) eventStream;
        String playerLogin = request.getLogin();

        Optional<GameObject.Player> playerOptional = players.stream().filter(pl -> pl.getLogin().equals(playerLogin)).findFirst();

        if (playerOptional.isEmpty()) {
            System.out.println("Player: " + playerLogin + " is not registered to this game");
            eventStream.onCompleted();
            return;
        }

        gameController.joinToGame(request, eventStream);
    }

    public void attackBlock(Game.AttackRequest request, StreamObserver<Empty> responseObserver) {
        gameController.addAttack(request.getPlayer(), request.getStart(), request.getEnd(), request.getIs50());
    }

    public void surrender(Game.SurrenderRequest request, StreamObserver<Empty> responseObserver) {
        gameController.gs
    }
}
