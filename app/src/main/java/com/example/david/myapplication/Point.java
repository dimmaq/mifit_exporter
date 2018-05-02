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
    boolean isLapStart;
    int cadence;

    public Point(int timestamp, long lat, long lon, long alt, int cadence) {
        this.timestamp = timestamp;
        this.lat = lat;
        this.lon = lon;
        this.alt = alt;
        this.hasHR = false;
        this.isLapStart = false;
        this.cadence = cadence;
    }

    public Point(int timestamp, long lat, long lon, long alt, int cadence, int hr) {
        this(timestamp, lat, lon, alt, cadence);
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

    public boolean getIsLapStart() {
        return isLapStart;
    }

    public void setLapStart(boolean lapStart) {
        isLapStart = lapStart;
    }

    public int getCadence() {
        return cadence;
    }
}
