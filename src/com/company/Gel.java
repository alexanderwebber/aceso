package com.company;

import java.awt.*;

class Gel extends Particle implements Drawable {
    //gel object variables
    Gel(double x, double y, double z, double R, Simulation S) {
        super(x, y, z, R, S);
        type = "Gel";
    }
    Gel(){
        super();
    }
    public double getZ() {
        return z;
    }

    public void draw(Graphics g) {
        g.setColor(new Color(48, 119, 121, 172)); //outline
        g.drawOval((int) (x), (int) (y), (int) (2 * R), (int) (2 * R));

        g.setColor(new Color(44, 44, 44, 0)); //fill
        g.fillOval((int) (x), (int) (y), (int) (2 * R), (int) (2 * R));
    }
    public void drawImage(Graphics g) {
        g.setColor(new Color(58, 255, 74, 172)); //outline
        g.drawOval((int) (x), (int) (y), (int) (2 * R), (int) (2 * R));

        g.setColor(new Color(44, 44, 44, 0)); //fill
        g.fillOval((int) (x), (int) (y), (int) (2 * R), (int) (2 * R));
    }
    void fall() {
       /* v = new Vector((B.side_length * 0.5 - x), (B.side_length * 0.5 - y), (B.side_length * 0.5 - z)).scale(.001);
        move();*/

        if(imImage == false) {
            updateCollision();
            //move();
        }
    }
    void settle() {
        v = Vector.random();
        if(imImage == false) {
            updateCollision();
        }

    }
}