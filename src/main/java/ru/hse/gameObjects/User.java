package ru.hse.gameObjects;

import java.util.Objects;

public class User {
    public enum Color{
        WHITE,
        BLACK,
        YELLOW,
        GREEN,
    }
    private final String login;
    private String color;
    private int countArmy = 1;
    private int countPlace = 1;
    private boolean isAlive = true;

    public User(String login, String color){
        this.login = login;
        this.color = color;
    }

    public String getLogin() {
        return login;
    }

    public String getColor(){
        return color;
    }

    public int getCountArmy(){
        return countArmy;
    }

    public int getCountPlace(){
        return countPlace;
    }

    public boolean isAlive() {
        return isAlive;
    }

    public void userIsDead(){
        isAlive = false;
    }

    public void setColor(String color){
        this.color = color;
    }

    public void setCountArmy(int countArmy){
        this.countArmy = countArmy;
    }

    public void setCountPlace(int countPlace){
        this.countPlace = countPlace;
    }
    // добавляем или удаляем войска
    public void addOrDeleteArmy(int addOrDeleteCountArmy){
        countArmy += addOrDeleteCountArmy;
    }

    public void addOrDeletePlace(int addOrDeleteCountPlace){
        countPlace += addOrDeleteCountPlace;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return login == user.login && countArmy == user.countArmy && color == user.color;
    }

    @Override
    public int hashCode() {
        return Objects.hash(login, color, countArmy);
    }
}