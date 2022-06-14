package ru.hse.gameObjects;

import ru.hse.Game;
import ru.hse.GameObject;
import ru.hse.gameObjects.generateGameMap.*;
import ru.hse.objects.Pair;

import java.util.ArrayList;
import java.util.Arrays;

public class GameMap {
    private final ArrayList<ArrayList<Block>> gameMap;
    private final ArrayList<User> users;
    private ArrayList<Pair> castlesInMap;
    private final int height;
    private final int width;
    private final int countCastels;
    private int countAliveCastels;
    // количество тиков, которые должны пройти до того момента, когда мы увеличим количество
    // войск для SimpleDrawableBlock клеток
    private final int numberOfTicksBeforeUpdate = 25;
    // количество тиков, которые были пройдены от времени последнего добавления армии обычным клеткам
    private int countCompletedTickets = 0;

    public GameMap(int height, int width, ArrayList<User> users) {
        this.height = height;
        this.width = width;
        this.countCastels = users.size();
        this.countAliveCastels = users.size();
        this.users = users;

        GeneratorGameMap generatorGameMap = new GeneratorGameMap();
        gameMap = generatorGameMap.generateGameMap(height, width, users.size());
        castlesInMap = generatorGameMap.getCoordinateCastlesInMap();

        giveCastlesToUsers();
    }


    public int getHeight(){
        return height;
    }

    public int getWidth(){
        return width;
    }

    public int getCountCastels(){
        return countCastels;
    }

    public int getCountAliveCastels(){
        return countAliveCastels;
    }

    public ArrayList<User> getUsers(){
        return users;
    }


    private void giveCastlesToUsers(){
        int i = 0;
        for(Pair castle : castlesInMap){
            ((CastleBlock)gameMap.get(castle.getX()).get(castle.getY())).setUser(users.get(i++));
        }
    }

    public ArrayList<ArrayList<Block>> getGameMap() {
        return gameMap;
    }

    // attack(start, end, is50);
    // true - если получилось захватить клетку, иначе false
    public synchronized boolean attack(Pair start, Pair end, boolean is50){
        if(!examinateCorrectAttack(start, end)){
            return false;
        }

        CapturedBlock startBlock = (CapturedBlock) gameMap.get(start.getX()).get(start.getY());
        CapturedBlock endBlock   = (CapturedBlock) gameMap.get(end.getX()).get(end.getY());

        User startUser = startBlock.getUser();
        User endUser = endBlock.getUser();

        if(startBlock.getCountArmy() < 2){
            return false;
        }

        int countArmyMove = (int)((startBlock.getCountArmy() - 1) * ((is50) ? (0.5) : 1));
        startBlock.setCountArmy(startBlock.getCountArmy() - countArmyMove);
        endBlock.addOrDeleteArmy(startUser, countArmyMove);

        if(startUser.equals(endUser)){
            return true;
        } else {
            if(startUser.equals(endBlock.getUser())){
                if(endBlock instanceof CastleBlock){
                    capturedCastle(startUser, endUser);
                }
                return true;
            } else {
                return false;
            }
        }
    }

    private boolean examinateCorrectAttack(Pair start, Pair end){
        Block startBlock = gameMap.get(start.getX()).get(start.getY());
        Block endBlock = gameMap.get(end.getX()).get(end.getY());
        if(!(startBlock instanceof CapturedBlock)){
            return false;
        }
        if(((CapturedBlock)startBlock).getUser() != null){
            return false;
        }
        if(!(endBlock instanceof CapturedBlock)){
            return false;
        }

        return true;
    }

    // capturedCastle(invader, captured) - нужно переделать в ферму королевство и обозначить игрока, который займёт её.
    public void capturedCastle(User invader, User captured){
        captured.userDead();
        countAliveCastels--;

        for(int x = 0; x < height; x++){
            for(int y = 0; y < width; y++){
                if(gameMap.get(x).get(y) instanceof CapturedBlock){
                    CapturedBlock block = (CapturedBlock) gameMap.get(x).get(y);
                    if(invader != null) {
                        invader.addOrDeleteArmy(block.getCountArmy());
                    }
                    if(block instanceof CastleBlock){
                        gameMap.get(x).set(y, new FarmBlock(x, y, block.getCountArmy(), invader));
                    } else {
                        block.setUser(invader);
                    }
                }
            }
        }
        captured.setCountArmy(0);
    }


    // nextTick(): которые для каждого активного королевства и фермы добавляет единицу в жизни
    public void nextTick(){
        ++countCompletedTickets;
        if(countCompletedTickets == numberOfTicksBeforeUpdate){
            countCompletedTickets = 0;
            for(int x = 0; x < height; x++){
                for(int y = 0; y < width; y++){
                    if(gameMap.get(x).get(y) instanceof CapturedBlock) {
                        ((CapturedBlock)gameMap.get(x).get(y)).nextTick();
                    }
                }
            }
        } else {
            // обновляем только для ферм и королевств
            for(int x = 0; x < height; x++){
                for(int y = 0; y < width; y++){
                    if(gameMap.get(x).get(y) instanceof FarmBlock || gameMap.get(x).get(y) instanceof CastleBlock) {
                        ((CapturedBlock)gameMap.get(x).get(y)).nextTick();
                    }
                }
            }
        }
    }

    public GameObject.GameMap toProtobufForPlayer(String login){
        GameObject.GameMap.Builder protobufGameMap = GameObject.GameMap.newBuilder();
        protobufGameMap.setHeight(height);
        protobufGameMap.setWidth(width);

        GameObject.BlockList.Builder blockList = GameObject.BlockList.newBuilder();
        for(int x = 0; x < protobufGameMap.getHeight(); x++){
            for(int y = 0; y < protobufGameMap.getWidth(); y++){
                Block blockInMap = gameMap.get(x).get(y);

                boolean hidden = blockIsHiddenForPlayer(x, y, login);

                if(blockInMap instanceof SimpleDrawableBlock){
                    blockList.addBlock(((SimpleDrawableBlock)blockInMap).toProtobuf(hidden));
                }

                if(blockInMap instanceof MountainBlock){
                    blockList.addBlock(((MountainBlock)blockInMap).toProtobuf(hidden));
                }

                if(blockInMap instanceof FarmBlock){
                    blockList.addBlock(((FarmBlock)blockInMap).toProtobuf(hidden));
                }

                if(blockInMap instanceof CastleBlock){
                    blockList.addBlock(((CastleBlock)blockInMap).toProtobuf(hidden));
                }

            }
        }

        protobufGameMap.setBlockList(blockList.build());

        return protobufGameMap.build();
    }


    Pair[] array = new Pair[]
            {       new Pair(-1, -1), new Pair(-1, 0), new Pair(-1, 1),
                    new Pair(0, -1), new Pair(0, 0), new Pair(0, 1),
                    new Pair(1, -1), new Pair(1, 0), new Pair(1, 1)
            };
    ArrayList<Pair> changeCoordinate = new ArrayList<>(Arrays.asList(array));
    private boolean blockIsHiddenForPlayer(int x, int y, String login){
        for(int i = 0; i < changeCoordinate.size(); i++){
            int changeX = changeCoordinate.get(i).getX();
            int changeY = changeCoordinate.get(i).getY();
            int newX = x + changeX;
            int newY = y + changeY;
            Block block = gameMap.get(newX).get(newY);
            if(
                    0 <= newX && newX < height &&
                            0 <= newY && newY < width &&
                            block instanceof CapturedBlock &&
                            ((CapturedBlock)block).getUser().getLogin().equals(login)
            ){
                return false;
            }
        }
        return true;
    }
}
