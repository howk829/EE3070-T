package com.example.ee3070t12.Objects;

public class Node {
    private int id;
    private int[] coordinate;

    public Node(int i, int[] c){
        id = i;
        coordinate = c;
    }

    public int getId() {
        return id;
    }

    public int[] getCoordinate() {
        return coordinate;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setCoordinate(int[] coordinate) {
        this.coordinate = coordinate;
    }
}
