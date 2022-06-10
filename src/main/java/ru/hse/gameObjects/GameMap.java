package ru.hse.gameObjects;

import ru.hse.objects.Pair;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Map;
import java.util.Random;

public class GameMap {
    // карта = создаётся в своём формате
    private final ArrayList<ArrayList<Cell>> gameMapNeedForCreate = new ArrayList<>();
    // карта = будет использоваться во время игры
    private final ArrayList<ArrayList<Block>> gameMap = new ArrayList<ArrayList<Block>>();
    // массив игроков, которые были в начале игры
    private final ArrayList<User> users;
    // массив из координат королевств
    private ArrayList<Pair> castlesInMap = new ArrayList<>();
    // нужен для отката изменений, если оказалось, что плохо создали карту и для какой-то пары королевств
    // не существует пути между ними
    private final ArrayList<Pair> walls = new ArrayList<>();
    // нужен при проверки на то, что из любого королевства можно в любое другое прийти
    private final ArrayList<ArrayList<Integer>> visited = new ArrayList<>();
    private final int height;
    private final int width;
    private final int countCastels;
    private final int minFarmsNumbers;
    private boolean createdMap = false;
    private final int NUMBER_GENERATE_MAPS = 100;
    // с какой вероятностью будет идти в соседнюю клетку и строить там тоже препятствие
    private final double factorForWall = 0.23;
    // коэффициент, сколько будем точек брать и запускаться от них, чтобы построить препятствия
    private final double numberOfTriggerToCreateWall = 0.14;
    // коэффицент, с какой вероятностью будет респавниться ферма рядом с королевском:
    // (rows + columns - расстояние от ближайшего королевства) * (коэфф) / (rows + columns)
    private final double factorForFarms = 0.2;
    // коэффициент, сколько будет точек брать и пытаться поставить ферму на это место
    private final double numberOfTriggerToCreateFarm = 0.15;
    // количество тиков, которые должны пройти до того момента, когда мы увеличим количество
    // войск для SimpleDrawableBlock клеток
    private final int numberOfTicksBeforeUpdate = 25;
    // количество тиков, которые были пройдены от времени последнего добавления армии обычным клеткам
    private int countCompletedTickets = 0;

    public GameMap(int height, int width, int countCastels, ArrayList<User> users) {
        // TODO: проверки, например не может быть countCastels < 2
        this.height = height;
        this.width = width;
        this.countCastels = countCastels;
        this.minFarmsNumbers = countCastels * 2;
        this.users = users;

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
    }

    public boolean isCreatedMap() {
        return createdMap;
    }

    public int getHeight(){
        return height;
    }

    public int getWidth(){
        return width;
    }

    public int getCountCastels(){
        return countCastels;
    }

    public ArrayList<User> getUsers(){
        return users;
    }

    // Создание карты
    //------------------------------------------------------------------------------------------------------------------
    private void createSceletonMap() {
        createdMap = true;

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
                    // будем по манхетонской метрике сравнивать
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
            // теперь выбираем максимальное минимальное расстояние из всевозможных
            if (i == 0 || maxMinDistance < minDistanceInIteration) { // TODO: странно, пока что ошибку выдает
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
        this.castlesInMap = mapsCastels.get(index);

        // 5) - 6)
        addWallsInMap();

        // 7 - 8)
        addFarmsInMap();

        // создаём карту по скелету сгенерированной карты
        createMap();

        // раздаём королевства игрокам
        giveCastlesToUsers();
    }

    private void createMap(){
//        for(int x = 0; x < height; x++) {
//            for (int y = 0; y < width; y++) {
//                Cell.Type type = gameMapNeedForCreate.get(x).get(y).getType();
//                if(type.equals(Cell.Type.Neutral)){
//                    System.out.print("0 ");
//                    continue;
//                }
//                if(type.equals(Cell.Type.Wall)){
//                    System.out.printf("1 ");
//                    continue;
//                }
//                if(type.equals(Cell.Type.Farm)){
//                    System.out.printf("2 ");
//                    continue;
//                }
//                if(type.equals(Cell.Type.Castle)){
//                    System.out.printf("3 ");
//                }
//            }
//            System.out.print("\n");
//        }
//        System.out.print("\n");
//        System.out.print("\n");
//        System.out.print("\n");


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
    }

    private void addFarmsInMap() {
        int countLauches = (int) (height * width * numberOfTriggerToCreateFarm);
        int minFarms = minFarmsNumbers;
        // или пока не выполним определенное количество попыток
        // или минимальное количество ферм ещё не было установлено
        while (countLauches > 0 || minFarms > 0) {
            countLauches--;
            int x = new Random().nextInt(height);     // TODO: тут нужно посмотреть, может стоит один создать
            int y = new Random().nextInt(width);  // [0; rows) и [0; columns)
            boolean isFarm = tryToAddFarm(x, y);
            if(isFarm){
                minFarms--;
            }
        }
    }

    // возращает truе = если получилось поставить ферму на это место.
    private boolean tryToAddFarm(int x, int y) {
        double distanceNearestCastleFromPoint = 0;
        // нашли минимальное расстояние до ближайшего королевства
        for (Pair castle : castlesInMap) {
            distanceNearestCastleFromPoint = Math.max(distanceNearestCastleFromPoint,
                    Math.abs(castle.getX() - x) + Math.abs(castle.getY() - y));
        }
        // пытаемся поставить ферму в этом место
        if ((height + width - distanceNearestCastleFromPoint) / (double)(2 * (height + width)) < factorForFarms
                && gameMapNeedForCreate.get(x).get(y).isFree()) {
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

            queue.add(castlesInMap.get(0));
            while (!queue.isEmpty()) {
                Pair v = queue.removeFirst();
                if (visited.get(v.getX()).get(v.getY()) == 0) {
                    visited.get(v.getX()).set(v.getY(), 1);
                    if (0 <= v.getX() - 1 && canMoveThroughCell(v.getX() - 1, v.getY())) {
                        queue.addLast(new Pair(v.getX() - 1, v.getY()));
                    }
                    if (v.getX() + 1 < height && canMoveThroughCell(v.getX() + 1, v.getY())) {
                        queue.addLast(new Pair(v.getX() + 1, v.getY()));
                    }
                    if (0 <= v.getY() - 1 && canMoveThroughCell(v.getX(), v.getY() - 1)) {
                        queue.addLast(new Pair(v.getX(), v.getY() - 1));
                    }
                    if (v.getY() + 1 < width && canMoveThroughCell(v.getX(), v.getY() + 1)) {
                        queue.addLast(new Pair(v.getX(), v.getY() + 1));
                    }
                }
            }

//            for(int i = 0; i < rows; i++) {
//                for (int j = 0; j < columns; j++) {
//                    if (gameMap.get(i).get(j).getType().equals(Cell.Type.Neutral)) {
//                        System.out.print("0 ");
//                    }
//                    if (gameMap.get(i).get(j).getType().equals(Cell.Type.Wall)) {
//                        System.out.print("1 ");
//                    }
//                    if (gameMap.get(i).get(j).getType().equals(Cell.Type.Farm)) {
//                        System.out.print("2 ");
//                    }
//                    if (gameMap.get(i).get(j).getType().equals(Cell.Type.Castle)) {
//                        System.out.print("3 ");
//                    }
//                }
//                System.out.print("\n");
//            }
//            System.out.print("\n");

            boolean isBreak = true;
            for (int i = 0; i < countCastels; i++) {
                if (visited.get(castlesInMap.get(i).getX()).get(castlesInMap.get(i).getY()) == 0) {
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
            walls.add(new Pair(x, y));  // добавляем координаты измененной ячейки в массив
            gameMapNeedForCreate.get(x).get(y).setCellType(Cell.Type.Wall);
            // запускаем вверх
            if (0 <= x - 1) {
                if (Math.random() < factorForWall) {
                    tryToAddWallsInDifferentDirections(x - 1, y);
                }
            }
            // запускаем вниз
            if (x + 1 < height) {
                if (Math.random() < factorForWall) {
                    tryToAddWallsInDifferentDirections(x + 1, y);
                }
            }
            // запускаем влево
            if (0 <= y - 1) {
                if (Math.random() < factorForWall) {
                    tryToAddWallsInDifferentDirections(x, y - 1);
                }
            }
            // запускаем вправо
            if (y + 1 < width) {
                if (Math.random() < factorForWall) {
                    tryToAddWallsInDifferentDirections(x, y + 1);
                }
            }
        }
    }

    private void giveCastlesToUsers(){
        // cначала пусть будем ручками выдавать цвет
        ArrayList<String> colors = new ArrayList<>();
        colors.add("RED");
        colors.add("BLUE");
        colors.add("GREEN");
        colors.add("YELLOW");
        colors.add("BROWN");
        colors.add("BLACK");
        colors.add("ORANGE");

        int i = 0;
        for(Pair castle : castlesInMap){
            System.out.println(users.get(i).getLogin());
            users.get(i).setColor(colors.get(i));
            System.out.println(users.get(i).getColor());
            ((CastleBlock)gameMap.get(castle.getX()).get(castle.getY())).setUser(users.get(i++));
        }
    }
    //------------------------------------------------------------------------------------------------------------------

    public ArrayList<ArrayList<Block>> getGameMap() {
        return gameMap;
    }

    // attack(start, end, is50);
    // true - если получилось захватить клетку, иначе false
    public boolean attack(Pair start, Pair end, boolean is50){
        // TODO: нужно как-то проверять, что ход сделал именно игрок, которого клетка в данный момент хода

        // TODO: при добавлении новых клеток, скорее всего они не будут захватываемыми
        // TODO: поэтому придётся менять эти проверки немного

        // проверяем, что клетки могут быть захвачены
        if(!(gameMap.get(start.getX()).get(start.getY()) instanceof CapturedBlock)){
            return false;
        }
        // проверяем, что можно сходить
        if(((CapturedBlock)gameMap.get(start.getX()).get(start.getY())).getUser() != null){
            return false;
        }
        // проверяем, что клетка может быть захвачена
        if(!(gameMap.get(end.getX()).get(end.getY()) instanceof CapturedBlock)){
            return false;
        }
        CapturedBlock startBlock = (CapturedBlock)gameMap.get(start.getX()).get(start.getY());
        CapturedBlock endBlock   = (CapturedBlock)gameMap.get(end.getX()).get(end.getY());

        // проверяем, что можно сделать ход
        if(startBlock.getCountArmy() < 2){
            return false;
        }

        // TODO: тут нужно изменять суммарное количество войск для игрока
        if(startBlock.getUser() == endBlock.getUser()){
            // TODO: вот тут может быть проблема с перемещением
            int countArmyMove = (int)((startBlock.getCountArmy() - 1) * ((is50) ? (0.5) : 1));
            startBlock.setArmy(1);
            endBlock.setArmy(endBlock.getCountArmy() + countArmyMove);
            return true;
        } else {
            int countArmyMove = (int)((startBlock.getCountArmy() - 1) * ((is50) ? (0.5) : 1));
            startBlock.setArmy(1);
            endBlock.setArmy(endBlock.getCountArmy() - countArmyMove);
            if(endBlock.getCountArmy() < 0){
                endBlock.setArmy((-1) * endBlock.getCountArmy());
                // если захватили королество, то нужно все клетки захватить
                if(endBlock.getClass().equals(CastleBlock.class)){
                    capturedCastle(startBlock.getUser(), endBlock.getUser());
                } else {
                    endBlock.setUser(startBlock.getUser());
                }
                return true;
            } else {
                return false;
            }
        }
    }

    // TODO: написать проверки
    // capturedCastle(invader, captured) - нужно переделать в ферму королевство и обозначить игрока, который займёт её.
    private void capturedCastle(User invader, User captured){
        // 1) меняем для клеток короля
        for(int x = 0; x < height; x++){
            for(int y = 0; y < width; y++){
                if(gameMap.get(x).get(y) instanceof CapturedBlock){
                    // 2) королевство меняем на ферму с таким же количеством армии
                    captured.addOrDeleteArmy(-((CapturedBlock) gameMap.get(x).get(y)).getCountArmy());
                    invader.addOrDeleteArmy(((CapturedBlock)gameMap.get(x).get(y)).getCountArmy());
                    if(gameMap.get(x).get(y).getClass().equals(CastleBlock.class)){
                        gameMap.get(x).set(y,
                                new FarmBlock(x, y, ((CapturedBlock) gameMap.get(x).get(y)).getCountArmy()));
                    } else {
                        ((CapturedBlock)gameMap.get(x).get(y)).setUser(invader);
                    }
                }
            }
        }
        // TODO: 3) кидаем сигнал, что нужно удалить игрока и вывести для него, что он проиграл
        // (тут же проверка того, что количество игроков больше двух, если нет, то нужно завершить игру и вывести для игроков результаты)
    }


    // nextTick(): которые для каждого активного королевства и фермы добавляет единицу в жизни
    // (скорее всего нужно будет ждать количество тиков и если какое-то количество тиков собралось, то
    // нужно будет обновлять все активные клетки: королевства, заваченные фермы, захваченные нейтральные клетки)
    public void nextTick(){
        ++countCompletedTickets;
        if(countCompletedTickets == numberOfTicksBeforeUpdate){
            // все занятые клетки обновляем
            countCompletedTickets = 0;
            for(int x = 0; x < height; x++){
                for(int y = 0; y < width; y++){
                    if(gameMap.get(x).get(y) instanceof CapturedBlock) {
                        ((CapturedBlock)gameMap.get(x).get(y)).nextTick();
                    }
                }
            }
        } else {
            // обновляем только для ферм и королевств
            for(int x = 0; x < height; x++){
                for(int y = 0; y < width; y++){
                    if(gameMap.get(x).get(y) instanceof FarmBlock || gameMap.get(x).get(y) instanceof CastleBlock) {
                        ((CapturedBlock)gameMap.get(x).get(y)).nextTick();
                    }
                }
            }
        }

        // обновляем количество армии у каждого игрока и количество полей
        updateCountArmyAndPlaces();
    }

    // обновляем количество армии у каждого игрока и количество занятых мест
    private void updateCountArmyAndPlaces(){
        for (int i = 0; i < users.size(); i++) {
            users.get(i).setCountArmy(0);
            users.get(i).setCountPlace(0);
        }

        for(int x = 0; x < height; x++){
            for(int y = 0; y < width; y++){
                if(gameMap.get(x).get(y) instanceof CapturedBlock) {
                    for (int i = 0; i < users.size(); i++) {
                        if (((CapturedBlock)gameMap.get(x).get(y)).getUser().getLogin() == users.get(i).getLogin()){
                            users.get(i).addOrDeleteArmy(((CapturedBlock) gameMap.get(x).get(y)).getCountArmy());
                            users.get(i).addOrDeletePlace(1);
                        }
                    }
                }
            }
        }
    }

    // будут очереди
    // clear_moves - сбросить очередь локальных ходов -> ячейка присылает, если захватили, то нужно присылать запрос
    // на клиента, чтобы удалил очередь ходов.
}
