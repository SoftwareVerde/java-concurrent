package com.softwareverde.concurrent.threadpool;

public interface MutableThreadPool extends ThreadPool {
    void start();
    void stop();
}
