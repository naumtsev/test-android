package ru.hse.services;

import com.google.protobuf.Empty;
import io.grpc.stub.StreamObserver;
import ru.hse.Game;
import ru.hse.RoomEventServiceGrpc;
import ru.hse.GameEvents;
import ru.hse.controllers.GameController;
import ru.hse.controllers.PlayersController;
import ru.hse.controllers.RoomController;
import ru.hse.gameObjects.*;

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
            // передаем количество игроков и потом карту
            // TODO: раскомментить  
            // createdGames.put(gameId, new GameController(room.getCountPlayersWaiting()));

            // присылаем им запросы на присоединение
            GameEvents.GameStartedEvent.Builder res = GameEvents.GameStartedEvent.newBuilder();
            res.setGameId(gameId);
            var r = GameEvents.RoomEvent.newBuilder().setGameStartedEvent(res.build());
            playersController.sendEventToAllPlayers(r);
        }
    }

    @Override
    public void joinToGame(GameEvents.JoinToGameRequest request, StreamObserver<GameEvents.GameEvent> responseObserver) {
        super.joinToGame(request, responseObserver);
    }

    @Override
    public void attackBlock(GameEvents.AttackRequest request, StreamObserver<Empty> responseObserver) {
        super.attackBlock(request, responseObserver);
    }

    @Override
    public void surrender(GameEvents.SurrenderRequest request, StreamObserver<Empty> responseObserver) {
        super.surrender(request, responseObserver);
    }

    @Override
    public void getGameMap(GameEvents.GetGameMap request, StreamObserver<GameEvents.GameStateUpdatedEvent> responseObserver) {
        // создаем новых пользователей
        ArrayList<User> users = new ArrayList<>();

        for(int i = 0; i < request.getCountPlayer(); i++){
            users.add(new User(generateString(), ""));
        }

        GameController gameController = new GameController(request.getHeight(), request.getWidth(), request.getCountPlayer(), users);
        GameMap gameFullMap = gameController.getFullMap();
//        message GameStateResponse {
//            enum GameState {
//                IN_PROGRESS = 0;
//                FINISHED = 1;
//            }
//            GameState gameState = 1;
//            Player player = 2;
//            GameMap gameMap = 3;
//            PlayerList playerList = 4;
//        }

//        GameEvents.GameStateUpdatedEvent.Builder res = GameEvents.GameStateUpdatedEvent.newBuilder();
//        res.setGameState();

        Game.GameStateResponse.Builder res = Game.GameStateResponse.newBuilder();
        // 1) Добавили gameState
        res.setGameState(Game.GameStateResponse.GameState.FINISHED);
        // 2) Сейчас это не нужно
//        res.setPlayer(Player);

        // 3) Карту генерим
        Game.GameMap.Builder gameMap = Game.GameMap.newBuilder();

        Game.BlockList.Builder blockList = Game.BlockList.newBuilder();
        for(int x = 0; x < gameFullMap.getHeight(); x++){
            for(int y = 0; y < gameFullMap.getWidth(); y++){
                Game.Block.Builder block = Game.Block.newBuilder();
                block.setX(x);
                block.setY(y);
                block.setIsHidden(false);
                if(gameFullMap.getGameMap().get(x).get(y) instanceof SimpleDrawableBlock){
                    Game.EmptyBlock.Builder blockInBlock = Game.EmptyBlock.newBuilder();
                    blockInBlock.setCountArmy(((SimpleDrawableBlock) gameFullMap.getGameMap().get(x).get(y)).getCountArmy());

                    Game.Player.Builder player = Game.Player.newBuilder();
                    player.setLogin(((SimpleDrawableBlock) gameFullMap.getGameMap().get(x).get(y)).getUser().getLogin());
                    player.setColor(((SimpleDrawableBlock) gameFullMap.getGameMap().get(x).get(y)).getUser().getColor());
                    player.setCountArmy(((SimpleDrawableBlock) gameFullMap.getGameMap().get(x).get(y)).getUser().getCountArmy());
                    player.setCountPlace(((SimpleDrawableBlock) gameFullMap.getGameMap().get(x).get(y)).getUser().getCountPlace());
                    player.setIsAlive(((SimpleDrawableBlock) gameFullMap.getGameMap().get(x).get(y)).getUser().isAlive());

                    blockInBlock.setOwner(player.build());
                }

                if(gameFullMap.getGameMap().get(x).get(y) instanceof MountainBlock){

                }

                if(gameFullMap.getGameMap().get(x).get(y) instanceof FarmBlock){
                    Game.EmptyBlock.Builder blockInBlock = Game.EmptyBlock.newBuilder();
                    blockInBlock.setCountArmy(((FarmBlock) gameFullMap.getGameMap().get(x).get(y)).getCountArmy());

                    Game.Player.Builder player = Game.Player.newBuilder();
                    player.setLogin(((FarmBlock) gameFullMap.getGameMap().get(x).get(y)).getUser().getLogin());
                    player.setColor(((FarmBlock) gameFullMap.getGameMap().get(x).get(y)).getUser().getColor());
                    player.setCountArmy(((FarmBlock) gameFullMap.getGameMap().get(x).get(y)).getUser().getCountArmy());
                    player.setCountPlace(((FarmBlock) gameFullMap.getGameMap().get(x).get(y)).getUser().getCountPlace());
                    player.setIsAlive(((FarmBlock) gameFullMap.getGameMap().get(x).get(y)).getUser().isAlive());

                    blockInBlock.setOwner(player.build());
                }

                if(gameFullMap.getGameMap().get(x).get(y) instanceof CastleBlock){
                    Game.EmptyBlock.Builder blockInBlock = Game.EmptyBlock.newBuilder();
                    blockInBlock.setCountArmy(((CastleBlock) gameFullMap.getGameMap().get(x).get(y)).getCountArmy());

                    Game.Player.Builder player = Game.Player.newBuilder();
                    player.setLogin(((CastleBlock) gameFullMap.getGameMap().get(x).get(y)).getUser().getLogin());
                    player.setColor(((CastleBlock) gameFullMap.getGameMap().get(x).get(y)).getUser().getColor());
                    player.setCountArmy(((CastleBlock) gameFullMap.getGameMap().get(x).get(y)).getUser().getCountArmy());
                    player.setCountPlace(((CastleBlock) gameFullMap.getGameMap().get(x).get(y)).getUser().getCountPlace());
                    player.setIsAlive(((CastleBlock) gameFullMap.getGameMap().get(x).get(y)).getUser().isAlive());

                    blockInBlock.setOwner(player.build());
                }

                blockList.addBlock(block.build());
            }
        }

        gameMap.setHeight(gameFullMap.getHeight());
        gameMap.setWidth(gameFullMap.getWidth());
        gameMap.setBlockList(blockList.build());

        // 4) добавили игроков
        Game.PlayerList.Builder playerList = Game.PlayerList.newBuilder();
        for(int i = 0; i < users.size(); i++){
            Game.Player.Builder player = Game.Player.newBuilder();
            player.setLogin(users.get(i).getLogin());
            player.setColor(users.get(i).getColor());
            player.setCountArmy(users.get(i).getCountArmy());
            player.setCountPlace(users.get(i).getCountPlace());
            player.setIsAlive(users.get(i).isAlive());

            playerList.addPlayer(player.build());
        }
        res.setPlayerList(playerList.build());
        //-----------------


        var r = GameEvents.GameStateUpdatedEvent.newBuilder().setGameStateResponse(res.build());
        responseObserver.onNext(r.build());
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
