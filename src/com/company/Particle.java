package com.company;

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

    String tag = "Tumor_Remove";

    private int numPBCJumpsX = 0;
    private int numPBCJumpsY = 0;
    private int numPBCJumpsZ = 0;


    // Possible Image Particles
    Particle imageX;
    Particle imageY;
    Particle imageZ;

    Particle imageXY1;
    Particle imageXY2;
    Particle imageXY3;

    Particle imageXZ1;
    Particle imageXZ2;
    Particle imageXZ3;

    Particle imageYZ1;
    Particle imageYZ2;
    Particle imageYZ3;

    Particle imageXYZ1;
    Particle imageXYZ2;
    Particle imageXYZ3;
    Particle imageXYZ4;
    Particle imageXYZ5;
    Particle imageXYZ6;
    Particle imageXYZ7;

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

    Particle[] getImageParticles() {
        Particle[] imageParticleArray = new Particle[19];

        imageParticleArray[0] = imageX;
        imageParticleArray[1] = imageY;
        imageParticleArray[2] = imageZ;

        imageParticleArray[3] = imageXY1;
        imageParticleArray[4] = imageXY2;
        imageParticleArray[5] = imageXY3;

        imageParticleArray[6] = imageXZ1;
        imageParticleArray[7] = imageXZ2;
        imageParticleArray[8] = imageXZ3;

        imageParticleArray[9] = imageYZ1;
        imageParticleArray[10] = imageYZ2;
        imageParticleArray[11] = imageYZ3;

        imageParticleArray[12] = imageXYZ1;
        imageParticleArray[13] = imageXYZ2;
        imageParticleArray[14] = imageXYZ3;
        imageParticleArray[15] = imageXYZ4;
        imageParticleArray[16] = imageXYZ5;
        imageParticleArray[17] = imageXYZ6;
        imageParticleArray[18] = imageXYZ7;

        return imageParticleArray;
    }

    int getNumOverlaps() {
        return overlappedCounter;
    }

    ArrayList<Double> getOverlapDistribution() { return overlaps; }

    synchronized void setXYZ(double newx, double newy, double newz) {
        S.vox.remove(this);
        this.x = newx;
        this.y = newy;
        this.z = newz;

        voxel = getVoxel();
        in_voxels = getVoxels();
        nearby = getNearby();
        S.vox.add(this);

        //Creation of the image at border
        // at the case of colliding with one of the borders:
        // We identify which border is under collision
        // According to the axis that collides, we mirror

        boolean updatedX = false;
        boolean updatedY = false;
        boolean updatedZ = false;
        boolean updatedXY = false;
        boolean updatedXZ = false;
        boolean updatedXYZ = false;
        boolean updatedYZ = false;

        // done
        if(this.x + this.getR() >= this.S.side_length && imImage == false) {
            if(this.y + this.getR() >= this.S.side_length) {
                if(this.z + getR() >= this.S.side_length) {
                    if(imageXYZ1 == null) {
                        imageXYZ1 = new Particle(this.getX(), this.getY(), this.getZ(), this.getR(), S);
                        imageXYZ1.imImage = true;
                    }

                    imageXYZ1.setX(this.getX() - this.S.side_length);
                    imageXYZ1.setY(this.getY() - this.S.side_length);
                    imageXYZ1.setZ(this.getZ() - this.S.side_length);

                    if(imageXYZ2 == null) {
                        imageXYZ2 = new Particle(this.getX(), this.getY(), this.getZ(), this.getR(), S);
                        imageXYZ2.imImage = true;
                    }

                    imageXYZ2.setX(this.getX());
                    imageXYZ2.setY(this.getY() - this.S.side_length);
                    imageXYZ2.setZ(this.getZ() - this.S.side_length);

                    if(imageXYZ3 == null) {
                        imageXYZ3 = new Particle(this.getX(), this.getY(), this.getZ(), this.getR(), S);
                        imageXYZ3.imImage = true;
                    }

                    imageXYZ3.setX(this.getX());
                    imageXYZ3.setY(this.getY());
                    imageXYZ3.setZ(this.getZ() - this.S.side_length);

                    if(imageXYZ4 == null) {
                        imageXYZ4 = new Particle(this.getX(), this.getY(), this.getZ(), this.getR(), S);
                        imageXYZ4.imImage = true;
                    }

                    imageXYZ4.setX(this.getX() - this.S.side_length);
                    imageXYZ4.setY(this.getY());
                    imageXYZ4.setZ(this.getZ() - this.S.side_length);

                    if(imageXYZ5 == null) {
                        imageXYZ5 = new Particle(this.getX(), this.getY(), this.getZ(), this.getR(), S);
                        imageXYZ5.imImage = true;
                    }

                    imageXYZ5.setX(this.getX() - this.S.side_length);
                    imageXYZ5.setY(this.getY());
                    imageXYZ5.setZ(this.getZ());

                    if(imageXYZ6 == null) {
                        imageXYZ6 = new Particle(this.getX(), this.getY(), this.getZ(), this.getR(), S);
                        imageXYZ6.imImage = true;
                    }

                    imageXYZ6.setX(this.getX());
                    imageXYZ6.setY(this.getY() - this.S.side_length);
                    imageXYZ6.setZ(this.getZ());

                    if(imageXYZ7 == null) {
                        imageXYZ7 = new Particle(this.getX(), this.getY(), this.getZ(), this.getR(), S);
                        imageXYZ7.imImage = true;
                    }

                    imageXYZ7.setX(this.getX() - this.S.side_length);
                    imageXYZ7.setY(this.getY() - this.S.side_length);
                    imageXYZ7.setZ(this.getZ());
                    updatedXYZ = true;
                }

                else if(this.z - getR() <= 0) {
                    if(imageXYZ1 == null) {
                        imageXYZ1 = new Particle(this.getX(), this.getY(), this.getZ(), this.getR(), S);
                        imageXYZ1.imImage = true;
                    }

                    imageXYZ1.setX(this.getX() - this.S.side_length);
                    imageXYZ1.setY(this.getY() - this.S.side_length);
                    imageXYZ1.setZ(this.getZ() + this.S.side_length);

                    if(imageXYZ2 == null) {
                        imageXYZ2 = new Particle(this.getX(), this.getY(), this.getZ(), this.getR(), S);
                        imageXYZ2.imImage = true;
                    }

                    imageXYZ2.setX(this.getX());
                    imageXYZ2.setY(this.getY());
                    imageXYZ2.setZ(this.getZ() + this.S.side_length);

                    if(imageXYZ3 == null) {
                        imageXYZ3 = new Particle(this.getX(), this.getY(), this.getZ(), this.getR(), S);
                        imageXYZ3.imImage = true;
                    }

                    imageXYZ3.setX(this.getX() - this.S.side_length);
                    imageXYZ3.setY(this.getY() - this.S.side_length);
                    imageXYZ3.setZ(this.getZ());

                    if(imageXYZ4 == null) {
                        imageXYZ4 = new Particle(this.getX(), this.getY(), this.getZ(), this.getR(), S);
                        imageXYZ4.imImage = true;
                    }

                    imageXYZ4.setX(this.getX() - this.S.side_length);
                    imageXYZ4.setY(this.getY());
                    imageXYZ4.setZ(this.getZ());

                    if(imageXYZ5 == null) {
                        imageXYZ5 = new Particle(this.getX(), this.getY(), this.getZ(), this.getR(), S);
                        imageXYZ5.imImage = true;
                    }

                    imageXYZ5.setX(this.getX());
                    imageXYZ5.setY(this.getY() - this.S.side_length);
                    imageXYZ5.setZ(this.getZ());

                    if(imageXYZ6 == null) {
                        imageXYZ6 = new Particle(this.getX(), this.getY(), this.getZ(), this.getR(), S);
                        imageXYZ6.imImage = true;
                    }

                    imageXYZ6.setX(this.getX());
                    imageXYZ6.setY(this.getY() - this.S.side_length);
                    imageXYZ6.setZ(this.getZ() + this.S.side_length);

                    if(imageXYZ7 == null) {
                        imageXYZ7 = new Particle(this.getX(), this.getY(), this.getZ(), this.getR(), S);
                        imageXYZ7.imImage = true;
                    }

                    imageXYZ7.setX(this.getX() - this.S.side_length);
                    imageXYZ7.setY(this.getY());
                    imageXYZ7.setZ(this.getZ() + this.S.side_length);

                    updatedXYZ = true;
                }

                else {
                    if(imageXY1 == null) {
                        imageXY1 = new Particle(this.getX(), this.getY(), this.getZ(), this.getR(), S);
                        imageXY1.imImage = true;
                    }
                    imageXY1.setX(this.getX() - this.S.side_length);
                    imageXY1.setY(this.getY() - this.S.side_length);
                    imageXY1.setZ(this.getZ());

                    if(imageXY2 == null) {
                        imageXY2 = new Particle(this.getX(), this.getY(), this.getZ(), this.getR(), S);
                        imageXY2.imImage = true;
                    }
                    imageXY2.setX(this.getX() - this.S.side_length);
                    imageXY2.setY(this.getY());
                    imageXY2.setZ(this.getZ());

                    if(imageXY3 == null) {
                        imageXY3 = new Particle(this.getX(), this.getY(), this.getZ(), this.getR(), S);
                        imageXY3.imImage = true;
                    }
                    imageXY3.setX(this.getX());
                    imageXY3.setY(this.getY() - this.S.side_length);
                    imageXY3.setZ(this.getZ());
                    updatedXY = true;
                }
            }

            else if(this.y - getR() <= 0) {
                if(this.z + getR() >= this.S.side_length) {
                    if(imageXYZ1 == null) {
                        imageXYZ1 = new Particle(this.getX(), this.getY(), this.getZ(), this.getR(), S);
                        imageXYZ1.imImage = true;
                    }

                    imageXYZ1.setX(this.getX() - this.S.side_length);
                    imageXYZ1.setY(this.getY() + this.S.side_length);
                    imageXYZ1.setZ(this.getZ() - this.S.side_length);

                    if(imageXYZ2 == null) {
                        imageXYZ2 = new Particle(this.getX(), this.getY(), this.getZ(), this.getR(), S);
                        imageXYZ2.imImage = true;
                    }

                    imageXYZ2.setX(this.getX() - this.S.side_length);
                    imageXYZ2.setY(this.getY());
                    imageXYZ2.setZ(this.getZ() - this.S.side_length);

                    if(imageXYZ3 == null) {
                        imageXYZ3 = new Particle(this.getX(), this.getY(), this.getZ(), this.getR(), S);
                        imageXYZ3.imImage = true;
                    }

                    imageXYZ3.setX(this.getX());
                    imageXYZ3.setY(this.getY());
                    imageXYZ3.setZ(this.getZ() - this.S.side_length);

                    if(imageXYZ4 == null) {
                        imageXYZ4 = new Particle(this.getX(), this.getY(), this.getZ(), this.getR(), S);
                        imageXYZ4.imImage = true;
                    }

                    imageXYZ4.setX(this.getX() - this.S.side_length);
                    imageXYZ4.setY(this.getY());
                    imageXYZ4.setZ(this.getZ());

                    if(imageXYZ5 == null) {
                        imageXYZ5 = new Particle(this.getX(), this.getY(), this.getZ(), this.getR(), S);
                        imageXYZ5.imImage = true;
                    }

                    imageXYZ5.setX(this.getX());
                    imageXYZ5.setY(this.getY() + this.S.side_length);
                    imageXYZ5.setZ(this.getZ());

                    if(imageXYZ6 == null) {
                        imageXYZ6 = new Particle(this.getX(), this.getY(), this.getZ(), this.getR(), S);
                        imageXYZ6.imImage = true;
                    }

                    imageXYZ6.setX(this.getX());
                    imageXYZ6.setY(this.getY() + this.S.side_length);
                    imageXYZ6.setZ(this.getZ() - this.S.side_length);

                    if(imageXYZ7 == null) {
                        imageXYZ7 = new Particle(this.getX(), this.getY(), this.getZ(), this.getR(), S);
                        imageXYZ7.imImage = true;
                    }

                    imageXYZ7.setX(this.getX() - this.S.side_length);
                    imageXYZ7.setY(this.getY() + this.S.side_length);
                    imageXYZ7.setZ(this.getZ());
                    updatedXYZ = true;
                }
                else if(this.z - getR() <= 0) {
                    if(imageXYZ1 == null) {
                        imageXYZ1 = new Particle(this.getX(), this.getY(), this.getZ(), this.getR(), S);
                        imageXYZ1.imImage = true;
                    }

                    imageXYZ1.setX(this.getX() - this.S.side_length);
                    imageXYZ1.setY(this.getY() + this.S.side_length);
                    imageXYZ1.setZ(this.getZ() + this.S.side_length);

                    if(imageXYZ2 == null) {
                        imageXYZ2 = new Particle(this.getX(), this.getY(), this.getZ(), this.getR(), S);
                        imageXYZ2.imImage = true;
                    }

                    imageXYZ2.setX(this.getX());
                    imageXYZ2.setY(this.getY());
                    imageXYZ2.setZ(this.getZ() + this.S.side_length);

                    if(imageXYZ3 == null) {
                        imageXYZ3 = new Particle(this.getX(), this.getY(), this.getZ(), this.getR(), S);
                        imageXYZ3.imImage = true;
                    }

                    imageXYZ3.setX(this.getX() - this.S.side_length);
                    imageXYZ3.setY(this.getY());
                    imageXYZ3.setZ(this.getZ());

                    if(imageXYZ4 == null) {
                        imageXYZ4 = new Particle(this.getX(), this.getY(), this.getZ(), this.getR(), S);
                        imageXYZ4.imImage = true;
                    }

                    imageXYZ4.setX(this.getX());
                    imageXYZ4.setY(this.getY() + this.S.side_length);
                    imageXYZ4.setZ(this.getZ());

                    if(imageXYZ5 == null) {
                        imageXYZ5 = new Particle(this.getX(), this.getY(), this.getZ(), this.getR(), S);
                        imageXYZ5.imImage = true;
                    }

                    imageXYZ5.setX(this.getX() - this.S.side_length);
                    imageXYZ5.setY(this.getY() + this.S.side_length);
                    imageXYZ5.setZ(this.getZ());

                    if(imageXYZ6 == null) {
                        imageXYZ6 = new Particle(this.getX(), this.getY(), this.getZ(), this.getR(), S);
                        imageXYZ6.imImage = true;
                    }

                    imageXYZ6.setX(this.getX());
                    imageXYZ6.setY(this.getY() + this.S.side_length);
                    imageXYZ6.setZ(this.getZ() + this.S.side_length);

                    if(imageXYZ7 == null) {
                        imageXYZ7 = new Particle(this.getX(), this.getY(), this.getZ(), this.getR(), S);
                        imageXYZ7.imImage = true;
                    }

                    imageXYZ7.setX(this.getX() - this.S.side_length);
                    imageXYZ7.setY(this.getY());
                    imageXYZ7.setZ(this.getZ() + this.S.side_length);
                    updatedXYZ = true;
                }

                else {
                    if(imageXY1 == null) {
                        imageXY1 = new Particle(this.getX(), this.getY(), this.getZ(), this.getR(), S);
                        imageXY1.imImage = true;
                    }
                    imageXY1.setX(this.getX() - this.S.side_length);
                    imageXY1.setY(this.getY());
                    imageXY1.setZ(this.getZ());

                    if(imageXY2 == null) {
                        imageXY2 = new Particle(this.getX(), this.getY(), this.getZ(), this.getR(), S);
                        imageXY2.imImage = true;
                    }
                    imageXY2.setX(this.getX() - this.S.side_length);
                    imageXY2.setY(this.getY() + this.S.side_length);
                    imageXY2.setZ(this.getZ());

                    if(imageXY3 == null) {
                        imageXY3 = new Particle(this.getX(), this.getY(), this.getZ(), this.getR(), S);
                        imageXY3.imImage = true;
                    }
                    imageXY3.setX(this.getX());
                    imageXY3.setY(this.getY() + this.S.side_length);
                    imageXY3.setZ(this.getZ());
                    updatedXY = true;
                }
            }

            else if(this.z + getR() >= this.S.side_length) {
                if(imageXZ1 == null) {
                    imageXZ1 = new Particle(this.getX(), this.getY(), this.getZ(), this.getR(), S);
                    imageXZ1.imImage = true;
                }
                imageXZ1.setX(this.getX() - this.S.side_length);
                imageXZ1.setY(this.getY());
                imageXZ1.setZ(this.getZ() - this.S.side_length);

                if(imageXZ2 == null) {
                    imageXZ2 = new Particle(this.getX(), this.getY(), this.getZ(), this.getR(), S);
                    imageXZ2.imImage = true;
                }
                imageXZ2.setX(this.getX() - this.S.side_length);
                imageXZ2.setY(this.getY());
                imageXZ2.setZ(this.getZ());

                if(imageXZ3 == null) {
                    imageXZ3 = new Particle(this.getX(), this.getY(), this.getZ(), this.getR(), S);
                    imageXZ3.imImage = true;
                }
                imageXZ3.setX(this.getX());
                imageXZ3.setY(this.getY());
                imageXZ3.setZ(this.getZ() - this.S.side_length);
                updatedXZ = true;
            }

            else if(this.z - getR() <= 0) {
                if(imageXZ1 == null) {
                    imageXZ1 = new Particle(this.getX(), this.getY(), this.getZ(), this.getR(), S);
                    imageXZ1.imImage = true;
                }
                imageXZ1.setX(this.getX() - this.S.side_length);
                imageXZ1.setY(this.getY());
                imageXZ1.setZ(this.getZ() + this.S.side_length);

                if(imageXZ2 == null) {
                    imageXZ2 = new Particle(this.getX(), this.getY(), this.getZ(), this.getR(), S);
                    imageXZ2.imImage = true;
                }
                imageXZ2.setX(this.getX() - this.S.side_length);
                imageXZ2.setY(this.getY());
                imageXZ2.setZ(this.getZ());

                if(imageXZ3 == null) {
                    imageXZ3 = new Particle(this.getX(), this.getY(), this.getZ(), this.getR(), S);
                    imageXZ3.imImage = true;
                }
                imageXZ3.setX(this.getX());
                imageXZ3.setY(this.getY());
                imageXZ3.setZ(this.getZ() + this.S.side_length);
                updatedXZ = true;
            }

            else {
                if(imageX == null) {
                    imageX = new Particle(this.getX(), this.getY(), this.getZ(), this.getR(), S);
                    imageX.imImage = true;
                }
                imageX.setX(this.getX() - this.S.side_length);
                imageX.setY(this.getY());
                imageX.setZ(this.getZ());
                updatedX = true;
            }

        }

        // done
        else if(this.x - this.getR() <= 0 && imImage == false) {
            if(this.y + this.getR() >= this.S.side_length) {
                if(this.z + getR() >= this.S.side_length) {
                    if(imageXYZ1 == null) {
                        imageXYZ1 = new Particle(this.getX(), this.getY(), this.getZ(), this.getR(), S);
                        imageXYZ1.imImage = true;
                    }

                    imageXYZ1.setX(this.getX() + this.S.side_length);
                    imageXYZ1.setY(this.getY() - this.S.side_length);
                    imageXYZ1.setZ(this.getZ() - this.S.side_length);

                    if(imageXYZ2 == null) {
                        imageXYZ2 = new Particle(this.getX(), this.getY(), this.getZ(), this.getR(), S);
                        imageXYZ2.imImage = true;
                    }

                    imageXYZ2.setX(this.getX());
                    imageXYZ2.setY(this.getY() - this.S.side_length);
                    imageXYZ2.setZ(this.getZ() - this.S.side_length);

                    if(imageXYZ3 == null) {
                        imageXYZ3 = new Particle(this.getX(), this.getY(), this.getZ(), this.getR(), S);
                        imageXYZ3.imImage = true;
                    }

                    imageXYZ3.setX(this.getX());
                    imageXYZ3.setY(this.getY());
                    imageXYZ3.setZ(this.getZ() - this.S.side_length);

                    if(imageXYZ4 == null) {
                        imageXYZ4 = new Particle(this.getX(), this.getY(), this.getZ(), this.getR(), S);
                        imageXYZ4.imImage = true;
                    }

                    imageXYZ4.setX(this.getX() + this.S.side_length);
                    imageXYZ4.setY(this.getY());
                    imageXYZ4.setZ(this.getZ() - this.S.side_length);

                    if(imageXYZ5 == null) {
                        imageXYZ5 = new Particle(this.getX(), this.getY(), this.getZ(), this.getR(), S);
                        imageXYZ5.imImage = true;
                    }

                    imageXYZ5.setX(this.getX() + this.S.side_length);
                    imageXYZ5.setY(this.getY());
                    imageXYZ5.setZ(this.getZ());

                    if(imageXYZ6 == null) {
                        imageXYZ6 = new Particle(this.getX(), this.getY(), this.getZ(), this.getR(), S);
                        imageXYZ6.imImage = true;
                    }

                    imageXYZ6.setX(this.getX());
                    imageXYZ6.setY(this.getY() - this.S.side_length);
                    imageXYZ6.setZ(this.getZ());

                    if(imageXYZ7 == null) {
                        imageXYZ7 = new Particle(this.getX(), this.getY(), this.getZ(), this.getR(), S);
                        imageXYZ7.imImage = true;
                    }

                    imageXYZ7.setX(this.getX() + this.S.side_length);
                    imageXYZ7.setY(this.getY() - this.S.side_length);
                    imageXYZ7.setZ(this.getZ());
                    updatedXYZ = true;
                }
                else if(this.z - getR() <= 0) {
                    if(imageXYZ1 == null) {
                        imageXYZ1 = new Particle(this.getX(), this.getY(), this.getZ(), this.getR(), S);
                        imageXYZ1.imImage = true;
                    }

                    imageXYZ1.setX(this.getX() + this.S.side_length);
                    imageXYZ1.setY(this.getY() - this.S.side_length);
                    imageXYZ1.setZ(this.getZ() + this.S.side_length);

                    if(imageXYZ2 == null) {
                        imageXYZ2 = new Particle(this.getX(), this.getY(), this.getZ(), this.getR(), S);
                        imageXYZ2.imImage = true;
                    }

                    imageXYZ2.setX(this.getX());
                    imageXYZ2.setY(this.getY());
                    imageXYZ2.setZ(this.getZ() + this.S.side_length);

                    if(imageXYZ3 == null) {
                        imageXYZ3 = new Particle(this.getX(), this.getY(), this.getZ(), this.getR(), S);
                        imageXYZ3.imImage = true;
                    }

                    imageXYZ3.setX(this.getX() + this.S.side_length);
                    imageXYZ3.setY(this.getY() - this.S.side_length);
                    imageXYZ3.setZ(this.getZ());

                    if(imageXYZ4 == null) {
                        imageXYZ4 = new Particle(this.getX(), this.getY(), this.getZ(), this.getR(), S);
                        imageXYZ4.imImage = true;
                    }

                    imageXYZ4.setX(this.getX() + this.S.side_length);
                    imageXYZ4.setY(this.getY());
                    imageXYZ4.setZ(this.getZ());

                    if(imageXYZ5 == null) {
                        imageXYZ5 = new Particle(this.getX(), this.getY(), this.getZ(), this.getR(), S);
                        imageXYZ5.imImage = true;
                    }

                    imageXYZ5.setX(this.getX());
                    imageXYZ5.setY(this.getY() - this.S.side_length);
                    imageXYZ5.setZ(this.getZ());

                    if(imageXYZ6 == null) {
                        imageXYZ6 = new Particle(this.getX(), this.getY(), this.getZ(), this.getR(), S);
                        imageXYZ6.imImage = true;
                    }

                    imageXYZ6.setX(this.getX());
                    imageXYZ6.setY(this.getY() - this.S.side_length);
                    imageXYZ6.setZ(this.getZ() + this.S.side_length);

                    if(imageXYZ7 == null) {
                        imageXYZ7 = new Particle(this.getX(), this.getY(), this.getZ(), this.getR(), S);
                        imageXYZ7.imImage = true;
                    }

                    imageXYZ7.setX(this.getX() + this.S.side_length);
                    imageXYZ7.setY(this.getY());
                    imageXYZ7.setZ(this.getZ() + this.S.side_length);
                    updatedXYZ = true;
                }

                else {
                    if(imageXY1 == null) {
                        imageXY1 = new Particle(this.getX(), this.getY(), this.getZ(), this.getR(), S);
                        imageXY1.imImage = true;
                    }
                    imageXY1.setX(this.getX() + this.S.side_length);
                    imageXY1.setY(this.getY() - this.S.side_length);
                    imageXY1.setZ(this.getZ());

                    if(imageXY2 == null) {
                        imageXY2 = new Particle(this.getX(), this.getY(), this.getZ(), this.getR(), S);
                        imageXY2.imImage = true;
                    }
                    imageXY2.setX(this.getX() + this.S.side_length);
                    imageXY2.setY(this.getY());
                    imageXY2.setZ(this.getZ());

                    if(imageXY3 == null) {
                        imageXY3 = new Particle(this.getX(), this.getY(), this.getZ(), this.getR(), S);
                        imageXY3.imImage = true;
                    }
                    imageXY3.setX(this.getX());
                    imageXY3.setY(this.getY() - this.S.side_length);
                    imageXY3.setZ(this.getZ());
                    updatedXY = true;
                }
            }

            else if(this.y - getR() <= 0) {
                if(this.z + getR() >= this.S.side_length) {
                    if(imageXYZ1 == null) {
                        imageXYZ1 = new Particle(this.getX(), this.getY(), this.getZ(), this.getR(), S);
                        imageXYZ1.imImage = true;
                    }

                    imageXYZ1.setX(this.getX() + this.S.side_length);
                    imageXYZ1.setY(this.getY() + this.S.side_length);
                    imageXYZ1.setZ(this.getZ() - this.S.side_length);

                    if(imageXYZ2 == null) {
                        imageXYZ2 = new Particle(this.getX(), this.getY(), this.getZ(), this.getR(), S);
                        imageXYZ2.imImage = true;
                    }

                    imageXYZ2.setX(this.getX() + this.S.side_length);
                    imageXYZ2.setY(this.getY());
                    imageXYZ2.setZ(this.getZ() - this.S.side_length);

                    if(imageXYZ3 == null) {
                        imageXYZ3 = new Particle(this.getX(), this.getY(), this.getZ(), this.getR(), S);
                        imageXYZ3.imImage = true;
                    }

                    imageXYZ3.setX(this.getX());
                    imageXYZ3.setY(this.getY());
                    imageXYZ3.setZ(this.getZ() - this.S.side_length);

                    if(imageXYZ4 == null) {
                        imageXYZ4 = new Particle(this.getX(), this.getY(), this.getZ(), this.getR(), S);
                        imageXYZ4.imImage = true;
                    }

                    imageXYZ4.setX(this.getX() + this.S.side_length);
                    imageXYZ4.setY(this.getY());
                    imageXYZ4.setZ(this.getZ());

                    if(imageXYZ5 == null) {
                        imageXYZ5 = new Particle(this.getX(), this.getY(), this.getZ(), this.getR(), S);
                        imageXYZ5.imImage = true;
                    }

                    imageXYZ5.setX(this.getX());
                    imageXYZ5.setY(this.getY() + this.S.side_length);
                    imageXYZ5.setZ(this.getZ());

                    if(imageXYZ6 == null) {
                        imageXYZ6 = new Particle(this.getX(), this.getY(), this.getZ(), this.getR(), S);
                        imageXYZ6.imImage = true;
                    }

                    imageXYZ6.setX(this.getX());
                    imageXYZ6.setY(this.getY() + this.S.side_length);
                    imageXYZ6.setZ(this.getZ() - this.S.side_length);

                    if(imageXYZ7 == null) {
                        imageXYZ7 = new Particle(this.getX(), this.getY(), this.getZ(), this.getR(), S);
                        imageXYZ7.imImage = true;
                    }

                    imageXYZ7.setX(this.getX() + this.S.side_length);
                    imageXYZ7.setY(this.getY() + this.S.side_length);
                    imageXYZ7.setZ(this.getZ());
                    updatedXYZ = true;
                }
                else if(this.z - getR() <= 0) {
                    if(imageXYZ1 == null) {
                        imageXYZ1 = new Particle(this.getX(), this.getY(), this.getZ(), this.getR(), S);
                        imageXYZ1.imImage = true;
                    }

                    imageXYZ1.setX(this.getX() + this.S.side_length);
                    imageXYZ1.setY(this.getY() + this.S.side_length);
                    imageXYZ1.setZ(this.getZ() + this.S.side_length);

                    if(imageXYZ2 == null) {
                        imageXYZ2 = new Particle(this.getX(), this.getY(), this.getZ(), this.getR(), S);
                        imageXYZ2.imImage = true;
                    }

                    imageXYZ2.setX(this.getX());
                    imageXYZ2.setY(this.getY());
                    imageXYZ2.setZ(this.getZ() + this.S.side_length);

                    if(imageXYZ3 == null) {
                        imageXYZ3 = new Particle(this.getX(), this.getY(), this.getZ(), this.getR(), S);
                        imageXYZ3.imImage = true;
                    }

                    imageXYZ3.setX(this.getX() + this.S.side_length);
                    imageXYZ3.setY(this.getY());
                    imageXYZ3.setZ(this.getZ());

                    if(imageXYZ4 == null) {
                        imageXYZ4 = new Particle(this.getX(), this.getY(), this.getZ(), this.getR(), S);
                        imageXYZ4.imImage = true;
                    }

                    imageXYZ4.setX(this.getX());
                    imageXYZ4.setY(this.getY() + this.S.side_length);
                    imageXYZ4.setZ(this.getZ());

                    if(imageXYZ5 == null) {
                        imageXYZ5 = new Particle(this.getX(), this.getY(), this.getZ(), this.getR(), S);
                        imageXYZ5.imImage = true;
                    }

                    imageXYZ5.setX(this.getX() + this.S.side_length);
                    imageXYZ5.setY(this.getY() + this.S.side_length);
                    imageXYZ5.setZ(this.getZ());

                    if(imageXYZ6 == null) {
                        imageXYZ6 = new Particle(this.getX(), this.getY(), this.getZ(), this.getR(), S);
                        imageXYZ6.imImage = true;
                    }

                    imageXYZ6.setX(this.getX());
                    imageXYZ6.setY(this.getY() + this.S.side_length);
                    imageXYZ6.setZ(this.getZ() + this.S.side_length);

                    if(imageXYZ7 == null) {
                        imageXYZ7 = new Particle(this.getX(), this.getY(), this.getZ(), this.getR(), S);
                        imageXYZ7.imImage = true;
                    }

                    imageXYZ7.setX(this.getX() + this.S.side_length);
                    imageXYZ7.setY(this.getY());
                    imageXYZ7.setZ(this.getZ() + this.S.side_length);
                    updatedXYZ = true;
                }

                else {
                    if(imageXY1 == null) {
                        imageXY1 = new Particle(this.getX(), this.getY(), this.getZ(), this.getR(), S);
                        imageXY1.imImage = true;
                    }
                    imageXY1.setX(this.getX() + this.S.side_length);
                    imageXY1.setY(this.getY());
                    imageXY1.setZ(this.getZ());

                    if(imageXY2 == null) {
                        imageXY2 = new Particle(this.getX(), this.getY(), this.getZ(), this.getR(), S);
                        imageXY2.imImage = true;
                    }
                    imageXY2.setX(this.getX() + this.S.side_length);
                    imageXY2.setY(this.getY() + this.S.side_length);
                    imageXY2.setZ(this.getZ());

                    if(imageXY3 == null) {
                        imageXY3 = new Particle(this.getX(), this.getY(), this.getZ(), this.getR(), S);
                        imageXY3.imImage = true;
                    }
                    imageXY3.setX(this.getX());
                    imageXY3.setY(this.getY() + this.S.side_length);
                    imageXY3.setZ(this.getZ());
                    updatedXY = true;
                }
            }

            else if(this.z + getR() >= this.S.side_length) {
                if(imageXZ1 == null) {
                    imageXZ1 = new Particle(this.getX(), this.getY(), this.getZ(), this.getR(), S);
                    imageXZ1.imImage = true;
                }
                imageXZ1.setX(this.getX() + this.S.side_length);
                imageXZ1.setY(this.getY());
                imageXZ1.setZ(this.getZ() - this.S.side_length);

                if(imageXZ2 == null) {
                    imageXZ2 = new Particle(this.getX(), this.getY(), this.getZ(), this.getR(), S);
                    imageXZ2.imImage = true;
                }
                imageXZ2.setX(this.getX() + this.S.side_length);
                imageXZ2.setY(this.getY());
                imageXZ2.setZ(this.getZ());

                if(imageXZ3 == null) {
                    imageXZ3 = new Particle(this.getX(), this.getY(), this.getZ(), this.getR(), S);
                    imageXZ3.imImage = true;
                }
                imageXZ3.setX(this.getX());
                imageXZ3.setY(this.getY());
                imageXZ3.setZ(this.getZ() - this.S.side_length);
                updatedXZ = true;
            }

            else if(this.z - getR() <= 0) {
                if(imageXZ1 == null) {
                    imageXZ1 = new Particle(this.getX(), this.getY(), this.getZ(), this.getR(), S);
                    imageXZ1.imImage = true;
                }
                imageXZ1.setX(this.getX() + this.S.side_length);
                imageXZ1.setY(this.getY());
                imageXZ1.setZ(this.getZ() + this.S.side_length);

                if(imageXZ2 == null) {
                    imageXZ2 = new Particle(this.getX(), this.getY(), this.getZ(), this.getR(), S);
                    imageXZ2.imImage = true;
                }
                imageXZ2.setX(this.getX() + this.S.side_length);
                imageXZ2.setY(this.getY());
                imageXZ2.setZ(this.getZ());

                if(imageXZ3 == null) {
                    imageXZ3 = new Particle(this.getX(), this.getY(), this.getZ(), this.getR(), S);
                    imageXZ3.imImage = true;
                }
                imageXZ3.setX(this.getX());
                imageXZ3.setY(this.getY());
                imageXZ3.setZ(this.getZ() + this.S.side_length);
                updatedXZ = true;
            }

            else {
                if(imageX == null) {
                    imageX = new Particle(this.getX(), this.getY(), this.getZ(), this.getR(), S);
                    imageX.imImage = true;
                }
                imageX.setX(this.getX() + this.S.side_length);
                imageX.setY(this.getY());
                imageX.setZ(this.getZ());
                updatedX = true;
            }
        }

        // done
        else if((this.y + this.getR()) >= this.S.side_length && imImage == false) {
            if(this.z + getR() >= this.S.side_length) {
                if(imageYZ1 == null) {
                    imageYZ1 = new Particle(this.getX(), this.getY(), this.getZ(), this.getR(), S);
                    imageYZ1.imImage = true;
                }
                imageYZ1.setX(this.getX());
                imageYZ1.setY(this.getY() - this.S.side_length);
                imageYZ1.setZ(this.getZ() - this.S.side_length);

                if(imageYZ2 == null) {
                    imageYZ2 = new Particle(this.getX(), this.getY(), this.getZ(), this.getR(), S);
                    imageYZ2.imImage = true;
                }
                imageYZ2.setX(this.getX());
                imageYZ2.setY(this.getY());
                imageYZ2.setZ(this.getZ() - this.S.side_length);

                if(imageYZ3 == null) {
                    imageYZ3 = new Particle(this.getX(), this.getY(), this.getZ(), this.getR(), S);
                    imageYZ3.imImage = true;
                }
                imageYZ3.setX(this.getX());
                imageYZ3.setY(this.getY() - this.S.side_length);
                imageYZ3.setZ(this.getZ());
                updatedYZ = true;
            }

            else if(this.z - this.getR() <= 0) {
                if(imageYZ1 == null) {
                    imageYZ1 = new Particle(this.getX(), this.getY(), this.getZ(), this.getR(), S);
                    imageYZ1.imImage = true;
                }
                imageYZ1.setX(this.getX());
                imageYZ1.setY(this.getY() - this.S.side_length);
                imageYZ1.setZ(this.getZ() + this.S.side_length);

                if(imageYZ2 == null) {
                    imageYZ2 = new Particle(this.getX(), this.getY(), this.getZ(), this.getR(), S);
                    imageYZ2.imImage = true;
                }
                imageYZ2.setX(this.getX());
                imageYZ2.setY(this.getY());
                imageYZ2.setZ(this.getZ() + this.S.side_length);

                if(imageYZ3 == null) {
                    imageYZ3 = new Particle(this.getX(), this.getY(), this.getZ(), this.getR(), S);
                    imageYZ3.imImage = true;
                }
                imageYZ3.setX(this.getX());
                imageYZ3.setY(this.getY() - this.S.side_length);
                imageYZ3.setZ(this.getZ());
                updatedYZ = true;
            }

            else {
                if(imageY == null) {
                    imageY = new Particle(this.getX(), this.getY(), this.getZ(), this.getR(), S);
                    imageY.imImage = true;
                }
                imageY.setX(this.getX());
                imageY.setY(this.getY() - this.S.side_length);
                imageY.setZ(this.getZ());
                updatedY = true;
            }
        }

        // done
        else if((this.y - this.getR()) <= 0 && imImage == false) {
            if(this.z + getR() >= this.S.side_length) {
                if(imageYZ1 == null) {
                    imageYZ1 = new Particle(this.getX(), this.getY(), this.getZ(), this.getR(), S);
                    imageYZ1.imImage = true;
                }
                imageYZ1.setX(this.getX());
                imageYZ1.setY(this.getY() + this.S.side_length);
                imageYZ1.setZ(this.getZ() - this.S.side_length);

                if(imageYZ2 == null) {
                    imageYZ2 = new Particle(this.getX(), this.getY(), this.getZ(), this.getR(), S);
                    imageYZ2.imImage = true;
                }
                imageYZ2.setX(this.getX());
                imageYZ2.setY(this.getY() + this.S.side_length);
                imageYZ2.setZ(this.getZ());

                if(imageYZ3 == null) {
                    imageYZ3 = new Particle(this.getX(), this.getY(), this.getZ(), this.getR(), S);
                    imageYZ3.imImage = true;
                }
                imageYZ3.setX(this.getX());
                imageYZ3.setY(this.getY());
                imageYZ3.setZ(this.getZ() - this.S.side_length);
                updatedYZ = true;
            }

            else if(this.z - this.getR() <= 0) {
                if(imageYZ1 == null) {
                    imageYZ1 = new Particle(this.getX(), this.getY(), this.getZ(), this.getR(), S);
                    imageYZ1.imImage = true;
                }
                imageYZ1.setX(this.getX());
                imageYZ1.setY(this.getY() + this.S.side_length);
                imageYZ1.setZ(this.getZ() + this.S.side_length);

                if(imageYZ2 == null) {
                    imageYZ2 = new Particle(this.getX(), this.getY(), this.getZ(), this.getR(), S);
                    imageYZ2.imImage = true;
                }
                imageYZ2.setX(this.getX());
                imageYZ2.setY(this.getY() + this.S.side_length);
                imageYZ2.setZ(this.getZ());

                if(imageYZ3 == null) {
                    imageYZ3 = new Particle(this.getX(), this.getY(), this.getZ(), this.getR(), S);
                    imageYZ3.imImage = true;
                }
                imageYZ3.setX(this.getX());
                imageYZ3.setY(this.getY());
                imageYZ3.setZ(this.getZ() + this.S.side_length);
                updatedYZ = true;
            }

            else {
                if(imageY == null) {
                    imageY = new Particle(this.getX(), this.getY(), this.getZ(), this.getR(), S);
                    imageY.imImage = true;
                }
                imageY.setX(this.getX());
                imageY.setY(this.getY() + this.S.side_length);
                imageY.setZ(this.getZ());
                updatedY = true;
            }
        }

        else if((this.z + this.getR()) >= this.S.side_length && imImage == false) {
            if(imageZ == null)
            {
                imageZ = new Particle(this.getX(), this.getY(), this.getZ(), this.getR(), S);
                imageZ.imImage = true;
            }
            imageZ.setX(this.getX());
            imageZ.setY(this.getY());
            imageZ.setZ(this.getZ() - this.S.side_length);
            updatedZ = true;
        }

        else if((this.z - this.getR()) <= 0 && imImage == false) {
            if(imageZ == null)
            {
                imageZ = new Particle(this.getX(), this.getY(), this.getZ(), this.getR(), S);
                imageZ.imImage = true;
            }
            imageZ.setX(this.getX());
            imageZ.setY(this.getY());
            imageZ.setZ(this.getZ() + this.S.side_length);
            updatedZ = true;
        }

        // we validate if there is a need for an image or not in for x y and z, update if necessary
        // remove if not
        if(updatedXYZ == true) {
            S.vox.remove(this.imageXYZ1);
            S.imageParticles.remove(this.imageXYZ1);
            this.imageXYZ1.voxel = this.imageXYZ1.getVoxel();
            this.imageXYZ1.in_voxels = this.imageXYZ1.getVoxels();
            this.imageXYZ1.nearby = this.imageXYZ1.getNearby();
            S.vox.add(this.imageXYZ1);
            S.imageParticles.add(this.imageXYZ1);

            S.vox.remove(this.imageXYZ2);
            S.imageParticles.remove(this.imageXYZ2);
            this.imageXYZ2.voxel = this.imageXYZ2.getVoxel();
            this.imageXYZ2.in_voxels = this.imageXYZ2.getVoxels();
            this.imageXYZ2.nearby = this.imageXYZ2.getNearby();
            S.vox.add(this.imageXYZ2);
            S.imageParticles.add(this.imageXYZ2);

            S.vox.remove(this.imageXYZ3);
            S.imageParticles.remove(this.imageXYZ3);
            this.imageXYZ3.voxel = this.imageXYZ3.getVoxel();
            this.imageXYZ3.in_voxels = this.imageXYZ3.getVoxels();
            this.imageXYZ3.nearby = this.imageXYZ3.getNearby();
            S.vox.add(this.imageXYZ3);
            S.imageParticles.add(this.imageXYZ3);

            S.vox.remove(this.imageXYZ4);
            S.imageParticles.remove(this.imageXYZ4);
            this.imageXYZ4.voxel = this.imageXYZ4.getVoxel();
            this.imageXYZ4.in_voxels = this.imageXYZ4.getVoxels();
            this.imageXYZ4.nearby = this.imageXYZ4.getNearby();
            S.vox.add(this.imageXYZ4);
            S.imageParticles.add(this.imageXYZ4);

            S.vox.remove(this.imageXYZ5);
            S.imageParticles.remove(this.imageXYZ5);
            this.imageXYZ5.voxel = this.imageXYZ5.getVoxel();
            this.imageXYZ5.in_voxels = this.imageXYZ5.getVoxels();
            this.imageXYZ5.nearby = this.imageXYZ5.getNearby();
            S.vox.add(this.imageXYZ5);
            S.imageParticles.add(this.imageXYZ5);

            S.vox.remove(this.imageXYZ6);
            S.imageParticles.remove(this.imageXYZ6);
            this.imageXYZ6.voxel = this.imageXYZ6.getVoxel();
            this.imageXYZ6.in_voxels = this.imageXYZ6.getVoxels();
            this.imageXYZ6.nearby = this.imageXYZ6.getNearby();
            S.vox.add(this.imageXYZ6);
            S.imageParticles.add(this.imageXYZ6);

            S.vox.remove(this.imageXYZ7);
            S.imageParticles.remove(this.imageXYZ7);
            this.imageXYZ7.voxel = this.imageXYZ7.getVoxel();
            this.imageXYZ7.in_voxels = this.imageXYZ7.getVoxels();
            this.imageXYZ7.nearby = this.imageXYZ7.getNearby();
            S.vox.add(this.imageXYZ7);
            S.imageParticles.add(this.imageXYZ7);
        }

        else {
            if(this.imageXYZ1 != null) {
                S.vox.remove(this.imageXYZ1);
                S.imageParticles.remove(this.imageXYZ1);
                this.imageXYZ1 = null;
            }

            if(this.imageXYZ2 != null) {
                S.vox.remove(this.imageXYZ2);
                S.imageParticles.remove(this.imageXYZ2);
                this.imageXYZ2 = null;
            }

            if(imageXYZ3 != null) {
                S.vox.remove(this.imageXYZ3);
                S.imageParticles.remove(this.imageXYZ3);
                this.imageXYZ3 = null;
            }

            if(this.imageXYZ4 != null) {
                S.vox.remove(this.imageXYZ4);
                S.imageParticles.remove(this.imageXYZ4);
                this.imageXYZ4 = null;
            }

            if(this.imageXYZ5 != null) {
                S.vox.remove(this.imageXYZ5);
                S.imageParticles.remove(this.imageXYZ5);
                this.imageXYZ5 = null;
            }

            if(this.imageXYZ6 != null) {
                S.vox.remove(this.imageXYZ6);
                S.imageParticles.remove(this.imageXYZ6);
                this.imageXYZ6 = null;
            }

            if(this.imageXYZ7 != null) {
                S.vox.remove(this.imageXYZ7);
                S.imageParticles.remove(this.imageXYZ7);
                this.imageXYZ7 = null;
            }
        }

        if(updatedXY == true) {
            S.vox.remove(this.imageXY1);
            S.imageParticles.remove(this.imageXY1);
            this.imageXY1.voxel = this.imageXY1.getVoxel();
            this.imageXY1.in_voxels = this.imageXY1.getVoxels();
            this.imageXY1.nearby = this.imageXY1.getNearby();
            S.vox.add(this.imageXY1);
            S.imageParticles.add(this.imageXY1);

            S.vox.remove(this.imageXY2);
            S.imageParticles.remove(this.imageXY2);
            this.imageXY2.voxel = this.imageXY2.getVoxel();
            this.imageXY2.in_voxels = this.imageXY2.getVoxels();
            this.imageXY2.nearby = this.imageXY2.getNearby();
            S.vox.add(this.imageXY2);
            S.imageParticles.add(this.imageXY2);

            S.vox.remove(this.imageXY3);
            S.imageParticles.remove(this.imageXY3);
            this.imageXY3.voxel = this.imageXY3.getVoxel();
            this.imageXY3.in_voxels = this.imageXY3.getVoxels();
            this.imageXY3.nearby = this.imageXY3.getNearby();
            S.vox.add(this.imageXY3);
            S.imageParticles.add(this.imageXY3);
        }

        else {
            if(this.imageXY1 != null) {
                S.vox.remove(this.imageXY1);
                S.imageParticles.remove(this.imageXY1);
                this.imageXY1 = null;
            }

            if(this.imageXY2 != null) {
                S.vox.remove(this.imageXY2);
                S.imageParticles.remove(this.imageXY2);
                this.imageXY2 = null;
            }

            if(this.imageXY3 != null) {
                S.vox.remove(this.imageXY3);
                S.imageParticles.remove(this.imageXY3);
                this.imageXY3 = null;
            }
        }

        if(updatedXZ == true) {
            S.vox.remove(this.imageXZ1);
            S.imageParticles.remove(this.imageXZ1);
            this.imageXZ1.voxel = this.imageXZ1.getVoxel();
            this.imageXZ1.in_voxels = this.imageXZ1.getVoxels();
            this.imageXZ1.nearby = this.imageXZ1.getNearby();
            S.vox.add(this.imageXZ1);
            S.imageParticles.add(this.imageXZ1);

            S.vox.remove(this.imageXZ2);
            S.imageParticles.remove(this.imageXZ2);
            this.imageXZ2.voxel = this.imageXZ2.getVoxel();
            this.imageXZ2.in_voxels = this.imageXZ2.getVoxels();
            this.imageXZ2.nearby = this.imageXZ2.getNearby();
            S.vox.add(this.imageXZ2);
            S.imageParticles.add(this.imageXZ2);

            S.vox.remove(this.imageXZ3);
            S.imageParticles.remove(this.imageXZ3);
            this.imageXZ3.voxel = this.imageXZ3.getVoxel();
            this.imageXZ3.in_voxels = this.imageXZ3.getVoxels();
            this.imageXZ3.nearby = this.imageXZ3.getNearby();
            S.vox.add(this.imageXZ3);
            S.imageParticles.add(this.imageXZ3);
        }

        else {
            if(this.imageXZ1 != null) {
                S.vox.remove(this.imageXZ1);
                S.imageParticles.remove(this.imageXZ1);
                this.imageXZ1 = null;
            }

            if(this.imageXZ2 != null) {
                S.vox.remove(this.imageXZ2);
                S.imageParticles.remove(this.imageXZ2);
                this.imageXZ2 = null;
            }

            if(this.imageXZ3 != null) {
                S.vox.remove(this.imageXZ3);
                S.imageParticles.remove(this.imageXZ3);
                this.imageXZ3 = null;
            }
        }

        if(updatedYZ == true) {
            S.vox.remove(this.imageYZ1);
            S.imageParticles.remove(this.imageYZ1);
            this.imageYZ1.voxel = this.imageYZ1.getVoxel();
            this.imageYZ1.in_voxels = this.imageYZ1.getVoxels();
            this.imageYZ1.nearby = this.imageYZ1.getNearby();
            S.vox.add(this.imageYZ1);
            S.imageParticles.add(this.imageYZ1);

            S.vox.remove(this.imageYZ2);
            S.imageParticles.remove(this.imageYZ2);
            this.imageYZ2.voxel = this.imageYZ2.getVoxel();
            this.imageYZ2.in_voxels = this.imageYZ2.getVoxels();
            this.imageYZ2.nearby = this.imageYZ2.getNearby();
            S.vox.add(this.imageYZ2);
            S.imageParticles.add(this.imageYZ2);

            S.vox.remove(this.imageYZ3);
            S.imageParticles.remove(this.imageYZ3);
            this.imageYZ3.voxel = this.imageYZ3.getVoxel();
            this.imageYZ3.in_voxels = this.imageYZ3.getVoxels();
            this.imageYZ3.nearby = this.imageYZ3.getNearby();
            S.vox.add(this.imageYZ3);
            S.imageParticles.add(this.imageYZ3);
        }

        else {
            if(this.imageYZ1 != null) {
                S.vox.remove(this.imageYZ1);
                S.imageParticles.remove(this.imageYZ1);
                this.imageYZ1 = null;
            }

            if(this.imageYZ2 != null) {
                S.vox.remove(this.imageYZ2);
                S.imageParticles.remove(this.imageYZ2);
                this.imageYZ2 = null;
            }

            if(this.imageYZ3 != null) {
                S.vox.remove(this.imageYZ3);
                S.imageParticles.remove(this.imageYZ3);
                this.imageYZ3 = null;
            }
        }

        if(updatedX == true) {
            S.vox.remove(this.imageX);
            S.imageParticles.remove(this.imageX);
            this.imageX.voxel = this.imageX.getVoxel();
            this.imageX.in_voxels = this.imageX.getVoxels();
            this.imageX.nearby = this.imageX.getNearby();
            S.vox.add(this.imageX);
            S.imageParticles.add(this.imageX);
        }

        else {
            if(this.imageX != null) {
                S.vox.remove(this.imageX);
                S.imageParticles.remove(this.imageX);
                this.imageX = null;
            }
        }

        if(updatedY == true) {
            S.vox.remove(this.imageY);
            S.imageParticles.remove(this.imageY);
            this.imageY.voxel = this.imageY.getVoxel();
            this.imageY.in_voxels = this.imageY.getVoxels();
            this.imageY.nearby = this.imageY.getNearby();
            S.vox.add(this.imageY);
            S.imageParticles.add(this.imageY);

        }

        else {
            if(this.imageY != null) {
                S.vox.remove(this.imageY);
                S.imageParticles.remove(this.imageY);
                this.imageY = null;
            }
        }

        if(updatedZ == true) {
            S.vox.remove(this.imageZ);
            S.imageParticles.remove(this.imageZ);
            this.imageZ.voxel = this.imageZ.getVoxel();
            this.imageZ.in_voxels = this.imageZ.getVoxels();
            this.imageZ.nearby = this.imageZ.getNearby();
            S.vox.add(this.imageZ);
            S.imageParticles.add(this.imageZ);
        }

        else {
            if(this.imageZ != null) {
                S.vox.remove(this.imageZ);
                S.imageParticles.remove(this.imageZ);
                this.imageZ = null;
            }
        }
    }

    public Particle getImageX() {
        return imageX;
    }

    public Particle getImageY() {
        return imageY;
    }

    public Particle getImageZ() {
        return imageZ;
    }

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
        double vox_length = (S.side_length / S.vox.voxels_per_side);
        int i = (int) (x / vox_length);
        i = i >= 0 ? i < S.vox.voxels_per_side ? i : i - S.vox.voxels_per_side : i + S.vox.voxels_per_side;
        int j = (int) (y / vox_length);
        j = j >= 0 ? j < S.vox.voxels_per_side ? j : j - S.vox.voxels_per_side : j + S.vox.voxels_per_side;
        int k = (int) (z / vox_length);
        k = k >= 0 ? k < S.vox.voxels_per_side ? k : k - S.vox.voxels_per_side : k + S.vox.voxels_per_side;
        return S.vox.voxels[i][j][k];
    }

    Voxel getVoxel(double x, double y, double z) {
        double vox_length = (S.side_length / S.vox.voxels_per_side);
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
        /*for(int i = 0; i < B.getNumTumor(); i++) {
            double radius_sum = B.getTumoroids().get(i).getR() + R;
            double dx = Math.abs(B.getTumoroids().get(i).getX() - x);
            double dy = Math.abs(B.getTumoroids().get(i).getY() - y);
            double dz = Math.abs(B.getTumoroids().get(i).getZ() - z);

            if (dx < radius_sum && dy < radius_sum && dz < radius_sum) {        // is it even close?
                if (dx * dx + dy * dy + dz * dz < radius_sum * radius_sum) {    // then compute radial distance and check *how* close
                    return false;
                }
            }
        }*/


        for (Particle other : nearby) {
            if (other != null && other.imImage == false) {
                double radiusSum = R + other.R;
                double dx, dy, dz;
                dx = x - other.x;
                dy = y - other.y;
                dz = z - other.z;

                //x bound
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
                }
                else if (voxel.y == 0 && other.voxel.y == S.vox.voxels_per_side - 1) {
                    dy += S.side_length;
                }
                //z bound
                if (voxel.z == S.vox.voxels_per_side - 1 && other.voxel.z == 0) {
                    dz -= S.side_length;
                }
                else if (voxel.z == 0 && other.voxel.z == S.vox.voxels_per_side - 1) {
                    dz += S.side_length;
                }

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

    public void updateCollision() {
        /*for(int i = 0; i < B.getNumTumor(); i++) {
            double radius_sum = B.getTumoroids().get(i).getR() + R;
            double dx = Math.abs(B.getTumoroids().get(i).getX() - x);
            double dy = Math.abs(B.getTumoroids().get(i).getY() - y);
            double dz = Math.abs(B.getTumoroids().get(i).getZ() - z);

            if (dx < radius_sum && dy < radius_sum && dz < radius_sum) {        // is it even close?
                if (dx * dx + dy * dy + dz * dz < radius_sum * radius_sum) {    // then compute radial distance and check *how* close
                    Vector diff = new Vector(dx, dy, dz);
                    double d = diff.magnitude() - radius_sum;
                    if (d < 0) { //overlap
                        v = v.add(diff.unitVector().scale(-d * B.getTumoroids().get(i).getR() / radius_sum));

                    }
                }
            }
        }*/

        // Reset overlappedCounter before every loop
        overlappedCounter = 0;
        overlaps.clear();


        for (Particle other : nearby) {
            try {
                if (other != null && other.imImage == false && other.type.equals("Gel")) {
                    double radiusSum = R + other.R;
                    double dx, dy, dz;
                    dx = x + v.x() - other.x;
                    dy = y + v.y() - other.y;
                    dz = z + v.z() - other.z;

                    ///x bound
                    //if other particle big x you small x bring their x here
                    if (voxel.x == S.vox.voxels_per_side - 1 && other.voxel.x == 0) {
                        dx -= S.side_length;
                        numPBCJumpsX++;
                    }
                    //if you big x other particle small x move x there to check
                    else if (voxel.x == 0 && other.voxel.x == S.vox.voxels_per_side - 1) {
                        dx += S.side_length;
                        numPBCJumpsX--;
                    }
                    //y bound
                    if (voxel.y == S.vox.voxels_per_side - 1 && other.voxel.y == 0) {
                        dy -= S.side_length;
                        numPBCJumpsY++;
                    } else if (voxel.y == 0 && other.voxel.y == S.vox.voxels_per_side - 1) {
                        dy += S.side_length;
                        numPBCJumpsY--;
                    }
                    //z bound
                    if (voxel.z == S.vox.voxels_per_side - 1 && other.voxel.z == 0) {
                        dz -= S.side_length;
                        numPBCJumpsZ++;
                    } else if (voxel.z == 0 && other.voxel.z == S.vox.voxels_per_side - 1) {
                        dz += S.side_length;
                        numPBCJumpsZ--;
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


        /*// Change with collision
        if(checkCollision() == true && imImage == false) {
            //Adjust position based on active forces and velocity
            double[] updatedForceArray = springModel(nearby, this);

            double forceXSquare = Math.pow(updatedForceArray[0], 2);
            double forceYSquare = Math.pow(updatedForceArray[1], 2);
            double forceZSquare = Math.pow(updatedForceArray[2], 2);

            double one_sqrtF = 1.0 / (Math.sqrt(forceXSquare + forceYSquare + forceZSquare));

            this.x += (updatedForceArray[0] * one_sqrtF * 0.7 * dt);
            this.y += (updatedForceArray[1] * one_sqrtF * 0.7 * dt);
            this.z += (updatedForceArray[2] * one_sqrtF * 0.7 * dt);

            Vector forceVector = new Vector((updatedForceArray[0] * one_sqrtF * 0.7),
                    (updatedForceArray[1] * one_sqrtF * 0.7),
                    (updatedForceArray[2] * one_sqrtF * 0.7));

            v = forceVector;
        }

        else if(checkCollision() == false) {
            Vector forceVector = new Vector(0.0, 0.0, 0.0);
            v = forceVector;
        }*/


        if(imImage != true) {
            move();
        }

    }

    static double[] springModel(Particle[] nearby, Particle particle) {
        // Force component array
        double[] forceArray = {0.0, 0.0, 0.0};

        for(Particle other : nearby) {

            if(Double.isNaN(other.getX()) || Double.isNaN(other.getY()) || Double.isNaN(other.getZ())) {
                continue;
            }

            if(other != null && other.imImage == false) {
                // Vector A calculation from Greg's notes
                double deltaXA = particle.getX() - other.getX();
                double deltaYA = particle.getY() - other.getY();
                double deltaZA = particle.getZ() - other.getZ();
                System.out.println(other.getX());


                double deltaXASquared = Math.pow(deltaXA, 2);
                double deltaYASquared = Math.pow(deltaYA, 2);
                double deltaZASquared = Math.pow(deltaZA, 2);

                //System.out.println(deltaXA);

            /*
            double vectorA = (deltaXA * other.getTranslateX())
                    + (deltaYA * other.getY())
                    + (deltaZA * other.getZ());


            // Magnitude and direction of resultant
            double deltaRA = Math.sqrt(particle.getRSquared()
                    + other.getRSquared()
                    - deltaXASquared
                    - deltaYASquared
                    - deltaZASquared);

             */
                double rCheckSquared = deltaXASquared + deltaYASquared + deltaZASquared;

                //System.out.println(rCheckSquared);

                // check if worth calculating
                if(rCheckSquared < (particle.getRSquared() + other.getRSquared())) {
                    // Force components
                    // Add to force component array
                    forceArray[0] += (((particle.getRSquared() + other.getRSquared()) / (rCheckSquared)) - 1.0) * deltaXA;

                    forceArray[1] += (((particle.getRSquared() + other.getRSquared()) / (rCheckSquared)) - 1.0) * deltaYA;

                    forceArray[2] += (((particle.getRSquared() + other.getRSquared()) / (rCheckSquared)) - 1.0) * deltaZA;

                }

                else {
                    continue;
                }

            }

        }

        return forceArray;

    }
    
    double mod(double a, double b) {
        return ((a % b) + b) % b;
    }

    protected void move() {
        if(imImage == false) {
            setXYZ(mod(x + v.x(), S.side_length), mod(y + v.y(), S.side_length), mod(z + v.z(), S.side_length));

            xPrime += v.x();
            yPrime += v.y();
            zPrime += v.z();

            /*dx += v.x();
            dy += v.y();
            dz += v.z();*/
        }
    }
}
