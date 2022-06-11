package ru.hse.gameObjects;

public abstract class Block {
    protected final int x;
    protected final int y;
    Block(int x, int y){
        this.x = x;
        this.y = y;
    }
}