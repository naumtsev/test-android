package ru.hse.gameObjects.generateGameMap;

import ru.hse.GameObject;
import ru.hse.gameObjects.generateGameMap.CapturedBlock;

public class CastleBlock extends CapturedBlock {
    CastleBlock(int x, int y) {
        super(x, y);
        countArmy = 1;
    }

    public GameObject.Block toProtobuf(boolean isHidden){
        GameObject.Block.Builder block = super.toProtobuf(isHidden).toBuilder();

        GameObject.CastleBlock.Builder blockInBlock = GameObject.CastleBlock.newBuilder();

        blockInBlock.setCountArmy(countArmy);
        blockInBlock.setOwner(user.toProtobufPlayer());

        block.setCastleBlock(blockInBlock.build());

        return block.build();
    }
}
