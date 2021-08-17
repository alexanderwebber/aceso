package com.company;

import java.awt.*;
import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public class Tumoroid extends Particle implements Drawable {


    //gel object variables
    double R, x, y, z;
    double volume = (4.0 / 3) * Math.PI * (Math.pow(R, 3));
    private int idNum;
    private int numNeighbors;

    private double dt = 0.01;
    private ArrayList<Double> imageParticle = new ArrayList<>();
    private double[] forceArray = {0, 0, 0};

    // Alive, being killed, dead, delete
    String status;

    private int timeSinceAttacked;

    private int timeSinceDead;

    private int growthTime;

    // initializes a gel particle right above the box with a random x, y (within the box dimensions)
    Tumoroid() {
        status = "alive";

        growthTime = 0;

        timeSinceAttacked = 0;

        timeSinceDead = 0;

        volume = (4.0  / 3) * Math.PI * (Math.pow(R, 3));


    }

    Tumoroid(double x, double y, double z, double R, int idNum, Simulation S) {
        super(x, y, z, R, S);
        this.x = x;
        this.y = y;
        this.z = z;
        this.R = R;

        type = "tumor";

        this.idNum = idNum;

        super.S = S;

        volume = (4.0 / 3) * Math.PI * (Math.pow(R, 3));

        status = "alive";

        imImage = false;
    }

    public double getR() {
        return R;
    }

    public void setR(double R) { this.R = R; }

    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
    }

    public double getZ() {
        return z;
    }

    public double getVolume() { return volume; }

    public void setZ(double z) {
        this.z = z;
    }

    public double getRSquared() { return R * R; }

    public ArrayList<Double> getImageParticle() {
        return imageParticle;
    }

    public String getStatus() { return status; }

    public int getIdNum() { return idNum; }

    public int getTimeSinceAttacked() { return timeSinceAttacked; }

    public int getTimeSinceDead() { return timeSinceDead; }
    
    public int getNumNeighbors() { return numNeighbors; }
    
    public void setNumNeighbors(int numNeighbors) { this.numNeighbors = numNeighbors; }

    public void setStatus(String status) { this.status = status; }

    void checkSetStatus() {


        if(this.getStatus().equals("being_attacked")) {

            if(timeSinceAttacked >= 60) {
                this.setStatus("dead");
            }
            else {
                timeSinceAttacked += 1;
                
            }
        }

        if(this.getStatus().equals("dead")) {

            if(timeSinceDead >= 540) {
                this.setStatus("delete");

            }
            else {
                timeSinceDead += 1;
            }
        }

    }

    @Override
    public void draw(Graphics g) {
        g.setColor(new Color(200, 0, 0, 192)); //outline
        g.drawOval((int) x, (int) y, (int) (2 * R), (int) (2 * R));

        g.setColor(new Color(200, 0, 0, 90)); //fill
        g.fillOval((int) x, (int) y, (int) (2 * R), (int) (2 * R));

        if(this.getStatus().equals("alive")) {
            g.setColor(new Color(200, 0, 0, 192)); //outline
            g.drawOval((int) x, (int) y, (int) (2 * R), (int) (2 * R));

            g.setColor(new Color(200, 0, 0, 90)); //fill
            g.fillOval((int) x, (int) y, (int) (2 * R), (int) (2 * R));
        }

        else if(this.getStatus().equals("being_attacked")) {
            g.setColor(new Color(0, 0, 255, 192)); //outline
            g.drawOval((int) x, (int) y, (int) (2 * R), (int) (2 * R));

            g.setColor(new Color(0, 0, 255, 90)); //fill
            g.fillOval((int) x, (int) y, (int) (2 * R), (int) (2 * R));
        }

        else if(this.getStatus().equals("dead")) {
            g.setColor(new Color(255, 255, 255, 192)); //outline
            g.drawOval((int) x, (int) y, (int) (2 * R), (int) (2 * R));

            g.setColor(new Color(255, 255, 255, 90)); //fill
            g.fillOval((int) x, (int) y, (int) (2 * R), (int) (2 * R));
        }
    }

}