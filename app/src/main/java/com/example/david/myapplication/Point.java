package com.example.david.myapplication;

/**
 * Created by p.shuchalov on 11.01.2018.
 */

public class Point {
    long lat;
    long lon;
    long alt;
    int timestamp;
    int hr;
    boolean hasHR;

    public Point(int timestamp, long lat, long lon, long alt) {
        this.timestamp = timestamp;
        this.lat = lat;
        this.lon = lon;
        this.alt = alt;
        this.hasHR = false;
    }

    public Point(int timestamp, long lat, long lon, long alt, int hr) {
        this(timestamp, lat, lon, alt);
        this.hr = hr;
        this.hasHR = true;
    }

    public int getTimestamp() {
        return timestamp;
    }

    public long getLat() {
        return lat;
    }

    public long getLon() {
        return lon;
    }

    public long getAlt() {
        return alt;
    }

    public boolean isHasHR() {
        return hasHR;
    }

    public int getHr() {
        return hr;
    }
}
