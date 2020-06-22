package com.example.ee3070t12.Objects;

public class Item {
    private String name;
    private double price;
    private int location;
    private String space = "                                                   ";

    public Item(String n, double p, int l){
        name = n;
        price = p;
        location = l;
    }

    public double getPrice() {
        return price;
    }

    public String getName() {
        return name;
    }

    public int getLocation() {
        return location;
    }

    public void setLocation(int location) {
        this.location = location;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    @Override
    public String toString()
    {
        return name + ": $" + price ;
    }
}
