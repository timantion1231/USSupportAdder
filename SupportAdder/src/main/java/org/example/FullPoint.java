package org.example;

public final class FullPoint implements Point {
    private final String lon;
    private final String lat;
    private final String num;
    private boolean status; // True if exists

    public FullPoint(String num, String lat, String lon) {
        this.lon = lon;
        this.lat = lat;
        this.num = num;
    }


    public String getLat() {
        return lat;
    }

    public String getLon() {
        return lon;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }

    public boolean getStatus() {
        return status;
    }

    public String getNum() {
        return num;
    }
}
