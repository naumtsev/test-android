package ru.hse;

import com.google.gson.Gson;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import ru.hse.AccountServiceGrpc;
import ru.hse.controllers.AccountController;
import ru.hse.controllers.GameController;
import ru.hse.controllers.RoomController;
import ru.hse.gameObjects.*;
import ru.hse.services.AccountService;
import ru.hse.services.RoomEventService;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.Inet4Address;
import java.util.ArrayList;

public class App {
        public static void main(String[] args) throws IOException, InterruptedException {
                //        MapCreater creater = new MapCreater();
                //        int width = 15;
                //        int height = 15;
                //        int castles = 4;
                //
                ArrayList<User> users = new ArrayList<>();
                for(int i = 0; i < 2; i++){
                        users.add(new User(Integer.toString(i), "BLACK"));
                }
                GameController newGameController = new GameController(20, 15,  2, users);
                newGameController.getGameMapForUser(new User("1", "BLACK"));


                GameMap gameMap = newGameController.getGameMap();
                ArrayList<ArrayList<Block>> array = gameMap.getGameMap();
                int countWalls = 0;
                int countFarm  = 0;
                int countCastle= 0;
                for(int x = 0; x < gameMap.getHeight(); x++) {
                        for (int y = 0; y < gameMap.getWidth(); y++) {
                                Class<?> type = array.get(x).get(y).getClass();
                                if (type.equals(SimpleDrawableBlock.class)) {
                                        System.out.print("0 ");
                                        continue;
                                }
                                if(type.equals(MountainBlock.class)){
                                        System.out.printf("1 ");
                                        countWalls++;
                                        continue;
                                }
                                if(type.equals(FarmBlock.class)){
                                        System.out.printf("2 ");
                                        countFarm++;
                                        continue;
                                }
                                if(type.equals(CastleBlock.class)){
                                        System.out.printf("3 ");
                                        countCastle++;
                                        continue;
                                }
                        }
                        System.out.print("\n");
                }

                System.out.println("Count Walls: "  + countWalls);
                System.out.println("Count Farm: "   + countFarm);
                System.out.println("Count Castle: " + countCastle + "\n");

                testGson();
        }

        static public void testGson(){
                Gson gson = new Gson();

//        // прикольно, он null элемент вообще не записывает
//        DrawingBlock drawingBlock = new DrawingBlock(1, 1, true);
//        String json = gson.toJson(drawingBlock);
//
//        System.out.println(json);
//
//        DrawingBlock drawingBlock1 = gson.fromJson(json, DrawingBlock.class);
//        System.out.println(drawingBlock1.getX() + " " + drawingBlock1.getY() + " " + drawingBlock1.isDraw() + " " + drawingBlock1.getType());

//        try(FileWriter writer = new FileWriter("map.json")){
//            int width = 5;
//            int height = 5;
//            ArrayList<ArrayList<DrawingBlock>> map = new ArrayList<>();
//            for(int x = 0; x < width; x++){
//                map.add(new ArrayList<DrawingBlock>());
//                for(int y = 0; y < height; y++){
//                    map.get(x).add(new DrawingBlock(x, y, false));
//                }
//            }
//
//            User user = new User(1000, User.Color.BLACK);
//            DrawingMap drawingMap = new DrawingMap(user, height, width, map);
//            writer.write(gson.toJson(drawingMap));
//            writer.close();
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        }

                try(FileReader reader = new FileReader("map1.json")){
                        DrawingMap map = gson.fromJson(reader, DrawingMap.class);
//            System.out.println(gson.toJson(map));
                        ArrayList<ArrayList<DrawingBlock>> drawingMap = map.getMap();

                        System.out.println(gson.toJson(map.getUser()));
                        System.out.println(gson.toJson(map.getHeight()));
                        System.out.println(gson.toJson(map.getWidth()));

                        for(int x = 0; x < map.getHeight(); x++){
                                for(int y = 0; y < map.getWidth(); y++){
                                        System.out.print(gson.toJson(drawingMap.get(x).get(y)) + " ");
                                }
                                System.out.print("\n");
                        }
                } catch (FileNotFoundException e) {
                        throw new RuntimeException(e);
                } catch (IOException e) {
                        throw new RuntimeException(e);
                }
        }

// SERVER START:
//                AccountController accountController = new AccountController();
//                RoomController roomController = new RoomController();
//
//
//                ServerBuilder<?> serverBuilder = ServerBuilder.forPort(8080);
//                serverBuilder.addService(new AccountService(accountController));
//                serverBuilder.addService(new RoomEventService(roomController));
//
//
//                Server server = serverBuilder.build();
//                server.start();
//
//                System.out.println("Server started on port " + server.getPort());
//                System.out.println("Server ip: " + Inet4Address.getLocalHost().getHostAddress());
//
//                server.awaitTermination();
}
