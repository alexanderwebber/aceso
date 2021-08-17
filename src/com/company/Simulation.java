package com.company;

import org.apache.commons.math3.distribution.ExponentialDistribution;
import org.apache.commons.math3.distribution.LogNormalDistribution;
import java.io.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Random;


class Box {
    static Random rand = new Random();
    double mu = 3.54192;
    double sigma = 0.690104;
    LogNormalDistribution logNormal = new LogNormalDistribution(sigma, mu);
    //ExponentialDistribution expDist = new ExponentialDistribution(mu);
    double sim_time = 0.0;
    double sideLength;
    double sum_sphere_volume = 0;                //Current volume
    double volume;       //volume of the cube
    double volume_ratio;              //Desired volume ratio
    BoxVoxels vox;       //Voxel memory for the particles
    ArrayList<Tumoroid> tumoroids = new ArrayList<>();

    Box() {

    }

    public void setSide(double side_length) {
        this.sideLength = side_length;
        volume = side_length * side_length * side_length;
    }

    public ArrayList<Tumoroid> getTumoroids() {
        return tumoroids;
    }

    public int getNumTumor() {
        return tumoroids.size();
    }



}

public class Simulation extends Box {
    //Memory
    Vector[] movements = new Vector[1000000];
    ArrayList<Gel> gels = new ArrayList<>();
    int numGels = 0;
    TCell[] tCells = new TCell[1000000];
    ArrayList<Particle> imageParticles = new ArrayList<>();
    ArrayList<Double> densityValues = new ArrayList<>();
    int numParticles = 0;
    double numTCells = 100;
    double averageDisplacementPanel;

    boolean tumor = true;
    Gel tumorGel;
    //double numGelsToSet = 1000;
    //Threads of control
    Thread settleThread = new Thread();
    Thread fillThread = new Thread();
    Thread fillLattice = new Thread();
    Thread fallThread = new Thread();
    Thread tCellThread = new Thread();
    Thread fillHexThread = new Thread();
    Thread fillFCCThread = new Thread();

    // Residence data stuff
    int timeLimitTCells = 1000000;
    static ArrayList<int[]> startValues = new ArrayList<>();

    ArrayList<Double> sliceDensityYZ = new ArrayList<>();
    ArrayList<Double> sliceDensityXY = new ArrayList<>();
    ArrayList<Double> sliceDensityXZ = new ArrayList<>();
    SliceDensityCalculator sdc = new SliceDensityCalculator(this);

    boolean gui = true;

    double t = 0;
    int fallTimeIterator = 0;
    double fall_time_limit = 1000;
    double dt = 1;
    boolean simulating = false;
    private Path savePath;

    // To be changed by panel settings
    double rAverageRadius = 50.0;
    double rangeOverAverageR = 0.0;

    //Constructors
    Simulation() {
        super();
        sideLength = 1000;
        volume = sideLength * sideLength * sideLength;
        volume_ratio = .70;
        //initFromCSVTumor("tumor.csv"); // BOX

    }

    void setAverageDisplacement(double averageDisplacement) {
    	this.averageDisplacementPanel = averageDisplacement;
    }

    void setVolume(double volume) {
    	this.volume = volume;
    }

    void settle() {
        settleThread = new Thread(() -> {
            for (int i = 0; i < 50; ++i) {
                int size = numGels;
                for (int j = 0; j < size; ++j) {
                    gels.get(j).settle();
                }
            }
        });

        settleThread.start();
    }

    void fill() {
        fillThread = new Thread(() -> {
            double startTime = System.nanoTime();
            // TODO: Setup tumor

            setSide(sideLength);

            volume_ratio = 0.64;

            if (settleThread.isAlive()) {
                settleThread.interrupt();
            }

            // Scale down average radius and std dev
            double sideLengthByNumGels = Math.cbrt((1000.0 * ((4.0 / 3.0) * (rAverageRadius * rAverageRadius * rAverageRadius) * Math.PI)) / volume_ratio);
            System.out.println(sideLengthByNumGels);
            setSideLength(sideLengthByNumGels);
            setVolume(sideLength * sideLength * sideLength);

            //int numGelsToSet = (int) ((0.67) * (Math.floor((side_length * side_length * side_length) / ((4 / 3) * (rAverageRadius * rAverageRadius * rAverageRadius) * Math.PI))));

            int numGelsToSet = 1000;
            double[] timeNull = new double[numGelsToSet];
            double[] timeFinal = new double[numGelsToSet];

            vox = new BoxVoxels(this);

            rAverageRadius = rAverageRadius * 0.1;

            for (int i = 0; i < numGelsToSet; i++) {
                addGel();
            }

            for(int i = 0; i < numGels; i++) {
                timeNull[i] = gels.get(i).getR();
            }

            while (sumSphereVolumes() / volume < volume_ratio) {
                scaleSpheres(1.01);
            }

            System.out.println("Average radius: " + calculateAvgRadius());
            System.out.println("Weighted average radius: " + calculateWeightedAvgRadius());

            for(int i = 0; i < numGels; i++) {
                timeFinal[i] = gels.get(i).getR();
            }

            settle();

            try {
                FileWriter radiiDistribution = new FileWriter("radii_distribution_LLSrad_" + outputMeanRadius() + "_LLSdispersion_" + outputRangeOverAverageR() + ".csv");

                radiiDistribution.append(String.format("%s,%s\n", "beginning","end"));

                for(int i = 0; i < numGels; i++) {
                    radiiDistribution.append(String.format("%.5f, %.5f\n", timeNull[i], timeFinal[i]));
                }

                radiiDistribution.close();
                System.out.println("finished time output");

            } catch (IOException e) {
                e.printStackTrace();
            }

            double endTime = System.nanoTime();

            double timeDiff = endTime - startTime;

            System.out.println("Time to fill: " + timeDiff / 1e9);
        });

        fillThread.start();
    }

    void fillFCC() {

    	// Corresponds to number required for ~74% packing fraction in 1 mL volume
    	int numCells = (int)Math.pow((1372/4), (1.0/3.0));

    	double sideLengthByNumGels = numCells * (4 / Math.sqrt(2)) * rAverageRadius;
        setSideLength(sideLengthByNumGels);
        setVolume(sideLength * sideLength * sideLength);

        vox = new BoxVoxels(this);

        // Position of molecule in
        Double[][] fccArray = {{0.0, 0.0, 0.0}, {0.0, 0.5, 0.5},{0.5, 0.5, 0.0}, {0.5, 0.0, 0.5}};

        // Loop through each position in minilattice, minilattice length is number of minilattices divided by box length
        for(int i = 0; i < numCells; i++) {
            for(int j = 0; j < numCells; j++) {
                for(int k = 0; k < numCells; k++) {
                    for(int a = 0; a < 4; a++) {
                        // Initial position before manipulation, generate four per mini lattice.
                        Double[] atomPosition = {0.0, 0.0, 0.0};
                        atomPosition[0] += (fccArray[a][0] + i) * (sideLengthByNumGels / numCells);
                        atomPosition[1] += (fccArray[a][1] + j) * (sideLengthByNumGels / numCells);
                        atomPosition[2] += (fccArray[a][2] + k) * (sideLengthByNumGels / numCells);

                        addGel(atomPosition[0], atomPosition[1], atomPosition[2], this.rAverageRadius, "Gel");

//                            try {
//
//								TimeUnit.MILLISECONDS.sleep(100);
//							} catch (InterruptedException e) {
//								// TODO Auto-generated catch block
//								e.printStackTrace();
//							}





                    }
                }
            }
        }
            //settle();
        try (FileWriter coordinationWriter = new FileWriter("gel_coordination_number_fcc.csv")) {
            coordinationWriter.append(String.format("%s\n", "Coordination Number"));

            for(int i = 0; i < gels.size(); i++) {
                coordinationWriter.append(String.format("%d\n", gels.get(i).returnCoordination()));
            }

            coordinationWriter.flush();

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    void fillHex(){
	    fillLattice = new Thread(() -> {
	        //setSide(1000);

	    	volume_ratio = 0.74048;

            double sideLengthByNumGels = 10 * (2 * rAverageRadius);
            setSideLength(sideLengthByNumGels);
            setVolume(sideLength * sideLength * sideLength);

            vox = new BoxVoxels(this);

	        // Position of molecule in

	        for(int k = 0; k < 10; k++) {
	        	for(int j = 0; j < 10; j++) {
	        		for(int i = 0; i < 10; i++) {
	        			Double[] atomPosition = {0.0, 0.0, 0.0};

	                	atomPosition[0] = ((2 * i) + ((j + k) % 2)) * rAverageRadius;

	                	atomPosition[1] = (Math.sqrt(3) * (j + (1/3) * (k % 2))) * rAverageRadius;

	                    atomPosition[2] = ((2 * Math.sqrt(6)) / 3) * k * rAverageRadius;

	                    addGel(atomPosition[0], atomPosition[1], atomPosition[2], this.rAverageRadius, "Gel");
	        		}

	        	}

	        }
	        //settle();
	        System.out.println(sideLength);
	    });
	    fillLattice.start();
	}


    void fall() {
        fallThread = new Thread(() -> {

            long startTime = System.nanoTime();

            while (fallTimeIterator < fall_time_limit) {
                for (int j = 0; j < numGels; j++) {
                    gels.get(j).fall();
                }

                fallTimeIterator++;
            }

            double endTime = System.nanoTime();

            double timeDiff = endTime - startTime;

//            System.out.println("Time to fall: " + timeDiff / 1e9);
//
//            sliceDensityXY = sdc.calculateAreaFractionDensityXY();
//
//            sliceDensityXZ = sdc.calculateAreaFractionDensityXZ();
//            sliceDensityYZ = sdc.calculateAreaFractionDensityYZ();
//
//            try (FileWriter densityWriter = new FileWriter("density_xyz.csv")) {
//                densityWriter.append(String.format("%s,%s,%s\n", "XY", "XZ", "YZ"));
//
//                for(int i = 0; i < sliceDensityXY.size(); i++) {
//                    densityWriter.append(String.format("%f,%f,%f\n", sliceDensityXY.get(i), sliceDensityXZ.get(i), sliceDensityYZ.get(i)));
//                }
//
//                densityWriter.flush();
//                densityWriter.close();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }


            try (FileWriter coordinationWriter = new FileWriter("gel_coordination_number.csv")) {
                coordinationWriter.append(String.format("%s\n", "Coordination Number"));

                for(int i = 0; i < gels.size(); i++) {
                    coordinationWriter.append(String.format("%d\n", gels.get(i).returnCoordination()));
                }

                coordinationWriter.flush();

            } catch (IOException e) {
                e.printStackTrace();
            }

            try (FileWriter radialDistributionWriter = new FileWriter("radial_distribution_number.csv")) {
                radialDistributionWriter.append(String.format("%s\n", "Coordination Number"));

                for(int i = 0; i < gels.size(); i++) {
                    radialDistributionWriter.append(String.format("%d\n", gels.get(i).returnCoordination()));
                }

                radialDistributionWriter.flush();

            } catch (IOException e) {
                e.printStackTrace();
            }

        });

        fallThread.start();
    }

    void fillUnthreaded() {

        double startTime = System.nanoTime();
        // TODO: Setup tumor

        setSide(sideLength);

        volume_ratio = 0.64;

        if (settleThread.isAlive()) {
            settleThread.interrupt();
        }

        // Scale down average radius and std dev
        double sideLengthByNumGels = Math.cbrt((1000.0 * ((4.0 / 3.0) * (rAverageRadius * rAverageRadius * rAverageRadius) * Math.PI)) / volume_ratio);
        System.out.println(sideLengthByNumGels);
        setSideLength(sideLengthByNumGels);
        setVolume(sideLength * sideLength * sideLength);

        //int numGelsToSet = (int) ((0.67) * (Math.floor((side_length * side_length * side_length) / ((4 / 3) * (rAverageRadius * rAverageRadius * rAverageRadius) * Math.PI))));

        int numGelsToSet = 1000;
        double[] timeNull = new double[numGelsToSet];
        double[] timeFinal = new double[numGelsToSet];

        vox = new BoxVoxels(this);

        rAverageRadius = rAverageRadius * 0.1;

        // Add tumor replacement gel
        if(tumor) {
            tumorGel = new Gel(sideLength / 2, sideLength / 2, sideLength / 2, 10, this, "TumorGel");
            addGel(tumorGel);
        }

        for (int i = 0; i < numGelsToSet; i++) {
            addGel();
        }

        for(int i = 0; i < numGels; i++) {
            timeNull[i] = gels.get(i).getR();
        }

        while (sumSphereVolumes() / volume < volume_ratio) {
            scaleSpheres(1.01);
        }

        for(int i = 0; i < numGels; i++) {
            timeFinal[i] = gels.get(i).getR();
        }

        settleUnthreaded();

//            try {
//                FileWriter radiiDistribution = new FileWriter("radii_distribution_LLSrad_" + outputMeanRadius() + "_LLSdispersion_" + outputRangeOverAverageR() + ".csv");
//
//                radiiDistribution.append(String.format("%s,%s\n", "beginning","end"));
//
//                for(int i = 0; i < numGels; i++) {
//                    radiiDistribution.append(String.format("%.5f, %.5f\n", timeNull[i], timeFinal[i]));
//                }
//
//                radiiDistribution.close();
//                System.out.println("finished time output");
//
//            } catch (IOException e) {
//                e.printStackTrace();
//            }

        System.out.println(sideLength);
        System.out.println(volume);

        double endTime = System.nanoTime();

        double timeDiff = endTime - startTime;

        System.out.println("Time to fill: " + timeDiff / 1e9);


    }

    void settleUnthreaded() {
        for (int i = 0; i < 50; ++i) {
            int size = numGels;
            for (int j = 0; j < size; ++j) {
                gels.get(j).settle();
            }
        }

    }


    // TODO: Fix null pointer issue. I think it has to do with the Voxels
    void fallUnthreaded() {

        long startTime = System.nanoTime();

        while (fallTimeIterator < fall_time_limit) {
            for (int j = 0; j < numGels; j++) {
                gels.get(j).fall();
            }

            fallTimeIterator++;
        }


        double endTime = System.nanoTime();

        double timeDiff = endTime - startTime;

        System.out.println("Time to fall: " + timeDiff / 1e9);

    }

    private void checkTumors() {
        for (int i = 0; i < this.getNumTumor(); ++i) {
            this.getTumoroids().get(i).checkSetStatus();
        }
    }

    void tumorGrow() {
        for(int i = 0; i < this.getNumTumor(); i++) {
            if((!this.getTumoroids().get(i).getStatus().equals("dead") || !this.getTumoroids().get(i).getStatus().equals("being_attacked")) || this.getTumoroids().get(i).getNumNeighbors() > 4){
                if(this.getTumoroids().get(i).getR() < 12.0) {
                	//TODO: Make growth rate variable
                    this.getTumoroids().get(i).setR(this.getTumoroids().get(i).getR() + 0.00015);
                    this.getTumoroids().get(i).move();
                }
                else {
                    this.getTumoroids().get(i).setR(6.0);
                    double x = this.getTumoroids().get(i).getX() + 6.0;
                    double y = this.getTumoroids().get(i).getY();
                    double z = this.getTumoroids().get(i).getZ();
                    this.addTumor(x, y, z, 6.0, this.getNumTumor());
                    this.getTumoroids().get(i).move();
                }
            }
        }
    }

    void tCellProliferate() {
        for(int i = 0; i < this.numTCells; i++) {
            if(this.tCells[i].getLifeTime() < 3) {
                    continue;
            }

            else {
                this.tCells[i].setLifeTime(0);
                double x = this.tCells[i].getX();
                double y = this.tCells[i].getY();
                double z = this.tCells[i].getZ();
                double R = 8.0;
                this.addTCell(x, y, z, R);
            }
        }
    }

    // Calculate the max radius of tumor for use in generating tumor gel.
    double calculateTumorGelRadius() {
        BufferedReader builder2;

        double radius = 0.0;
        double minX = 0.0;
        double minY = 0.0;
        double minZ = 0.0;
        double maxX = 0.0;
        double maxY = 0.0;
        double maxZ = 0.0;

        try {
            builder2 = new BufferedReader(new FileReader("tumor.csv"));

            String thisline;
            Random r = new Random();

            int numTumor = 559;

            // Add tumor replacement gel
            if(tumor) {
                /*
                 * Gel tumorGel = new Gel(side_length / 2, side_length / 2, side_length / 2,
                 * 2.0, this, "TumorGel"); addGel(tumorGel);
                 */

                for (int i = 0; i < numTumor; i++) {
                    double randomRadius = 6.0 + (11.9 - 6.0) * r.nextDouble();
                    thisline = builder2.readLine();
                    int comma = thisline.indexOf(',');
                    double x = Double.parseDouble(thisline.substring(0, comma)) - 50;
                    int comma2 = comma + 1 + thisline.substring(comma + 1).indexOf(',');
                    double y = Double.parseDouble(thisline.substring(comma + 1, comma2)) - 50;
                    comma = comma2 + 1 + thisline.substring(comma2 + 1).indexOf(',');
                    double z = Double.parseDouble(thisline.substring(comma2 + 1, comma)) - 50;
                    double R = randomRadius;

                    if(i == 0) {
                        minX = x;
                        maxX = x;
                        minY = y;
                        maxY = y;
                        minZ = z;
                        maxZ = z;
                    }
                    else {
                        if(x < minX) {
                            minX = x - randomRadius;
                        }
                        else if(x > maxX) {
                            maxX = x + randomRadius;
                        }
                        if(y < minY) {
                            minY = y - randomRadius;
                        }
                        else if(y > maxY) {
                            maxY = y + randomRadius;
                        }
                        if(z < minZ) {
                            minZ = z - randomRadius;
                        }
                        else if(z > maxZ) {
                            maxZ = z + randomRadius;
                        }
                    }
                }

            }
            double xDiff = maxX - minX;
            double yDiff = maxY - minY;
            double zDiff = maxZ - minZ;

            double firstMax = Math.max(xDiff, yDiff);
            radius = Math.max(firstMax, zDiff);

        } catch (Exception e) {
            e.printStackTrace();
        }

        return radius;
    }

    public void tumorGarbageCollector() {
        for(int i = 0; i < this.getTumoroids().size(); i++) {
            // Update status based on interactions and timesteps.
            this.getTumoroids().get(i).checkSetStatus();

            if (this.getTumoroids().get(i).getStatus().equals("delete")) {
                this.getTumoroids().remove(i);
            }
        }
    }

    public double returnGelRange() {
        double min = 0.0;
        double max = 0.0;

        if (numGels != 0) {
            min = gels.get(0).getR();
            max = gels.get(0).getR();

            for (int i = 0; i < numGels; i++) {
                if (gels.get(i).getR() < min) {
                    min = gels.get(i).getR();
                } else if (gels.get(i).getR() > max) {
                    max = gels.get(i).getR();
                }
            }
        }

        return max - min;
    }


    // Output average radius
    double outputMeanRadius() {
        double sum = 0.0;

        for (int i = 0; i < numGels - 1; i++) {
            sum += gels.get(i).getR();

        }

        return sum / numGels;
    }

    double outputRangeOverAverageR() {

        double rangeOverAverageR = returnGelRange() / (2 * calculateWeightedAvgRadius());

        return rangeOverAverageR;
    }

    // Scales size of spheres by input percentage
    void scaleSpheres(double percentage) {

        for (int i = 0; i < numGels; i++) {
            if(gels.get(i).equals(tumorGel) && gels.get(i).getR() > calculateTumorGelRadius()) {
                continue;
            }

            else {
                gels.get(i).setR(gels.get(i).getR() * percentage);
            }
        }

    }

    // Returns sum of volumes of spheres
    double sumSphereVolumes() {
        double sumVolume = 0.0;

        for (int i = 0; i < numGels; i++) {
            sumVolume += gels.get(i).volume();
        }



        return sumVolume;
    }

    public double calculateAvgRadius() {
        double avgRadius;
        double sumRadius = 0;

        for (int i = 0; i < numGels; i++) {
            sumRadius += gels.get(i).getR();
        }

        avgRadius = sumRadius / numGels;

        return avgRadius;
    }

    // Sum of squares divided by sum
    public double calculateWeightedAvgRadius() {
        double weightedAvgRadius;
        double sumRadius = 0;
        double sumOfSquaresRadius = 0;

        for (int i = 0; i < numGels; i++) {
            sumOfSquaresRadius += gels.get(i).getR() * gels.get(i).getR();
            sumRadius += gels.get(i).getR();

        }

        weightedAvgRadius = sumOfSquaresRadius / sumRadius;

        return weightedAvgRadius;
    }


    public double getNumTCells() {
        return numTCells;
    }

    public void setSideLength(double sideLength) {
    	this.sideLength = sideLength;
    }

    public double getSideLength() { return sideLength; }

    public void setNumTCells(double numTCells) {
        this.numTCells = numTCells;
    }

    public void setSavePath(Path savePath) {
        this.savePath = savePath;
    }

    ArrayList<double[]> findSpaces() {

        ArrayList<double[]> spaces = new ArrayList<>();

        int counter = 0;
        // Update this to be dynamic particle radius
        for(int i = 0; i < sideLength / 4; i += 4) {
            for(int j = 0; j < sideLength / 4; j += 4) {
                for(int k = 0; k < sideLength / 4; k += 4) {
                    if(checkGelCollision(i, j, k, 8, this)) {
                        continue;
                    }

                    else {
                        double[] coordinates = new double[3];

                        coordinates[0] = i;
                        coordinates[1] = j;
                        coordinates[2] = k;
                        spaces.add(coordinates);
                    }

                }
            }
        }

        return spaces;
    }

    public static boolean checkGelCollision(double x, double y, double z, double radius, Simulation sim) {
        for (Gel other : sim.gels) {
            if (other != null) {
                double radiusSum = radius + other.getR();

                // Variables to calculate distance between this gel and the others
                double differenceX, differenceY, differenceZ;

                differenceX = x - other.getX();
                differenceY = y - other.getY();
                differenceZ = z - other.getZ();

                differenceX = differenceX - sim.sideLength
                        * roundAwayFromZero(differenceX / sim.sideLength);
                differenceY = differenceY - sim.sideLength
                        * roundAwayFromZero(differenceY / sim.sideLength);
                differenceZ = differenceZ - sim.sideLength
                        * roundAwayFromZero(differenceZ / sim.sideLength);

                // Check if radii overlap which indicates collision
                // If the magnitude of the distance between the two centers is less than the
                if (Math.abs(differenceX) < radiusSum && Math.abs(differenceY) < radiusSum && Math.abs(differenceZ) < radiusSum) {

                    double overlap = magnitude(differenceX, differenceY, differenceZ) - radiusSum;

                    // If there's overlap (collision), return true
                    if (overlap < 0) {
                        return true;
                    }
                }
            }
        }
        // Else, no overlap.
        return false;
    }

    // Takes components of a 3D vector and resolves them into some magnitude
    public static double magnitude(double dx, double dy, double dz) {
        double sum = 0;

        sum += dx * dx;
        sum += dy * dy;
        sum += dz * dz;

        return Math.sqrt(sum);
    }

    // Takes a double and rounds to the nearest integer, using symmetric rounding (away from zero)
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

    void runTCells() {
        tCellThread = new Thread(() -> {
        	vox = new BoxVoxels(this);

        	if(calculateAvgRadius() < 40.0) {
                long spaceTime = System.nanoTime();
                ArrayList<double[]> spaces = findSpaces();

                long finalSpaceTime = System.nanoTime() - spaceTime;
                System.out.println("Space finding takes: " + (finalSpaceTime / 1e9) + " seconds");
            }

    		//addTCellsFromList(spaces);

    		addTCells();

            int numTumor = 559;

            BufferedReader builder2;
            try {
                builder2 = new BufferedReader(new FileReader("tumor.csv"));

                String thisline;
                Random r = new Random();

                // Add tumor replacement gel
                if(tumor) {
                    /*
                     * Gel tumorGel = new Gel(side_length / 2, side_length / 2, side_length / 2,
                     * 2.0, this, "TumorGel"); addGel(tumorGel);
                     */

                    for (int i = 0; i < numTumor; i++) {
                        double randomRadius = 6.0 + (11.9 - 6.0) * r.nextDouble();
                        thisline = builder2.readLine();
                        int comma = thisline.indexOf(',');
                        double x = (tumorGel.getX() - sideLength / 2) + Double.parseDouble(thisline.substring(0, comma)) - 50;
                        int comma2 = comma + 1 + thisline.substring(comma + 1).indexOf(',');
                        double y = (tumorGel.getY() - sideLength / 2) + Double.parseDouble(thisline.substring(comma + 1, comma2)) - 50;
                        comma = comma2 + 1 + thisline.substring(comma2 + 1).indexOf(',');
                        double z = (tumorGel.getZ() - sideLength / 2) + Double.parseDouble(thisline.substring(comma2 + 1, comma)) - 50;
                        double R = randomRadius;
                        this.addTumor(x, y, z, R, i);
                    }

                    vox.remove(tumorGel);
                    gels.remove(tumorGel);
                    numGels--;

                }

            } catch (Exception e) {
                e.printStackTrace();
            }

            boolean breadcrumbs = false;

            //ArrayList<double[]> xyzOutput = new ArrayList<>();

            String avgString = String.format("%07.3f", this.calculateWeightedAvgRadius());
            String stdevString = String.format("%05.3f", this.returnGelRange() / this.calculateWeightedAvgRadius());
            LocalDate currentDate = LocalDate.now();
            String formattedDate = currentDate.format(DateTimeFormatter.ofPattern("ddMMMyyyy"));
            String msdFileName = formattedDate + "_MSDvsTime_LLS-radius" + avgString + "_LLS-dispersion" + stdevString + ".csv";
            //String residenceFileName = "residence" + "_" + calculateAvgRadius() + "_" + timeLimitTCells + "_" + (returnGelRange() / calculateAvgRadius()) + ".csv";
            int abridgedTimer = 0;

            double averageDisplacement;

            int stepReduction = 10;

            double[][] msdArray = new double[2][timeLimitTCells / stepReduction];

            try {

                FileWriter cellWriter = new FileWriter("cell_displacements_individual" + "_LLSwAvg" + calculateWeightedAvgRadius() + "_LLSdispersion" + outputRangeOverAverageR() + "_logNormal.csv");


            	//FileWriter breadcrumbWriter = new FileWriter("breadcrumbs.csv");
                //FileWriter breadcrumbWriterNoPBC = new FileWriter("breadcrumbs_no_pbc.csv");
                //FileWriter xyzWriter = new FileWriter("abs_xyz.csv");

                //FileWriter residenceWriter = new FileWriter(residenceFileName);

                //xyzWriter.append(String.format("%s,%s,%s\n", "x", "y", "z"));

                long startTime = System.nanoTime();

                // Keeping track of time steps for linear regression

                //double xTotal = 0;
                //double yTotal = 0;
                //double zTotal = 0;

                System.out.println("Starting TCells");

                sim_time = 0;

                int intTimer = 0;

                while (sim_time < timeLimitTCells) {

                    //cellWriter.append(String.format("%.3f,", sim_time));
                	averageDisplacement = 0.0;

                	tCellProliferate();

                    for (int i = 0; i < numParticles; i++) {
                    	tCells[i].cellMove();

                    	//xTotal += Math.abs(this.tcells[i].velocityX);
                    	//yTotal += Math.abs(this.tcells[i].velocityY);
                    	//zTotal += Math.abs(this.tcells[i].velocityZ);

                        //breadcrumbWriter.append(String.format("%f,%f,%f,", this.tcells[i].x, this.tcells[i].y, this.tcells[i].z));
                        //breadcrumbWriterNoPBC.append(String.format("%f,%f,%f,", this.tcells[i].xPrime, this.tcells[i].yPrime, this.tcells[i].zPrime));

                        averageDisplacement += this.tCells[i].displacement() * this.tCells[i].displacement();

                        if((int)sim_time % stepReduction == 0) {
                            //xyzWriter.append(String.format("%f,%f,%f\n", xTotal, yTotal, zTotal));
                            cellWriter.append(String.format("%f,%f,%f,", this.tCells[i].x, this.tCells[i].y, this.tCells[i].z));
                        }

                    }

                    if(tumor) {
                		tumorGarbageCollector();
                        tumorGrow();
                        checkTumors();
                	}

                    //setAverageDisplacement(averageDisplacement);

                    //xyzWriter.append(String.format("%f,%f,%f\n", xTotal, yTotal, zTotal));
                    if((int)sim_time % stepReduction == 0) {
                        //xyzWriter.append(String.format("%f,%f,%f\n", xTotal, yTotal, zTotal));
                        msdArray[0][intTimer] = sim_time;
                        msdArray[1][intTimer] = averageDisplacement / numTCells;

                        intTimer++;
                    }

                    if((int)sim_time % 1000 == 0) {
                        //xyzWriter.append(String.format("%f,%f,%f\n", xTotal, yTotal, zTotal));
                        setAverageDisplacement((int)(averageDisplacement / numTCells));

                    }

                    //breadcrumbWriter.append(String.format("\n"));
                    //breadcrumbWriterNoPBC.append(String.format("\n"));

                    if((int)sim_time % stepReduction == 0) {
                        //xyzWriter.append(String.format("%f,%f,%f\n", xTotal, yTotal, zTotal));
                        cellWriter.append(String.format("\n"));
                    }

                    t += dt;

                    //System.out.println(sim_time);
                    sim_time++;
                }

                FileWriter avgWriter = new FileWriter(msdFileName);
                avgWriter.append(String.format("%s,%s\n", "time","msd"));

                for(int i = 0; i < msdArray[1].length; i++) {
                	avgWriter.append(String.format("%.3f,%.5f\n", msdArray[0][i], msdArray[1][i]));

                }

//                for(int i = 0; i < startValues.size(); i++) {
//                    residenceWriter.append(String.format("%d,%d,%d\n", startValues.get(i)[0], startValues.get(i)[1], startValues.get(i)[2]));
//                }

				/*
				 * for(int i = 0; i < numTCells; i++) { tcells[i].outputXYZCSV(); }
				 */

                cellWriter.flush();
                avgWriter.flush();
                //residenceWriter.flush();
                //breadcrumbWriter.flush();
                //breadcrumbWriterNoPBC.flush();
                //xyzWriter.flush();

                long finishTime = System.nanoTime() - startTime;
                System.out.println("T Cell Running Time: " + finishTime / 1e9 + " seconds");
                //System.out.println("Average collisions with tumorGel: " + );
                System.out.println(returnGelRange());
                System.out.println(calculateAvgRadius());
                System.out.println("range / mu(r) = " + (returnGelRange() / calculateAvgRadius()));
                System.out.println("mu(r) / r* = " + (calculateAvgRadius() / 8));

                avgWriter.close();
                cellWriter.close();
                //breadcrumbWriter.close();
                //breadcrumbWriterNoPBC.close();
                //xyzWriter.close();


            } catch (Exception e) {
                e.printStackTrace();

            }


            //new Breadcrumbs(t, dt, (int)numTCells, this, "breadcrumbs.csv");
            //new Breadcrumbs(t, dt, (int)numTCells, this, "breadcrumbs_no_pbc.csv");
            if (gui) {
                //new Graph2(msdFileName, t, dt);
            } else {
                //new Graph2("msd_vs_time.csv" + calculateAvgRadius() + timeLimitTCells + ".csv", t, dt);
            }
        });

        tCellThread.start();
    }

    void runTCellsIterable(int runNum) {

        int stepReduction = 10;

    		vox = new BoxVoxels(this);

            if(calculateWeightedAvgRadius() < 40.0) {
                long spaceTime = System.nanoTime();
                ArrayList<double[]> spaces = findSpaces();

                long finalSpaceTime = System.nanoTime() - spaceTime;
                System.out.println("Space finding takes: " + (finalSpaceTime / 1e9) + " seconds");

                addTCellsFromList(spaces);
            }
            else {
                addTCells();
            }

            int numTumor = 559;


            //ArrayList<double[]> xyzOutput = new ArrayList<>();
            //String residenceFileName = "residence" + "_" + calculateAvgRadius() + "_" + timeLimitTCells + "_" + (returnGelRange() / calculateAvgRadius()) + ".csv";
            String runNumString = String.format("%06d", runNum);
            String avgString = String.format("%07.3f", this.calculateWeightedAvgRadius());
            String dispersionString = String.format("%05.3f", this.returnGelRange() / (2 * this.calculateWeightedAvgRadius()));
            LocalDate currentDate = LocalDate.now();
            String formattedDate = currentDate.format(DateTimeFormatter.ofPattern("ddMMMyyyy"));
            String msdFileName = formattedDate + "_MSDvsTime_LLS-radius" + avgString + "_LLS-dispersion" + dispersionString + "_runNum-" + runNumString + ".csv";
            int abridgedTimer = 0;

            double averageDisplacement;

            double[][] msdArray = new double[2][timeLimitTCells/stepReduction];

            try {

                //FileWriter cellWriter = new FileWriter("cell_displacements_individual" + "_" + calculateAvgRadius() + "_" + timeLimitTCells + "_" + tcells[0].velocity + ".csv");


            	//FileWriter breadcrumbWriter = new FileWriter("breadcrumbs.csv");
                //FileWriter breadcrumbWriterNoPBC = new FileWriter("breadcrumbs_no_pbc.csv");
                //FileWriter xyzWriter = new FileWriter("abs_xyz.csv");

                //FileWriter residenceWriter = new FileWriter(residenceFileName);

                //xyzWriter.append(String.format("%s,%s,%s\n", "x", "y", "z"));

                long startTime = System.nanoTime();

                // Keeping track of time steps for linear regression

                //double xTotal = 0;
                //double yTotal = 0;
                //double zTotal = 0;

                System.out.println("Starting TCells");

                sim_time = 0;

                int intTimer = 0;

                while (sim_time < timeLimitTCells) {

                    //cellWriter.append(String.format("%.3f,", sim_time));
                	averageDisplacement = 0.0;

                    for (int i = 0; i < numTCells; i++) {
                    	tCells[i].cellMove();

                    	//xTotal += Math.abs(this.tcells[i].velocityX);
                    	//yTotal += Math.abs(this.tcells[i].velocityY);
                    	//zTotal += Math.abs(this.tcells[i].velocityZ);


                        //breadcrumbWriter.append(String.format("%f,%f,%f,", this.tcells[i].x, this.tcells[i].y, this.tcells[i].z));
                        //breadcrumbWriterNoPBC.append(String.format("%f,%f,%f,", this.tcells[i].xPrime, this.tcells[i].yPrime, this.tcells[i].zPrime));

                        averageDisplacement += this.tCells[i].displacement() * this.tCells[i].displacement();


                        //cellWriter.append(Strin
                        // g.format("%.5f,", tcells[i].displacement()));

                    }

                    if(tumor) {
                		tumorGarbageCollector();
                        tumorGrow();
                        checkTumors();
                	}

                    //setAverageDisplacement(averageDisplacement);
                    //System.out.println((int)sim_time);

                    if((int)sim_time % stepReduction == 0) {
                    	//xyzWriter.append(String.format("%f,%f,%f\n", xTotal, yTotal, zTotal));
                        msdArray[0][intTimer] = sim_time;
                        msdArray[1][intTimer] = averageDisplacement / numTCells;

                        intTimer++;
                    }


                    //breadcrumbWriter.append(String.format("\n"));
                    //breadcrumbWriterNoPBC.append(String.format("\n"));

                    //cellWriter.append(String.format("\n"));

                    t += dt;

                    //System.out.println(sim_time);
                    sim_time++;
                }

                System.out.println("T-Cells Finished");

                FileWriter avgWriter = new FileWriter(msdFileName);
                avgWriter.append(String.format("%s,%s\n", "time","msd"));

                for(int i = 0; i < msdArray[1].length; i++) {
                	avgWriter.append(String.format("%.3f,%.5f\n", msdArray[0][i], msdArray[1][i]));

                }

//                for(int i = 0; i < startValues.size(); i++) {
//                    residenceWriter.append(String.format("%d,%d,%d\n", startValues.get(i)[0], startValues.get(i)[1], startValues.get(i)[2]));
//                }

				/*
				 * for(int i = 0; i < numTCells; i++) { tcells[i].outputXYZCSV(); }
				 */

                //cellWriter.flush();
                avgWriter.flush();
                //residenceWriter.flush();
                //breadcrumbWriter.flush();
                //breadcrumbWriterNoPBC.flush();
                //xyzWriter.flush();

                long finishTime = System.nanoTime() - startTime;
                System.out.println("T Cell Running Time: " + finishTime / 1e9 + " seconds");
                //System.out.println("Average collisions with tumorGel: " + );
                System.out.println(returnGelRange());
                System.out.println(calculateAvgRadius());
                System.out.println("range / mu(r) = " + (returnGelRange() / calculateAvgRadius()));
                System.out.println("mu(r) / r* = " + (calculateAvgRadius() / 8));

                avgWriter.close();
                //cellWriter.close();
                //breadcrumbWriter.close();
                //breadcrumbWriterNoPBC.close();
                //xyzWriter.close();


            } catch (Exception e) {
                e.printStackTrace();

            }


            //new Breadcrumbs(t, dt, (int)numTCells, this, "breadcrumbs.csv");
            //new Breadcrumbs(t, dt, (int)numTCells, this, "breadcrumbs_no_pbc.csv");
//            if (gui) {
//                //new Graph2(msdFileName, t, dt);
//            } else {
//                //new Graph2("msd_vs_time.csv" + calculateAvgRadius() + timeLimitTCells + ".csv", t, dt);
//            }
//

            System.exit(0);
    }


    void addTCellsFromList(ArrayList<double[]> spaces) {
        int idNum = 0;

        long spaceTime = System.nanoTime();

        while (numParticles < getNumTCells()) {
            addTCellFromList(idNum, spaces);

            idNum++;
        }

        long finalSpaceTime = System.nanoTime() - spaceTime;

        System.out.println("Time to fill t-cells: " + (finalSpaceTime / 1e9));
    }

    public void addTumor(double x, double y, double z, double R, int idNum) {
        Tumoroid tumoroid = new Tumoroid(x, y , z, R, idNum, this);
        vox.add(tumoroid);
        tumoroids.add(tumoroid);

    }

    void addTCells() {
        int idNum = 0;

        long spaceTime = System.nanoTime();

        while (numParticles < getNumTCells()) {
            addTCell(idNum);

            idNum++;
        }

        long finalSpaceTime = System.nanoTime() - spaceTime;

        System.out.println("Time to fill t-cells: " + (finalSpaceTime / 1e9));
    }

    void addTestTCells() {
        int idNum = 0;
        while (numParticles < getNumTCells()) {
            TCell c = new TCell(500, 500, 500, 8, idNum, this, rand, logNormal);
            vox.add(c);
            tCells[numParticles++] = c;
            sum_sphere_volume += c.volume();

            idNum++;
        }
    }

    void addTCell(double x, double y, double z, double R) {
        TCell c = new TCell(x, y, z, R, 0, this, rand, logNormal);

        vox.add(c);
        tCells[numParticles++] = c;
        sum_sphere_volume += c.volume();
    }

    void addTCell(int idNum) {
        double R = 8;

        //TODO: Change back to global positioning

        double x = R + rand.nextDouble() * (sideLength - 2 * R);
        double y = R + rand.nextDouble() * (sideLength - 2 * R);
        double z = R + rand.nextDouble() * (sideLength - 2 * R);

        // Change this to creating class after checking for collision


        if (checkGelCollision(x, y, z, R, this) == false) {
            TCell c = new TCell(x, y, z, R, idNum, this, rand, logNormal);
            vox.add(c);
            tCells[numParticles++] = c;
            sum_sphere_volume += c.volume();
            //System.out.println(c.getIdNum());
        }
    }

    void addTCellFromList(int idNum, ArrayList<double[]> spaces) {
        double R = 8;

        //TODO: Change back to global positioning

        double [] position = spaces.get(rand.nextInt(spaces.size()));

        double x = position[0];
        double y = position[1];
        double z = position[2];

        TCell c = new TCell(x, y, z, R, idNum, this, rand, logNormal);

        vox.add(c);
        tCells[numParticles++] = c;
        sum_sphere_volume += c.volume();

        //System.out.println(c.getIdNum());

    }

    void reset() {
        numGels = 0;
        sum_sphere_volume = 0;
        sideLength = 1000;
        for (int i = 0; i < vox.voxels_per_side; ++i) {
            for (int j = 0; j < vox.voxels_per_side; ++j) {
                for (int k = 0; k < vox.voxels_per_side; ++k) {
                    vox.voxels[i][j][k].clear();
                }
            }
        }
    }

    //TODO: Check for negatives, delete zero (truncate)
    void addGel() {
        // Spawn new gel in the box
        double R = (rAverageRadius * (1 - rangeOverAverageR)) + (((rAverageRadius * (1 + rangeOverAverageR)) - (rAverageRadius * (1 - rangeOverAverageR))) * rand.nextDouble());

        double x = R + rand.nextDouble() * (sideLength - 2 * R);
        double y = R + rand.nextDouble() * (sideLength - 2 * R);
        double z = R + rand.nextDouble() * (sideLength - 2 * R);

        Gel g = new Gel(x, y, z, R, this);

        // Ensure that the location isn't occupied, and truncate zero
        if (g.checkCollision() == false && R != 0) {
            vox.add(g);
            gels.add(numGels++, g);
            sum_sphere_volume += g.volume();
        }
    }

    void addGel(double x, double y, double z, double R, String name) {
        Gel g = new Gel(x, y, z, R, this, name);
        vox.add(g);
        gels.add(numGels++, g);
        sum_sphere_volume += g.volume();
    }

    void addGel(Gel gel) {
        vox.add(gel);
        gels.add(numGels++, gel);
        sum_sphere_volume += gel.volume();
    }

    void start() {
        simulating = true;
        Thread simThread = new Thread(() -> {
            while (simulating && t < fall_time_limit) {
                //timestep();
            }
            simulating = false;
        });
        simThread.start();
    }


    void pause() {
        simulating = false;
    }

}

class BoxVoxels {
    int voxels_per_side = 5;
    double voxel_side_length;
    Voxel[][][] voxels;
	BoxVoxels(Box B) {
        voxel_side_length = B.sideLength /voxels_per_side;
        voxels = new Voxel[voxels_per_side][voxels_per_side][voxels_per_side];
        for (int i = 0; i < voxels_per_side; ++i) {
            for (int j = 0; j < voxels_per_side; ++j) {
                for (int k = 0; k < voxels_per_side; ++k) {
                    voxels[i][j][k] = new Voxel(i,j,k);
                }
            }
        }
    }
    BoxVoxels(Box B, int voxels_per_side) {
        this.voxels_per_side = voxels_per_side;
        voxel_side_length = B.sideLength /voxels_per_side;
        voxels = new Voxel[voxels_per_side][voxels_per_side][voxels_per_side];
        for (int i = 0; i < voxels_per_side; ++i) {
            for (int j = 0; j < voxels_per_side; ++j) {
                for (int k = 0; k < voxels_per_side; ++k) {
                    voxels[i][j][k] = new Voxel(i,j,k);
                }
            }
        }
    }
    void add(Particle p) {
        for (Voxel v : p.in_voxels) {
            v.add(p);
        }
    }
    synchronized void remove(Particle p) {
        for (Voxel v : p.in_voxels) {
            v.remove(p);
        }
        p.voxel = null;
        p.in_voxels = new Voxel[]{};
        p.nearby = new Particle[]{};
    }
}

class Voxel {
    int x, y, z;
    HashSet<Particle> set;
    Particle[] particles = new Particle[]{};

    Voxel(int i, int j, int k) {
        set = new HashSet<>();
        x = i;
        y = j;
        z = k;
    }
    synchronized void add(Particle p) {
        set.add(p);
        particles = new Particle[set.size()];
        Object[] temp = set.toArray();
        for (int i = 0; i < set.size(); ++i)
            particles[i] = (Particle) temp[i];
    }
    synchronized void remove(Particle p) {
        set.remove(p);
        particles = new Particle[set.size()];
        Object[] temp = set.toArray();
        for (int i = 0; i < set.size(); ++i)
            particles[i] = (Particle) temp[i];
    }
    void clear() {
        set = new HashSet<>();
        particles = new Particle[]{};
    }
}