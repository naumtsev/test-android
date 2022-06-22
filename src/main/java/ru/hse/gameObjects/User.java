package ru.hse.gameObjects;

import ru.hse.GameObject;
import ru.hse.objects.Pair;

import java.util.LinkedList;
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

    private LinkedList<Attack> steps = new LinkedList<>();

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

    public void userDead(){
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

    public void addStep(Pair start, Pair end, boolean is50){
        steps.addLast(new Attack(start, end, is50));
    }

    public void deleteStep(){
        if(steps.isEmpty()) {
            steps.removeLast();
        }
    }
    public boolean haveStep(){
        return !steps.isEmpty();
    }
    public Attack getStep(){
        return steps.getFirst();
    }

    public Attack removeStep(){
        return steps.removeFirst();
    }

    public LinkedList<Attack> getSteps(){
        return steps;
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

    public GameObject.Player toProtobufPlayer(){
        GameObject.Player.Builder player = GameObject.Player.newBuilder();
        player.setLogin(login);
        player.setColor(color);
        return player.build();
    }
    public GameObject.GamePlayerInfo toProtobufGamePlayerInfo(){
        GameObject.GamePlayerInfo.Builder gamePlayerInfo = GameObject.GamePlayerInfo.newBuilder();

        gamePlayerInfo.setPlayer(toProtobufPlayer());
        gamePlayerInfo.setCountArmy(countArmy);
        gamePlayerInfo.setCountLand(countPlace);
        gamePlayerInfo.setAlive(isAlive);

        return gamePlayerInfo.build();
    }
}