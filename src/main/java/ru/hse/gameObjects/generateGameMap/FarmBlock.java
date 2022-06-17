package ru.hse.gameObjects.generateGameMap;

import ru.hse.GameObject;
import ru.hse.gameObjects.User;
import ru.hse.gameObjects.generateGameMap.CapturedBlock;

import java.util.Random;

public class FarmBlock extends CapturedBlock {
    private final int minCountArmy = 40;

    FarmBlock(int x, int y) {
        super(x, y);
        countArmy = minCountArmy + (new Random().nextInt(15));
    }

    public FarmBlock(int x, int y, int countArmy, User user) {
        super(x, y);
        this.countArmy = countArmy;
        this.user = user;
    }

    public GameObject.Block toProtobuf(boolean isHidden){
        GameObject.Block.Builder block = super.toProtobuf(isHidden).toBuilder();

        GameObject.FarmBlock.Builder blockInBlock = GameObject.FarmBlock.newBuilder();

        blockInBlock.setCountArmy(countArmy);
        if(user != null) {
            blockInBlock.setOwner(user.toProtobufPlayer());
        }
        block.setFarmBlock(blockInBlock.build());

        return block.build();
    }
}