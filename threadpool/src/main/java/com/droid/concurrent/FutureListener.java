package com.droid.concurrent;

public interface FutureListener<T> {
    /**
     * called when job is done
     *
     * @param future back data
     */
    void onFutureDone(Future<T> future);
}
