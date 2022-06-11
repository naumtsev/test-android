package ru.hse.gameObjects.generateGameMap;

import ru.hse.objects.Pair;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Random;

public class GeneratorGameMap {
    // карта = создаётся в своём формате
    private ArrayList<ArrayList<Cell>> gameMapNeedForCreate;
    // массив из координат королевств
    private ArrayList<Pair> coordinateCastlesInMap;
    private ArrayList<Pair> walls;
    // нужен при проверки на то, что из любого королевства можно в любое другое прийти
    private ArrayList<ArrayList<Integer>> visited;
    private int height;
    private int width;
    private int countCastels;
    private int minFarmsNumbers;
    private final int NUMBER_GENERATE_MAPS = 100;
    // с какой вероятностью будет идти в соседнюю клетку и строить там тоже препятствие
    private final double factorForWall = 0.2;
    // коэффициент, сколько будем точек брать и запускаться от них, чтобы построить препятствия
    private final double numberOfTriggerToCreateWall = 0.1;
    // коэффицент, с какой вероятностью будет респавниться ферма рядом с королевском:
    // (rows + columns - расстояние от ближайшего королевства) * (коэфф) / (rows + columns)
    private final double factorForFarms = 0.2;
    // коэффициент, сколько будет точек брать и пытаться поставить ферму на это место
    private final double numberOfTriggerToCreateFarm = 0.05;

    private static Pair[] array = new Pair[]
            {new Pair(-1, 0), new Pair(1, 0), new Pair(0, -1), new Pair(0, 1)};
    private static ArrayList<Pair> changeCoordinate = new ArrayList<>(Arrays.asList(array));

    public GeneratorGameMap(){}

    public ArrayList<Pair> getCoordinateCastlesInMap(){
        return coordinateCastlesInMap;
    }

    public ArrayList<ArrayList<Block>> generateGameMap(int height, int width, int countCastels){
        preparateToCreateMap();

        this.height = height;
        this.width = width;
        this.countCastels = countCastels;
        this.minFarmsNumbers = countCastels * 2;

        for (int i = 0; i < height; i++) {
            ArrayList<Cell> arrMap = new ArrayList<>(width);
            ArrayList<Integer> arrVisited = new ArrayList<>(width);
            for (int j = 0; j < width; j++) {
                arrMap.add(new Cell());
                arrVisited.add(0);
            }
            this.gameMapNeedForCreate.add(arrMap);
            this.visited.add(arrVisited);
        }

        createSceletonMap();

        return createMap();
    }

    private void preparateToCreateMap(){
        gameMapNeedForCreate = new ArrayList<>();
        coordinateCastlesInMap = new ArrayList<>();
        visited = new ArrayList<>();
        walls = new ArrayList<>();
        height = 0;
        width = 0;
        countCastels = 0;
        minFarmsNumbers = 0;
    }

    private void createSceletonMap() {
        // план
        // 1) Создать какое-то количество карт(подобрать константу)
        // 2) Запускаем рандомизированное добавление государст на карту
        // 3) Проверяем, что расстояние между государствами большое(тут вопросик, может нужно сначала
        // генерить карту, а потом государства, т.к. наверно расстояние лучше брать, как кратчайшить путь)
        // 4) Выбираем карту, у которой будет самое наибольшее наименьшее расстояние между игроками
        // 5) Запускаем алгоритм генерации препятствий
        // 6) Проверяем, что одна компонента связности, иначе опять запускаем(или можно удалить какие-то
        // элементики, чтобы одна компонента стала)
        // 7) Кидаем какое-то количество ферм на карту, которые с большей вероятностью появляются ближе к замкам
        // 8) Удаляем какие-то фермы(допустим, чтобы замок не был окрёжен фермами)
        // (можно просто для каждой свободной вершины поставить ферму с коэффициентом, который зависити от расстояния
        // до ближайшего королевства)

        // 1)
        ArrayList<ArrayList<Pair>> mapsCastels = new ArrayList<>(10);
        for (int i = 0; i < NUMBER_GENERATE_MAPS; i++) {
            // 2)
            ArrayList<Pair> castles = new ArrayList<>(countCastels);
            for (int j = 0; j < countCastels; j++) {
                int x, y;

                while (true) {
                    x = new Random().nextInt(height);       // TODO: тут нужно посмотреть, может не стоит создавать
                    y = new Random().nextInt(width);        // много элементов
                    if(gameMapNeedForCreate.get(x).get(y).isFree()){
                        break;
                    }
                }

                castles.add(new Pair(x, y));
                gameMapNeedForCreate.get(x).get(y).setCellType(Cell.Type.Castle);
            }

            mapsCastels.add(castles);
            for(Pair castle : castles){
                gameMapNeedForCreate.get(castle.getX()).get(castle.getY()).setCellType(Cell.Type.Neutral);
            }
        }
        // 3) - 4)
        int index = 0;
        int maxMinDistance = 0;
        for (int i = 0; i < NUMBER_GENERATE_MAPS; i++) {
            int minDistanceInIteration = 0;
            for (int firstCastle = 0; firstCastle < countCastels; firstCastle++) {
                for (int secondCastle = firstCastle + 1; secondCastle < countCastels; secondCastle++) {
                    int x1 = mapsCastels.get(i).get(firstCastle).getX();
                    int y1 = mapsCastels.get(i).get(firstCastle).getY();
                    int x2 = mapsCastels.get(i).get(secondCastle).getX();
                    int y2 = mapsCastels.get(i).get(secondCastle).getY();

                    if (firstCastle == 0 && secondCastle == 1) {
                        minDistanceInIteration = Math.abs(x1 - x2) + Math.abs(y1 - y2);
                    }

                    if (Math.abs(x1 - x2) + Math.abs(y1 - y2) < minDistanceInIteration) {
                        minDistanceInIteration = Math.abs(x1 - x2) + Math.abs(y1 - y2);
                    }
                }
            }
            if (i == 0 || maxMinDistance < minDistanceInIteration) {
                maxMinDistance = minDistanceInIteration;
                index = i;
            }
        }

        for (int i = 0; i < countCastels; i++) {
            int x = mapsCastels.get(index).get(i).getX();
            int y = mapsCastels.get(index).get(i).getY();
            gameMapNeedForCreate.get(x).get(y).setCellType(Cell.Type.Castle);

            System.out.println("X = " + x + "\nY = " + y + "\n");
        }

        // сохраним координаты всех королевств на карте
        this.coordinateCastlesInMap = mapsCastels.get(index);

        // 5) - 6)
        addWallsInMap();

        // 7 - 8)
        addFarmsInMap();
    }


    private void addFarmsInMap() {
        int countLauches = (int) (height * width * numberOfTriggerToCreateFarm);
        int minFarms = minFarmsNumbers;
        // или пока не выполним определенное количество попыток
        // или минимальное количество ферм ещё не было установлено
        while (countLauches > 0 || minFarms > 0) {
            countLauches--;
            int x = new Random().nextInt(height);
            int y = new Random().nextInt(width);
            boolean isFarm = tryToAddFarm(x, y);
            if(isFarm){
                minFarms--;
            }
        }
    }

    private boolean tryToAddFarm(int x, int y) {
//        double distanceNearestCastleFromPoint = 0;

//        for (Pair castle : castlesInMap) {
//            distanceNearestCastleFromPoint = Math.max(distanceNearestCastleFromPoint,
//                    Math.abs(castle.getX() - x) + Math.abs(castle.getY() - y));
//        }

//        if ((height + width - distanceNearestCastleFromPoint) / (double)(2 * (height + width)) < factorForFarms
//                && gameMapNeedForCreate.get(x).get(y).isFree()) {
//            gameMapNeedForCreate.get(x).get(y).setCellType(Cell.Type.Farm);
//            return true;
//        }

        if (gameMapNeedForCreate.get(x).get(y).isFree()) {
            gameMapNeedForCreate.get(x).get(y).setCellType(Cell.Type.Farm);
            return true;
        }
        return false;
    }
    private void addWallsInMap() {
        while (true) {
            int countLauches = (int) (height * width * numberOfTriggerToCreateWall);
            while (countLauches > 0) {
                countLauches--;
                int x = new Random().nextInt(height);     // TODO: тут нужно посмотреть, может стоит один создать
                int y = new Random().nextInt(width);  // [0; rows) и [0; columns)
                tryToAddWallsInDifferentDirections(x, y);
            }

            // 6)
            // запустим поиск в ширину из одного королевства, если получилось дойти до всех других, то закончим
            // если не получится, то нужно откатить все изменения и запустить этот алгоритм снова
            // создадим друмерный массив, в котором будем помечать клетки (visited)

            LinkedList<Pair> queue = new LinkedList<>();
            queue.add(coordinateCastlesInMap.get(0));

            while (!queue.isEmpty()) {
                Pair v = queue.removeFirst();
                if (visited.get(v.getX()).get(v.getY()) == 0) {
                    visited.get(v.getX()).set(v.getY(), 1);
                    for(int i = 0; i < changeCoordinate.size(); i++){
                        int x = v.getX();
                        int y = v.getY();
                        int changeX = changeCoordinate.get(i).getX();
                        int changeY = changeCoordinate.get(i).getY();
                        if(0 <= x + changeX && x + changeX < height &&
                           0 <= y + changeY && y + changeY < width  &&
                            canMoveThroughCell(x + changeX, y + changeY)){
                            queue.addLast(new Pair(x + changeX, y + changeY));
                        }
                    }
                }
            }

            boolean isBreak = true;
            for (int i = 0; i < countCastels; i++) {
                if (visited.get(coordinateCastlesInMap.get(i).getX()).get(coordinateCastlesInMap.get(i).getY()) == 0) {
                    isBreak = false;
                    break;
                }
            }

            if (isBreak) {
                break;
            }

            for (int i = 0; i < height; i++) {
                for (int j = 0; j < width; j++) {
                    visited.get(i).set(j, 0);
                }
            }

            for (int i = 0; i < walls.size(); i++) {
                gameMapNeedForCreate.get(walls.get(i).getX()).get(walls.get(i).getY()).setCellType(Cell.Type.Neutral);
            }
            walls.clear();
        }
    }

    private boolean canMoveThroughCell(int x, int y) {
        return (gameMapNeedForCreate.get(x).get(y).getType().equals(Cell.Type.Neutral)
                || gameMapNeedForCreate.get(x).get(y).getType().equals(Cell.Type.Farm)
                || gameMapNeedForCreate.get(x).get(y).getType().equals(Cell.Type.Castle));
    }

    private void tryToAddWallsInDifferentDirections(int x, int y) {
        if (gameMapNeedForCreate.get(x).get(y).isFree()) {
            walls.add(new Pair(x, y));
            gameMapNeedForCreate.get(x).get(y).setCellType(Cell.Type.Wall);
            for(int i = 0; i < changeCoordinate.size(); i++){
                int changeX = changeCoordinate.get(i).getX();
                int changeY = changeCoordinate.get(i).getY();
                if(0 <= x + changeX && x + changeX < height && 0 <= y + changeY && y + changeY < width){
                    if (Math.random() < factorForWall) {
                        tryToAddWallsInDifferentDirections(x + changeX, y + changeY);
                    }
                }
            }
        }
    }


    private ArrayList<ArrayList<Block>> createMap(){
        ArrayList<ArrayList<Block>> gameMap = new ArrayList<>();

        for(int x = 0; x < height; x++){
            gameMap.add(new ArrayList<>());
            for(int y = 0; y < width; y++){
                Cell.Type type = gameMapNeedForCreate.get(x).get(y).getType();
                if(type.equals(Cell.Type.Neutral)){
                    gameMap.get(x).add(new SimpleDrawableBlock(x, y));
                    continue;
                }
                if(type.equals(Cell.Type.Wall)){
                    gameMap.get(x).add(new MountainBlock(x, y));
                    continue;
                }
                if(type.equals(Cell.Type.Farm)){
                    gameMap.get(x).add(new FarmBlock(x, y));
                    continue;
                }
                if(type.equals(Cell.Type.Castle)){
                    gameMap.get(x).add(new CastleBlock(x, y));
                }
            }
        }

        return gameMap;
    }
}
