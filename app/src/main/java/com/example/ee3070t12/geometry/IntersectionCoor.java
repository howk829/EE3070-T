package com.example.ee3070t12.geometry;

public class IntersectionCoor implements Comparable<IntersectionCoor>  {
    private double[] Coor1;
    private int pointID;

    public IntersectionCoor (double[] d, int id){
        Coor1 = d;
        pointID = id;
    }

    public double[] getCoor1() {
        return Coor1;
    }

    public int getPointID() {
        return pointID;
    }

    @Override
    public int compareTo(IntersectionCoor Others){
       return pointID - Others.pointID;
    }
}
