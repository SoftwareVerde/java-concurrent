package com.softwareverde.concurrent.threadpool;

public interface ThreadPool {
    void execute(Runnable runnable);
}
