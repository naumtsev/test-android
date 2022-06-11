package ru.hse.gameObjects.generateGameMap;

import ru.hse.GameObject;
import ru.hse.gameObjects.generateGameMap.Block;

public class MountainBlock extends Block {
    MountainBlock(int x, int y) {
        super(x, y);
    }

    public GameObject.Block toProtobuf(boolean isHidden){
        GameObject.Block.Builder block = super.toProtobuf(isHidden).toBuilder();

        GameObject.WallBlock.Builder blockInBlock = GameObject.WallBlock.newBuilder();
        block.setWallBlock(blockInBlock.build());

        return block.build();
    }
}