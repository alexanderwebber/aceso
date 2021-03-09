package com.company;

import java.io.*;
import java.lang.reflect.Array;
import java.nio.file.Path;
import java.sql.SQLOutput;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Random;

class Box {
    static Random rand = new Random();
    double sim_time = 0.0;
    double side_length;
    double sum_sphere_volume = 0;                //Current volume
    double volume;       //volume of the cube
    double volume_ratio;              //Desired volume ratio
    BoxVoxels vox;       //Voxel memory for the particles
    ArrayList<Tumoroid> tumoroids = new ArrayList<>();


    Box() {
        

    }

    public void setSide(double side_length) {
        this.side_length = side_length;
        volume = side_length * side_length * side_length;
    }
    
    public ArrayList<Tumoroid> getTumoroids() {
        return tumoroids;
    }

    public int getNumTumor() {
        return tumoroids.size();
    }

    public void addTumor(double x, double y, double z, double R, int idNum) {
        tumoroids.add(new Tumoroid(x, y , z, R, idNum));
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
    double numTCells = 1000;
    double averageDisplacementPanel;
    

    boolean tumor = false;
    Gel tumorGel;
    //double numGelsToSet = 1000;
    //Threads of control
    Thread settleThread = new Thread();
    Thread fillThread = new Thread();
    Thread fallThread = new Thread();
    Thread tCellThread = new Thread();

    // Residence data stuff
    int timeLimitTCells = 100000;
    static ArrayList<int[]> startValues = new ArrayList<>();


    boolean gui = true;

    SliceDensityCalculator sdc = new SliceDensityCalculator(this);
    double t = 0;
    int fallTimeIterator = 0;
    double fall_time_limit = 1000;
    double dt = 1;
    boolean simulating = false;
    private Path savePath;


    // To be changed by panel settings
    double rAverageRadius = 80;
    double rangeOverAverageR = 0.0;

    //Constructors
    Simulation() {
        super();
        side_length = 1000;
        volume = side_length * side_length * side_length;
        volume_ratio = .70;
        //initFromCSVTumor("tumor.csv"); // BOX

    }
    
    void setAverageDisplacement(double averageDisplacement) {
    	this.averageDisplacementPanel = averageDisplacement;
    	
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
            
            setSide(side_length);
            
            volume_ratio = .66;
            if (settleThread.isAlive()) {
                settleThread.interrupt();
            }
            
            // Scale down average radius and std dev
            int numGelsToSet = (int) ((0.67) * (Math.floor((side_length * side_length * side_length) / ((4 / 3) * (rAverageRadius * rAverageRadius * rAverageRadius) * Math.PI))));
            
            vox = new BoxVoxels(this);
            
            rAverageRadius = rAverageRadius * 0.01;

            for (int i = 0; i < numGelsToSet; i++) {
                addGel();
                
            }

            while (sumSphereVolumes() / volume < volume_ratio) {
                scaleSpheres(1.01);
            }

            

            settle();

            double endTime = System.nanoTime();

            double timeDiff = endTime - startTime;

            System.out.println("Time to fill: " + timeDiff / 1e9);
        });

        fillThread.start();
    }
    
    void fall() {
        fallThread = new Thread(() -> {

            long startTime = System.nanoTime();

            while (fallTimeIterator < fall_time_limit) {
                for (int j = 0; j < numGels; j++) {
                    gels.get(j).fall();
                }

                fallTimeIterator++;
                System.out.println(fallTimeIterator);
            }

            double endTime = System.nanoTime();

            double timeDiff = endTime - startTime;

            System.out.println("Time to fall: " + timeDiff / 1e9);

        });

        fallThread.start();
    }
    
    void runSim() throws InterruptedException {
    	fillThread = new Thread(() -> {
            double startTime = System.nanoTime();

            setSide(side_length);
            volume_ratio = .66;
            if (settleThread.isAlive()) {
                settleThread.interrupt();
            }

            // Scale down average radius and std dev
            int numGelsToSet = (int) ((0.67) * (Math.floor((side_length * side_length * side_length) / ((4 / 3) * (rAverageRadius * rAverageRadius * rAverageRadius) * Math.PI))));

            
            rAverageRadius = rAverageRadius * 0.01;

            //rangeOverAverageR = rangeOverAverageR * 0.01;

            vox = new BoxVoxels(this);

            // Add tumor replacement gel
            if(tumor) {
                tumorGel = new Gel(side_length / 2, side_length / 2, side_length / 2, 2.0, this, "TumorGel");
                addGel(tumorGel);
            }


            for (int i = 0; i < numGelsToSet; i++) {
                addGel();
            
            }

            while (sumSphereVolumes() / volume < volume_ratio) {
                scaleSpheres(1.01);


            }

            System.out.println(rangeOverAverageR);

            settle();

            double endTime = System.nanoTime();

            double timeDiff = endTime - startTime;

            System.out.println("Time to fill: " + timeDiff / 1e9);
        });
    	
    	fillThread.start();
    	fillThread.join();

        
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

            System.out.println("Time to fall: " + timeDiff / 1e9);

        });

        fallThread.start();
        fallThread.join();
    
    	
    }
    
    void fillUnthreaded() {
       
        double startTime = System.nanoTime();

        setSide(side_length);
        volume_ratio = .66;

        // Scale down average radius and std dev
        int numGelsToSet = (int) ((0.67) * (Math.floor((side_length * side_length * side_length) / ((4 / 3) * (rAverageRadius * rAverageRadius * rAverageRadius) * Math.PI))));


        rAverageRadius = rAverageRadius * 0.01;

        //rangeOverAverageR = rangeOverAverageR * 0.01;

        vox = new BoxVoxels(this);

        // Add tumor replacement gel
        if(tumor) {
            Gel tumorGel = new Gel(side_length / 2, side_length / 2, side_length / 2, 2.0, this, "TumorGel");
            addGel(tumorGel);
        }

        for (int i = 0; i < numGelsToSet; i++) {
            addGel();
        }
        

        while (sumSphereVolumes() / volume < volume_ratio) {
            scaleSpheres(1.01);

        }

        System.out.println("Range: " + rangeOverAverageR);

        settle();

        double endTime = System.nanoTime();

        double timeDiff = endTime - startTime;

        System.out.println("Time to fill: " + timeDiff / 1e9);
        
        
        // TODO: Move this to separate function. Fix null pointer issue. I think it has to do with the Voxels
       

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
            if(!this.getTumoroids().get(i).getStatus().equals("dead") || !this.getTumoroids().get(i).getStatus().equals("being_attacked")){
                if(this.getTumoroids().get(i).getR() < 12.0) {
                	//TODO: Make growth rate variable
                    this.getTumoroids().get(i).setR(this.getTumoroids().get(i).getR() + 0.00015);
                }
                else {
                    this.getTumoroids().get(i).setR(6.0);
                    double x = this.getTumoroids().get(i).getX() + 6.0;
                    double y = this.getTumoroids().get(i).getY();
                    double z = this.getTumoroids().get(i).getZ();
                    this.addTumor(x, y, z, 6.0, this.getNumTumor());
                    //this.getTumoroids().get(i).move(i, this);
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
        double rangeOverAverageR = 0.0;

        rangeOverAverageR = returnGelRange() / outputMeanRadius();


        return rangeOverAverageR;
    }

    // Scales size of spheres by input percentage
    void scaleSpheres(double percentage) {

        for (int i = 0; i < numGels; i++) {
            gels.get(i).setR(gels.get(i).getR() * percentage);
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


    public double getNumTCells() {
        return numTCells;
    }

    public void setNumTCells(double numTCells) {
        this.numTCells = numTCells;
    }

    public void setSavePath(Path savePath) {
        this.savePath = savePath;
    }

    void runTCells() {
        tCellThread = new Thread(() -> {
            vox = new BoxVoxels(this);
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
	                        double x = Double.parseDouble(thisline.substring(0, comma));
	                        int comma2 = comma + 1 + thisline.substring(comma + 1).indexOf(',');
	                        double y = Double.parseDouble(thisline.substring(comma + 1, comma2));
	                        comma = comma2 + 1 + thisline.substring(comma2 + 1).indexOf(',');
	                        double z = Double.parseDouble(thisline.substring(comma2 + 1, comma));
	                        double R = randomRadius;
	                        this.addTumor(x, y, z, R, i);
	                    }
	            		System.out.println("tumor runs");
	            }
			
			} catch (Exception e) {
				e.printStackTrace();
			}
            
            //ArrayList<double[]> xyzOutput = new ArrayList<>();

            String msdFileName = "msd_vs_time.csv" + "_" + calculateAvgRadius() + "_" + timeLimitTCells + "_" + (returnGelRange() / calculateAvgRadius()) + ".csv";
            //String residenceFileName = "residence" + "_" + calculateAvgRadius() + "_" + timeLimitTCells + "_" + (returnGelRange() / calculateAvgRadius()) + ".csv";
            //int abridgedTimer = 0;
            
            double averageDisplacement;
            

            try {
                FileWriter avgWriter = new FileWriter(msdFileName);
                avgWriter.append(String.format("%s,%s,%s\n", "time", "nonsense", "msd"));
                //FileWriter cellWriter = new FileWriter("cell_displacements_individual" + "_" + calculateAvgRadius() + "_" + timeLimitTCells + ".csv");
                FileWriter breadcrumbWriter = new FileWriter("breadcrumbs.csv");
                FileWriter breadcrumbWriterNoPBC = new FileWriter("breadcrumbs_no_pbc.csv");

                //FileWriter residenceWriter = new FileWriter(residenceFileName);

                

                long startTime = System.nanoTime();

                // Keeping track of time steps for linear regression

                while (sim_time < timeLimitTCells) {
                    averageDisplacement = 0.0;

                    //cellWriter.append(String.format("%.3f,", sim_time));
                    
                    

                    for (int i = 0; i < numTCells; i++) {

                    	tcells[i].cellMove();

                        breadcrumbWriter.append(String.format("%f,%f,%f,", this.tcells[i].x, this.tcells[i].y, this.tcells[i].z));
                        breadcrumbWriterNoPBC.append(String.format("%f,%f,%f,", this.tcells[i].xPrime, this.tcells[i].yPrime, this.tcells[i].zPrime));
                        averageDisplacement += this.tcells[i].displacement() * this.tcells[i].displacement();
                        
                        //cellWriter.append(String.format("%.5f,", tcells[i].displacement()));
                        if((int) sim_time % 100 == 0) {
                        	

                            // When t-cell first enters range of tumor

                            
                            setAverageDisplacement(averageDisplacement);
                           
                            //abridgedTimer++;
                        }
                        
                    }
                    
                    if(tumor) {
                		tumorGarbageCollector();
                        tumorGrow();
                        checkTumors();
                	}

                    
                    breadcrumbWriter.append(String.format("\n"));
                    breadcrumbWriterNoPBC.append(String.format("\n"));
                    
                    //cellWriter.append(String.format("\n"));

                    t += dt;

                    avgWriter.append(String.format("%.3f,%.5f,%.5f\n", sim_time, averageDisplacement, averageDisplacement / this.numParticles));
                    
                    
                    //xyzWriter.append("\n"); //formatting

                    //System.out.println(sim_time);
                    sim_time++;
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
                breadcrumbWriter.flush();
                //breadcrumbWriterNoPBC.flush();

                long finishTime = System.nanoTime() - startTime;
                System.out.println("T Cell Running Time: " + finishTime / 1e9 + " seconds");
                //System.out.println("Average collisions with tumorGel: " + );
                System.out.println(returnGelRange());
                System.out.println(calculateAvgRadius());
                System.out.println("range / mu(r) = " + (returnGelRange() / calculateAvgRadius()));
                System.out.println("mu(r) / r* = " + (calculateAvgRadius() / 8));
                
                avgWriter.close();
                breadcrumbWriter.close();
                breadcrumbWriterNoPBC.close();


            } catch (Exception e) {
                e.printStackTrace();
                e.printStackTrace();
            }


            new Breadcrumbs(t, dt, (int)numTCells, this, "breadcrumbs.csv");
            new Breadcrumbs(t, dt, (int)numTCells, this, "breadcrumbs_no_pbc.csv");
            if (gui) {
                //new Graph2(msdFileName, t, dt);
            } else {
                //new Graph2("msd_vs_time.csv" + calculateAvgRadius() + timeLimitTCells + ".csv", t, dt);
            }
        });

        tCellThread.start();
    }


    void addTCells() {
        int idNum = 0;
        while (numParticles < getNumTCells()) {
            addTcell(idNum);

            idNum++;
        }
    }

    void addTestTCells() {
        int idNum = 0;
        while (numParticles < getNumTCells()) {
            TCell c = new TCell(500, 500, 500, 8, idNum, this);
            vox.add(c);
            tcells[numParticles++] = c;
            sum_sphere_volume += c.volume();

            idNum++;
        }
    }

    void addTcell(int idNum) {
        double R = 8;

        //TODO: Change back to global positioning
       
        double x = R + rand.nextDouble() * (side_length - 2 * R);
        double y = R + rand.nextDouble() * (side_length - 2 * R);
        double z = R + rand.nextDouble() * (side_length - 2 * R);

        TCell c = new TCell(x, y, z, R, idNum, this);

        if (c.checkCollision() == false) {
            vox.add(c);
            tcells[numParticles++] = c;
            sum_sphere_volume += c.volume();
            //System.out.println(c.getIdNum());
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
        double R = (rand.nextDouble() * rangeOverAverageR * rAverageRadius) + rAverageRadius;

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