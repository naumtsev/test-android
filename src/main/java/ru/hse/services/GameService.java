package ru.hse.services;

import com.google.protobuf.Empty;
import io.grpc.Server;
import io.grpc.stub.ServerCallStreamObserver;
import io.grpc.stub.StreamObserver;
import ru.hse.Game;
import ru.hse.GameObject;
import ru.hse.GameServiceGrpc;
import ru.hse.controllers.GameController;

import java.util.List;
import java.util.Optional;

public class GameService extends GameServiceGrpc.GameServiceImplBase {
    private final GameController gameController;
    private Server server;
    List<GameObject.Player> players;
    public GameService(int height, int width, List<GameObject.Player> players) {
        this.gameController = new GameController(height, width, players, this::onFinish);
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

    public void attackBlock(Game.AttackRequest request, StreamObserver<Game.PlayerMovesResponse> responseObserver) {
        gameController.addAttack(request.getPlayer(), request.getStart(), request.getEnd(), request.getIs50());
        responseObserver.onNext(gameController.getMovesForPlayer(request.getPlayer().getLogin()));
        responseObserver.onCompleted();
    }

    public void deleteAttack(Game.ClearAttacksRequest request, StreamObserver<Game.PlayerMovesResponse> responseObserver) {
        gameController.deleteAttack(request.getPlayerLogin());
        responseObserver.onNext(gameController.getMovesForPlayer(request.getPlayerLogin()));
        responseObserver.onCompleted();
    }

    public void surrender(Game.SurrenderRequest request, StreamObserver<Empty> responseObserver) {
        gameController.makePlayerLeave(request.getPlayerLogin());
        responseObserver.onCompleted();
    }

    public void onFinish() {
        server.shutdownNow();
    }
}
