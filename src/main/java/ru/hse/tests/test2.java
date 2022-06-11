package ru.hse.tests;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import ru.hse.Room;
import ru.hse.RoomServiceGrpc;

import java.util.Iterator;

public class test2 {

    public static void main(String[] args) {
            ManagedChannel channel = ManagedChannelBuilder.forAddress("172.18.0.1", 8080)
                    .usePlaintext()
                    .build();

            RoomServiceGrpc.RoomServiceBlockingStub stub = RoomServiceGrpc.newBlockingStub(channel);

            Room.JoinToRoomRequest req  = Room.JoinToRoomRequest.newBuilder().setRoomName("").build();

            Iterator<Room.RoomEvent> eventStream = stub.joinToRoom(req);
            eventStream.forEachRemaining(ev -> {
                System.out.println(ev.getEventCase());
            });
    }
}
