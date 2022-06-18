package ru.hse.controllers;

import io.grpc.stub.ServerCallStreamObserver;
import io.grpc.stub.StreamObserver;
import ru.hse.Game;
import ru.hse.GameObject;
import ru.hse.gameObjects.Attack;
import ru.hse.gameObjects.GameMap;
import ru.hse.gameObjects.User;
import ru.hse.gameObjects.generateGameMap.Block;
import ru.hse.gameObjects.generateGameMap.CapturedBlock;
import ru.hse.objects.Pair;
import ru.hse.objects.PlayerWithIO;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class GameController implements Runnable {
    boolean running = false;
    private final List<PlayerWithIO<Game.GameEvent>> joinedPlayers = new ArrayList<PlayerWithIO<Game.GameEvent>>();
    private final List<GameObject.Player> players;

    GameMap gameMap;
    final ArrayList<User> users;

    Runnable onFinish;

    Boolean wasStarted = false;

    public GameController(int height, int width, List<GameObject.Player> players, Runnable onFinish) {
        this.onFinish = onFinish;
        ArrayList<User> users = new ArrayList<User>();
        for(int i = 0; i < players.size(); i++){
            GameObject.Player player = players.get(i);
            users.add(new User(player.getLogin(), player.getColor()));
        }

        gameMap = new GameMap(height, width, users);
        this.users = users;

        this.players = players;
    }

    public GameMap getGameMap() {
        return gameMap;
    }

    public void addAttack(GameObject.Player player, Game.BlockCoordinate start, Game.BlockCoordinate end, boolean is50) {
        System.out.println("Get attack: \n  "
                        + "x_start=" + start.getX() + "; y_start=" + start.getY() + "\n  "
                        + "x_end=" + end.getX() + "; y_end=" + end.getY());
        Pair startPair = new Pair(start.getX(), start.getY());
        Pair endPair = new Pair(end.getX(), end.getY());

        for (int i = 0; i < users.size(); i++) {
            User user = users.get(i);
            if (user.isAlive() && user.getLogin().equals(player.getLogin())) {
                synchronized (users) {
                        user.addStep(startPair, endPair, is50);
                }
            }
        }
    }


    public void joinToGame(Game.JoinToGameRequest request, StreamObserver<Game.GameEvent> eventStream) {
        ServerCallStreamObserver<Game.GameEvent> servEventStream = (ServerCallStreamObserver<Game.GameEvent>) eventStream;
        String playerLogin = request.getLogin();

        Optional<GameObject.Player> playerOptional = players.stream().filter(pl -> pl.getLogin().equals(playerLogin)).findFirst();

        if (playerOptional.isEmpty()) {
            System.out.println("Player: " + playerLogin + " is not registered to this game");
            eventStream.onCompleted();
            return;
        }

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



        synchronized (joinedPlayers) {
            joinedPlayers.add(new PlayerWithIO<Game.GameEvent>(playerOptional.get(), eventStream));
        }

        System.out.println("Joined player: " + playerLogin);

        synchronized (wasStarted) {
            if (!wasStarted) {
                wasStarted = true;
                Thread thread = new Thread(this);
                thread.start();
            }
        }

    }


    public void makePlayerLeave(String login){
        for(int i = 0; i < users.size(); i++) {
            User user = users.get(i);
            if(user.getLogin().equals(login)){
                synchronized (users.get(i)) {
                    gameMap.capturedCastle(null, user);
                }
            }
        }
    }

    public boolean playerIsAlive(String login){
        for(int i = 0; i < users.size(); i++) {
            User user = users.get(i);
            if(user.getLogin().equals(login)){
                return user.isAlive();
            }
        }
        return false; // никогда не дойдёт до этого, если запрашиваемый игрок есть в игре
    }

    @Override
    public void run(){
        running = true;
        try {
            Thread.sleep(5000);
        } catch (InterruptedException ignored) {
        }

        while (running) {

            makeStep();

            synchronized (users) {
                for (var user: users) {
                    sendEventToPlayer(user.getLogin(), getGameStateForPlayer(user.getLogin()));
//                }
                }
            }

            try {
                Thread.sleep(1000);
            } catch (Exception ignored) {

            }

            if (gameMap.getCountAliveCastels() <= 1) {
                running = false;
            }
        }

        System.out.println("Game Finished");
        onFinish.run();
        // отослать всем responce, что игра завершилась
    }

    private Game.GameEvent getGameStateForPlayer(String login) {
        GameObject.GameStateResponse.Builder gameStateResponse = GameObject.GameStateResponse.newBuilder();

        if (running) {
            gameStateResponse.setGameState(GameObject.GameStateResponse.GameState.IN_PROGRESS);
        } else {
            gameStateResponse.setGameState(GameObject.GameStateResponse.GameState.FINISHED);
        }

        for (User user : users) {
            if(user.getLogin().equals(login)) {
                gameStateResponse.setPlayer(user.toProtobufPlayer());
            }
        }

        gameStateResponse.setGameMap(gameMap.toProtobufForPlayer(login));

        for(User user : users) {
            gameStateResponse.addGamePlayerInfo(user.toProtobufGamePlayerInfo());
        }

        Game.GameEvent.Builder gameEvent = Game.GameEvent.newBuilder();
        gameEvent.setGameStateResponse(gameStateResponse.build());

        return gameEvent.build();
    }


    private void makeStep(){
        for(var castles : gameMap.getCastlesInMap()){
            System.out.println("x = " + castles.getX() + "; y = " + castles.getY() + "\n");
        }

        gameMap.nextTick();

        ArrayList<ArrayList<Block>> logGameMap = gameMap.getGameMap();
        for(int y = 0; y < gameMap.getHeight(); y++){
            for(int x = 0; x < gameMap.getWidth(); x++){
                Block block = logGameMap.get(y).get(x);
                if(block instanceof CapturedBlock){
                    System.out.print(((CapturedBlock) block).getCountArmy() + "   ");
                } else {
                    System.out.print(0 + "   ");
                }
            }
            System.out.println();
        }


//        users.forEach(this::makeStepForPlayer);
        for(int i = 0; i < users.size(); i++){
            User user = users.get(i);
            synchronized (users.get(i)) {
                makeStepForPlayer(user);
            }
        }
    }

    private void makeStepForPlayer(User user){
        while ((user.isAlive() && user.haveStep())) {
            Attack attack = user.removeStep();
            System.out.println("Player " + user.getLogin() + " make step!");
            System.out.println("Step: \n  "
                    + "x_start=" + attack.getStart().getX() + "; y_start=" + attack.getStart().getY() + "\n  "
                    + "x_end=" + attack.getEnd().getX() + "; y_end=" + attack.getEnd().getY());
            if (!gameMap.attack(user, attack.getStart(), attack.getEnd(), attack.isIs50())) {
                System.out.println("Attack false!");
                Pair endPosition = attack.getEnd();
                while (user.haveStep() && endPosition.equals(user.getStep().getStart())) {
                    endPosition = user.removeStep().getEnd();
                }
            } else {
                break;
            }
        }
    }
    public void broadcast(Game.GameEvent event) {
        synchronized (joinedPlayers) {
            for (var playerWithIO: joinedPlayers) {
                playerWithIO.getEventStream().onNext(event);
            }
        }
    }

    public boolean sendEventToPlayer(String playerLogin, Game.GameEvent event) {
        synchronized (joinedPlayers) {
            for (var playerWithIO: joinedPlayers) {
                if (playerWithIO.getPlayer().getLogin().equals(playerLogin)) {
//                    System.out.println("Sent " + event.getEventCase() + " to player: " + playerLogin);
                    playerWithIO.getEventStream().onNext(event);
                    return true;
                }
            }
        }
        return false;
    }

    private void onPlayerError(String playerLogin) {
        disconnectPlayer(playerLogin, true);
    }

    private void disconnectPlayer(String playerLogin, boolean onError) {
        synchronized (joinedPlayers) {
            Optional<PlayerWithIO<Game.GameEvent>> optionalPlayerWithIO = getPlayerWithIO(playerLogin);
            if (optionalPlayerWithIO.isPresent()) {
                PlayerWithIO<Game.GameEvent> playerWithIO = optionalPlayerWithIO.get();

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