package com.company;

import java.awt.*;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public class TCell extends Particle implements Drawable {
    Simulation S;
    private boolean isActivated;

    private int idNum;

    private boolean isAttacking;

    private boolean reachedMaxKills;

    private int numKills;

    // type (for now only CD8+
    private String type;

    // Killing, not killing
    private int status;

    private int timeAttacking;

    private int lastTimeKilled;

    // Random number gen:
    Random random = new Random();
    int[] randArray = new int[3];

    // Assign random velocity value
    double velocityX = ThreadLocalRandom.current().nextDouble(3.1);
    double velocityY = ThreadLocalRandom.current().nextDouble(3.1 - velocityX);
    double velocityZ = (3.1 - velocityX - velocityY);

    double velocity = 3.0;
    TCell(){}
    TCell(double x, double y, double z, double R, Simulation S, Vector v){
        super(x, y, z, R, S);
        type = "TCell";
        this.S = S;
        numKills = 0;

        this.v = v;
    }

    TCell(double x, double y, double z, double R, Simulation S) {
        super(x, y, z, R, S);
        type = "TCell";
        this.S = S;
        numKills = 0;

        randArray[0] = (random.nextInt(1) + 1) * (random.nextBoolean() ? -1 : 1);
        randArray[1] = (random.nextInt(1) + 1) * (random.nextBoolean() ? -1 : 1);
        randArray[2] = (random.nextInt(1) + 1) * (random.nextBoolean() ? -1 : 1);

        // Randomize the polarity
        velocityX *= randArray[random.nextInt(3)];
        velocityY *= randArray[random.nextInt(3)];
        velocityZ *= randArray[random.nextInt(3)];

        v = new Vector(velocityX, velocityY, velocityZ);

    }

    double distanceTraveled() {
        return Vector.magnitude(dx, dy, dz);
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

    void cellMoveTest() {
        updateCollision();
        this.move();
    }

    void cellMove() {
        /*if(checkCollision() == false) {

        }*/

        this.v = Vector.random2();
        updateCollision();

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