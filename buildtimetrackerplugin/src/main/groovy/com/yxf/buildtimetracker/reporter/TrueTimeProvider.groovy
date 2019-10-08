package com.yxf.buildtimetracker

public class TrueTimeProvider {
    public long getCurrentTime() {
        return System.currentTimeMillis();
    }
}