package ru.hse.gameObjects;

import io.grpc.stub.StreamObserver;
import ru.hse.GameEvents;
import ru.hse.controllers.PlayersController;

import java.util.ArrayList;

public class Room {
    private final String roomName;
    private final int maxCountPlayers;

    private PlayersController playersController = new PlayersController();

    public Room(String roomName, int maxCountPlayers){
        this.roomName = roomName;
        this.maxCountPlayers = maxCountPlayers;
    }

    public String getRoomName(){
        return roomName;
    }
    public int getCountPlayersWaiting(){
        return playersController.getCountPlayesWaiting();
    }

    public boolean fullRoom(){
        return maxCountPlayers == getCountPlayersWaiting();
    }
    public boolean addPlayer(String playerName, StreamObserver<GameEvents.RoomEvent> streamObserver){
        return playersController.addPlayer(playerName, streamObserver);
    }

    public boolean deletePlayer(String playerName){
        return playersController.deletePlayer(playerName);
    }

    // возвращает массив из логинов игроков, которые отсоединились
    public ArrayList<String> ping(){
        ArrayList<String> disconnectPlayers = playersController.pingPlayers();
        for(String playerName : disconnectPlayers){
            deletePlayer(playerName);
        }
        return disconnectPlayers;
    }
}
