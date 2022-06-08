package ru.hse.observers;

import io.grpc.stub.StreamObserver;

public class eventObserver<V> implements StreamObserver<V> {
    // вызываем super
    @Override
    public void onNext(V value) {
    }

    // вызываем переопределенный
    // нужно сразу игрока отключать от игры/лобби
    // как-то нужно понимать, где находится игрок
    // если в комнате, то можно просто отключить
    // если в игре, то нужно сделать так, что игрок проиграл и отключить
    @Override
    public void onError(Throwable t) {

    }

    // вызываем super
    @Override
    public void onCompleted() {
    }
}
