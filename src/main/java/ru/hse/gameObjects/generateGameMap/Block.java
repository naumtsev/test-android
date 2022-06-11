package ru.hse.gameObjects.generateGameMap;

import ru.hse.GameObject;

public abstract class Block {
    protected final int x;
    protected final int y;
    Block(int x, int y){
        this.x = x;
        this.y = y;
    }

    public GameObject.Block toProtobuf(boolean isHidden){
        GameObject.Block.Builder block = GameObject.Block.newBuilder();
        block.setX(x);
        block.setY(y);
        block.setHidden(isHidden);

        return block.build();
    }
}