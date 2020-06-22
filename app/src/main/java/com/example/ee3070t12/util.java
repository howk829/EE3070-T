package com.example.ee3070t12;

public final class util {

    private util(){
        throw new UnsupportedOperationException();
    }

    public static double calculateDistance(double[] d1, double[] d2){
        double distance = Math.sqrt(((d1[0] - d2[0]) * (d1[0] - d2[0])) + ((d1[1] - d2[1]) * (d1[1] - d2[1])));
        return  distance;
    }

    public static double calculateDistance2(int[] d1, int[] d2){
        double distance = Math.sqrt(((d1[0] - d2[0]) * (d1[0] - d2[0])) + ((d1[1] - d2[1]) * (d1[1] - d2[1])));
        return  distance;
    }

    public static double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();

        long factor = (long) Math.pow(10, places);
        value = value * factor;
        long tmp = Math.round(value);
        return (double) tmp / factor;
    }
}
