package com.company;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

class Visualization extends JPanel {
    Simulation S;
    boolean box_view = true;
    boolean slicer_view = false;
    boolean see_box = true;
    boolean see_gels = true;
    boolean see_tcell = true;
    //SimulationSettings settings;
    static Drawable[] drawthis = new Drawable[1000000];
    static int drawObjects;
    String filename = "box_particles.csv";
    Rotator o = new Rotator();
    Shifter k = new Shifter();
    SlicerShifter sl = new SlicerShifter();
    double cos_theta;
    double sin_theta;
    double cos_phi;
    double sin_phi;

    Visualization() {
        setSize(1500, 1500);
        addKeyListener(k);
        addMouseListener(o);
        addMouseMotionListener(o);
        addMouseWheelListener(o);
        addMouseWheelListener(sl);
    }
    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        g.setColor(Color.black);
        g.fillRect(0, 0, this.getWidth(), this.getHeight());
        if (box_view) {
            drawObjects = 0;
            cos_theta = Math.cos(o.theta);
            sin_theta = Math.sin(o.theta);
            cos_phi = Math.cos(o.phi);
            sin_phi = Math.sin(o.phi);
            if (see_box)
                addBox(S);
            if (see_gels)
                addGels();
            if (see_tcell)
                addParticles();
            try {
                new QuickSort(drawthis, 0, drawObjects);
            } catch (Exception e) {
                e.printStackTrace();
            }
            for (int i = 0; i < drawObjects; ++i) {
                if (drawthis[i] != null) {
                    drawthis[i].draw(g);
                }
            }
        }
        else if (slicer_view) {
            g.setColor(Color.white);
            g.drawString(String.format("z = %f", sl.getZ()), getWidth() - 100, getHeight() - 25);
            double scale = .5;
            if (see_box) {
                int x0 = (int) (-S.side_length * scale / 2.0 + getWidth() / 2.0);
                int y0 = (int) (-S.side_length * scale / 2.0 + getHeight() / 2.0);
                int x1 = (int) (S.side_length * scale / 2.0 + getWidth() / 2.0);
                int y1 = (int) (S.side_length * scale / 2.0 + getHeight() / 2.0);
                g.setColor(Color.blue);
                g.drawLine(x0, y0, x0, y1);
                g.drawLine(x0, y0, x1, y0);
                g.drawLine(x0, y1, x1, y1);
                g.drawLine(x1, y0, x1, y1);
                x0 -= 160 * scale;
                y0 -= 160 * scale;
                x1 += 160 * scale;
                y1 += 160 * scale;
                g.setColor(Color.green);
                g.drawLine(x0, y0, x0, y1);
                g.drawLine(x0, y0, x1, y0);
                g.drawLine(x0, y1, x1, y1);
                g.drawLine(x1, y0, x1, y1);
            }
            if (see_gels) {
                for (int i = 0; i < S.numGels; ++i) {
                    Gel temp = new Gel();
                    double d = Math.abs(sl.getZ() - S.gels.get(i).z);
                    if (d < S.gels.get(i).R) {
                        temp.R = (Math.sqrt(S.gels.get(i).R * S.gels.get(i).R - d * d));
                        temp.x = (S.gels.get(i).x - temp.R - S.side_length / 2);
                        temp.y = (S.gels.get(i).y - temp.R - S.side_length / 2);
                        temp.x *= scale;
                        temp.y *= scale;
                        temp.R *= scale;

                        temp.x += getWidth() / 2.0;
                        temp.y += getHeight() / 2.0;

                        temp.draw(g);

                        // TODO: Don't forget this for image drawing
                        if (S.gels.get(i).R + S.gels.get(i).x > S.side_length) {
                            temp.x -= S.side_length / 2;
                            temp.drawImage(g);
                            temp.x += S.side_length / 2;
                        }
                        if (S.gels.get(i).R + S.gels.get(i).y > S.side_length) {
                            temp.y -= S.side_length / 2;
                            temp.drawImage(g);
                            temp.y += S.side_length / 2;
                        }
                        if (S.gels.get(i).x - S.gels.get(i).R < 0) {
                            temp.x += S.side_length / 2;
                            temp.drawImage(g);
                            temp.x -= S.side_length / 2;
                        }
                        if (S.gels.get(i).y - S.gels.get(i).R < 0) {
                            temp.y += S.side_length / 2;
                            temp.drawImage(g);
                            temp.y -= S.side_length / 2;
                        }
                    }
                }
            }

            if (see_tcell) {
                for (int i = 0; i < S.numParticles; ++i) {
                    TCell temp = new TCell();
                    double d = Math.abs(sl.getZ() - S.tcells[i].z);
                    if (d < S.tcells[i].R) {
                        temp.R = (Math.sqrt(S.tcells[i].R * S.tcells[i].R - d * d));
                        temp.x = (S.tcells[i].x - temp.R - S.side_length / 2);
                        temp.y = (S.tcells[i].y - temp.R - S.side_length / 2);
                        //scaaaaale
                        temp.x *= scale;
                        temp.y *= scale;
                        temp.R *= scale;

                        temp.x += getWidth() / 2.0;
                        temp.y += getHeight() / 2.0;

                        temp.draw(g);
                    }
                }
            }
        }

    }
    private void addBox(Box B) {
        double s = B.side_length / 2;
        double[][] corners =  new double[][] {
                {-s, -s, -s},
                {-s, -s, s},
                {-s, s, -s},
                {-s, s, s},
                {s, -s, -s},
                {s, -s, s},
                {s, s, -s},
                {s, s, s}
        };
        for (double[] corner : corners) {
            double x = corner[0];
            double y = corner[1];
            double z = corner[2];
            //transform
            corner[0] = x * cos_theta - y * sin_theta;
            corner[1] = z * cos_phi + (x * sin_theta + y * cos_theta) * sin_phi;
            corner[2] = z * sin_phi - (x * sin_theta + y * cos_theta) * cos_phi;
            //shift
            corner[0] += k.offset_x;
            corner[1] += k.offset_y;
            //scale
            corner[0] *= o.r / 2000;
            corner[1] *= o.r / 2000;
            corner[2] *= o.r / 2000;
            //center
            corner[0] += this.getWidth() / 2;
            corner[1] += this.getHeight() / 2;
        }
        drawthis[drawObjects++] = new LineSegment(corners[0][0], corners[0][1], corners[0][2], corners[1][0], corners[1][1], corners[1][2]);
        drawthis[drawObjects++] = new LineSegment(corners[0][0], corners[0][1], corners[0][2], corners[2][0], corners[2][1], corners[2][2]);
        drawthis[drawObjects++] = new LineSegment(corners[0][0], corners[0][1], corners[0][2], corners[4][0], corners[4][1], corners[4][2]);
        drawthis[drawObjects++] = new LineSegment(corners[1][0], corners[1][1], corners[1][2], corners[3][0], corners[3][1], corners[3][2]);
        drawthis[drawObjects++] = new LineSegment(corners[1][0], corners[1][1], corners[1][2], corners[5][0], corners[5][1], corners[5][2]);
        drawthis[drawObjects++] = new LineSegment(corners[2][0], corners[2][1], corners[2][2], corners[3][0], corners[3][1], corners[3][2]);
        drawthis[drawObjects++] = new LineSegment(corners[2][0], corners[2][1], corners[2][2], corners[6][0], corners[6][1], corners[6][2]);
        drawthis[drawObjects++] = new LineSegment(corners[3][0], corners[3][1], corners[3][2], corners[7][0], corners[7][1], corners[7][2]);
        drawthis[drawObjects++] = new LineSegment(corners[4][0], corners[4][1], corners[4][2], corners[5][0], corners[5][1], corners[5][2]);
        drawthis[drawObjects++] = new LineSegment(corners[4][0], corners[4][1], corners[4][2], corners[6][0], corners[6][1], corners[6][2]);
        drawthis[drawObjects++] = new LineSegment(corners[5][0], corners[5][1], corners[5][2], corners[7][0], corners[7][1], corners[7][2]);
        drawthis[drawObjects++] = new LineSegment(corners[6][0], corners[6][1], corners[6][2], corners[7][0], corners[7][1], corners[7][2]);
    }
    private void addGels() {
        for (int i = 0; i < S.numGels; i++) {
            Gel temp = new Gel();
            temp.R = S.gels.get(i).R;
            double x = S.gels.get(i).x - S.side_length / 2;
            double y = S.gels.get(i).y - S.side_length / 2;
            double z = S.gels.get(i).z - S.side_length / 2;
            //transform
            temp.x = x * cos_theta - y * sin_theta;
            temp.y = z * cos_phi + (x * sin_theta + y * cos_theta) * sin_phi;
            temp.z = z * sin_phi - (x * sin_theta + y * cos_theta) * cos_phi;
            //shift
            temp.x += k.offset_x;
            temp.y += k.offset_y;
            //scale
            temp.x *= o.r / 2000;
            temp.y *= o.r / 2000;
            temp.z *= o.r / 2000;
            temp.R *= o.r / 2000;
            //center
            temp.x +=  (double) (getWidth() / 2) - temp.R;
            temp.y +=  (double) (getHeight() / 2) - temp.R;
            drawthis[drawObjects++] = temp;
        }
    }
    private void addParticles() {
        for (int i = 0; i < S.numParticles; ++i) {
            TCell temp = new TCell();
            temp.R = S.tcells[i].R;
            double x = S.tcells[i].x - S.side_length / 2;
            double y = S.tcells[i].y - S.side_length / 2;
            double z = S.tcells[i].z - S.side_length / 2;
            //transform
            temp.x = x * cos_theta - y * sin_theta;
            temp.y = z * cos_phi + (x * sin_theta + y * cos_theta) * sin_phi;
            temp.z = z * sin_phi - (x * sin_theta + y * cos_theta) * cos_phi;
            //shift
            temp.x += k.offset_x;
            temp.y += k.offset_y;
            //scale
            temp.x *= o.r / 2000;
            temp.y *= o.r / 2000;
            temp.z *= o.r / 2000;
            temp.R *= o.r / 2000;
            //center
            temp.x +=  (double) (getWidth() / 2) - temp.R;
            temp.y +=  (double) (getHeight() / 2) - temp.R;
            drawthis[drawObjects++] = temp;
        }
    }
    class SlicerShifter extends MouseAdapter {
        double z = 500;
        double scroll_amount = 5;
        double getZ() {return z;}

        @Override
        public void mouseWheelMoved(MouseWheelEvent e) {
            super.mouseWheelMoved(e);
            if (e.getPreciseWheelRotation() > 0) {
                if (z >= S.side_length - 2) {
                    z = S.side_length;
                }
                else {
                    z += scroll_amount;
                }
            }
            else {
                if (z <= 2) {
                    z = 0;
                }
                else {
                    z -= scroll_amount;
                }
            }
        }
    }
    class Rotator extends MouseAdapter {
        static final double PI = 3.14159265358979323846264338327950288419716939937511;
        double r = 1000, theta = -.07, phi = -0.03;
        int x0, y0;

        @Override
        public void mousePressed(MouseEvent e) {
            super.mousePressed(e);
            x0 = e.getX();
            y0 = e.getY();
        }

        @Override
        public void mouseDragged(java.awt.event.MouseEvent e) {
            theta += PI * (e.getX() - x0) / 1800;
            phi += PI * (y0 - e.getY()) / 1800;
            x0 = e.getX();
            y0 = e.getY();
        }

        @Override
        public void mouseWheelMoved(MouseWheelEvent e) {
            super.mouseWheelMoved(e);
            int scroll_amount = 40;

            if (e.getPreciseWheelRotation() > 0) {
                r += scroll_amount;
            } else {
                r -= scroll_amount;
            }
        }
    }
    class Shifter extends KeyAdapter {
        int offset_x = 0;
        int offset_y = 0;
        int shift_amount = 10;
        @Override
        public void keyPressed(KeyEvent e) {
            super.keyPressed(e);
            if (e.getKeyCode() == 37) { // left arrow
                offset_x -= shift_amount;
            }
            if (e.getKeyCode() == 38) { // up arrow
                offset_y -= shift_amount;
            }
            if (e.getKeyCode() == 39) { // right arrow
                offset_x += shift_amount;
            }
            if (e.getKeyCode() == 40) { // down arrow
                offset_y += shift_amount;
            }
        }
    }
}

class FillVisualization extends Visualization {
    FillVisualization() {
        S = new Simulation();
        add(new FillSettingsViz(this));
    }
}

/*
class SimulationVisualization extends Visualization {
    SimulationVisualization(String filename) {
        this.filename = filename;
        S = new Simulation(filename);
        settings = new SimulationSettings(this);
        add(settings);
    }

    SimulationVisualization() {
        S = new Simulation();
        o = new Rotator();
        k = new Shifter();
        sl = new SlicerShifter();
        addKeyListener(k);
        addMouseListener(o);
        addMouseMotionListener(o);
        addMouseWheelListener(o);
        addMouseWheelListener(sl);
        settings = new SimulationSettings(this);
        add(settings);
    }
}*/
