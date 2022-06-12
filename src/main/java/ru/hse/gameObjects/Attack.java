package ru.hse.gameObjects;

import ru.hse.Game;
import ru.hse.GameObject;
import ru.hse.objects.Pair;

public class Attack {
    private final Pair start;
    private final Pair end;
    private final boolean is50;

    public Attack(Pair start, Pair end, boolean is50){
        this.start = start;
        this.end = end;
        this.is50 = is50;
    }

    public Pair getStart() {
        return start;
    }

    public Pair getEnd() {
        return end;
    }

    public boolean isIs50() {
        return is50;
    }

    public Game.AttackResponce toProtobuf(){
        Game.AttackResponce.Builder attack = Game.AttackResponce.newBuilder();

        attack.setStart(start.toProtobuf());
        attack.setEnd(end.toProtobuf());

        return attack.build();
    }
}