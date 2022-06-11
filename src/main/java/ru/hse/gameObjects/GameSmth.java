package ru.hse.gameObjects;

import com.google.gson.Gson;
import io.grpc.stub.StreamObserver;
import ru.hse.Game;
import ru.hse.GameObject;
import ru.hse.gameObjects.generateGameMap.*;
import ru.hse.objects.Pair;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.concurrent.ConcurrentHashMap;

public class GameSmth implements Runnable {
    boolean running = false;
    GameMap gameMap;
    ArrayList<User> users;

    public GameSmth(int height, int width, ArrayList<GameObject.Player> players) {
        ArrayList<User> users = new ArrayList<>();
        for(int i = 0; i < players.size(); i++){
            GameObject.Player player = players.get(i);
            users.add(new User(player.getLogin(), player.getColor()));
        }

        gameMap = new GameMap(height, width, users);
        this.users = users;
    }

    public GameMap getGameMap() {
        return gameMap;
    }

    public void addAttack(String login, Pair start, Pair end, boolean is50) {
        for (int i = 0; i < users.size(); i++) {
            User user = users.get(i);
            if (user.isAlive() && user.getLogin().equals(login)) {
                user.addStep(start, end, is50);
            }
        }
    }

    public void makePlayerLeave(String login){
        for(int i = 0; i < users.size(); i++) {
            User user = users.get(i);
            if(user.getLogin().equals(login)){
                gameMap.capturedCastle(null, user);
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

    public void run(){
        running = true;
        while (running) {
            // need to close game
            if (gameMap.getCountAliveCastels() == 1) {
                running = false;
            }

            makeStep();
            for(int i = 0; i < users.size(); i++){

            }

            try {
                Thread.sleep(1000);
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }
        }
        // отослать всем responce, что игра завершилась
    }


    private void sendMapToPlayer(String login, GameObject.GameStateResponse gameStateResponse){

    }

    private GameObject.GameStateResponse getGameStateForPlayer(String login){
        GameObject.GameStateResponse.Builder gameStateResponce = GameObject.GameStateResponse.newBuilder();

        if(running) {
            gameStateResponce.setGameState(GameObject.GameStateResponse.GameState.IN_PROGRESS);
        } else {
            gameStateResponce.setGameState(GameObject.GameStateResponse.GameState.FINISHED);
        }

        for(User user : users) {
            if(user.getLogin().equals(login)) {
                gameStateResponce.setPlayer(user.toProtobufPlayer());
            }
        }

        gameStateResponce.setGameMap(gameMap.toProtobufForPlayer(login));

        for(User user : users) {
            gameStateResponce.addGamePlayerInfo(user.toProtobufGamePlayerInfo());
        }

        return gameStateResponce.build();
    }


    private void makeStep(){
        gameMap.nextTick();

        for(int i = 0; i < users.size(); i++){
            User user = users.get(i);
            makeStepForPlayer(user);
        }
    }

    private void makeStepForPlayer(User user){
        while((user.isAlive() && !user.haveStep())) {
            Attack attack = user.removeStep();
            if (!gameMap.attack(attack.getStart(), attack.getEnd(), attack.isIs50())) {
                Pair endPosition = attack.getEnd();
                while (user.haveStep() && endPosition.equals(user.getStep().getStart())) {
                    endPosition = user.removeStep().getEnd();
                }
            } else {
                break;
            }
        }
    }

    public GameMap getFullMap(){
        int count = (int)(Math.random() * 1000);
        while(0 < count){
            count--;
            gameMap.nextTick();
        }
        return gameMap;
    }


}