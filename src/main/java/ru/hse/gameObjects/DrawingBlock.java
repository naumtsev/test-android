package ru.hse.gameObjects;

import com.google.gson.annotations.SerializedName;

public class DrawingBlock {

    public enum Type{
        Neutral,
        Wall,
        Farm,
        Castle
    }

    @SerializedName("x")
    private final int x;

    @SerializedName("y")
    private final int y;

    @SerializedName("draw")
    private final boolean draw;

    @SerializedName("type")
    private final Type type;

    public DrawingBlock(int x, int y, boolean draw){
        this.x = x;
        this.y = y;
        this.draw = draw;
        this.type = null;
    }

    public DrawingBlock(int x, int y, boolean draw, Type type){
        this.x = x;
        this.y = y;
        this.draw = draw;
        this.type = type;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public boolean isDraw(){
        return draw;
    }

    // если нельзя рисовать, то торчит null наружу
    public Type getType(){
        return type;
    }
}