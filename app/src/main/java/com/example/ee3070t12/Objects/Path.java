package com.example.ee3070t12.Objects;

import java.util.ArrayList;
import java.util.Collections;

public class Path implements Comparable<Path>{
    private int from;
    private int to;
    private int id;
    private ArrayList<String> path;

    private static final ArrayList<Path> objectPathList = new ArrayList();

    public Path(int f, int t, int i, ArrayList<String> p){
        from = f;
        to = t;
        id = i;
        path = p;
    }

    public int getFrom() {
        return from;
    }

    public int getTo() {
        return to;
    }

    public int getId() {
        return id;
    }

    public ArrayList getPath() {
        return path;
    }

    @Override
    public int compareTo(Path other)
    {
        return Integer.toString(id).compareTo(Integer.toString(other.id));
    }

    public static void putPath(Path p)
    {
        objectPathList.add(p);
    }

    public static Path getPath(String id){
        ArrayList<String> Searchpath = new ArrayList();
        Path key = new Path(0,0,Integer.parseInt(id),Searchpath);
        int index = Collections.binarySearch(objectPathList,key);

        return objectPathList.get(index);
    }

}
