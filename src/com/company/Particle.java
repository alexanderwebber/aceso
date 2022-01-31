package com.company;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashSet;



public class Particle {
    static final double PI = 3.14159265358979323846264338327950288419716939937511;
    double R, x, y, z, dx = 0.0, dy = 0.0, dz = 0.0, xPrime, yPrime, zPrime;
    double x0, y0, z0;
    Vector v = new Vector();
    Voxel[] in_voxels;
    Particle[] nearby;
    String type;
    Voxel voxel;
    Simulation S;
    double dt = 0.01;

    ArrayList<Double> overlaps = new ArrayList<>();

    int overlappedCounter;
    int tumorHitCounter = 0;
    int residenceTime = 0;

    String tag = "Tumor_Remove";

    private int numPBCJumpsX = 0;
    private int numPBCJumpsY = 0;
    private int numPBCJumpsZ = 0;


    // Possible Image Particles

    boolean imImage;

    Particle() {
    }

    Particle(Simulation S) {
        this.S = S;
    }

    public Particle(double x, double y, double z, double R, Simulation S) {
        x0 = x;
        y0 = y;
        z0 = z;

        xPrime = x;
        yPrime = y;
        zPrime = z;

        this.x = x;
        this.y = y;
        this.z = z;
        this.R = R;
        this.S = S;
        voxel = getVoxel();
        in_voxels = getVoxels();
        nearby = getNearby();
        imImage = false;
        
    }
    
    //Particles are assumed to be spheres
    double volume() {
        return 4 * PI * Math.pow(R, 3) / 3;
    }

    int getTumorHitCounter() {
        return tumorHitCounter;

    }

    int getResidenceTime() {
        return residenceTime;
    }

    int getNumOverlaps() {
        return overlappedCounter;
    }

    ArrayList<Double> getOverlapDistribution() { return overlaps; }

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

    public void setZ(double z) {
        this.z = z;
    }

    public double getR() {
        return R;
    }

    public void setR(double R) { this.R = R; }

    public double getxPrime() {
        return xPrime;
    }

    public double getyPrime() {
        return yPrime;
    }

    public double getzPrime() {
        return zPrime;
    }

    public double getRSquared() { return R * R; }

    public int getNumPBCJumpsX() { return numPBCJumpsX; }

    public int getNumPBCJumpsY() {
        return numPBCJumpsY;
    }

    public int getNumPBCJumpsZ() {
        return numPBCJumpsZ;
    }

    Voxel[] getVoxels() {
        HashSet<Voxel> ans = new HashSet<>();
        ans.add(getVoxel(x, y, z));
        ans.add(getVoxel(x + R, y + R, z + R));
        ans.add(getVoxel(x + R, y + R, z - R));
        ans.add(getVoxel(x + R, y - R, z + R));
        ans.add(getVoxel(x + R, y - R, z - R));
        ans.add(getVoxel(x - R, y + R, z + R));
        ans.add(getVoxel(x - R, y + R, z - R));
        ans.add(getVoxel(x - R, y - R, z + R));
        ans.add(getVoxel(x - R, y - R, z - R));
        Voxel[] arr = new Voxel[ans.size()];
        Object[] temp = ans.toArray();
        for (int i = 0; i < ans.size(); ++i)
            arr[i] = (Voxel) temp[i];
        return arr;
    }

    public Particle[] getNearby() {
        HashSet<Particle> ans = new HashSet<>();
        for (Voxel v : in_voxels) {
            for (Particle p : v.particles) {
                ans.add(p);
            }
        }
        ans.remove(this);
        Particle[] arr = new Particle[ans.size()];
        int index = 0;
        for (Particle p : ans) {
            arr[index++] = p;
        }
        return arr;
    }

    Voxel getVoxel() {
        double vox_length = (S.sideLength / S.vox.voxels_per_side);
        int i = (int) (x / vox_length);
        i = i >= 0 ? i < S.vox.voxels_per_side ? i : i - S.vox.voxels_per_side : i + S.vox.voxels_per_side;
        int j = (int) (y / vox_length);
        j = j >= 0 ? j < S.vox.voxels_per_side ? j : j - S.vox.voxels_per_side : j + S.vox.voxels_per_side;
        int k = (int) (z / vox_length);
        k = k >= 0 ? k < S.vox.voxels_per_side ? k : k - S.vox.voxels_per_side : k + S.vox.voxels_per_side;
        return S.vox.voxels[i][j][k];
    }

    Voxel getVoxel(double x, double y, double z) {
        double vox_length = (S.sideLength / S.vox.voxels_per_side);
        int i = (int) (x / vox_length);
        i = i >= 0 ? i < S.vox.voxels_per_side ? i : i - S.vox.voxels_per_side : i + S.vox.voxels_per_side;
        int j = (int) (y / vox_length);
        j = j >= 0 ? j < S.vox.voxels_per_side ? j : j - S.vox.voxels_per_side : j + S.vox.voxels_per_side;
        int k = (int) (z / vox_length);
        k = k >= 0 ? k < S.vox.voxels_per_side ? k : k - S.vox.voxels_per_side : k + S.vox.voxels_per_side;
        return S.vox.voxels[i][j][k];
    }

    // returns false if no collision
    public boolean checkCollision() {

        nearby = getNearby();

        for (Particle other : S.gels) {
            if (other != null) {
                double radiusSum = this.R + other.R;
                double dx, dy, dz;
                dx = this.x - other.x;
                dy = this.y - other.y;
                dz = this.z - other.z;

                //x bound
                //if other particle big x you small x bring their x here
                if (voxel.x == S.vox.voxels_per_side - 1 && other.voxel.x == 0) {
                    dx -= S.sideLength;
                }
                //if you big x other particle small x move x there to check
                else if (voxel.x == 0 && other.voxel.x == S.vox.voxels_per_side - 1) {
                    dx += S.sideLength;
                }
                //y bound
                if (voxel.y == S.vox.voxels_per_side - 1 && other.voxel.y == 0) {
                    dy -= S.sideLength;
                }
                else if (voxel.y == 0 && other.voxel.y == S.vox.voxels_per_side - 1) {
                    dy += S.sideLength;
                }
                //z bound
                if (voxel.z == S.vox.voxels_per_side - 1 && other.voxel.z == 0) {
                    dz -= S.sideLength;
                }
                else if (voxel.z == 0 && other.voxel.z == S.vox.voxels_per_side - 1) {
                    dz += S.sideLength;
                }

                if (Math.abs(dx) < radiusSum && Math.abs(dy) < radiusSum && Math.abs(dz) < radiusSum) {
                    Vector diff = new Vector(dx, dy, dz);
                    if (diff.magnitude() < Math.pow(radiusSum, 2)) {
                        return true;
                    }
                }
            }
        }
        
        for (Particle other : S.getTumoroids()) {
            if (other != null) {
                double radiusSum = R + other.R;

                double dx, dy, dz;

                dx = x - other.x;
                dy = y - other.y;
                dz = z - other.z;

                if (Math.abs(dx) < radiusSum && Math.abs(dy) < radiusSum && Math.abs(dz) < radiusSum) {
                    Vector diff = new Vector(dx, dy, dz);

                    if (diff.magnitude() < Math.pow(radiusSum, 2)) {
                        return true;
                    }
                }
            }
        }
        
        return false;
    }
    
    public static double roundAwayFromZero(double numberToRound) {
		double roundedNumber;
		
		// Need to use BigDecimal since Java's rounding doesn't work with PBC
		// Have to use two BigDecimals since the class is immutable
		BigDecimal firstIntermediateNumber = new BigDecimal(numberToRound);
		BigDecimal secondIntermediateNumber;
		
		secondIntermediateNumber = firstIntermediateNumber.setScale(0, RoundingMode.HALF_UP);
		
		// Convert BigDecimal to a double then return
		roundedNumber = secondIntermediateNumber.doubleValue();

		return roundedNumber;
	}

    int returnCoordination() {
        nearby = getNearby();

        int coordinationNumber = 0;

        for (Particle other : S.gels) {
            if (other != null && !other.equals(this)) {
                // Dilation of 1%
                double dilationRadiusSum = (this.R * (1 + 0.01)) + (other.R * (1 + 0.01));

                double dx, dy, dz;

                dx = this.x - other.x;
                dy = this.y - other.y;
                dz = this.z - other.z;


                // Minimum image convention
//                dx = dx - S.side_length * roundAwayFromZero(dx / S.side_length);
//                dy = dy - S.side_length * roundAwayFromZero(dy / S.side_length);
//                dz = dz - S.side_length * roundAwayFromZero(dz / S.side_length);

                //x bound
                //if other particle big x you small x bring their x here
                if (voxel.x == S.vox.voxels_per_side - 1 && other.voxel.x == 0) {
                    dx -= S.sideLength;
                }
                //if you big x other particle small x move x there to check
                else if (voxel.x == 0 && other.voxel.x == S.vox.voxels_per_side - 1) {
                    dx += S.sideLength;
                }

                //y bound
                if (voxel.y == S.vox.voxels_per_side - 1 && other.voxel.y == 0) {
                    dy -= S.sideLength;
                }
                else if (voxel.y == 0 && other.voxel.y == S.vox.voxels_per_side - 1) {
                    dy += S.sideLength;
                }

                //z bound
                if (voxel.z == S.vox.voxels_per_side - 1 && other.voxel.z == 0) {
                    dz -= S.sideLength;
                }
                else if (voxel.z == 0 && other.voxel.z == S.vox.voxels_per_side - 1) {
                    dz += S.sideLength;
                }

                if (Math.abs(dx) < dilationRadiusSum && Math.abs(dy) < dilationRadiusSum && Math.abs(dz) < dilationRadiusSum) {
                    Vector diff = new Vector(dx, dy, dz);

                    if (diff.magnitude() - dilationRadiusSum < 0) {
                        coordinationNumber++;
                    }
                }
            }
        }

        return coordinationNumber;
    }

    boolean checkCollision(double x, double y, double z, double R) {
        nearby = getNearby();
        ArrayList<Particle> combinedList = new ArrayList<>();

        combinedList.addAll(S.gels);
        //combinedList.addAll(S.tumoroids);

        for (Particle other : combinedList) {
            if (other != null) {
                double radiusSum = R + other.R;

                double dx, dy, dz;

                dx = x - other.x;
                dy = y - other.y;
                dz = z - other.z;
                
                
                // Minimum image convention
//                dx = dx - S.side_length * roundAwayFromZero(dx / S.side_length);
//                dy = dy - S.side_length * roundAwayFromZero(dy / S.side_length);
//                dz = dz - S.side_length * roundAwayFromZero(dz / S.side_length);

                //x bound
                //if other particle big x you small x bring their x here
//                if (voxel.x == S.vox.voxels_per_side - 1 && other.voxel.x == 0) {
//                    dx -= S.sideLength;
//                }
//                //if you big x other particle small x move x there to check
//                else if (voxel.x == 0 && other.voxel.x == S.vox.voxels_per_side - 1) {
//                    dx += S.sideLength;
//                }
//
//                //y bound
//                if (voxel.y == S.vox.voxels_per_side - 1 && other.voxel.y == 0) {
//                    dy -= S.sideLength;
//                }
//                else if (voxel.y == 0 && other.voxel.y == S.vox.voxels_per_side - 1) {
//                    dy += S.sideLength;
//                }
//
//                //z bound
//                if (voxel.z == S.vox.voxels_per_side - 1 && other.voxel.z == 0) {
//                    dz -= S.sideLength;
//                }
//                else if (voxel.z == 0 && other.voxel.z == S.vox.voxels_per_side - 1) {
//                    dz += S.sideLength;
//                }

                if (Math.abs(dx) < radiusSum && Math.abs(dy) < radiusSum && Math.abs(dz) < radiusSum) {
                    Vector diff = new Vector(dx, dy, dz);

                    if (diff.magnitude() - radiusSum < 0) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    public void updateCollision() {

        nearby = getNearby();

        // Reset overlappedCounter before every loop
        overlappedCounter = 0;
        overlaps.clear();

        ArrayList<Particle> combinedList = new ArrayList<>();

        combinedList.addAll(S.gels);
        combinedList.addAll(S.tumoroids);

        for (Particle other : combinedList) {
            try {
                if (other != null) {
                	
                    double radiusSum = R + other.R;
                    double dx, dy, dz;
                    dx = x + v.x() - other.x;
                    dy = y + v.y() - other.y;
                    dz = z + v.z() - other.z;
                    
                    
//                    dx = dx - S.side_length * roundAwayFromZero(dx / S.side_length);
//                    dy = dy - S.side_length * roundAwayFromZero(dy / S.side_length);
//                    dz = dz - S.side_length * roundAwayFromZero(dz / S.side_length);
                 
                    //if you big x other particle small x move x there to check
                    if (voxel.x == S.vox.voxels_per_side - 1 && other.voxel.x == 0) {
                        dx -= S.sideLength;
                    }
                    else if (voxel.x == 0 && other.voxel.x == S.vox.voxels_per_side - 1) {
                        dx += S.sideLength;
                    }

                    //y bound
                    if (voxel.y == S.vox.voxels_per_side - 1 && other.voxel.y == 0) {
                        dy -= S.sideLength;
                    }
                    else if (voxel.y == 0 && other.voxel.y == S.vox.voxels_per_side - 1) {
                        dy += S.sideLength;
                    }

                      //z bound
                    if (voxel.z == S.vox.voxels_per_side - 1 && other.voxel.z == 0) {
                        dz -= S.sideLength;
                    }
                    else if (voxel.z == 0 && other.voxel.z == S.vox.voxels_per_side - 1) {
                        dz += S.sideLength;
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

                }
            }

            catch(NullPointerException e) {
                System.out.println(e);
            }

        }


        if(imImage == false) {
            move();
        }

    }
    
    double mod(double a, double b) {
        return ((a % b) + b) % b;
    }

    protected void move() {
        if(imImage == false) {

            setXYZ(mod(x + v.x(), S.sideLength), mod(y + v.y(), S.sideLength), mod(z + v.z(), S.sideLength));

            xPrime += v.x();
            yPrime += v.y();
            zPrime += v.z();
        }
    }
    
    synchronized void setXYZ(double newx, double newy, double newz) {
        S.vox.remove(this);
        this.x = newx;
        this.y = newy;
        this.z = newz;

        voxel = getVoxel();
        in_voxels = getVoxels();
        S.vox.add(this);

    }
}
