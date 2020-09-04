package com.company;

import java.awt.*;
import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public class Tumoroid extends Particle implements Drawable {
    // Random number gen:
    Random random = new Random();
    int[] randArray = new int[3];

    //gel object variables
    double R, x, y, z;
    double volume = (4.0 / 3) * Math.PI * (Math.pow(R, 3));
    private double one_sqrtF;
    private int idNum;

    // Velocity vector
    private double velocityX;
    private double velocityY;
    private double velocityZ;
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

        R = 6.0;

        randArray[0] = (random.nextInt(1) + 1) * (random.nextBoolean() ? -1 : 1);
        randArray[1] = (random.nextInt(1) + 1) * (random.nextBoolean() ? -1 : 1);
        randArray[2] = (random.nextInt(1) + 1) * (random.nextBoolean() ? -1 : 1);

        volume = (4.0 / 3) * Math.PI * (Math.pow(R, 3));

        // math to keep it always within the sphere
        x = 400 + (600 - 400) * random.nextDouble();
        y = 400 + (600 - 400) * random.nextDouble();
        z = 400 + (600 - 400) * random.nextDouble();

        // Assign random velocity value
        velocityX = ThreadLocalRandom.current().nextDouble(3.1);
        velocityY = ThreadLocalRandom.current().nextDouble(3.1 - velocityX);
        velocityZ = (3.1 - velocityX - velocityY);

        // Randomize the polarity
        velocityX *= randArray[random.nextInt(3)];
        velocityY *= randArray[random.nextInt(3)];
        velocityZ *= randArray[random.nextInt(3)];

    }

    Tumoroid(double x, double y, double z, double R, int idNum) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.R = R;
        this.idNum = idNum;

        volume = (4.0 / 3) * Math.PI * (Math.pow(R, 3));

        double rSquared = Math.pow(R, 2.0);

        status = "alive";
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

    public int getTimeSinceAttacked() { return getTimeSinceDead(); }

    public int getTimeSinceDead() { return getTimeSinceAttacked(); }

    public void setStatus(String status) { this.status = status; }

    public void setForceArray(double forceX, double forceY, double forceZ) {
        forceArray[0] = forceX;
        forceArray[1] = forceY;
        forceArray[2] = forceZ;
    }

    // First check within PBC:
    // Positive faces
    void move(int index, Simulation s) {
        /*double x = Math.abs(this.getX() - 500);
        double y = Math.abs(this.getY() - 500);
        double z = Math.abs(this.getZ() - 500);
        double distanceVector = Math.pow(x, 2) + Math.pow(y, 2) + Math.pow(z, 2);
        if(distanceVector > 10000) {
            if(this.getX() < 500) {
                this.setX(this.getX() + 200);
                this.setY(this.getY() + 200);
                this.setZ(this.getZ() + 200);
            }
            else {
                this.setX(this.getX() - 200);
                this.setY(this.getY() - 200);
                this.setZ(this.getZ() - 200);
            }
        }*/

        if (this.x > 600) {
            setX(getX() - 200);
        }
        if (this.y > 600) {
            setY(getY() - 200);

        }
        if (this.z > 600) {
            setZ(getZ() - 200);
        }

        // Negative faces
        if (this.x < 400) {
            setX(getX() + 200);
        }
        if (this.y < 400) {
            setY(getY() + 200);
        }
        if (this.z < 400) {
            setZ(getZ() + 200);
        }

        // Change without collision
        double dx = this.getR() * 2 * velocityX * dt;
        double dy = this.getR() * 2 * velocityY * dt;
        double dz = this.getR() * 2 * velocityZ * dt;

        if (checkCollision(this.x, this.y, this.z, this.R, index, s)) {
            //Adjust position based on active forces and velocity
            double[] updatedForceArray = springModel(s, this);

            double forceXSquare = Math.pow(updatedForceArray[0], 2);
            double forceYSquare = Math.pow(updatedForceArray[1], 2);
            double forceZSquare = Math.pow(updatedForceArray[2], 2);

            one_sqrtF = 1.0 / (Math.sqrt(forceXSquare + forceYSquare + forceZSquare));


            this.x += (updatedForceArray[0] * one_sqrtF * 0.7 * dt);
            this.y += (updatedForceArray[1] * one_sqrtF * 0.7 * dt);
            this.z += (updatedForceArray[2] * one_sqrtF * 0.7 * dt);

        } else {
            /*this.x += dx;
            this.y += dy;
            this.z += dz;*/
        }

        // Image particle assignment, if within PBC:
        // Check if next step results in entering PBC
        // Positive faces
        // q/check variable below is used to see if image particle is created:
        /*int q = 0;
        if ((getX()) > 600 - (getR())) {
            this.imageParticle.add(0, (dx + getX()) - 200);
            q += 1;
        }
        // Still add move in case other coordinate moves into PBC, don't increment monitor variable
        else {
            this.imageParticle.add(0, dx + getX());
        }
        if ((getY()) > 600 - (4 * getR())) {
            this.imageParticle.add(1, (dy + getY()) - 200);
            q += 1;
        } else {
            this.imageParticle.add(1, dy + getY());
        }
        if ((getZ()) > 600 - (4 * getR())) {
            this.imageParticle.add(2, (dz + getZ()) - 200);
            q += 1;
        } else {
            this.imageParticle.add(2, dz + getZ());
        }
        // Negative faces
        if ((getX()) < 400 + (4 * getR())) {
            this.imageParticle.add(0, (dx + getX()) + 200);
            q += 1;
        } else {
            this.imageParticle.add(0, dx + getX());
        }
        if ((getY()) < 400 + (4 * getR())) {
            this.imageParticle.add(1, (dy + getY()) + 200);
            q += 1;
        } else {
            this.imageParticle.add(1, dy + getY());
        }
        if ((getZ()) < 400 + (4 * getR())) {
            this.imageParticle.add(2, (dz + getZ()) + 200);
            q += 1;
        } else {
            this.imageParticle.add(2, dz + getZ());
        }
        if (q == 0) {
            for (int i = 0; i < imageParticle.size(); i++) {
                imageParticle.clear();
            }
        }*/

    }

    boolean checkCollision(double x, double y, double z, double R, int index, Simulation s) {
        //check other tumor cells
        for (int i = 0; i < s.getNumTumor(); i++) {
            if (i != index) {
                //dont check the sphere you're trying to move
                double radius_sum = s.getTumoroids().get(i).getR() + R;
                double dx = Math.abs(s.getTumoroids().get(i).getX() - x);
                double dy = Math.abs(s.getTumoroids().get(i).getY() - y);
                double dz = Math.abs(s.getTumoroids().get(i).getZ() - z);

                if (dx < radius_sum && dy < radius_sum && dz < radius_sum) {        // is it even close?
                    if (dx * dx + dy * dy + dz * dz < radius_sum * radius_sum) {    // then compute radial distance and check *how* close
                        return true;
                    }
                }
            }
        }

        return false;
    }

    static double[] springModel(Simulation s, Tumoroid tumor) {

        // Force component array
        double[] forceArray = {0, 0, 0};

        for(int i = 0; i < s.getNumTumor(); i++) {

            if(tumor != s.getTumoroids().get(i)) {
                // Vector A calculation from Greg's notes
                double deltaXA = tumor.getX() - s.getTumoroids().get(i).getX();
                double deltaYA = tumor.getY() - s.getTumoroids().get(i).getY();
                double deltaZA = tumor.getZ() - s.getTumoroids().get(i).getZ();


                double deltaXASquared = Math.pow(deltaXA, 2);
                double deltaYASquared = Math.pow(deltaYA, 2);
                double deltaZASquared = Math.pow(deltaZA, 2);

            /*
            double vectorA = (deltaXA * s.getTumoroids().get(i).getTranslateX())
                    + (deltaYA * s.getTumoroids().get(i).getY())
                    + (deltaZA * s.getTumoroids().get(i).getZ());
            // Magnitude and direction of resultant
            double deltaRA = Math.sqrt(tumor.getRSquared()
                    + s.getTumoroids().get(i).getRSquared()
                    - deltaXASquared
                    - deltaYASquared
                    - deltaZASquared);
             */
                double rCheckSquared = deltaXASquared + deltaYASquared + deltaZASquared;


                // check if worth calculating
                if(rCheckSquared < (tumor.getRSquared() + s.getTumoroids().get(i).getRSquared())) {
                    // Force components
                    // Add to force component array
                    if(!Double.isNaN((((tumor.getRSquared() + s.getTumoroids().get(i).getRSquared()) / (rCheckSquared)) - 1.0) * deltaXA)) {
                        forceArray[0] += (((tumor.getRSquared() + s.getTumoroids().get(i).getRSquared()) / (rCheckSquared)) - 1.0) * deltaXA;
                    }
                    if(!Double.isNaN((((tumor.getRSquared() + s.getTumoroids().get(i).getRSquared()) / (rCheckSquared)) - 1.0) * deltaYA)) {
                        forceArray[1] += (((tumor.getRSquared() + s.getTumoroids().get(i).getRSquared()) / (rCheckSquared)) - 1.0) * deltaYA;
                    }
                    if(!Double.isNaN((((tumor.getRSquared() + s.getTumoroids().get(i).getRSquared()) / (rCheckSquared)) - 1.0) * deltaZA)) {
                        forceArray[2] += (((tumor.getRSquared() + s.getTumoroids().get(i).getRSquared()) / (rCheckSquared)) - 1.0) * deltaZA;
                    }

                }

                else {
                    continue;
                }


            /*// Now check each gel's image particle, if exists
            if(!s.getTumoroids().get(i).getImageParticle().isEmpty()) {
                // Vector A calculation from Greg's notes
                double deltaXAImage = tumor.getX() - s.getTumoroids().get(i).getImageParticle().get(0);
                double deltaYAImage = tumor.getY() - s.getTumoroids().get(i).getImageParticle().get(1);
                double deltaZAImage = tumor.getZ() - s.getTumoroids().get(i).getImageParticle().get(1);
                if(deltaXAImage == 0.0 && deltaYAImage == 0.0 && deltaZAImage == 0.0) {
                    continue;
                }
                double deltaXASquaredImage = Math.pow(deltaXA, 2);
                double deltaYASquaredImage = Math.pow(deltaYA, 2);
                double deltaZASquaredImage = Math.pow(deltaZA, 2);
            *//*
            double vectorA = (deltaXA * s.getTumoroids().get(i).getTranslateX())
                    + (deltaYA * s.getTumoroids().get(i).getY())
                    + (deltaZA * s.getTumoroids().get(i).getZ());
            // Magnitude and direction of resultant
            double deltaRA = Math.sqrt(tumor.getRSquared()
                    + s.getTumoroids().get(i).getRSquared()
                    - deltaXASquared
                    - deltaYASquared
                    - deltaZASquared);
             *//*
                double rCheckSquaredImage = deltaXASquaredImage + deltaYASquaredImage + deltaZASquaredImage;
                // Force components
                double forceXImage = ((tumor.getRSquared() + s.getTumoroids().get(i).getRSquared()) / (rCheckSquaredImage) - 1.0) * deltaXAImage;
                double forceYImage = ((tumor.getRSquared() + s.getTumoroids().get(i).getRSquared()) / (rCheckSquaredImage) - 1.0) * deltaYAImage;
                double forceZImage = ((tumor.getRSquared() + s.getTumoroids().get(i).getRSquared()) / (rCheckSquaredImage) - 1.0) * deltaZAImage;
                // Add to force component array
                forceArray[0] += forceXImage;
                forceArray[1] += forceYImage;
                forceArray[2] += forceZImage;
            }*/

            }

        }

        return forceArray;

    }

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
        g.setColor(new Color(0, 255, 0, 192)); //outline
        g.drawOval((int) x, (int) y, (int) (2 * R), (int) (2 * R));

        g.setColor(new Color(0, 255, 0, 90)); //fill
        g.fillOval((int) x, (int) y, (int) (2 * R), (int) (2 * R));

        if(this.getStatus().equals("alive")) {
            g.setColor(new Color(0, 255, 0, 192)); //outline
            g.drawOval((int) x, (int) y, (int) (2 * R), (int) (2 * R));

            g.setColor(new Color(0, 255, 0, 90)); //fill
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