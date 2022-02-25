package com.company;

import org.apache.commons.math3.distribution.ExponentialDistribution;
import org.apache.commons.math3.distribution.LogNormalDistribution;

import java.awt.*;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public class TCell extends Particle implements Drawable {
    Simulation S;
    private boolean isActivated = true;

    private int idNum;

    private boolean isAttacking;

    private int lifeTime = 0;

    private boolean reachedMaxKills;

    boolean previousNearTumor = false;
    boolean nearTumor;
    int arrayListPosition;
    ArrayList<double[]> xyzOutput = new ArrayList<>();
    int timeBetweenKill = 0;
    String xyzFileName;

    private int numKills;

    ArrayList<Integer> individualAverageTimeBetweenKills = new ArrayList<>();

    // Killing, not killing
    int status;

    private int timeAttacking;

    private int refractoryPeriod;

    private int lastTimeKilled;

    // Random number gen:
    Random random;
    int[] randArray = new int[3];

    // Assign random velocity value
    double velocity = 3.5;
    
    static double velocityX;
    static double velocityY;
    static double velocityZ;

    Color tCellColorOutline = new Color(0, 255, 0, 192);

    Color tCellColorBody = new Color(0, 255, 0, 90);

    TCell(){

    }

    TCell(double x, double y, double z, double R, int idNum, Simulation S, Random random, LogNormalDistribution logNormal, int doublingTime, int refractoryPeriod) {
        super(x, y, z, R, S);
        type = "TCell";
        this.S = S;
        numKills = 0;
        velocity = 3.5;
        
        this.random = random;

        this.idNum = idNum;

        //this.velocity = logNormal.sample();
        
        //v = new Vector(velocityX, velocityY, velocityZ);
        xyzFileName = "xyz" + "_" + "id" + idNum + ".csv";

        this.refractoryPeriod = refractoryPeriod;

        lastTimeKilled = 0;

        lifeTime = random.nextInt(doublingTime);

        timeAttacking = random.nextInt(720);

        if(timeAttacking != 0) {
            isActivated = false;
            this.setStatus(2);
        }
        else {
            isActivated = true;
            this.setStatus(1);
        }


    }

    void settCellColorOutline(Color outlineColor) {
        tCellColorOutline = outlineColor;
    }

    void settCellColorBody(Color bodyColor) {
        tCellColorBody = bodyColor;
    }

    double distanceTraveled() {
        return Vector.magnitude(dx, dy, dz);
    }

    public void outputXYZCSV() throws IOException {
        FileWriter xyzWriter = new FileWriter(this.xyzFileName);

        for(int i = 0; i < xyzOutput.size(); i++) {
            xyzWriter.append(String.format("%.3f,%.3f,%.3f\n", xyzOutput.get(i)[0],xyzOutput.get(i)[1], xyzOutput.get(i)[2]));
        }

        xyzWriter.flush();
    }

    //TODO launch with same random seed, launch with different random seed
    double displacement() {

		/*
		 * double pbcAdjustmentX = S.side_length * getNumPBCJumpsX(); double
		 * pbcAdjustmentY = S.side_length * getNumPBCJumpsY(); double pbcAdjustmentZ =
		 * S.side_length * getNumPBCJumpsZ();
		 */
        //TODO Adjust to consider jumps across PBC
        return Vector.magnitude(this.getxPrime() - x0, this.getyPrime() - y0, this.getzPrime() - z0);
    }

    public int getNumKills() {
        return numKills;
    }

    public double getZ() {
        return z;
    }

    public boolean getNearTumor() {
        return nearTumor;
    }

    public boolean getPreviousNearTumor() { return previousNearTumor; }

    public void setActivated(boolean activated) { this.isActivated = activated; }

    public void setStatus(int status) { this.status = status; }

    public boolean getActivated() { return isActivated; }

    public String getType() {
        return type;
    }

    public int getLastTimeKilled() {
        return lastTimeKilled;
    }

    public int getIdNum() {
        return idNum;
    }

    public void setIsAttacking(boolean isAttacking) {
        this.isAttacking = isAttacking;
    }

    public void setLastTimeKilled(int lastTimeKilled) {
        this.lastTimeKilled = lastTimeKilled;
    }

    public void setType(String type) {
        this.type = type;
    }

    public boolean getIsAttacking() {
        return isAttacking;
    }

    public int getStatus() { return this.status; }

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

    public void setZ(double z) {
        this.z = z;
    }

    public double getR() {
        return R;
    }

    public void incrementLifeTime() {
        lifeTime++;
    }

    public int getLifeTime() {
        return lifeTime;
    }

    public void setLifeTime(int lifeTime) {
        this.lifeTime = lifeTime;
    }

    void cellMove() throws IOException {
        incrementLifeTime();

        previousNearTumor = nearTumor;
        
        this.v = Vector.random3(velocity, random);
        
        if(!isActivated && checkCollision(this.getX() + this.v.x(), this.getY() + this.v.y(), this.getZ() + this.v.z(), this.getR())) {

        }
        else {
            move();
        }

        // TODO: Change back to normal amount of kills
        if(numKills < 1000000) {
            if(!isActivated) {
                if(isAttacking) {

                    if(timeAttacking >= 720) {
                        setIsAttacking(false);
                        this.setStatus(3);
                        timeAttacking = 0;
                        this.velocity = 3.5;
                    }
                    else {
                        timeAttacking++;
                        this.lastTimeKilled++;
                    }
                }
                else {
                    if(this.lastTimeKilled >= refractoryPeriod) {
                        setActivated(true);
                        this.setStatus(1);
                        setLastTimeKilled(0);
                    }
                    else {
                        this.lastTimeKilled++;
                        timeBetweenKill++;
                    }
                }

            }



            // check tumor cells
            else {

                for(int i = 0; i < S.getNumTumor(); i++) {

                    if(isActivated == false) {
                        break;
                    }

                    if(S.getTumoroids().get(i).getStatus().equals("dead") || S.getTumoroids().get(i).getStatus().equals("being_attacked")) {
                        continue;
                    }

                    double radiusSum = this.getR() + S.getTumoroids().get(i).getR();

                    double distanceX = Math.abs(S.getTumoroids().get(i).getX() - this.getX());
                    double distanceY = Math.abs(S.getTumoroids().get(i).getY() - this.getY());
                    double distanceZ = Math.abs(S.getTumoroids().get(i).getZ() - this.getZ());

                    double distanceToTumorCenterX = Math.abs(S.tumorGel.getX() - this.getX());
                    double distanceToTumorCenterY = Math.abs(S.tumorGel.getY() - this.getY());
                    double distanceToTumorCenterZ = Math.abs(S.tumorGel.getZ() - this.getZ());

                    double distanceToTumorCell = Math.sqrt((distanceX * distanceX) + (distanceY * distanceY) + (distanceZ * distanceZ));
                    double distanceVector = Math.sqrt((distanceToTumorCenterX * distanceToTumorCenterX) + (distanceToTumorCenterY * distanceToTumorCenterY) + (distanceToTumorCenterZ * distanceToTumorCenterZ));

                    if(distanceToTumorCell - radiusSum < 0) {
                        this.velocity = 0.0;
                        setActivated(false);
                        setIsAttacking(true);
                        individualAverageTimeBetweenKills.add(timeBetweenKill);
                        this.setStatus(2);
                        S.getTumoroids().get(i).setStatus("delete");
                        numKills++;
                        setLastTimeKilled(0);
                    }
                }

            }
        }

    }

    public int addStartPoint(TCell tcell) {
        int[] startRow = {tcell.getIdNum(), (int)S.sim_time, 0};
        S.startValues.add(startRow);

        int position = S.startValues.indexOf(startRow);

        return position;
    }
    
    static void random3(double velocity, Random random) {
    	double dx = random.nextDouble() - 0.5;
    	double dy = random.nextDouble() - 0.5;
    	double dz = random.nextDouble() - 0.5;
    	
    	double length = Math.sqrt((dx * dx) + (dy * dy) + (dz * dz));
    	
    	velocityX = dx * (velocity / length);
    	velocityY = dy * (velocity / length);
    	velocityZ = dz * (velocity / length);
    	
    }
    
    static void randomLeftRight(double velocity, Random random) {
    	double dx = random.nextDouble() - 0.5;
    	double dy = random.nextDouble() - 0.5;
    	double dz = random.nextDouble() - 0.5;
    	
    	double length = Math.sqrt((dx * dx));
    	
    	velocityX = dx * (velocity / length);
    	//velocityY = dy * (velocity / length);
    	//velocityZ = dz * (velocity / length);
    	
    }
    
//    @Override
//    protected void move() {
//        if(imImage == false) {
//        	
//            setXYZ(mod(x + velocityX, S.side_length), mod(y + velocityY, S.side_length), mod(z + velocityZ, S.side_length));
//
//            xPrime += velocityX;
//            yPrime += velocityY;
//            zPrime += velocityZ;
//
//        }
//    }


    
    public void draw(Graphics g) {

        g.setColor(tCellColorOutline);
        g.drawOval((int) x, (int) y, (int) (2 * R), (int) (2 * R));

        g.setColor(tCellColorBody); //fill
        g.fillOval((int) x, (int) y, (int) (2 * R), (int) (2 * R));

//        if(this.getStatus() == 1) {
//            g.setColor(new Color(0, 255, 0, 192)); //outline
//            g.drawOval((int) x, (int) y, (int) (2 * R), (int) (2 * R));
//
//            g.setColor(new Color(0, 255, 0, 90)); //fill
//            g.fillOval((int) x, (int) y, (int) (2 * R), (int) (2 * R));
//        }
//
//        else if(this.getStatus() == 2) {
//
//            g.setColor(new Color(255, 165, 0, 192)); //outline
//            g.drawOval((int) x, (int) y, (int) (2 * R), (int) (2 * R));
//
//            g.setColor(new Color(255, 165, 0, 90)); //fill
//            g.fillOval((int) x, (int) y, (int) (2 * R), (int) (2 * R));
//        }
//
//        else if(this.getStatus() == 3) {
//            g.setColor(new Color(255, 255, 0, 192)); //outline
//            g.drawOval((int) x, (int) y, (int) (2 * R), (int) (2 * R));
//
//            g.setColor(new Color(255, 255, 0, 90)); //fill
//            g.fillOval((int) x, (int) y, (int) (2 * R), (int) (2 * R));
//        }
//
//        else if(this.getStatus() == 4) {
//            g.setColor(new Color(255, 0, 255, 192)); //outline
//            g.drawOval((int) x, (int) y, (int) (2 * R), (int) (2 * R));
//
//            g.setColor(new Color(255, 0, 255, 90)); //fill
//            g.fillOval((int) x, (int) y, (int) (2 * R), (int) (2 * R));
//        }
    }
}