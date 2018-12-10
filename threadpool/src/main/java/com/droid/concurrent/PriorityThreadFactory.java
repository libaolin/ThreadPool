package com.droid.concurrent;

import android.os.Process;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

public class PriorityThreadFactory implements ThreadFactory {

    private final int mPriority;
    private final AtomicInteger mNumber = new AtomicInteger();
    private final String mName;

    PriorityThreadFactory(String name, int priority) {
        mName = name;
        mPriority = priority;
    }

    @Override
    public Thread newThread(Runnable r) {
        return new Thread(r, mName + '-' + mNumber.getAndIncrement()) {
            @Override
            public void run() {
                Process.setThreadPriority(mPriority);
                super.run();
            }
        };
    }

}
