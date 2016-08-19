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
public class OnBlockHandler implements Observable.OnSubscribe<OnBlockParameters> {

    private final Set<Subscriber<? super OnBlockParameters>> subscribers = Sets.newConcurrentHashSet();
    public final Observable<OnBlockParameters> observable;

    public OnBlockHandler() {
        observable = Observable.create(this);
    }

    public void newBlock(final OnBlockParameters params) {
        removeUnSubscribed();
        subscribers.forEach(subscriber -> subscriber.onNext(params));
    }

    @Override
    public void call(Subscriber<? super OnBlockParameters> subscriber) {
        subscribers.add(subscriber);
        removeUnSubscribed();
    }

    private void removeUnSubscribed() {
        Set<Subscriber<? super OnBlockParameters>> unsubscribed = subscribers.stream().filter(Subscriber::isUnsubscribed).collect(Collectors.toSet());
        subscribers.removeAll(unsubscribed);

    }
}

