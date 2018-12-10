package com.droid.concurrent;

import android.util.Log;

import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class ThreadPool {
    private static final String TAG = ThreadPool.class.getSimpleName();

    private static final int CORE_POOL_SIZE = 4;
    private static final int MAX_POOL_SIZE = 8;
    private static final int KEEP_ALIVE_TIME = 10; // 10 seconds
    private final Executor mExecutor;

    /**
     * cancel listener
     */
    public interface CancelListener {
        void onCancel();
    }

    /**
     * job context
     */
    public interface JobContext {
        /**
         * whether the job is cancelled
         *
         * @return true is cancelled
         */
        boolean isCancelled();

        /**
         * set cancel listener
         *
         * @param listener l
         */
        void setCancelListener(CancelListener listener);
    }

    /**
     * job interface
     *
     * @param <T>
     */
    public interface Job<T> {
        T run(JobContext jc);
    }

    /**
     * Thread Pool to use
     */
    public ThreadPool() {
        this(CORE_POOL_SIZE, MAX_POOL_SIZE);
    }

    private ThreadPool(int initPoolSize, int maxPoolSize) {
        mExecutor = new ThreadPoolExecutor(
                initPoolSize, maxPoolSize, KEEP_ALIVE_TIME,
                TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>(),
                new PriorityThreadFactory("Thread-Pool",
                        android.os.Process.THREAD_PRIORITY_BACKGROUND));
        ((ThreadPoolExecutor) mExecutor).setRejectedExecutionHandler(new RejectedExecutionHandler() {
            @Override
            public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
                if (!isExecutorShutDown()) {
                    Log.w(TAG, new RejectedExecutionException("Task " + r.toString() + " rejected from " + executor.toString()));
                }
            }
        });
    }

    /**
     * shut down the executor
     */
    public void shutDownExecutor() {
        ((ThreadPoolExecutor) mExecutor).shutdown();
    }

    /**
     * whether the executor is shut down
     *
     * @return true if shut down
     */
    public boolean isExecutorShutDown() {
        return ((ThreadPoolExecutor) mExecutor).isShutdown();
    }

    /**
     * submit a job to run
     *
     * @param job      job
     * @param listener done listener
     * @param <T>      data
     * @return future
     */
    public <T> Future<T> submit(Job<T> job, FutureListener<T> listener) {
        Worker<T> w = new Worker<T>(job, listener);
        if (!isExecutorShutDown()) {
            mExecutor.execute(w);
        }
        return w;
    }

    /**
     * submit a job to run
     *
     * @param job job
     * @param <T> data
     * @return future
     */
    public <T> Future<T> submit(Job<T> job) {
        return submit(job, null);
    }

    /**
     * worker
     *
     * @param <T> T
     */
    private class Worker<T> implements Runnable, Future<T>, JobContext {
        @SuppressWarnings("hiding")
        private static final String TAG = "ThreadPool-Worker";
        private Job<T> mJob;
        private FutureListener<T> mListener;
        private boolean mIsDone;
        private T mResult;
        private CancelListener mCancelListener;
        private volatile boolean mIsCancelled;

        Worker(Job<T> job, FutureListener<T> listener) {
            mJob = job;
            mListener = listener;
        }

        // This is called by a thread in the thread pool.
        @Override
        public void run() {
            T result = null;

            try {
                result = mJob.run(this);
            } catch (Throwable ex) {
                Log.w(TAG, "Exception in running a job", ex);
            }

            synchronized (this) {
                mResult = result;
                mIsDone = true;
                notifyAll();
            }

            if (mListener != null) {
                mListener.onFutureDone(this);
            }
        }

        // Below are the methods for Future.
        @Override
        public synchronized void cancel() {
            if (mIsCancelled) {
                return;
            }

            mIsCancelled = true;

            if (mCancelListener != null) {
                mCancelListener.onCancel();
            }
        }

        @Override
        public boolean isCancelled() {
            return mIsCancelled;
        }

        @Override
        public synchronized boolean isDone() {
            return mIsDone;
        }

        @Override
        public synchronized T get() {
            while (!mIsDone) {
                try {
                    wait();
                } catch (Exception ex) {
                    Log.w(TAG, "ignore exception", ex);
                }
            }
            return mResult;
        }

        @Override
        public void waitDone() {
            get();
        }

        // Below are the methods for JobContext (only called from the
        // thread running the job)
        @Override
        public synchronized void setCancelListener(CancelListener listener) {
            mCancelListener = listener;
            if (mIsCancelled && mCancelListener != null) {
                mCancelListener.onCancel();
            }
        }
    }
}
