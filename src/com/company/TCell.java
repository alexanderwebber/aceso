package com.company;

import java.awt.*;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public class TCell extends Particle implements Drawable {
    Simulation S;
    private boolean isActivated;

    private int idNum;

    private boolean isAttacking;

    private boolean reachedMaxKills;

    boolean previousNearTumor = false;
    boolean nearTumor;
    int arrayListPosition;
    ArrayList<double[]> xyzOutput = new ArrayList<>();
    String xyzFileName;

    private int numKills;

    // Killing, not killing
    private int status;

    private int timeAttacking;

    private int lastTimeKilled;

    // Random number gen:
    Random random;
    int[] randArray = new int[3];

    // Assign random velocity value
    double velocity = 3.0;
    
    static double velocityX;
    static double velocityY;
    static double velocityZ;

    TCell(){

    }


    TCell(double x, double y, double z, double R,  int idNum, Simulation S, Random random) {
        super(x, y, z, R, S);
        type = "TCell";
        this.S = S;
        numKills = 0;
        
        this.random = random;

        this.idNum = idNum;
        
        this.status = 1;

        
        //v = new Vector(velocityX, velocityY, velocityZ);
        xyzFileName = "xyz" + "_" + "id" + idNum + ".csv";

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

    void cellMove() throws IOException {
    	
        previousNearTumor = nearTumor;

        if(isAttacking == false) {
        	randomLeftRight(velocity, random);
        	//this.updateCollision();
        	
        	this.move();
        	
        }
        
        
        //double[] tempArray = {this.getX(), this.getY(), this.getZ()};
        //xyzOutput.add(tempArray);



        if(numKills < 20) {
            if(this.lastTimeKilled >= 360) {
                setActivated(true);
                this.setStatus(1);
                lastTimeKilled = 0;
            }

            if(isActivated == false && reachedMaxKills == false) {
                this.lastTimeKilled++;
            }

            if(isAttacking == true) {
            	
                if(timeAttacking >= 60) {
                    setIsAttacking(false);
                    this.setStatus(3);
                    timeAttacking = 0;
                    //this.velocity = 4 * Math.exp(-0.01 * R) + 0.7;
                }
                else {
                    timeAttacking++;
                }
            }

            // check tumor cells
            if(isActivated == true) {
                for(int i = 0; i < S.getNumTumor(); i++) {

                    if(isActivated == false) {
                        continue;
                    }

                    if(S.getTumoroids().get(i).getStatus().equals("dead")) {
                        continue;
                    }

                    double radius_sum_squared = Math.pow((this.getR() * 1.3) + S.getTumoroids().get(i).getR(), 2);

                    double distanceX = Math.abs(S.getTumoroids().get(i).getX() - this.getX());
                    double distanceY = Math.abs(S.getTumoroids().get(i).getY() - this.getY());
                    double distanceZ = Math.abs(S.getTumoroids().get(i).getZ() - this.getZ());

                    double distanceVector = Math.pow(distanceX, 2) + Math.pow(distanceY, 2) + Math.pow(distanceZ, 2);

                    if(distanceVector < radius_sum_squared) {
                        this.velocity = 0.0;
                        setActivated(false);
                        setIsAttacking(true);
                        this.setStatus(2);
                        S.getTumoroids().get(i).setStatus("being_attacked");
                        numKills++;
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
    
    @Override
    public void updateCollision() {

        //nearby = getNearby();

        // Reset overlappedCounter before every loop
        overlappedCounter = 0;
        overlaps.clear();

        for (Particle other : S.gels) {
            try {
                if (other != null) {
                	
                    double radiusSum = R + other.R;
                    double dx, dy, dz;
                    dx = x + velocityX - other.x;
                    dy = y + velocityY - other.y;
                    dz = z + velocityZ - other.z;
                    
                    ///x bound
                    //if other particle big x you small x bring their x here
                    if (voxel.x == S.vox.voxels_per_side - 1 && other.voxel.x == 0) {
                        dx -= S.side_length;
                    }
                    //if you big x other particle small x move x there to check
                    else if (voxel.x == 0 && other.voxel.x == S.vox.voxels_per_side - 1) {
                        dx += S.side_length;
                    }
                    //y bound
                    if (voxel.y == S.vox.voxels_per_side - 1 && other.voxel.y == 0) {
                        dy -= S.side_length;
                    } else if (voxel.y == 0 && other.voxel.y == S.vox.voxels_per_side - 1) {
                        dy += S.side_length;
                    }
                    //z bound
                    if (voxel.z == S.vox.voxels_per_side - 1 && other.voxel.z == 0) {
                        dz -= S.side_length;
                    } else if (voxel.z == 0 && other.voxel.z == S.vox.voxels_per_side - 1) {
                        dz += S.side_length;
                    }
                    
                    
                    if (Math.abs(dx) < radiusSum && Math.abs(dy) < radiusSum && Math.abs(dz) < radiusSum) {//check box
                        Vector diff = new Vector(dx, dy, dz);
                        double d = diff.magnitude() - radiusSum;
                        if (d < 0) { //overlap
                            v = v.add(diff.unitVector().scale(-d * other.R / radiusSum));
                            other.v = other.v.add(diff.unitVector().scale(d * R / radiusSum));
                            overlappedCounter++;
                            overlaps.add(diff.magnitude() / radiusSum);
                        }

                    }
                    
					/*
					 * if(this.type.equals("TCell")) { for(int i = 0; i <
					 * other.getImageParticles().length; i++) { if(other.getImageParticles()[i] !=
					 * null) { double radiusSumImage = R + other.R; double dxImage, dyImage,
					 * dzImage;
					 * 
					 * dxImage = x + v.x() - other.getImageParticles()[i].x; dyImage = y + v.y() -
					 * other.getImageParticles()[i].y; dzImage = z + v.z() -
					 * other.getImageParticles()[i].z;
					 * 
					 * if (Math.abs(dxImage) < radiusSum && Math.abs(dyImage) < radiusSum &&
					 * Math.abs(dzImage) < radiusSumImage) {//check box Vector diff = new
					 * Vector(dxImage, dyImage, dzImage); double d = diff.magnitude() -
					 * radiusSumImage; if (d < 0) { //overlap v = v.add(diff.unitVector().scale(-d *
					 * other.getImageParticles()[i].R / radiusSumImage)); overlappedCounter++;
					 * overlaps.add(diff.magnitude() / radiusSum); }
					 * 
					 * } } } }
					 */

                    else {
                    }

                }
            }

            catch(NullPointerException e) {
                System.out.println(e);
            }

        }


        if(imImage != true) {
            this.move();
        }
    }
    
    @Override
    protected void move() {
        if(imImage == false) {
        	
            setXYZ(mod(x + velocityX, S.side_length), mod(y + velocityY, S.side_length), mod(z + velocityZ, S.side_length));

            xPrime += velocityX;
            yPrime += velocityY;
            zPrime += velocityZ;

        }
    }
    
    
    public void draw(Graphics g) {

        g.setColor(new Color(200, 0, 0, 192)); //outline
        g.drawOval((int) x, (int) y, (int) (2 * R), (int) (2 * R));

        g.setColor(new Color(200, 0, 0, 90)); //fill
        g.fillOval((int) x, (int) y, (int) (2 * R), (int) (2 * R));

        if(this.getStatus() == 1) {
            g.setColor(new Color(200, 0, 0, 192)); //outline
            g.drawOval((int) x, (int) y, (int) (2 * R), (int) (2 * R));

            g.setColor(new Color(200, 0, 0, 90)); //fill
            g.fillOval((int) x, (int) y, (int) (2 * R), (int) (2 * R));
        }

        else if(this.getStatus() == 2) {
            g.setColor(new Color(255, 255, 0, 192)); //outline
            g.drawOval((int) x, (int) y, (int) (2 * R), (int) (2 * R));

            g.setColor(new Color(255, 255, 0, 90)); //fill
            g.fillOval((int) x, (int) y, (int) (2 * R), (int) (2 * R));
        }

        else if(this.getStatus() == 3) {
            g.setColor(new Color(255, 165, 0, 192)); //outline
            g.drawOval((int) x, (int) y, (int) (2 * R), (int) (2 * R));

            g.setColor(new Color(255, 165, 0, 90)); //fill
            g.fillOval((int) x, (int) y, (int) (2 * R), (int) (2 * R));
        }

        else if(this.getStatus() == 4) {
            g.setColor(new Color(255, 0, 255, 192)); //outline
            g.drawOval((int) x, (int) y, (int) (2 * R), (int) (2 * R));

            g.setColor(new Color(255, 0, 255, 90)); //fill
            g.fillOval((int) x, (int) y, (int) (2 * R), (int) (2 * R));
        }
    }
}