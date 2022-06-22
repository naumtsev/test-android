package ru.hse.objects;

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

    public GameObject.BlockCoordinate toProtobuf(){
        GameObject.BlockCoordinate.Builder blockCoordinate = GameObject.BlockCoordinate.newBuilder();

        blockCoordinate.setX(x);
        blockCoordinate.setY(y);

        return blockCoordinate.build();
    }
}