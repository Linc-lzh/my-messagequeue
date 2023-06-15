package com.message.util;

import java.util.concurrent.ThreadFactory;

public class NxThreadFactory implements ThreadFactory {
    private String name;

    int threadSeqId = 0;

    public NxThreadFactory(String name) {
        this.name = name;
    }

    @Override
    public Thread newThread(Runnable r) {
        return new Thread(r, name + (threadSeqId++));
    }
}