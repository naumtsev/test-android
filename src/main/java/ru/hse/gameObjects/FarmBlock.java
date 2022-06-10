package ru.hse.gameObjects;

import java.util.Random;

public class FarmBlock extends CapturedBlock{
    private final int minCountArmy = 40;
    FarmBlock(int x, int y) {
        super(x, y);
        countArmy = minCountArmy + (new Random().nextInt(15));
    }

    FarmBlock(int x, int y, int countArmy){
        super(x, y);
        this.countArmy = countArmy;
    }
}