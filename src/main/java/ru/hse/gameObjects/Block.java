package ru.hse.gameObjects;

public abstract class Block {
    protected final int x;
    protected final int y;

    // ещё можно цвета добавить
    static enum Color{
        White,
        Red,
        Blue
    }

    protected Color background = Color.White;
    Block(int x, int y){
        this.x = x;
        this.y = y;
    }
}