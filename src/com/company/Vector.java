package com.company;

import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

class Vector {
    static final double PI = 3.14159265358979323846264338327950288419716939937511;
    double[] vect = new double[3];
    public Vector() {
        vect[0]=0;
        vect[1]=0;
        vect[2]=0;
    }
    public Vector(double x, double y, double z) {
        vect[0]=x;
        vect[1]=y;
        vect[2]=z;
    }
    double x(){return vect[0];}
    double y(){return vect[1];}
    double z(){return vect[2];}
    Vector unitVector() {
        double magnitude = magnitude();
        return new Vector(x()/magnitude, y()/magnitude, z()/magnitude);
    }
    public double magnitude() {
        double sum = 0;
        for (int i = 0; i <= 2; ++i)
            sum += Math.pow(vect[i], 2);
        return Math.sqrt(sum);
    }

    /*public Vector replace(Vector u) {

    }*/

    public static double magnitude(double dx, double dy, double dz) {
        double sum = 0;
        sum += Math.pow(dx, 2);
        sum += Math.pow(dy, 2);
        sum += Math.pow(dz, 2);
        return Math.sqrt(sum);
    }
    public Vector scale(double d) {
        return new Vector(vect[0]*d, vect[1]*d, vect[2]*d);
    }
    public Vector add(Vector u) {
        return new Vector(vect[0]+u.x(), vect[1]+u.y(), vect[2]+u.z());
    }
    public Vector add(double x, double y, double z) {
        return new Vector(vect[0]+x, vect[1]+y, vect[2]+z);
    }
    public Vector minus(Vector u) {
        return new Vector(vect[0]-u.x(), vect[1]-u.y(), vect[2]-u.z());
    }
    static Vector random() {
        double theta = Math.random() * 2 * PI;
        double phi = Math.acos(2 * Math.random() - 1);
        return new Vector(Math.sin(phi) * Math.cos(theta), Math.sin(phi) * Math.sin(theta), Math.cos(phi));
    }

    static Vector random2(double velocity, Random random) {
        // Random number gen:
        
        int[] randArray = new int[3];

        randArray[0] = (random.nextInt(1) + 1) * (random.nextBoolean() ? -1 : 1);
        randArray[1] = (random.nextInt(1) + 1) * (random.nextBoolean() ? -1 : 1);
        randArray[2] = (random.nextInt(1) + 1) * (random.nextBoolean() ? -1 : 1);

        // Assign random velocity value
        double velocityX = ThreadLocalRandom.current().nextDouble(0, velocity);
        double velocityY = ThreadLocalRandom.current().nextDouble(0, velocity - velocityX);
        double velocityZ = (velocity - velocityX - velocityY);

        // Randomize the polarity
        velocityX *= randArray[random.nextInt(3)];
        velocityY *= randArray[random.nextInt(3)];
        velocityZ *= randArray[random.nextInt(3)];

        return new Vector(velocityX, velocityY, velocityZ);
    }
    
    static Vector random3(double velocity, Random random) {
    	double dx = random.nextDouble() - 0.5;
    	double dy = random.nextDouble() - 0.5;
    	double dz = random.nextDouble() - 0.5;
    	
    	double length = Math.sqrt((dx * dx) + (dy * dy) + (dz * dz));
    	
    	dx = dx * (velocity / length);
    	dy = dy * (velocity / length);
    	dz = dz * (velocity / length);
    	
    	
    	return new Vector(dx, dy, dz);
    }

    double dot(Vector u) {
        return x()*u.x()+ y()*u.y()+ z()*u.z();
    }
    public String toString() {
        return "["+x()+","+y()+","+z()+"]";
    }
}
