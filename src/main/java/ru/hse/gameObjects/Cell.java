package ru.hse.gameObjects;

public class Cell {
    // добавить поле, которое будет отвечать за id игрока, которые владеет данной клеткой (изначально отрицательно)

    // добавить поле, занята кем-то эта ячейка или нет (uses или haveCastles)

    // переделать атаку для ячейки: idFirst, idSecond, army.

    // nextTick(): добавляет единицу в количество армии

    public enum Type {
        Neutral,    // пустая клетка
        Wall,       // препятствие, через которое невозможно пройти
        Farm,       // ферма, которую если захватить, то можно получать дополнительные очки
        Castle,     // замок некоторого игрока
    }

    static enum Condition {
        Free,       // клетка никем не занята
        Engaged     // клетка кем-то занята
    }

    private Type cellType;
    private Condition cellCondition;

    public Cell() {
        cellType = Type.Neutral;
        cellCondition = Condition.Free;
    }

    // TODO: (наверно лучше тут что-то впилить, что будет возращать castleIf, если не пустая клетка)
    // Возращает true, если клетка никем не занята
    public boolean isFree() {
        return cellCondition.equals(Condition.Free);
    }

    // Возращает тип клетки
    public Type getType() {
        return cellType;
    }

    public void setCellType(Type newType) {
        cellType = newType;
        if (newType.equals(Type.Neutral)) {
            setCellCondition(Condition.Free);
        } else {
            setCellCondition(Condition.Engaged);
        }
    }

    private void setCellCondition(Condition cellCondition) {
        this.cellCondition = cellCondition;
    }
}