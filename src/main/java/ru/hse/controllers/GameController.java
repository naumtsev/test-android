package ru.hse.controllers;

import io.grpc.stub.ServerCallStreamObserver;
import io.grpc.stub.StreamObserver;
import ru.hse.Game;
import ru.hse.GameConfig;
import ru.hse.GameObject;
import ru.hse.gameObjects.Attack;
import ru.hse.gameObjects.GameMap;
import ru.hse.gameObjects.User;
import ru.hse.gameObjects.generateGameMap.Block;
import ru.hse.gameObjects.generateGameMap.CapturedBlock;
import ru.hse.objects.Pair;
import ru.hse.objects.PlayerWithIO;

import java.util.*;

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

    public void addAttack(GameObject.Player player, GameObject.BlockCoordinate start, GameObject.BlockCoordinate end, boolean is50) {
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

    public void deleteAttack(String playerLogin) {
        for (int i = 0; i < users.size(); i++) {
            User user = users.get(i);
            if (user.isAlive() && user.getLogin().equals(playerLogin)) {
                synchronized (users) {
                    user.clearSteps();
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
            Thread.sleep(GameConfig.waitingStartTime);
        } catch (InterruptedException ignored) {
        }

        while (running) {

            HashSet<String> nameAliveUsers = new HashSet<>();
            for(User user : users){
                if(user.isAlive()){
                    nameAliveUsers.add(user.getLogin());
                }
            }

            makeStep();

            synchronized (users) {
                for (var user: users) {
                    sendEventToPlayer(user.getLogin(), getGameStateForPlayer(user.getLogin()));
                }
            }

            for(User user : users){
                if(!user.isAlive() && nameAliveUsers.contains(user.getLogin())){
                    finishEventForPlayer(user.getLogin(), nameAliveUsers.size());
                    disconnectPlayer(user.getLogin(), false);
                    break;
                }
            }

            try {
                Thread.sleep(GameConfig.tickTime);
            } catch (Exception ignored) {
            }

            if (gameMap.getCountAliveCastels() <= 1 || joinedPlayers.size() <= 1) {
                running = false;
            }
        }

        // заканчиваем игру для последнего игрока
        for(User user : users){
            if(user.isAlive()){
                finishEventForPlayer(user.getLogin(), 1);
                disconnectPlayer(user.getLogin(), false);
            }
        }

        System.out.println("Game Finished");
        onFinish.run();
    }

    public Game.PlayerMovesResponse getMovesForPlayer(String login){
        Game.PlayerMovesResponse.Builder movesForPlayers = Game.PlayerMovesResponse.newBuilder();

        synchronized (users) {
            for (User user: users) {
                if (user.getLogin().equals(login)) {
                    for (Attack attack : user.getSteps()) {
                        movesForPlayers.addPlayerMove(attack.toProtobufMove());
                    }
                }
            }
        }

        return movesForPlayers.build();
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

        for (User user : users) {
            if(user.getLogin().equals(login)) {
                for(Attack attack : user.getSteps()) {
                    gameStateResponse.addPlayerMove(attack.toProtobufMove());
                }
            }
        }

        Game.GameEvent.Builder gameEvent = Game.GameEvent.newBuilder();
        gameEvent.setGameStateResponse(gameStateResponse.build());

        return gameEvent.build();
    }


    private void makeStep(){
        for(var castles : gameMap.getCastlesInMap()){
        }

        gameMap.nextTick();

        ArrayList<ArrayList<Block>> logGameMap = gameMap.getGameMap();
//        for(int y = 0; y < gameMap.getHeight(); y++){
//            for(int x = 0; x < gameMap.getWidth(); x++){
//                Block block = logGameMap.get(y).get(x);
//                if(block instanceof CapturedBlock){
//                    System.out.print(((CapturedBlock) block).getCountArmy() + "   ");
//                } else {
//                    System.out.print(0 + "   ");
//                }
//            }
//            System.out.println();
//        }


//        users.forEach(this::makeStepForPlayer);
        for (int i = 0; i < users.size(); i++) {
            User user = users.get(i);
            synchronized (users.get(i)) {
                makeStepForPlayer(user);
            }
        }

    }

    public void finishEventForPlayer(String login, Integer result) {
        Game.GameFinishedEvent.Builder gameStateResponse = Game.GameFinishedEvent.newBuilder();

        gameStateResponse.setReason("You take " + result.toString() + " place!");

        Game.GameEvent.Builder gameEvent = Game.GameEvent.newBuilder();
        gameEvent.setGameFinishedEvent(gameStateResponse.build());

        sendEventToPlayer(login,gameEvent.build());
    }

    private void makeStepForPlayer(User user){
        while ((user.isAlive() && user.haveStep())) {
            Attack attack = user.removeStep();
            if (!gameMap.attack(user, attack.getStart(), attack.getEnd(), attack.isIs50())) {
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