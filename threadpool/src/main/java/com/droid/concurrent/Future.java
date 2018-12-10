package com.droid.concurrent;

public interface Future<T> {
    /**
     * cancel the job
     */
    void cancel();

    /**
     * whether the job is cancelled
     *
     * @return true if the job is cancelled
     */
    boolean isCancelled();

    /**
     * whether the job is done
     *
     * @return true if the job is done
     */
    boolean isDone();

    /**
     * get the result T
     *
     * @return T
     */
    T get();

    /**
     * wait done
     */
    void waitDone();
}
