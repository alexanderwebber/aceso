package com.company;

import org.apache.commons.math3.distribution.LogNormalDistribution;

import java.awt.*;
import java.io.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Random;
import java.util.concurrent.TimeUnit;


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
    int tumorDoublingTime = 36;
    int tCellDoublingTime = 1080;
    int startingNumTumor = 0;

    public int getTumorDoublingTime() {
        return tumorDoublingTime;
    }

    public void setTumorDoublingTime(int tumorDoublingTime) {
        this.tumorDoublingTime = tumorDoublingTime;
    }

    public int gettCellDoublingTime() {
        return tCellDoublingTime;
    }

    public void setTCellDoublingTime(int tCellDoublingTime) {
        this.tCellDoublingTime = tCellDoublingTime;
    }

    public void setStartingTumorCells(int numTumor) { this.startingNumTumor = numTumor; }

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
    int numTumorInCSV = 559;
    int numTumor = 0;
    TCell[] tCells = new TCell[1000000];
    ArrayList<Particle> imageParticles = new ArrayList<>();
    ArrayList<Double> densityValues = new ArrayList<>();
    int numParticles = 0;
    double numTCells = 100;
    double numTCellRatio = 5;
    double tumorGelNoGelRadius = 100;
    double averageDisplacementPanel;
    int tCellRefractoryPeriod = 360;

    public double gettCellRefractoryPeriod() {
        return tCellRefractoryPeriod;
    }

    public void setTCellRefractoryPeriod(int tCellRefractoryPeriod) {
        this.tCellRefractoryPeriod = tCellRefractoryPeriod;
    }

    boolean tumor = true;
    Gel tumorGel;
    //double numGelsToSet = 1000;
    //Threads of control
    Thread settleThread = new Thread();
    Thread fillThread = new Thread();
    Thread fillLattice = new Thread();
    Thread fallThread = new Thread();
    Thread tCellThread = new Thread();
    Thread tumorThread = new Thread();

    public int getNumParticles() {
        return numParticles;
    }

    public void setNumParticles(int numParticles) {
        this.numParticles = numParticles;
    }

    public double getTumorGelNoGelRadius() {
        return tumorGelNoGelRadius;
    }

    public void setTumorGelNoGelRadius(double tumorGelNoGelRadius) {
        this.tumorGelNoGelRadius = tumorGelNoGelRadius;
    }

    public double getNumTCellRatio() {
        return numTCellRatio;
    }

    public void setNumTCellRatio(double numTCellRatio) {
        this.numTCellRatio = numTCellRatio;
    }

    // Residence data stuff
    int simulationTimeLimit = 3600;

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
    Visualization panel;

    // To be changed by panel settings
    double rAverageRadius = 50.0;
    double rangeOverAverageR = 0.0;

    //Constructors
    // With GUI


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

    void setPanel(Visualization panel) {
        this.panel = panel;
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
            setSideLength(sideLengthByNumGels);
            setVolume(sideLength * sideLength * sideLength);

            //int numGelsToSet = (int) ((0.67) * (Math.floor((side_length * side_length * side_length) / ((4 / 3) * (rAverageRadius * rAverageRadius * rAverageRadius) * Math.PI))));

            int numGelsToSet = 1000;
            double[] timeNull = new double[numGelsToSet];
            double[] timeFinal = new double[numGelsToSet];

            vox = new BoxVoxels(this);

            rAverageRadius = rAverageRadius * 0.1;

            // Add tumor replacement gel
//            if(tumor) {
//                tumorGel = new Gel(sideLength / 2, sideLength / 2, sideLength / 2, 10, this, "TumorGel");
//                addGel(tumorGel);
//            }

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

    void fillGelsByCSV() throws IOException {
        BufferedReader gelBuilder;

        setSide(sideLength);

        vox = new BoxVoxels(this);

        double sideLengthByNumGels = Math.cbrt((1000.0 * ((4.0 / 3.0) * (rAverageRadius * rAverageRadius * rAverageRadius) * Math.PI)) / volume_ratio);
        setSideLength(sideLengthByNumGels);
        setVolume(sideLength * sideLength * sideLength);

        try {
            gelBuilder = new BufferedReader(new FileReader("test_gel_with_tumorGel.csv"));


            String thisLine;

            for (int i = 0; i < 938; i++) {
                thisLine = gelBuilder.readLine();
                int comma = thisLine.indexOf(',');
                double x = Double.parseDouble(thisLine.substring(0, comma));
                int comma2 = comma + 1 + thisLine.substring(comma + 1).indexOf(',');
                double y = Double.parseDouble(thisLine.substring(comma + 1, comma2));
                comma = comma2 + 1 + thisLine.substring(comma2 + 1).indexOf(',');
                double z = Double.parseDouble(thisLine.substring(comma2 + 1, comma));
                int commaR = comma + 1 + thisLine.substring(comma2 + 1).indexOf(',');
                double R = Double.parseDouble(thisLine.substring(comma + 1, commaR - 1));

                if(i == 0) {
                    tumorGel = new Gel(x, y, z, R, this);
                    vox.add(tumorGel);
                    gels.add(numGels++, tumorGel);
                    sum_sphere_volume += tumorGel.volume();
                }
                else {
                    Gel g = new Gel(x, y, z, R, this);
                    vox.add(g);
                    gels.add(numGels++, g);
                    sum_sphere_volume += g.volume();
                }

            }
        } catch (IOException e) {
            e.printStackTrace();
        }
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
    void fallUnthreaded() throws IOException {

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

        //print out gel:
//        FileWriter gelWriter = new FileWriter("test_gel_with_tumorGel.csv");
//        for(Gel gel: gels) {
//            gelWriter.append(String.format("%f,%f,%f,%f,\n", gel.x, gel.y, gel.z, gel.R));
//        }
    }

//    private void generateTumor(int startingNumTumor) {
//
//        //Set up tumor
//        if(startingNumTumor < 1337 && limitReached == false) {
//            addTumor();
//            this.setStartingTumorCells(this.getTumoroids().size());
//            for(int i = 0; i < startingNumTumor; i++) {
//                this.getTumoroids().get(i).updateCollision();
//            }
//            System.out.println(startingNumTumor);
//        }
//        else {
//            for(int i = 0; i < B.numTumor; i++) {
//                limitReached = true;
//
//                //B.getTumors().get(i).move(i, B);
//
//                double x = Math.abs(this.getTumoroids().get(i).getX() - 500);
//                double y = Math.abs(this.getTumoroids().get(i).getY() - 500);
//                double z = Math.abs(this.getTumoroids().get(i).getZ() - 500);
//
//                double distanceVector = Math.pow(x, 2) + Math.pow(y, 2) + Math.pow(z, 2);
//
//                if(distanceVector > 10000) {
//                    B.getTumoroids().remove(i);
//                    B.setNumTumor(B.getTumoroids().size());
//                }
//            }
//        }
//    }

    private void checkTumors() {
        for (int i = 0; i < this.getNumTumor(); ++i) {
            this.getTumoroids().get(i).checkSetStatus();
        }
    }

    void tumorGrow() {
        for(int i = 0; i < this.getNumTumor(); i++) {
            if(this.getTumoroids().get(i).getStatus().equals("alive")) {
                if(this.getTumoroids().get(i).getR() < 12.0) {
                	//TODO: Make growth rate variable
                    this.getTumoroids().get(i).R = this.getTumoroids().get(i).getR() + (1.0 / tumorDoublingTime) * 0.0333333333;
                }
                else {
                    this.getTumoroids().get(i).setR(6.0);
                    double x = this.getTumoroids().get(i).getX() + 6.0;
                    double y = this.getTumoroids().get(i).getY();
                    double z = this.getTumoroids().get(i).getZ();
                    this.addTumor(x, y, z, 6.0, this.getNumTumor());

                }

            }
            this.getTumoroids().get(i).updateCollision();
        }
    }

    void tCellProliferate() {
        for(int i = 0; i < this.numParticles; i++) {
            if(this.tCells[i].getLifeTime() < tCellDoublingTime) {
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

                double minRadius = 0.0;
                double maxRadius = 0.0;

                for (int i = 0; i < numTumor; i++) {
                    double randomRadius = 6.0 + (11.9 - 6.0) * r.nextDouble();
                    thisline = builder2.readLine();
                    int comma = thisline.indexOf(',');
                    double x = Double.parseDouble(thisline.substring(0, comma));
                    int comma2 = comma + 1 + thisline.substring(comma + 1).indexOf(',');
                    double y = Double.parseDouble(thisline.substring(comma + 1, comma2));
                    comma = comma2 + 1 + thisline.substring(comma2 + 1).indexOf(',');
                    double z = Double.parseDouble(thisline.substring(comma2 + 1, comma));
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
                            minX = x;
                            minRadius = randomRadius;
                        }
                        else if(x > maxX) {
                            maxX = x;
                            maxRadius = randomRadius;
                        }
                        if(y < minY) {
                            minY = y;
                            minRadius = randomRadius;
                        }
                        else if(y > maxY) {
                            maxY = y;
                            maxRadius = randomRadius;
                        }
                        if(z < minZ) {
                            minZ = z;
                            minRadius = randomRadius;
                        }
                        else if(z > maxZ) {
                            maxZ = z;
                            maxRadius = randomRadius;
                        }
                    }
                }

                double xDiff = (maxX + maxRadius) - (minX - minRadius);
                double yDiff = (maxY + maxRadius) - (minY - minRadius);
                double zDiff = (maxZ + maxRadius) - (minZ - minRadius);

                double firstMax = Math.max(xDiff, yDiff);
                radius = Math.max(firstMax, zDiff);

            }


        } catch (Exception e) {
            e.printStackTrace();
        }

        return radius / 2;
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

        if(gels.size() != 0) {
            double rangeOverAverageR = returnGelRange() / (2 * calculateWeightedAvgRadius());
        }

        else {
            rangeOverAverageR = 0.0;
        }

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
        ArrayList<Particle> combinedList = new ArrayList<>();

        combinedList.addAll(sim.gels);
        combinedList.addAll(sim.tumoroids);

        for (Particle other : combinedList) {
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

    void buildTumorFromCSV() {

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

                for (int i = 0; i < numTumorInCSV; i++) {
                    double R = 6.0 + (12.0 - 6.0) * r.nextDouble();
                    thisline = builder2.readLine();
                    int comma = thisline.indexOf(',');
                    double x = (tumorGel.getX() - 500) + Double.parseDouble(thisline.substring(0, comma));
                    int comma2 = comma + 1 + thisline.substring(comma + 1).indexOf(',');
                    double y = (tumorGel.getY() - 500) + Double.parseDouble(thisline.substring(comma + 1, comma2));
                    comma = comma2 + 1 + thisline.substring(comma2 + 1).indexOf(',');
                    double z = (tumorGel.getZ() - 500) + Double.parseDouble(thisline.substring(comma2 + 1, comma));
                    this.addTumor(x, y, z, R, i);
                }

                vox.remove(tumorGel);
                gels.remove(tumorGel);
                numGels--;

            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    void runTumor() throws IOException {
        setSide(sideLength);

        vox = new BoxVoxels(this);

        tumorGelNoGelRadius = 115;

        Gel tumorGelSingle = new Gel(sideLength / 2, sideLength / 2, sideLength / 2, tumorGelNoGelRadius, this, "TumorGel");

        addGel(tumorGelSingle);

        generateTumor(tumorGelSingle);

        //buildTumorFromCSV();

        int[] numTumorCellsVsTime = new int[simulationTimeLimit];

        while (sim_time < simulationTimeLimit) {
            tumorGarbageCollector();
            tumorGrow();

            numTumorCellsVsTime[(int)sim_time] = numTumor;

            sim_time++;
        }

        numTumorVsTimeToCSVSingle(numTumorCellsVsTime);
    }

    void runTumorThread() {
        tumorThread = new Thread(() -> {
            try {
                runTumor();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        tumorThread.start();
    }

    double[] calculateTMaxForTumors(int[] numTumorCellsVsTime) {
        int tMax = 0;
        int numTumorMax = 0;

        double[] returnArray = new double[2];

        for(int i = 0; i < numTumorCellsVsTime.length; i++) {
            if(numTumorCellsVsTime[i] >= numTumorMax) {
                numTumorMax = numTumorCellsVsTime[i];
                tMax = i;
            }
        }

        returnArray[0] = tMax / 180.0;
        returnArray[1] = numTumorMax;

        return returnArray;
    }

    double calculateTExtinctionForTumors(int[] numTumorCellsVsTime) {
        int tExtinction = 0;

        for(int i = 0; i < numTumorCellsVsTime.length; i++) {
            if(numTumorCellsVsTime[i] == 0) {
                tExtinction = i;
                break;
            }
        }

        return tExtinction / 180.0;
    }

    void numTumorVsTimeToCSVSingle(int[] numTumorCellsVsTime) throws IOException {
            String tumorVsTimeOutputString = "tumorVsTime.csv";
            FileWriter tumorCellsVsTimeWriter = new FileWriter(tumorVsTimeOutputString);

            for(int i = 0; i < numTumorCellsVsTime.length; i++) {
                tumorCellsVsTimeWriter.append(String.format("%d\n", numTumorCellsVsTime[i]));
            }

            tumorCellsVsTimeWriter.flush();
    }

    void numTumorVsTimeToCSV(int[] numTumorCellsVsTime, double[] tumorTime, int runNum) {
        double[] tMaxAndNumMaxTumor = calculateTMaxForTumors(numTumorCellsVsTime);
        double tMax = tMaxAndNumMaxTumor[0];
        double tExtinction = calculateTExtinctionForTumors(numTumorCellsVsTime);
        int numMaxTumor = (int)tMaxAndNumMaxTumor[1];

        try {
            String tumorToTCellRatioString = String.format("%.0f", numTCellRatio);
            String numTCellsString = String.format("%.0f", numTCells);
            String numTumorsString = String.format("%d", startingNumTumor);
            String numMaxTumorString = String.format("%d", numMaxTumor);
            String tCellGrowthInHoursString = String.format("%.0f", tCellDoublingTime / 180.0);
            String tumorGrowthInHoursString = String.format("%d", tumorDoublingTime);
            String tCellRefractoryPeriodInTimeHoursString = String.format("%.1f", tCellRefractoryPeriod / 180.0);
            String tMaxString = String.format("%.2f", tMax);
            String tExtinctionString = String.format("%.2f", tExtinction);

            LocalDate currentDate = LocalDate.now();
            String formattedDate = currentDate.format(DateTimeFormatter.ofPattern("ddMMMyyyy"));

            String tumorVsTimeOutputString = "runNum" + runNum
                    + "_tumorToTCellRatio" + tumorToTCellRatioString
                    + "_numTumorsStarting" + numTumorsString
                    + "_numMaxTumor" + numMaxTumorString
                    + "_tCellDoublingTimeInHours" + tCellGrowthInHoursString
                    + "_tumorDoublingTimeInHours" + tumorGrowthInHoursString
                    + "_tCellRefractoryPeriodInSteps" + tCellRefractoryPeriodInTimeHoursString
                    + "_tMax" + tMaxString
                    + "_tExtinction" + tExtinctionString
                    + "_"
                    + formattedDate
                    + ".csv";


            FileWriter tumorCellsVsTimeWriter = new FileWriter(tumorVsTimeOutputString);

            tumorCellsVsTimeWriter.append(String.format("%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s\n",
                    "runNum",
                    "tumorToTCellRatio",
                    "numTumorsStarting",
                    "numMaxTumor",
                    "tCellDoublingTimeInHours",
                    "tumorDoublingTimeInHours",
                    "tCellRefractoryPeriodInSteps",
                    "tMax",
                    "tExtinction",
                    "time",
                    "currentNumTumor"));

            tumorCellsVsTimeWriter.append(String.format("%d,%s,%s,%s,%s,%s,%s,%s,%s,",
                    runNum,
                    tumorToTCellRatioString,
                    numTumorsString,
                    numMaxTumor,
                    tCellGrowthInHoursString,
                    tumorGrowthInHoursString,
                    tCellRefractoryPeriodInTimeHoursString,
                    tMaxString,
                    tExtinctionString));

            for(int i = 0; i < numTumorCellsVsTime.length; i++) {
                if(i == 0) {
                    tumorCellsVsTimeWriter.append(String.format("%.1f,%d\n", tumorTime[i], numTumorCellsVsTime[i]));
                }

                tumorCellsVsTimeWriter.append(String.format(",,,,,,,,,%.1f,%d\n", tumorTime[i], numTumorCellsVsTime[i]));
            }

            tumorCellsVsTimeWriter.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    void numTCellsVsTimeToCSV(int[] numTumorCellsVsTime, double[] tumorTime, int runNum) {
        try {
            FileWriter tCellsVsTimeWriter = new FileWriter("tCellProliferationVsTime_runNum" + runNum + ".csv");

            for(int i = 0; i < numTumorCellsVsTime.length; i++) {
                tCellsVsTimeWriter.append(String.format("%.1f,%d\n", tumorTime[i], numTumorCellsVsTime[i]));
            }

            tCellsVsTimeWriter.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    void generateTumor(Gel tumorGel) {

        int iD = 0;

        double tumorRadiusSquared = tumorGel.getR() * tumorGel.getR();

        double volumeOfTumorCell = ((4 / 3) * Math.PI * Math.pow(12, 3));

        double intendedNumTumor = (Math.pow(2 * tumorGel.getR(), 3) / volumeOfTumorCell);

        //Set up tumor
        while (numTumor < intendedNumTumor) {
            double x = (tumorGel.getX() - tumorGel.getR()) + rand.nextDouble() * (tumorGel.getR() * 2);
            double y = (tumorGel.getY() - tumorGel.getR()) + rand.nextDouble() * (tumorGel.getR() * 2);
            double z = (tumorGel.getZ() - tumorGel.getR()) + rand.nextDouble() * (tumorGel.getR() * 2);
            double R = 6.0 + (12.0 - 6.0) * rand.nextDouble();
            addTumor(x, y, z, R, iD);

//            for(int i = 0; i < this.numTumor; i++) {
//                this.getTumoroids().get(i).updateCollision();
//            }

            iD++;
        }

        vox.remove(tumorGel);
        gels.remove(tumorGel);
        numGels--;

        for(int i = 0; i < numTumor; i++) {

            //B.getTumors().get(i).move(i, B);

            double x = Math.abs(getTumoroids().get(i).getX() - tumorGel.getX());
            double y = Math.abs(getTumoroids().get(i).getY() - tumorGel.getY());
            double z = Math.abs(getTumoroids().get(i).getZ() - tumorGel.getZ());

            double distanceVector = Math.pow(x, 2) + Math.pow(y, 2) + Math.pow(z, 2);

            if(distanceVector > tumorRadiusSquared) {
                getTumoroids().get(i).setStatus("delete");
            }
        }

        tumorGarbageCollector();

    }

    // TODO: Start t-cells in distribution of refractory times
    // TODO: Start around, not within
    // TODO: For t-cell entry, add t-cells randomly around tumor at rate in analytic model
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

            tumorGelNoGelRadius = 70;

            tumorGel = new Gel(sideLength / 2, sideLength / 2, sideLength / 2, tumorGelNoGelRadius, this, "TumorGel");

            addGel(tumorGel);

            generateTumor(tumorGel);

            //TODO: Figure out why we need to run this more than once.
            for(int i = 0; i < 5; i++) {
                tumorGarbageCollector();
            }

            numTumor = getNumTumor();

            //buildTumorFromCSV();

            setNumTCells(Math.ceil(numTumor / 100.0));

            addTCells();

            boolean breadcrumbs = false;

            //ArrayList<double[]> xyzOutput = new ArrayList<>();

            String avgString = String.format("%07.3f", this.calculateWeightedAvgRadius());
            //String stdevString = String.format("%05.3f", this.returnGelRange() / this.calculateWeightedAvgRadius());
            LocalDate currentDate = LocalDate.now();
            String formattedDate = currentDate.format(DateTimeFormatter.ofPattern("ddMMMyyyy"));
            //String msdFileName = formattedDate + "_MSDvsTime_LLS-radius" + avgString + "_LLS-dispersion" + stdevString + ".csv";
            //String residenceFileName = "residence" + "_" + calculateAvgRadius() + "_" + timeLimitTCells + "_" + (returnGelRange() / calculateAvgRadius()) + ".csv";
            int abridgedTimer = 0;

            double averageDisplacement;

            int stepReduction = 10;

            double[][] msdArray = new double[2][simulationTimeLimit / stepReduction];

            try {

                //FileWriter cellWriter = new FileWriter("cell_displacements_individual" + "_LLSwAvg" + calculateWeightedAvgRadius() + "_LLSdispersion" + outputRangeOverAverageR() + "_logNormal.csv");

                //FileWriter refractoryWriter = new FileWriter("refractory.csv");
                FileWriter killWriter = new FileWriter("killsvstime.csv");

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

                int[] numTumorCellsVsTime = new int[simulationTimeLimit];
                double[] tumorTime = new double[simulationTimeLimit];

                //int[][] refractoryTime = new int[numParticles][720];

                int[] numKillsVsTime = new int[simulationTimeLimit];
                int[] numDeathsVsTime = new int[simulationTimeLimit];

                int[] numTCellsVsTime = new int[simulationTimeLimit];

                int numDeaths = 0;

                while (sim_time < simulationTimeLimit) {

                    //cellWriter.append(String.format("%.3f,", sim_time));
                	averageDisplacement = 0.0;

                    if(this.getTumoroids().size() > 0) {
                        tCellProliferate();
                    }

//                    if((int)sim_time < 360) {
//                        for(int i = 0; i < numTCells; i++) {
//                            refractoryTime[i][(int)sim_time] = tCells[i].getLastTimeKilled();
//                        }
//                    }

                    int numKills = 0;

                    for(int i = 0; i < numParticles; i++) {
                        numKills += tCells[i].getNumKills();
                    }

                    numKillsVsTime[(int)sim_time] = numKills;

                    for (int i = 0; i < numParticles; i++) {
                    	tCells[i].cellMove();

                        averageDisplacement += this.tCells[i].displacement() * this.tCells[i].displacement();

                        if((int)sim_time % stepReduction == 0) {
                            //xyzWriter.append(String.format("%f,%f,%f\n", xTotal, yTotal, zTotal));
                            //cellWriter.append(String.format("%f,%f,%f,", this.tCells[i].x, this.tCells[i].y, this.tCells[i].z));
                        }
                    }

                    for(int i = 0; i < getTumoroids().size(); i++) {
                        if(getTumoroids().get(i).getStatus().equals("delete")) {
                            numDeaths++;
                        }
                    }
                    numDeathsVsTime[(int)sim_time] = numDeaths;

                    if(tumor) {
                		tumorGarbageCollector();
                		tumorGrow();
                        numTumorCellsVsTime[(int)sim_time] = this.getTumoroids().size();
                        numTCellsVsTime[(int)sim_time] = numParticles;
                        tumorTime[(int)sim_time] = sim_time / 180.0;
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
                        //cellWriter.append(String.format("\n"));
                    }

                    if(this.getTumoroids().size() == 0) {
                        break;
                    }

                    if((int)sim_time % 90 == 0) {
                        panel.printBMP((int)sim_time, "BMPs");
                    }

                    t += dt;

                    //System.out.println(sim_time);
                    sim_time++;
                }

                // TODO: Calculate average time between kills
                double overallSum = 0;
                for(int i = 0; i < numParticles; i++) {
                    double individualSum = 0;
                    for(int j = 0; j < tCells[i].individualAverageTimeBetweenKills.size(); j++) {
                        individualSum += tCells[i].individualAverageTimeBetweenKills.get(j);
                    }
                    if(tCells[i].individualAverageTimeBetweenKills.size() != 0) {
                        overallSum += individualSum / tCells[i].individualAverageTimeBetweenKills.size();
                    }

                }

                double overallAvg = overallSum / numParticles;

                System.out.println("overall avg: " + overallAvg);

                if(tumor) {
                    numTumorVsTimeToCSV(numTumorCellsVsTime, tumorTime, 0);
                    numTCellsVsTimeToCSV(numTCellsVsTime, tumorTime, 0);
                }

                //FileWriter avgWriter = new FileWriter(msdFileName);
                //avgWriter.append(String.format("%s,%s\n", "time","msd"));

                for(int i = 0; i < msdArray[1].length; i++) {
                	//avgWriter.append(String.format("%.3f,%.5f\n", msdArray[0][i], msdArray[1][i]));

                }

//                for(int i = 0; i < 360; i++) {
//                    for(int j = 0; j < numTCells; j++) {
//                        refractoryWriter.append(String.format("%d,", refractoryTime[j][i]));
//                    }
//                    refractoryWriter.append("\n");
//                }

                for(int i = 0; i < numKillsVsTime.length; i++) {
                    killWriter.append(String.format("%d,%d\n", numKillsVsTime[i], numDeathsVsTime[i]));
                }

//                for(int i = 0; i < startValues.size(); i++) {
//                    residenceWriter.append(String.format("%d,%d,%d\n", startValues.get(i)[0], startValues.get(i)[1], startValues.get(i)[2]));
//                }

				/*
				 * for(int i = 0; i < numTCells; i++) { tcells[i].outputXYZCSV(); }
				 */

                //cellWriter.flush();
                //avgWriter.flush();
                //residenceWriter.flush();
                //breadcrumbWriter.flush();
                //breadcrumbWriterNoPBC.flush();
                //xyzWriter.flush();

                long finishTime = System.nanoTime() - startTime;
                System.out.println("T Cell Running Time: " + finishTime / 1e9 + " seconds");
                //System.out.println("Average collisions with tumorGel: " + );
                //System.out.println(returnGelRange());
                //System.out.println(calculateAvgRadius());
                //System.out.println("range / mu(r) = " + (returnGelRange() / calculateAvgRadius()));
                //System.out.println("mu(r) / r* = " + (calculateAvgRadius() / 8));

                //avgWriter.close();
                //refractoryWriter.close();
                killWriter.close();
                //cellWriter.close();
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

    void runTCellsIterable(int runNum) throws InterruptedException {

        vox = new BoxVoxels(this);

        if(calculateAvgRadius() < 40.0) {
            long spaceTime = System.nanoTime();
            ArrayList<double[]> spaces = findSpaces();

            long finalSpaceTime = System.nanoTime() - spaceTime;
            System.out.println("Space finding takes: " + (finalSpaceTime / 1e9) + " seconds");
        }

        tumorGel = new Gel(sideLength / 2, sideLength / 2, sideLength / 2, tumorGelNoGelRadius, this, "TumorGel");

        addGel(tumorGel);

        generateTumor(tumorGel);

        for(int i = 0; i < 5; i++) {
            tumorGarbageCollector();
        }

        numTumor = getNumTumor();
        System.out.println(numTumor);

        //buildTumorFromCSV();

        startingNumTumor = numTumor;

        setNumTCells(Math.ceil(numTumor / getNumTCellRatio()));

        addTCells();

        boolean breadcrumbs = false;
        LocalDate currentDate = LocalDate.now();
        String formattedDate = currentDate.format(DateTimeFormatter.ofPattern("ddMMMyyyy"));
        //String msdFileName = formattedDate + "_MSDvsTime_LLS-radius" + avgString + "_LLS-dispersion" + stdevString + "_runNum" + runNum + ".csv";
        //String residenceFileName = "residence" + "_" + calculateAvgRadius() + "_" + timeLimitTCells + "_" + (returnGelRange() / calculateAvgRadius()) + ".csv";
        int abridgedTimer = 0;

        double averageDisplacement;

        int stepReduction = 10;

        double[][] msdArray = new double[2][simulationTimeLimit / stepReduction];

        try {
            FileWriter breadcrumbWriter = new FileWriter("breadcrumbs.csv");
            FileWriter breadcrumbWriterNoPBC = new FileWriter("breadcrumbs_no_pbc.csv");

            long startTime = System.nanoTime();

            System.out.println("Starting TCells");

            sim_time = 0;

            int intTimer = 0;

            int[] numTumorCellsVsTimeTemp = new int[simulationTimeLimit];
            double[] tumorTimeTemp = new double[simulationTimeLimit];

            int[] numTumorCellsVsTime = new int[0];
            double[] tumorTime = new double[0];

            int[] numKillsVsTimeTemp = new int[simulationTimeLimit];
            int[] numDeathsVsTimeTemp = new int[simulationTimeLimit];

            int[] numTCellsVsTimeTemp = new int[simulationTimeLimit];

            int numDeaths = 0;

            while (sim_time < simulationTimeLimit) {
                if((sim_time / simulationTimeLimit) % 0.1 == 0) {
                    String percentageComplete = String.format("%.1f", 100 * sim_time / simulationTimeLimit);
                    System.out.println(percentageComplete + "% Complete");

                    System.out.println("Number of T-Cells: " + numParticles);
                    System.out.println("Number of Tumor Cells: " + this.getTumoroids().size());
                }
                averageDisplacement = 0.0;

                if(this.getTumoroids().size() > 0) {
                    tCellProliferate();
                }

                int numKills = 0;

                for(int i = 0; i < numParticles; i++) {
                    numKills += tCells[i].getNumKills();
                }

                numKillsVsTimeTemp[(int)sim_time] = numKills;

                for (int i = 0; i < numParticles; i++) {
                    tCells[i].cellMove();

                    averageDisplacement += this.tCells[i].displacement() * this.tCells[i].displacement();
                }

                for(int i = 0; i < getTumoroids().size(); i++) {
                    if(getTumoroids().get(i).getStatus().equals("delete")) {
                        numDeaths++;
                    }
                }
                numDeathsVsTimeTemp[(int)sim_time] = numDeaths;

                if(tumor) {
                    numTumorCellsVsTimeTemp[(int)sim_time] = this.getTumoroids().size();
                    numTCellsVsTimeTemp[(int)sim_time] = numParticles;
                    tumorTimeTemp[(int)sim_time] = sim_time / 180;
                    tumorGarbageCollector();
                    tumorGrow();
                }

                if((int)sim_time % stepReduction == 0) {
                    msdArray[0][intTimer] = sim_time;
                    msdArray[1][intTimer] = averageDisplacement / numTCells;

                    intTimer++;
                }

                if((int)sim_time % 1000 == 0) {
                    setAverageDisplacement((int) (averageDisplacement / numTCells));
                }

                if((int)sim_time % 90 == 0) {
                    panel.printBMP((int)sim_time, "with_tCells_visible");
                }

                t += dt;

                sim_time++;

                // Use this if you want to cuto off sim early (once tumor cells are killed off)
//                if(this.getTumoroids().size() == 0) {
//                    numTumorCellsVsTimeTemp[(int)sim_time] = this.getTumoroids().size();
//                    numTCellsVsTimeTemp[(int)sim_time] = numParticles;
//                    tumorTimeTemp[(int)sim_time] = sim_time / 180.0;
//
//                    numTumorCellsVsTime = new int[(int)sim_time + 1];
//                    tumorTime = new double[(int)sim_time + 1];
//
//                    for(int time = 0; time <= (int)sim_time; time++) {
//                        numTumorCellsVsTime[time] = numTumorCellsVsTimeTemp[time];
//                        tumorTime[time] = tumorTimeTemp[time];
//                    }
//
//                    break;
//                }
            }

            if(tumor) {
                numTumorVsTimeToCSV(numTumorCellsVsTimeTemp, tumorTime, runNum);
                //numTCellsVsTimeToCSV(numTCellsVsTime, tumorTime, runNum);
            }

            //FileWriter avgWriter = new FileWriter(msdFileName);
            //avgWriter.append(String.format("%s,%s\n", "time","msd"));

            for(int i = 0; i < msdArray[1].length; i++) {
                //avgWriter.append(String.format("%.3f,%.5f\n", msdArray[0][i], msdArray[1][i]));

            }

//                for(int i = 0; i < startValues.size(); i++) {
//                    residenceWriter.append(String.format("%d,%d,%d\n", startValues.get(i)[0], startValues.get(i)[1], startValues.get(i)[2]));
//                }

            /*
             * for(int i = 0; i < numTCells; i++) { tcells[i].outputXYZCSV(); }
             */

            //cellWriter.flush();
            //avgWriter.flush();
            //residenceWriter.flush();
            breadcrumbWriter.flush();
            breadcrumbWriterNoPBC.flush();
            //xyzWriter.flush();

            long finishTime = System.nanoTime() - startTime;
            System.out.println("T Cell Running Time: " + finishTime / 1e9 + " seconds");
            //System.out.println("Average collisions with tumorGel: " + );
            //System.out.println(returnGelRange());
            //System.out.println(calculateAvgRadius());
            //System.out.println("range / mu(r) = " + (returnGelRange() / calculateAvgRadius()));
            //System.out.println("mu(r) / r* = " + (calculateAvgRadius() / 8));

            //avgWriter.close();
            //refractoryWriter.close();
            //killWriter.close();
            //cellWriter.close();
            breadcrumbWriter.close();
            breadcrumbWriterNoPBC.close();
            //xyzWriter.close();


        } catch (Exception e) {
            e.printStackTrace();

        }


//        new Breadcrumbs(t, dt, numParticles, this, "breadcrumbs.csv");
//        TimeUnit.MILLISECONDS.sleep(1000000);
        //new Breadcrumbs(t, dt, numParticles, this, "breadcrumbs_no_pbc.csv");
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
        numTumor++;

    }

    void addTCells() {
        int idNum = 0;

        long spaceTime = System.nanoTime();

        while (numParticles < getNumTCells()) {
            addTCellsNearTumor(idNum);

            idNum++;
        }

        long finalSpaceTime = System.nanoTime() - spaceTime;

        System.out.println("Time to fill t-cells: " + (finalSpaceTime / 1e9));
    }

    void addTestTCells() {
        int idNum = 0;
        while (numParticles < getNumTCells()) {
            TCell c = new TCell(500, 500, 500, 8, idNum, this, rand, logNormal, tCellDoublingTime, tCellRefractoryPeriod);
            vox.add(c);
            tCells[numParticles++] = c;
            sum_sphere_volume += c.volume();

            idNum++;
        }
    }

    void addTCell(double x, double y, double z, double R) {
        TCell c = new TCell(x, y, z, R, 0, this, rand, logNormal, tCellDoublingTime, tCellRefractoryPeriod);
        c.setLifeTime(0);
        c.setLastTimeKilled(0);
        c.setActivated(true);
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
            TCell c = new TCell(x, y, z, R, idNum, this, rand, logNormal, tCellDoublingTime, tCellRefractoryPeriod);
            vox.add(c);
            tCells[numParticles++] = c;
            sum_sphere_volume += c.volume();
            //System.out.println(c.getIdNum());
        }
    }

    void addTCellsNearTumor(int idNum) {
        int[] randArray = new int[3];

        randArray[0] = (rand.nextInt(1) + 1) * (rand.nextBoolean() ? -1 : 1);
        randArray[1] = (rand.nextInt(1) + 1) * (rand.nextBoolean() ? -1 : 1);
        randArray[2] = (rand.nextInt(1) + 1) * (rand.nextBoolean() ? -1 : 1);
//
//        System.out.println(randArray[0]);
//        System.out.println(randArray[1]);
//        System.out.println(randArray[2]);

        double R = 8;

        double tumorRadius = tumorGel.getR();

        //TODO: Figure out why t-cells so far from tumor (probably not aligned to shifting tumorGel)
        // Maybe because tumor gel is deleted!
//        double shiftAmount = Math.sqrt(2) * tumorRadius;
//
//        double x = (tumorGel.getX()) + shiftAmount * randArray[0];
//        double y = (tumorGel.getY()) - shiftAmount * randArray[1];
//        //double z = (tumorGel.getZ()) + tumorRadius *  (0.50 + (0.25 * rand.nextDouble())) * randArray[2];

        // This is only around the tumor
        double x = (tumorGel.getX()) + tumorRadius *  (0.50 + (0.20 * rand.nextDouble())) * randArray[0];
        double y = (tumorGel.getY()) + tumorRadius *  (0.50 + (0.20 * rand.nextDouble())) * randArray[1];
        double z = (tumorGel.getZ()) + tumorRadius *  (0.50 + (0.20 * rand.nextDouble())) * randArray[2];

        // This is in and around the tumor
//        double x = (tumorGel.getX() - calculateTumorGelRadius()) + rand.nextDouble() * (calculateTumorGelRadius() * 2);
//        double y = (tumorGel.getY() - calculateTumorGelRadius()) + rand.nextDouble() * (calculateTumorGelRadius() * 2);
//        double z = (tumorGel.getZ() - calculateTumorGelRadius()) + rand.nextDouble() * (calculateTumorGelRadius() * 2);


//        if(numParticles < 75) {
//            x = (tumorGel.getX() - 50) + rand.nextDouble() * 100;
//            y = (tumorGel.getY() - 50) + rand.nextDouble() * 100;
//            z = (tumorGel.getZ() - 50) + rand.nextDouble() * 100;
//        }


        // Change this to creating class after checking for collision


        if (checkGelCollision(x, y, z, R, this) == false) {
            TCell c = new TCell(x, y, z, R, idNum, this, rand, logNormal, tCellDoublingTime, tCellRefractoryPeriod);
            vox.add(c);
            tCells[numParticles++] = c;
            sum_sphere_volume += c.volume();

            //System.out.println(c.getIdNum());
        }
    }

    void addTCell(Gel tumorGel, int idNum) {
        double R = 8;

        //TODO: Change back to global positioning

        double x = R + rand.nextDouble() * (sideLength - 2 * R);
        double y = R + rand.nextDouble() * (sideLength - 2 * R);
        double z = R + rand.nextDouble() * (sideLength - 2 * R);

        // Change this to creating class after checking for collision


        if (checkGelCollision(x, y, z, R, this) == false) {
            TCell c = new TCell(x, y, z, R, idNum, this, rand, logNormal, tCellDoublingTime, tCellRefractoryPeriod);
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

        TCell c = new TCell(x, y, z, R, idNum, this, rand, logNormal, tCellDoublingTime, tCellRefractoryPeriod);

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