package ru.hse.objects;

import ru.hse.Game;
import ru.hse.GameObject;

public class Pair {
    private int x;
    private int y;

    public Pair(int x, int y){
        this.x = x;
        this.y = y;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public void setX(int x) {
        this.x = x;
    }

    public void setY(int y) {
        this.y = y;
    }

    public Game.BlockCoordinate toProtobuf(){
        Game.BlockCoordinate.Builder pair = Game.BlockCoordinate.newBuilder();
        pair.setX(x);
        pair.setY(y);
        return pair.build();
    }
}