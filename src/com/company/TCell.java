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
    Random random = new Random();
    int[] randArray = new int[3];

    // Assign random velocity value
    double velocityX = ThreadLocalRandom.current().nextDouble(1.1);
    double velocityY = ThreadLocalRandom.current().nextDouble(1.1 - velocityX);
    double velocityZ = (1.1 - velocityX - velocityY);

    double velocity = 1.0;

    TCell(){

    }


    TCell(double x, double y, double z, double R,  int idNum, Simulation S) {
        super(x, y, z, R, S);
        type = "TCell";
        this.S = S;
        numKills = 0;

        this.idNum = idNum;

        randArray[0] = (random.nextInt(1) + 1) * (random.nextBoolean() ? -1 : 1);
        randArray[1] = (random.nextInt(1) + 1) * (random.nextBoolean() ? -1 : 1);
        randArray[2] = (random.nextInt(1) + 1) * (random.nextBoolean() ? -1 : 1);

        // Randomize the polarity
        velocityX *= randArray[random.nextInt(3)];
        velocityY *= randArray[random.nextInt(3)];
        velocityZ *= randArray[random.nextInt(3)];

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

        double pbcAdjustmentX = S.side_length * getNumPBCJumpsX();
        double pbcAdjustmentY = S.side_length * getNumPBCJumpsY();
        double pbcAdjustmentZ = S.side_length * getNumPBCJumpsZ();

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

    public int getStatus() { return status; }

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

        this.v = Vector.random2();

        while(checkCollision(mod(this.x + v.x(), S.side_length), mod(this.y + v.y(), S.side_length), mod(this.z + v.z(), S.side_length), this.R)) {
            this.v = Vector.random2();

        }

        //TODO redundant?
        move();

        double[] tempArray = {this.getX(), this.getY(), this.getZ()};
        xyzOutput.add(tempArray);



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
                    this.velocity = 4 * Math.exp(-0.01 * R) + 0.7;
                }
                else {
                    timeAttacking++;
                }
            }

            // check tumor cells
            if(true) {
                /*for(int i = 0; i < S.getNumTumor(); i++) {

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
                }*/

                for(int i = 0; i < S.gels.size(); i++) {

                    /*if(isActivated == false) {
                        continue;
                    }*/

                    /*if(S.getTumoroids().get(i).getStatus().equals("dead")) {
                        continue;
                    }*/



                    double radius_sum_squared = Math.pow((this.getR() * 1.3) + S.gels.get(i).getR(), 2);

                    double distanceX = Math.abs(S.gels.get(i).getX() - this.getX());
                    double distanceY = Math.abs(S.gels.get(i).getY() - this.getY());
                    double distanceZ = Math.abs(S.gels.get(i).getZ() - this.getZ());

                    double distanceVector = Math.pow(distanceX, 2) + Math.pow(distanceY, 2) + Math.pow(distanceZ, 2);

                    if(S.gels.get(i).name.equals("TumorGel")) {
                        if (distanceVector <= radius_sum_squared + (2 * this.getR())) {
                            residenceTime++;
                            nearTumor = true;
                            //System.out.println("Near Tumor Cell");
                            //System.out.println(nearTumor);
                            if(nearTumor && !previousNearTumor) {
                                this.arrayListPosition = addStartPoint(this);
                                System.out.println("entered tumor");
                            }
                        }
                        else {
                            residenceTime = 0;
                            nearTumor = false;
                            if(!nearTumor && previousNearTumor) {
                                S.startValues.get(this.arrayListPosition)[2] = (int)S.sim_time;

                                System.out.println("left tumor");


                                //S.startValues.remove(this.arrayListPosition);

                            }
                        }
                    }


                    if(distanceVector < radius_sum_squared) {
                        this.velocity = 0.0;
                        setActivated(false);
                        setIsAttacking(true);
                        this.setStatus(2);
                        //S.getTumoroids().get(i).setStatus("being_attacked");
                        //numKills++;
                        tumorHitCounter++;

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