package ru.hse.objects;

import io.grpc.stub.StreamObserver;
import ru.hse.GameObject;
import ru.hse.Room;

public  class PlayerWithIO<T> {
    StreamObserver<T> eventStream;
    GameObject.Player player;
    public PlayerWithIO(GameObject.Player player, StreamObserver<T> playerEventStream) {
        this.player = player;
        this.eventStream = playerEventStream;
    }


    public StreamObserver<T> getEventStream() {
        return eventStream;
    }

    public GameObject.Player getPlayer() {
        return player;
    }
}
