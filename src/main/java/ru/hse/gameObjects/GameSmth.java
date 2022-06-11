package ru.hse.gameObjects;

import com.google.gson.Gson;
import ru.hse.gameObjects.*;
import ru.hse.objects.Pair;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;

public class GameSmth {
    GameMap gameMap;
    ArrayList<User> users;
    ArrayList<LinkedList<Attack>> movesUsers = new ArrayList<>();

    public GameSmth(int height, int width, int countCastles, ArrayList<User> users) {
        gameMap = new GameMap(height, width, countCastles, users);
        this.users = users;
    }

    public GameMap getGameMap() {
        return gameMap;
    }

    public void addMove(String login, Pair start, Pair end, boolean is50) {
        for (int i = 0; i < users.size(); i++) {
            if (users.get(i).isAlive() && users.get(i).getLogin().equals(login)) {
                movesUsers.get(i).addLast(new Attack(start, end, is50));
            }
        }
    }

    // TODO: при умирании игрока, все ходы игрока должны быть уничтожены (можно в принципе ничего с ними не делать)
    public void attack(Pair start, Pair end, boolean is50) {
        // TODO: должна быть проверка, что ход делает именно игрок, который захватил еду клетку
        gameMap.attack(start, end, is50);
        // тут нужно для каждого игрока брать один элемент из буфера и пытаться сделать ход
        for (int i = 0; i < movesUsers.size(); i++) {
            if (users.get(i).isAlive() && !movesUsers.get(i).isEmpty()) {
                Attack attack = movesUsers.get(i).removeFirst();
                // тут удаляем ходы, которые не должны будут выполниться
                if (!gameMap.attack(attack.getStart(), attack.getEnd(), attack.isIs50())) {
                    Pair endPosition = attack.getEnd();
                    while (!movesUsers.get(i).isEmpty() && endPosition == movesUsers.get(i).getFirst().getStart()) {
                        endPosition = movesUsers.get(i).removeFirst().getEnd();
                    }
                }
            }
        }
        // TODO: также нужно давать сигнал, если не получилось сделать ход, чтобы все последующие ходы не отрисовывались
        // ----- сама логика уже реализована
        // т.к. ходы не должны отрисовываться, то нужно их тупо убрать(то есть если следующий ход начинается с прошлого
        // конца, то нужно при неудачном перемещении удалить этот ход)
    }

    //
    public void getGameMapForUser(User user) {
        boolean haveUser = false;
        // TODO: тут может быть проблемаа с users -> users = null
//        for(int i = 0; i < users.size(); i++){
//            if(user == users.get(i)){
//                haveUser = true;
//                break;
//            }
//        }
//        if(!haveUser){
//            throw new RuntimeException("Don't find user: " + user.getId());
//        }

        Gson gson = new Gson();

        try (FileWriter writer = new FileWriter("map1.json")) {
            ArrayList<ArrayList<DrawingBlock>> arrayDrawingMap = new ArrayList<ArrayList<DrawingBlock>>();
            ArrayList<ArrayList<Block>> map = gameMap.getGameMap();

            int height = gameMap.getHeight();
            int width = gameMap.getWidth();

            for (int x = 0; x < height; x++) {
                arrayDrawingMap.add(new ArrayList<DrawingBlock>());
                for (int y = 0; y < width; y++) {
                    boolean haveNearbyUserBlock = false;
                    if (0 <= x - 1 && 0 <= y - 1
                            && (map.get(x - 1).get(y - 1) instanceof CapturedBlock)
                            && ((CapturedBlock) map.get(x - 1).get(y - 1)).getUser() != null
                            && ((CapturedBlock) map.get(x - 1).get(y - 1)).getUser().equals(user)) {
                        haveNearbyUserBlock = true;
                    }
                    if (0 <= x - 1
                            && (map.get(x - 1).get(y) instanceof CapturedBlock)
                            && ((CapturedBlock) map.get(x - 1).get(y)).getUser() != null
                            && ((CapturedBlock) map.get(x - 1).get(y)).getUser().equals(user)) {
                        haveNearbyUserBlock = true;
                    }
                    if (0 <= x - 1 && y + 1 < width
                            && (map.get(x - 1).get(y + 1) instanceof CapturedBlock)
                            && ((CapturedBlock) map.get(x - 1).get(y + 1)).getUser() != null
                            && ((CapturedBlock) map.get(x - 1).get(y + 1)).getUser().equals(user)) {
                        haveNearbyUserBlock = true;
                    }
                    if (0 <= y - 1
                            && (map.get(x).get(y - 1) instanceof CapturedBlock)
                            && ((CapturedBlock) map.get(x).get(y - 1)).getUser() != null
                            && ((CapturedBlock) map.get(x).get(y - 1)).getUser().equals(user)) {
                        haveNearbyUserBlock = true;
                    }

                    if ((map.get(x).get(y) instanceof CapturedBlock)
                            && ((CapturedBlock) map.get(x).get(y)).getUser() != null
                            && ((CapturedBlock) map.get(x).get(y)).getUser().equals(user)) {
                        haveNearbyUserBlock = true;
                    }
                    if (y + 1 < width
                            && (map.get(x).get(y + 1) instanceof CapturedBlock)
                            && ((CapturedBlock) map.get(x).get(y + 1)).getUser() != null
                            && ((CapturedBlock) map.get(x).get(y + 1)).getUser().equals(user)) {
                        haveNearbyUserBlock = true;
                    }
                    if (x + 1 < height && 0 <= y - 1
                            && (map.get(x + 1).get(y - 1) instanceof CapturedBlock)
                            && ((CapturedBlock) map.get(x + 1).get(y - 1)).getUser() != null
                            && ((CapturedBlock) map.get(x + 1).get(y - 1)).getUser().equals(user)) {
                        haveNearbyUserBlock = true;
                    }
                    if (x + 1 < height
                            && (map.get(x + 1).get(y) instanceof CapturedBlock)
                            && ((CapturedBlock) map.get(x + 1).get(y)).getUser() != null
                            && ((CapturedBlock) map.get(x + 1).get(y)).getUser().equals(user)) {
                        haveNearbyUserBlock = true;
                    }

                    if (x + 1 < height && y + 1 < width
                            && (map.get(x + 1).get(y + 1) instanceof CapturedBlock)
                            && ((CapturedBlock) map.get(x + 1).get(y + 1)).getUser() != null
                            && ((CapturedBlock) map.get(x + 1).get(y + 1)).getUser().equals(user)) {
                        haveNearbyUserBlock = true;
                    }

                    if (haveNearbyUserBlock) {
                        if (map.get(x).get(y).getClass().equals(SimpleDrawableBlock.class)) {
                            arrayDrawingMap.get(x).add(new DrawingBlock(x, y, true, DrawingBlock.Type.Neutral));
                        }
                        if (map.get(x).get(y).getClass().equals(MountainBlock.class)) {
                            arrayDrawingMap.get(x).add(new DrawingBlock(x, y, true, DrawingBlock.Type.Wall));
                        }
                        if (map.get(x).get(y).getClass().equals(FarmBlock.class)) {
                            arrayDrawingMap.get(x).add(new DrawingBlock(x, y, true, DrawingBlock.Type.Farm));
                        }
                        if (map.get(x).get(y).getClass().equals(CastleBlock.class)) {
                            arrayDrawingMap.get(x).add(new DrawingBlock(x, y, true, DrawingBlock.Type.Castle));
                        }
                    } else {
                        arrayDrawingMap.get(x).add(new DrawingBlock(x, y, false));
                    }
                }
            }

            DrawingMap drawingMap = new DrawingMap(user, height, width, arrayDrawingMap);
            writer.write(gson.toJson(drawingMap));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public GameMap getFullMap(){
        // 1) Создать карту
        // 2) Сделать какое-то количество ходов
        // 3) Обновить количество армии и полей у каждого игрока
        // 4) Сформировать ответ


        // 1) создали
        // 2) сделали какое-то количество ходов
        int count = (int)(Math.random() * 1000);
        while(0 < count){
            count--;
            gameMap.nextTick();
        }
        // 3) само обновляется
        // 4) сформировать ответ
        return gameMap;
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

    }
}