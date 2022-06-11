package ru.hse.gameObjects;

public class CapturedBlock extends Block {
    protected User user = null;
    protected int countArmy;

    CapturedBlock(int x, int y){
        super(x, y);
        countArmy = 0;
    }

    public User getUser(){
        return user;
    }

    public void setUser(User newUser){
        this.user = newUser;
    }


    // TODO: скорее всего нужно будет ещё user передвать, чтобы можно
    // было быстро менять его в случае захвата
    // изменить: можно просто клетку передава

    // передаем новое количество воинов в армии
    public void setArmy(int otherArmy){
        // TODO: тут нужно у user ещё дополнительно что-то вызывать
        // всё же по ссылкам работаем и нужно сумму всех очечей считать
        // если что нужно менять именно пользователя
        if(user == null){
            throw new RuntimeException("Block: x=" + x + ", y=" + y + " don't have user!!!");
        }
        user.addOrDeleteArmy(otherArmy - countArmy);
        countArmy = otherArmy;
    }

    public boolean isFree(){
        return user == null;
    }

    public int getCountArmy(){
        return countArmy;
    }

    public void nextTick(){
        if(user != null) {
            ++countArmy;
        }
    }
}
