package ru.hse.controllers;

import ru.hse.Game;
import ru.hse.objects.PlayerWithIO;

import java.util.ArrayList;

public class GameController implements Runnable {
    private final ArrayList<PlayerWithIO<Game.GameEvent>> joinedPlayers = new ArrayList<PlayerWithIO<Game.GameEvent>>();
    private boolean running = false;

    public GameController() {



    }



    public void run() {

        while (running) {

        }


    }


    public boolean isRunning() {
        return running;
    }

    public void setRunning(boolean running) {
        this.running = running;
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


}
