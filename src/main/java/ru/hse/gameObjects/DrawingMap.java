package ru.hse.gameObjects;

import java.util.ArrayList;
import com.google.gson.annotations.SerializedName;

public class DrawingMap {
    @SerializedName("user")
    private final User user;
    @SerializedName("height")
    private final int height;
    @SerializedName("width")
    private final int width;
    @SerializedName("map")
    private final ArrayList<ArrayList<DrawingBlock>> map;

    public DrawingMap(User user, int height, int width, ArrayList<ArrayList<DrawingBlock>> map){
        this.user = user;
        this.height = height;
        this.width = width;
        this.map = map;
    }

    public User getUser(){
        return user;
    }

    public int getHeight(){
        return height;
    }

    public int getWidth(){
        return width;
    }

    public ArrayList<ArrayList<DrawingBlock>> getMap(){
        return map;
    }
}