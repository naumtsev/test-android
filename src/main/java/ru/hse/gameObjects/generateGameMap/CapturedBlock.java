package ru.hse.gameObjects.generateGameMap;

import ru.hse.gameObjects.User;

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


    public void setCountArmy(int newCountArmy){
        countArmy = newCountArmy;
    }
    public void addOrDeleteArmy(User invader, int otherArmy){
        if(invader.equals(user)){
            countArmy += otherArmy;
        } else {
            if(countArmy < otherArmy){
                if(!isFree()) {
                    user.addOrDeleteArmy(-countArmy);
                    user.addOrDeletePlace(-1);
                }
                invader.addOrDeleteArmy(-countArmy);
                invader.addOrDeletePlace(1);
                setUser(invader);
                countArmy = otherArmy - countArmy;
            } else {
                if(!isFree()) {
                    user.addOrDeleteArmy(-otherArmy);
                }
                invader.addOrDeleteArmy(-otherArmy);
                countArmy -= otherArmy;
            }
        }
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
