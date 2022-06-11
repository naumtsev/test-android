package ru.hse.gameObjects.generateGameMap;

import ru.hse.GameObject;

public class SimpleDrawableBlock extends CapturedBlock {
    SimpleDrawableBlock(int x, int y) {
        super(x, y);
        countArmy = 1;
    }

    public GameObject.Block toProtobuf(boolean isHidden){
        GameObject.Block.Builder block = super.toProtobuf(isHidden).toBuilder();

        GameObject.EmptyBlock.Builder blockInBlock = GameObject.EmptyBlock.newBuilder();

        blockInBlock.setCountArmy(countArmy);
        blockInBlock.setOwner(user.toProtobufPlayer());

        block.setEmptyBlock(blockInBlock.build());

        return block.build();
    }
}