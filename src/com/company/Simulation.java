package com.company;

import java.io.*;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Random;

class Box {
    static Random rand = new Random();
    double sim_time = 0.0;
    double real_time;
    double side_length;
    double sum_sphere_volume = 0;                //Current volume
    double volume;       //volume of the cube
    double volume_ratio;              //Desired volume ratio
    BoxVoxels vox;       //Voxel memory for the particles
    private ArrayList<Tumoroid> tumoroids = new ArrayList<>();

    Box() {
        side_length = 1000;
        volume = side_length * side_length * side_length;
        volume_ratio = .70;
        vox = new BoxVoxels(this);
    }

    Box(double side_length, double volume_ratio) {
        this.side_length = side_length;
        volume = side_length * side_length * side_length;
        this.volume_ratio = volume_ratio;
        vox = new BoxVoxels(this);
    }

    public void setSide(double side_length) {
        this.side_length = side_length;
        volume = side_length * side_length * side_length;
    }

    public void addTumor(double x, double y, double z, double R, int idNum) {
        tumoroids.add(new Tumoroid(x, y , z, R, idNum));
    }

    public ArrayList<Tumoroid> getTumoroids() {
        return tumoroids;
    }

    public void addTumorRandom() { tumoroids.add(new Tumoroid()); }

    public int getNumTumor() {
        return tumoroids.size();
    }
}

public class Simulation extends Box {
    //Memory
    Vector[] movements = new Vector[1000000];
    ArrayList<Gel> gels = new ArrayList<>();
    int numGels = 0;
    TCell[] tcells = new TCell[1000000];
    ArrayList<Particle> imageParticles = new ArrayList<>();
    ArrayList<Double> densityValues = new ArrayList<>();
    int numParticles = 0;
    double numTCells = 100;
    double numGelsToSet = 1000;
    //Threads of control
    Thread settleThread = new Thread();
    Thread fillThread = new Thread();
    Thread fallThread = new Thread();
    Thread tCellThread = new Thread();

    SliceDensityCalculator sdc = new SliceDensityCalculator(this);
    double t = 0;
    int fallTimeIterator = 0;
    double time_limit = 10000;
    double dt = 1;
    boolean simulating = false;
    private Path savePath;

    // To be changed by panel settings
    double rAverageRadius = 50;
    double rStandardDeviation = 1;

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

            setSide(side_length);
            volume_ratio = .66;
            if (settleThread.isAlive()) {
                settleThread.interrupt();
            }

            addGel(side_length/2, side_length/2, side_length/2, 1);

            // Scale down average radius and std dev
            rAverageRadius = rAverageRadius * 0.01;

            rStandardDeviation = rStandardDeviation * 0.01;

            for (int i = 0; i < numGelsToSet; i++)
                addGel();

            while (sumSphereVolumes() / volume < volume_ratio) {
                scaleSpheres(1.01);
            }

            /*gels.remove(0);

            numGels--;*/

            //initFromCSVTumor("tumor.csv");

            settle();

            double endTime = System.nanoTime();

            double timeDiff = endTime - startTime;

            System.out.println("Time to fill: " + timeDiff / 1e9);
        });
        fillThread.start();
    }

    // Output average radius
    double outputMeanRadius() {
        double sum = 0.0;

        for(int i = 0; i < numGels - 1; i++) {

            sum += gels.get(i).getR();

        }

        return sum / numGels;
    }


    // Output standard deviation
    double outputStandardDeviation() {

        double sumSquared = 0.0;
        double stddev = 0.0;

        for(int i = 0; i < numGels; i++) {

            sumSquared += Math.pow(gels.get(i).getR() - outputMeanRadius(), 2);

        }

        stddev = Math.sqrt(sumSquared / numGels);

        return stddev;

    }

    // Scales size of spheres by input percentage
    void scaleSpheres(double percentage) {

        for(int i = 0; i < numGels; i++) {
            gels.get(i).setR(gels.get(i).getR() * percentage);
        }

    }

    // Returns sum of volumes of spheres
    double sumSphereVolumes() {
        double sumVolume = 0.0;

        for(int i = 0; i < numGels; i++) {
            sumVolume += gels.get(i).volume();
        }

        return sumVolume;
    }

    void initFromCSVTumor(String filename2) {
        try {
            int numTumor = 559;
            Random r = new Random();
            //Tumors from csv

            System.out.println("test");

            BufferedReader builder2 = new BufferedReader(new FileReader(filename2));

            String thisline = builder2.readLine();

            for (int i = 0; i < numTumor; i++) {
                double randomRadius = 6.0 + (11.9 - 6.0) * r.nextDouble();

                int comma = thisline.indexOf(',');
                double x = Double.parseDouble(thisline.substring(0, comma));
                int comma2 = comma + 1 + thisline.substring(comma + 1).indexOf(',');
                double y = Double.parseDouble(thisline.substring(comma + 1, comma2));
                comma = comma2 + 1 + thisline.substring(comma2 + 1).indexOf(',');
                double z = Double.parseDouble(thisline.substring(comma2 + 1, comma));
                double R = randomRadius;
                this.addTumor(x, y, z, R, i);

            }

            System.out.println(getNumTumor());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    void fillFromCSV() {

        try {

            String fileName  = "gelPositions.csv";

            BufferedReader builder = new BufferedReader(new FileReader(fileName));
            String thisline = builder.readLine();       //top line has side_length and volume ratio
            side_length = Double.parseDouble(thisline.substring(0, thisline.indexOf(',')));
            volume = side_length * side_length * side_length;
            volume_ratio = Double.parseDouble(thisline.substring(thisline.indexOf(',') + 1));
            thisline = builder.readLine(); //second line is num gels and num particles
            numGels = Integer.parseInt(thisline.substring(0, thisline.indexOf(',')));

            for (int i = 0; i < numGels; i++) {
                thisline = builder.readLine();
                int comma = thisline.indexOf(',');
                double x = Double.parseDouble(thisline.substring(0, comma));
                int comma2 = comma + 1 + thisline.substring(comma + 1).indexOf(',');
                double y = Double.parseDouble(thisline.substring(comma + 1, comma2));
                comma = comma2 + 1 + thisline.substring(comma2 + 1).indexOf(',');
                double z = Double.parseDouble(thisline.substring(comma2 + 1, comma));
                double R = Double.parseDouble(thisline.substring(comma + 1));
                Gel g = new Gel(x, y, z, R, this);
                vox.add(g);
                gels.add(i, g);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    void fall() {
        fallThread = new Thread(() -> {

            long startTime = System.nanoTime();

            while(fallTimeIterator < time_limit) {
                for (int j = 0; j < numGels; j++) {
                    gels.get(j).fall();
                }

                /*if(fallTimeIterator % 1000 == 0) {
                    densityValues.add(sdc.calculateAreaFractionDensityXY());
                    densityValues.add(sdc.calculateAreaFractionDensityXZ());
                    densityValues.add(sdc.calculateAreaFractionDensityYZ());

                    *//*writeDensityToCSV();

                    System.out.println("Done writing density slices to CSV. Running python script for graph output:");

                    runPython("python density_graph.py");*//*
                }*/

                fallTimeIterator++;
            }

            //this.outputGelPositions("gelPositions.csv");
            /*long finishTime = System.nanoTime() - startTime;

            System.out.println("Done running gels. Settling time: " + finishTime / 1e9 + " seconds");

            System.out.println("Writing density slices to CSV");

            System.out.println("Done writing density slices to CSV. Running python script for graph output:");

            runPython("python density_graph.py");

            System.out.println("Done running python script");*/

            double endTime = System.nanoTime();

            double timeDiff = endTime - startTime;

            System.out.println("Time to fall: " + timeDiff / 1e9);

        });

        fallThread.start();
    }

    void runPython(String fileName) {
        try {
            Process p = Runtime.getRuntime().exec(fileName);
        }

        catch(Exception e) {
            e.printStackTrace();
        }
    }

    void writeDensityToCSV() {
        try {
            //FileWriter xyzWriter = new FileWriter("breadcrumbs.csv");
            FileWriter avgWriter = new FileWriter(getSavePath().toString() +  "/density_vs_time.csv");

            int position = 0;

            avgWriter.append(String.format("%s,%s,%s,%s,\n", "Position", "XY", "XZ", "YZ"));

            for(int i = 0; i < side_length; i++) {

                //Write-out the average_displacement for this step
                avgWriter.append(String.format("%d,%.5f,%.5f,%.5f,\n", position,sdc.getSliceDensityXY().get(i),
                        sdc.getSliceDensityXZ().get(i),
                        sdc.getSliceDensityYZ().get(i)));

                position++;
            }

            avgWriter.flush();

        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }

    public double getNumTCells() {
        return numTCells;
    }

    public void setNumTCells(double numTCells) {
        this.numTCells = numTCells;
    }

    public void setSavePath(Path savePath) {
        this.savePath = savePath;
    }

    public Path getSavePath() {
        return savePath;
    }

    void runTCells() {
        tCellThread = new Thread(() -> {
            addTCells();

            try {
                //FileWriter xyzWriter = new FileWriter("breadcrumbs.csv");



                //FileWriter avgWriter = new FileWriter(getSavePath().toString() + "/msd_vs_time.csv");
                FileWriter avgWriter = new FileWriter("msd_vs_time.csv");

                //FileWriter cellWriter = new FileWriter(getSavePath().toString() + "/cell_displacements_individual.csv");
                FileWriter cellWriter = new FileWriter("cell_displacements_individual.csv");

                double average_displacement;

                long startTime = System.nanoTime();

                while(sim_time++ < time_limit) {
                    average_displacement = 0.0;

                    //TODO Change back to cellMove once fixed PBC MSD

                    //this.tcells[j].cellMoveTest();


                    cellWriter.append(String.format("%.3f,", sim_time));
                    for(int i = 0; i < numTCells; i++) {
                        this.tcells[i].cellMove();

                        cellWriter.append(String.format("%.5f,", tcells[i].displacement()));

                        average_displacement += this.tcells[i].displacement() * this.tcells[i].displacement();
                    }
                    cellWriter.append(String.format("\n"));



                    //xyzWriter.append(String.format("%f,%f,%f,", this.tcells[j].x, this.tcells[j].y, this.tcells[j].z));

                    t += dt;

                    //Write-out the average_displacement for this step
                    avgWriter.append(String.format("%.3f,%.5f,%.5f\n", sim_time, average_displacement, average_displacement / this.numParticles));

                    //xyzWriter.append("\n"); //formatting
                }

                avgWriter.flush();
                //avgWriter.close();

                long finishTime = System.nanoTime() - startTime;
                System.out.println("T Cell Running Time: " + finishTime / 1e9 + " seconds");

            }
            catch(Exception e) {
                e.printStackTrace();
            }

            //new Breadcrumbs(t, dt, 10, this);
            //new Graph2(getSavePath().toString() + "/msd_vs_time.csv", t, dt);
            //new Graph("output.csv", t, dt);
        });

        tCellThread.start();
    }

    //Constructors
    Simulation(String filename) {
        super();
        //initFromCSV(filename); // BOX
    }

    Simulation() {
        super();
        //initFromCSVTumor("tumor.csv"); // BOX
    }

    void addTCells() {
        while(numParticles < getNumTCells()) {
            addTcell();
            //addTestCell();
        }
    }

    void addTestCell() {
        double R = 8;

        double x = side_length * 0.5;
        double y = side_length * 0.5;
        double z = side_length * 0.5;

        TCell c = new TCell(x, y, z, R, this, new Vector(3, 0, 0));

        if (c.checkCollision() == false) {
            vox.add(c);
            tcells[numParticles++] = c;
            sum_sphere_volume += c.volume();
        }
    }

    void addTcell() {
        double R = 8;

        double x = R + rand.nextDouble() * (side_length - 2 * R);
        double y = R + rand.nextDouble() * (side_length - 2 * R);
        double z = R + rand.nextDouble() * (side_length - 2 * R);

        TCell c = new TCell(x, y, z, R, this);

        if (c.checkCollision() == false) {
            vox.add(c);
            tcells[numParticles++] = c;
            sum_sphere_volume += c.volume();
        }
    }

    void outputGelPositions(String output_filename) {

        try {

            FileWriter gelWriter = new FileWriter(output_filename);
            for (int i = 0; i < this.numGels; i++) {
                //Write-out xyz-positions of gels
                gelWriter.append(String.format("%f, %f, %f, %f", this.gels.get(i).x, this.gels.get(i).y, this.gels.get(i).z, this.gels.get(i).getR()));
                gelWriter.append("\n");
            }
        }

        catch (Exception e) {
            e.printStackTrace();
        }
    }

    void reset() {
        numGels = 0;
        sum_sphere_volume = 0;
        side_length = 1000;
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
        //spawn new gel in the box
        double R = rand.nextGaussian() * rStandardDeviation + rAverageRadius;

        double x = R + rand.nextDouble() * (side_length - 2 * R);
        double y = R + rand.nextDouble() * (side_length - 2 * R);
        double z = R + rand.nextDouble() * (side_length - 2 * R);

        Gel g = new Gel(x, y, z, R, this);

        // Ensure that the location isn't occupied, and truncate zero
        if (g.checkCollision() == false && R != 0) {
            vox.add(g);
            gels.add(numGels++, g);
            sum_sphere_volume += g.volume();
        }
    }

    void addGel(double x, double y, double z, double R) {
        Gel g = new Gel(x, y, z, R, this);
        vox.add(g);
        gels.add(numGels++, g);
        sum_sphere_volume += g.volume();
    }

    private void checkTumors() {
        for (int i = 0; i < this.getNumTumor(); ++i) {
            this.getTumoroids().get(i).checkSetStatus();
        }
    }

    void tumorGrow() {
        for(int i = 0; i < this.getNumTumor(); i++) {
            if(!this.getTumoroids().get(i).getStatus().equals("dead") || !this.getTumoroids().get(i).getStatus().equals("being_attacked")){
                if(this.getTumoroids().get(i).getR() < 12.0) {
                    this.getTumoroids().get(i).setR(this.getTumoroids().get(i).getR() + 0.00014);
                }
                else {
                    this.getTumoroids().get(i).setR(6.0);
                    double x = this.getTumoroids().get(i).getX() + 6.0;
                    double y = this.getTumoroids().get(i).getY();
                    double z = this.getTumoroids().get(i).getZ();
                    this.addTumor(x, y, z, 6.0, this.getNumTumor());
                    this.getTumoroids().get(i).move(i, this);
                }
            }
        }
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

    void timeStep() {
        for (int i = 0; i < numParticles; i++) {
            tcells[i].cellMove();
        }

        // TODO re-enable when implementing tumors
        /*tumorGarbageCollector();
        tumorGrow();
        checkTumors();*/

        t += dt;
    }
    void start() {
        simulating = true;
        Thread simThread = new Thread(() -> {
            while (simulating && t < time_limit) {
                //timestep();
            }
            simulating = false;
        });
        simThread.start();
    }



    void pause() {simulating = false;}

    //setters
    void setTime_limit(double time_limit) {
        this.time_limit = time_limit;
    }
    void setDt(double dt) {
        this.dt = dt;
    }

    void writeOut(String filename) {
        //write to csv
        try {
            FileWriter csvWriter = new FileWriter(filename);
            csvWriter.append(String.valueOf(side_length));
            csvWriter.append(",");
            csvWriter.append(String.valueOf(volume_ratio));
            csvWriter.append("\n");
            csvWriter.append(String.valueOf(numGels));
            csvWriter.append(",");
            csvWriter.append(String.valueOf(numParticles));
            csvWriter.append("\n");
            for (int i = 0; i < numGels; ++i) {
                csvWriter.append(String.valueOf(gels.get(i).x));
                csvWriter.append(",");
                csvWriter.append(String.valueOf(gels.get(i).y));
                csvWriter.append(",");
                csvWriter.append(String.valueOf(gels.get(i).z));
                csvWriter.append(",");
                csvWriter.append(String.valueOf(gels.get(i).R));
                csvWriter.append("\n");
            }
            for (int i = 0; i < numParticles; ++i) {
                csvWriter.append(String.valueOf(tcells[i].x));
                csvWriter.append(",");
                csvWriter.append(String.valueOf(tcells[i].y));
                csvWriter.append(",");
                csvWriter.append(String.valueOf(tcells[i].z));
                csvWriter.append(",");
                csvWriter.append(String.valueOf(tcells[i].R));
                csvWriter.append("\n");
            }
            csvWriter.flush();
            csvWriter.close();
        } catch (IOException e) {
            System.out.println("exception occurred" + e);
        }
    }
}

class BoxVoxels {
    int voxels_per_side = 5;
    double voxel_side_length;
    Voxel[][][] voxels;
    BoxVoxels(Box B) {
        voxel_side_length = B.side_length/voxels_per_side;
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
        voxel_side_length = B.side_length/voxels_per_side;
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