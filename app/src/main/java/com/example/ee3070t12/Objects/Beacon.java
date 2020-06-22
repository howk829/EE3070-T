package com.example.ee3070t12.Objects;

public class Beacon {

    public String name;
    public int rssi;

    public Beacon(String n, int r){
            name = n;
            rssi = -r;
    }

    public String getName(){
        return name;
    }

    public int getRSSI(){
        return rssi;
    }
}
