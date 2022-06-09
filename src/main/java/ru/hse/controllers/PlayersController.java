package ru.hse.controllers;

import io.grpc.stub.StreamObserver;
import ru.hse.Game;
import ru.hse.GameEvents;

import java.util.ArrayList;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class PlayersController {
    private int countPlayes = 0;
    private ConcurrentHashMap<String, StreamObserver<GameEvents.RoomEvent>> playersObservers = new ConcurrentHashMap<>();

    public PlayersController(){
    }

    public int getCountPlayes(){
        return countPlayes;
    }

    public Set<String> getPlayers(){
        return playersObservers.keySet();
    }

    public synchronized boolean addPlayer(String playerName, StreamObserver<GameEvents.RoomEvent> streamObserver){
        if(playersObservers.containsKey(playerName)) {
            playersObservers.put(playerName, streamObserver);
            ++countPlayes;
            return true;
        }
        return false;
    }

    public synchronized boolean deletePlayer(String playerName){
        if(playersObservers.containsKey(playerName)){
            playersObservers.remove(playerName);
            --countPlayes;
            return true;
        }
        return false;
    }

    // должен выдавать массив имён игроков, которые
    public synchronized ArrayList<String> pingPlayers() {
        ArrayList<String> disconnectPlayers = new ArrayList<>();
        for(var pairPlayer : playersObservers.entrySet()){
            try {
                GameEvents.PingRequest.Builder ping = GameEvents.PingRequest.newBuilder();
                ping.setPing(true);
                var r = GameEvents.RoomEvent.newBuilder().setPing(ping);
                pairPlayer.getValue().onNext(r.build());
            } catch (Exception e) {
                // отрубаем поток для чтения
                pairPlayer.getValue().onCompleted();
                disconnectPlayers.add(pairPlayer.getKey());
            }
        }

        // удаляем игроков из map
        for(String playerName : disconnectPlayers){
            deletePlayer(playerName);
        }

        return disconnectPlayers;
    }

    // передаём игрока и сразу event, который нужно будет ему отослать
    public void sendEventToOnePlayer(String playerName, GameEvents.RoomEvent.Builder event){
        try {
            playersObservers.get(playerName).onNext(event.build());
        } catch(Exception e){
            // удаляем игрока, если случился дисконект
            playersObservers.get(playerName).onCompleted();
            deletePlayer(playerName);
        }
    }

    // TODO: проверить, что event может вызываться несколько раз
    public void sendEventToAllPlayers(GameEvents.RoomEvent.Builder event) {
        ArrayList<String> disconnectPlayers = new ArrayList<>();

        for(var pairPlayer : playersObservers.entrySet()){
            try {
                pairPlayer.getValue().onNext(event.build());
            } catch (Exception e) {
                // отрубаем поток для чтения
                pairPlayer.getValue().onCompleted();
                disconnectPlayers.add(pairPlayer.getKey());
            }
        }

        // удаляем игроков из map
        for(String playerName : disconnectPlayers){
            deletePlayer(playerName);
        }
    }
}
