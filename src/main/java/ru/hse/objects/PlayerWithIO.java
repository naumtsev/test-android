package ru.hse.objects;

import io.grpc.stub.StreamObserver;
import ru.hse.Room;

public class PlayerWithIO {
    String login;
    StreamObserver<Room.RoomEvent> eventStream;

    public PlayerWithIO(String playerLogin, StreamObserver<Room.RoomEvent> playerEventStream) {
        this.login = playerLogin;
        this.eventStream = playerEventStream;
    }


    public StreamObserver<Room.RoomEvent> getEventStream() {
        return eventStream;
    }

    public String getLogin() {
        return login;
    }
}
