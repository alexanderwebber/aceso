package com.company;

import java.awt.*;

// Used to draw lines for the simulation space outline
public class LineSegment implements Drawable {
    double x1;
    double x2;
    double y1;
    double y2;
    double z1;
    double z2;

    LineSegment(double x1, double y1, double z1, double x2, double y2, double z2) {
        this.x1 = x1;
        this.x2 = x2;
        this.y1 = y1;
        this.y2 = y2;
        this.z1 = z1;
        this.z2 = z2;
    }

    @Override
    public double getZ() {
        return Math.max(z1, z2);
    }

    @Override
    public void draw(Graphics g) {
        g.setColor(Color.white);
        g.drawLine((int) x1, (int) y1, (int) x2, (int) y2);
    }
}
