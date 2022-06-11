package ru.hse;

import com.google.gson.Gson;
import io.grpc.*;
import ru.hse.controllers.GameController;
import ru.hse.gameObjects.*;
import ru.hse.services.AccountService;
import ru.hse.services.LoggerInterceptor;
import ru.hse.services.RoomService;

import javax.annotation.Nullable;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.Inet4Address;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

public class App {
        public static void main(String[] args) throws IOException, InterruptedException {
                ServerBuilder<?> serverBuilder = ServerBuilder.forPort(8080);
                serverBuilder.keepAliveTime(500, TimeUnit.MILLISECONDS);

                serverBuilder.intercept(new LoggerInterceptor());
                serverBuilder.addService(new AccountService());
                serverBuilder.addService(new RoomService());

                Server server = serverBuilder.build();
                server.start();


                Runtime.getRuntime().addShutdownHook(new Thread(server::shutdownNow));

                String ipAddresses = IpUtil.resolveIPAddresses();

                System.out.println("Server started listening on port " + server.getPort());
                System.out.println("Server IPs: " + ipAddresses);

                server.awaitTermination();
        }




        public void debug() {
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
                int countFarm = 0;
                int countCastle = 0;
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
        }
}
