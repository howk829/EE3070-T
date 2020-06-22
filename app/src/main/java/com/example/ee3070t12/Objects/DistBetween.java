package com.example.ee3070t12.Objects;

public class DistBetween {
    private double distance;
    private double[] first_coor;
    private double[] second_coor;

    public DistBetween(double d,double[] f, double[] s){
        distance = d;
        first_coor = f;
        second_coor = s;
    }

    public double getDistance(){ return distance;}

    public double[] getFirst_coor(){return first_coor;}

    public double[] getSecond_coor(){return second_coor;}

}
