package org.adridadou.ethereum.handler;

import com.google.common.collect.Sets;
import rx.Observable;
import rx.Subscriber;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * Created by davidroon on 19.08.16.
 * This code is released under Apache 2 license
 */
public class AbstractHandler<T> implements Observable.OnSubscribe<T> {
    private final Set<Subscriber<? super T>> subscribers = Sets.newConcurrentHashSet();
    public final Observable<T> observable;

    public AbstractHandler() {
        observable = Observable.create(this);
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

}
