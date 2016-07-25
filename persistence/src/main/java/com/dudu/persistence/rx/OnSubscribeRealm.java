package com.dudu.persistence.rx;

import android.content.Context;
import android.support.annotation.NonNull;

import com.dudu.commonlib.CommonLib;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.exceptions.RealmException;
import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.functions.Action0;
import rx.subscriptions.Subscriptions;

/**
 * Created by Administrator on 2016/2/19.
 */
abstract class OnSubscribeRealm<T> implements Observable.OnSubscribe<T> {
    private final String fileName;

    private final List<Subscriber<? super T>> subscribers = new ArrayList<>();
    private final AtomicBoolean canceled = new AtomicBoolean();
    private final Object lock = new Object();
    private final Context context;

    public OnSubscribeRealm() {
        this(null);
    }

    public OnSubscribeRealm(String fileName) {
        this.context = CommonLib.getInstance().getContext().getApplicationContext();
        this.fileName = fileName;
    }
    @Override
    public void call (final Subscriber<? super T> subscriber) {
        synchronized (lock) {
            boolean canceled = this.canceled.get();
            if (!canceled && !subscribers.isEmpty()) {
                subscriber.add(newUnsubscribeAction(subscriber));
                subscribers.add(subscriber);
                return;
            } else if (canceled) {
                return;
            }
        }
        subscriber.add(newUnsubscribeAction(subscriber));
        subscribers.add(subscriber);

        RealmConfiguration.Builder builder = RealmManage.getDefaultConfig(context);
        if (fileName != null) {
            builder.name(fileName);
        }
        Realm realm;
        try {
            realm = Realm.getInstance(
                    builder.build());
        }catch (Exception e){
            sendOnError(e);
            return;
        }
        boolean withError = false;

        T object = null;
        try {
            if (!this.canceled.get()) {
                realm.beginTransaction();
                object = get(realm);
                if (object != null && !this.canceled.get()) {
                    realm.commitTransaction();
                } else {
                    realm.cancelTransaction();
                }
            }
        } catch (RuntimeException e) {
            realm.cancelTransaction();
            sendOnError(new RealmException("Error during transaction.", e));
            withError = true;
        } catch (Error e) {
            realm.cancelTransaction();
            sendOnError(e);
            withError = true;
        }
        if (object != null && !this.canceled.get() && !withError) {
            sendOnNext(object);
        }

        try {
            realm.close();
        } catch (RealmException ex) {
            sendOnError(ex);
            withError = true;
        }
        if (!withError) {
            sendOnCompleted();
        }
        this.canceled.set(false);
    }

    private void sendOnNext(T object) {
        for (int i = 0; i < subscribers.size(); i++) {
            Subscriber<? super T> subscriber = subscribers.get(i);
            subscriber.onNext(object);
        }
    }

    private void sendOnError(Throwable e) {
        for (int i = 0; i < subscribers.size(); i++) {
            Subscriber<? super T> subscriber = subscribers.get(i);
            subscriber.onError(e);
        }
    }

    private void sendOnCompleted() {
        for (int i = 0; i < subscribers.size(); i++) {
            Subscriber<? super T> subscriber = subscribers.get(i);
            subscriber.onCompleted();
        }
    }

    @NonNull
    private Subscription newUnsubscribeAction(final Subscriber<? super T> subscriber) {
        return Subscriptions.create(new Action0() {
            @Override
            public void call() {
                synchronized (lock) {
                    subscribers.remove(subscriber);
                    if (subscribers.isEmpty()) {
                        canceled.set(true);
                    }
                }
            }
        });
    }

    public abstract T get(Realm realm);
}
