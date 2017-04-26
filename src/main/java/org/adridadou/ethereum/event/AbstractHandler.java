package org.adridadou.ethereum.event;

import rx.Observable;
import rx.Subscriber;


import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Created by davidroon on 19.08.16.
 * This code is released under Apache 2 license
 */
public class AbstractHandler<T> implements Observable.OnSubscribe<T> {
    public final Observable<T> observable;
    private final Set<Subscriber<? super T>> subscribers = ConcurrentHashMap.newKeySet();

    public AbstractHandler() {
        observable = Observable.unsafeCreate(this);
    }

    public void newBlock(final T param) {
        removeUnSubscribed();
        subscribers.forEach(subscriber -> subscriber.onNext(param));
    }

    @Override
    public void call(Subscriber<? super T> subscriber) {
        subscribers.add(subscriber);
        removeUnSubscribed();
    }

    private void removeUnSubscribed() {
        Set<Subscriber<? super T>> unsubscribed = subscribers.stream().filter(Subscriber::isUnsubscribed).collect(Collectors.toSet());
        subscribers.removeAll(unsubscribed);
    }

    public void on(final T param) {
        removeUnSubscribed();
        subscribers.forEach(subscriber -> subscriber.onNext(param));
    }

}
